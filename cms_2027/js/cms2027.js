/*
 * CMS-2027 — 跨平台文本加密封装协议（Web 实现）
 *
 * KDF:    PBKDF2-HMAC-SHA256 (600,000 次迭代, 32 字节密钥)
 * Cipher: AES-256-GCM (12 字节 nonce, 16 字节标签, AAD = 协议头)
 * 格式:   Base64( magic"CMS27" | ver | kdfId | cipherId | iter24 | saltLen | salt | nonceLen | nonce | ct|tag )
 *
 * 同时兼容浏览器 (window.CMS2027) 与 Node.js (module.exports)。
 */
(function (global) {
  "use strict";

  var MAGIC = [0x43, 0x4d, 0x53, 0x32, 0x37]; // "CMS27"
  var VERSION = 0x01;
  var KDF_ID_PBKDF2_SHA256 = 0x01;
  var CIPHER_ID_AES_256_GCM = 0x01;
  var DEFAULT_ITERATIONS = 600000;
  var SALT_LEN = 16;
  var NONCE_LEN = 12;
  var TAG_LEN = 16;
  var HEADER_LEN = 5 + 1 + 1 + 1 + 3 + 1 + SALT_LEN + 1 + NONCE_LEN; // 41
  var MIN_PAYLOAD_LEN = HEADER_LEN + TAG_LEN;

  // 内置密钥（普通模式）
  var BUILTIN_KEY = "passwd-cms_2027-buildin-clcwpwqi-267164699";

  function utf8Encode(str) {
    return new TextEncoder().encode(str);
  }

  function utf8Decode(bytes) {
    return new TextDecoder("utf-8", { fatal: true }).decode(bytes);
  }

  // ---- Base64（标准，含填充），二进制安全 ----
  function bytesToBase64(bytes) {
    var bin = "";
    var CHUNK = 0x8000;
    for (var i = 0; i < bytes.length; i += CHUNK) {
      bin += String.fromCharCode.apply(null, bytes.subarray(i, i + CHUNK));
    }
    return btoa(bin);
  }

  function base64ToBytes(b64) {
    var bin = atob(b64);
    var bytes = new Uint8Array(bin.length);
    for (var i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
    return bytes;
  }

  function concatBytes(a, b) {
    var out = new Uint8Array(a.length + b.length);
    out.set(a, 0);
    out.set(b, a.length);
    return out;
  }

  function randomBytes(n) {
    var b = new Uint8Array(n);
    crypto.getRandomValues(b);
    return b;
  }

  function buildHeader(iterations, salt, nonce) {
    var h = new Uint8Array(HEADER_LEN);
    var p = 0;
    for (var i = 0; i < MAGIC.length; i++) h[p++] = MAGIC[i];
    h[p++] = VERSION;
    h[p++] = KDF_ID_PBKDF2_SHA256;
    h[p++] = CIPHER_ID_AES_256_GCM;
    h[p++] = (iterations >>> 16) & 0xff;
    h[p++] = (iterations >>> 8) & 0xff;
    h[p++] = iterations & 0xff;
    h[p++] = SALT_LEN;
    h.set(salt, p); p += SALT_LEN;
    h[p++] = NONCE_LEN;
    h.set(nonce, p);
    return h;
  }

  function deriveKey(passphraseBytes, salt, iterations) {
    return crypto.subtle
      .importKey("raw", passphraseBytes, { name: "PBKDF2" }, false, ["deriveKey"])
      .then(function (baseKey) {
        return crypto.subtle.deriveKey(
          {
            name: "PBKDF2",
            hash: "SHA-256",
            salt: salt,
            iterations: iterations,
          },
          baseKey,
          { name: "AES-GCM", length: 256 },
          false,
          ["encrypt", "decrypt"]
        );
      });
  }

  /**
   * 加密。opts 可注入 salt/nonce/iterations（仅用于生成测试向量）。
   * @returns Promise<string> Base64 封装的密文
   */
  function encrypt(plaintext, passphrase, opts) {
    opts = opts || {};
    var iterations = opts.iterations || DEFAULT_ITERATIONS;
    var salt = opts.salt ? new Uint8Array(opts.salt) : randomBytes(SALT_LEN);
    var nonce = opts.nonce ? new Uint8Array(opts.nonce) : randomBytes(NONCE_LEN);
    var header = buildHeader(iterations, salt, nonce);
    return deriveKey(utf8Encode(String(passphrase)), salt, iterations).then(
      function (key) {
        return crypto.subtle
          .encrypt(
            { name: "AES-GCM", iv: nonce, additionalData: header, tagLength: TAG_LEN * 8 },
            key,
            utf8Encode(String(plaintext))
          )
          .then(function (ctBuf) {
            return bytesToBase64(concatBytes(header, new Uint8Array(ctBuf)));
          });
      }
    );
  }

  function parseHeader(bytes) {
    if (bytes.length < MIN_PAYLOAD_LEN) throw new Error("INVALID_PAYLOAD");
    for (var i = 0; i < MAGIC.length; i++) {
      if (bytes[i] !== MAGIC[i]) throw new Error("INVALID_PAYLOAD");
    }
    if (bytes[5] !== VERSION) throw new Error("UNSUPPORTED_VERSION");
    if (bytes[6] !== KDF_ID_PBKDF2_SHA256) throw new Error("UNSUPPORTED_KDF");
    if (bytes[7] !== CIPHER_ID_AES_256_GCM) throw new Error("UNSUPPORTED_CIPHER");
    var iterations = (bytes[8] << 16) | (bytes[9] << 8) | bytes[10];
    var saltLen = bytes[11];
    var saltStart = 12;
    var nonceLenPos = saltStart + saltLen;
    var nonceLen = bytes[nonceLenPos];
    var nonceStart = nonceLenPos + 1;
    var headerEnd = nonceStart + nonceLen;
    if (bytes.length < headerEnd + TAG_LEN) throw new Error("INVALID_PAYLOAD");
    return {
      iterations: iterations,
      salt: bytes.slice(saltStart, saltStart + saltLen),
      nonce: bytes.slice(nonceStart, nonceStart + nonceLen),
      header: bytes.slice(0, headerEnd),
      ciphertext: bytes.slice(headerEnd),
    };
  }

  /**
   * 解密。密钥错误或数据被篡改时 reject Error("DECRYPTION_FAILED")。
   * @returns Promise<string> 明文
   */
  function decrypt(payload, passphrase) {
    var bytes;
    try {
      bytes = base64ToBytes(String(payload).trim());
    } catch (e) {
      return Promise.reject(new Error("INVALID_PAYLOAD"));
    }
    var parsed;
    try {
      parsed = parseHeader(bytes);
    } catch (e) {
      return Promise.reject(e);
    }
    return deriveKey(utf8Encode(String(passphrase)), parsed.salt, parsed.iterations).then(
      function (key) {
        return crypto.subtle
          .decrypt(
            {
              name: "AES-GCM",
              iv: parsed.nonce,
              additionalData: parsed.header,
              tagLength: TAG_LEN * 8,
            },
            key,
            parsed.ciphertext
          )
          .then(function (ptBuf) {
            return utf8Decode(new Uint8Array(ptBuf));
          })
          .catch(function () {
            throw new Error("DECRYPTION_FAILED");
          });
      }
    );
  }

  /** 判断文本是否为 CMS-2027 封装格式（不验证密钥） */
  function isCms2027Payload(text) {
    if (!text || typeof text !== "string") return false;
    try {
      parseHeader(base64ToBytes(text.trim()));
      return true;
    } catch (e) {
      return false;
    }
  }

  var api = {
    MAGIC: "CMS27",
    VERSION: VERSION,
    DEFAULT_ITERATIONS: DEFAULT_ITERATIONS,
    BUILTIN_KEY: BUILTIN_KEY,
    encrypt: encrypt,
    decrypt: decrypt,
    isCms2027Payload: isCms2027Payload,
  };

  if (typeof module !== "undefined" && module.exports) {
    module.exports = api;
  } else {
    global.CMS2027 = api;
  }
})(typeof window !== "undefined" ? window : globalThis);
