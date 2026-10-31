// CMS-2027 JS 实现自测脚本：node selftest.js
"use strict";
const CMS2027 = require("./cms2027.js");
const fs = require("fs");
const path = require("path");

function hexToBytes(hex) {
  const out = new Uint8Array(hex.length / 2);
  for (let i = 0; i < out.length; i++) out[i] = parseInt(hex.substr(i * 2, 2), 16);
  return out;
}

(async () => {
  let failures = 0;
  const check = (name, cond) => {
    console.log((cond ? "PASS" : "FAIL") + "  " + name);
    if (!cond) failures++;
  };

  // 1. 基本往返
  const t0 = Date.now();
  const ct1 = await CMS2027.encrypt("Hello CMS-2027!", "my-secret-key");
  const pt1 = await CMS2027.decrypt(ct1, "my-secret-key");
  check("roundtrip ascii", pt1 === "Hello CMS-2027!");

  // 2. Unicode（汉字/emoji/俄语/拉丁语等多语言）往返
  const uni = "汉字测试 emoji🔐✨ Русский Français Deutsch Latina 繁體中文 ñ ü αβγ مرحبا こんにちは";
  const ct2 = await CMS2027.encrypt(uni, "密钥🔑with-unicode");
  const pt2 = await CMS2027.decrypt(ct2, "密钥🔑with-unicode");
  check("roundtrip unicode plaintext + unicode key", pt2 === uni);

  // 3. 内置密钥模式
  const ct3 = await CMS2027.encrypt("normal mode", CMS2027.BUILTIN_KEY);
  const pt3 = await CMS2027.decrypt(ct3, CMS2027.BUILTIN_KEY);
  check("roundtrip builtin key", pt3 === "normal mode");

  // 4. 错误密钥必须失败
  let wrongKeyFailed = false;
  try {
    await CMS2027.decrypt(ct1, "wrong-key");
  } catch (e) {
    wrongKeyFailed = e.message === "DECRYPTION_FAILED";
  }
  check("wrong key rejected (DECRYPTION_FAILED)", wrongKeyFailed);

  // 5. 篡改检测
  const bytes = Buffer.from(ct1, "base64");
  bytes[bytes.length - 1] ^= 0x01;
  let tamperFailed = false;
  try {
    await CMS2027.decrypt(bytes.toString("base64"), "my-secret-key");
  } catch (e) {
    tamperFailed = true;
  }
  check("tampered ciphertext rejected", tamperFailed);

  // 6. 随机性：同明文同密钥两次输出不同
  const c1 = await CMS2027.encrypt("same", "same");
  const c2 = await CMS2027.encrypt("same", "same");
  check("random salt/nonce (outputs differ)", c1 !== c2);
  check("second output decrypts", (await CMS2027.decrypt(c2, "same")) === "same");

  // 7. isCms2027Payload
  check("isCms2027Payload true", CMS2027.isCms2027Payload(ct1));
  check("isCms2027Payload false", !CMS2027.isCms2027Payload("not a payload"));

  // 8. 确定性测试向量（固定 salt/nonce），写入 test-vectors.json
  const salt = hexToBytes("00112233445566778899aabbccddeeff");
  const nonce = hexToBytes("0102030405060708090a0b0c");
  const vecCt = await CMS2027.encrypt("CMS-2027 测试向量 test vector 🔐", "vec-key-267164699", {
    salt, nonce,
  });
  const vecPt = await CMS2027.decrypt(vecCt, "vec-key-267164699");
  check("deterministic vector roundtrip", vecPt === "CMS-2027 测试向量 test vector 🔐");

  const vectors = {
    algorithm: "CMS-2027 v1",
    kdf: "PBKDF2-HMAC-SHA256",
    cipher: "AES-256-GCM",
    vectors: [
      {
        name: "deterministic-fixed-salt-nonce",
        plaintext: "CMS-2027 测试向量 test vector 🔐",
        passphrase: "vec-key-267164699",
        iterations: 600000,
        salt_hex: "00112233445566778899aabbccddeeff",
        nonce_hex: "0102030405060708090a0b0c",
        output_base64: vecCt,
      },
    ],
  };
  fs.writeFileSync(
    path.join(__dirname, "..", "test-vectors.json"),
    JSON.stringify(vectors, null, 2) + "\n"
  );
  console.log("\nTest vector written. Encryption+decryption total time: " + (Date.now() - t0) + "ms");
  console.log("Vector output: " + vecCt);

  if (failures > 0) {
    console.error(failures + " test(s) FAILED");
    process.exit(1);
  }
  console.log("All tests passed.");
})();
