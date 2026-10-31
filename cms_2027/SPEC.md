# CMS-2027 算法规范（Protocol Specification v1）

CMS-2027 是一种面向文本内容的加密封装协议。它不重新发明底层密码学原语，
而是将业界公认的高强度原语（AES-256-GCM 与 PBKDF2-HMAC-SHA256）以固定格式组合，
并定义了跨平台（Web / Android）完全一致的二进制封装格式，保证两端加密结果可互相解密。

## 1. 设计目标

- **高强度**：256 位密钥、认证加密（AEAD）、高迭代次数口令派生、每次加密随机盐与随机 nonce。
- **跨平台一致**：仅依赖 Web Crypto API（浏览器）与 javax.crypto（Android/JVM）共有的原语。
- **全字符集**：所有文本（原文与密钥）一律按 UTF-8 编码，支持字母、数字、汉字、拼音、
  emoji 以及世界上绝大多数语言文字。
- **防篡改**：GCM 认证标签覆盖密文与协议头，任何改动或错误密钥都会导致解密失败。

## 2. 密码学原语

| 用途 | 算法 | 参数 |
|------|------|------|
| 密钥派生 (KDF) | PBKDF2-HMAC-SHA256 | 默认迭代 600,000 次，输出 32 字节 |
| 对称加密 (AEAD) | AES-256-GCM | 12 字节 nonce，16 字节认证标签 |
| 随机数 | CSPRNG | 浏览器 `crypto.getRandomValues` / Android `SecureRandom` |

## 3. 封装格式（加密输出的二进制布局）

```
偏移  长度  字段            说明
0     5     magic           ASCII "CMS27" (0x43 0x4D 0x53 0x32 0x37)
5     1     version         协议版本，当前为 0x01
6     1     kdfId           KDF 标识，0x01 = PBKDF2-HMAC-SHA256
7     1     cipherId        密码标识，0x01 = AES-256-GCM
8     3     iterations      KDF 迭代次数，uint24 大端
11    1     saltLength      盐长度，当前为 16 (0x10)
12    16    salt            随机盐
28    1     nonceLength     nonce 长度，当前为 12 (0x0C)
29    12    nonce           随机 nonce
41    N     ciphertext||tag AES-GCM 密文 + 16 字节认证标签
```

最终输出 = `Base64(header(41B) || ciphertext || tag)`，标准 Base64（含 `+/=`）。

## 4. 加密流程

1. 明文 `P` 与口令 `K` 分别编码为 UTF-8 字节序列。
2. 生成 16 字节随机 `salt`、12 字节随机 `nonce`。
3. `key = PBKDF2-HMAC-SHA256(password=K, salt, iterations=600000, dkLen=32)`。
4. 组装 41 字节协议头 `header`。
5. `ct = AES-256-GCM-Encrypt(key, nonce, plaintext=P, aad=header)`。
6. 输出 `Base64(header || ct)`。

## 5. 解密流程

1. Base64 解码；校验长度 ≥ 57 字节（41 头 + 16 标签）、magic、version、kdfId、cipherId。
2. 按头部参数派生 `key`。
3. `AES-256-GCM-Decrypt(key, nonce, ct, aad=header)`。
4. 认证失败（密钥错误 / 数据被篡改）抛出统一异常 `DECRYPTION_FAILED`。
5. 明文按 UTF-8 解码还原字符串。

## 6. 辅助接口

- `encrypt(plaintext, passphrase) -> string`
- `decrypt(payload, passphrase) -> string`
- `isCms2027Payload(text) -> boolean`：判断一段文本是否为合法的 CMS-2027 封装格式
  （仅做格式校验，不验证密钥）。

## 7. 安全说明

- 每次加密使用独立随机盐与 nonce，同一明文 + 同一密钥每次输出均不同。
- 迭代次数写入协议头，未来可平滑调高默认值且旧数据仍可解密。
- 口令不在输出中出现；错误口令与篡改数据在 GCM 认证阶段被一并拒绝。
- 普通模式（内置密钥）仅提供混淆级保护，因为密钥公开；强加密模式（自定义密钥）
  提供完整的密码学保护，密钥强度由用户口令决定。

## 8. 测试向量

见同目录 `test-vectors.json`，包含固定 salt/nonce 的确定性向量与 Unicode 样例，
两个平台实现都必须能复现该向量。
