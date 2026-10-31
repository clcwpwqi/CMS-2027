package com.clcwpwqi.cms2027.core

import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * CMS-2027 — 跨平台文本加密封装协议（Android/Kotlin 实现）
 *
 * KDF:    PBKDF2-HMAC-SHA256 (600,000 次迭代, 32 字节密钥)
 * Cipher: AES-256-GCM (12 字节 nonce, 16 字节标签, AAD = 协议头)
 * 格式:   Base64( magic"CMS27" | ver | kdfId | cipherId | iter24 | saltLen | salt | nonceLen | nonce | ct|tag )
 */
object Cms2027 {

    private val MAGIC = byteArrayOf(0x43, 0x4D, 0x53, 0x32, 0x37) // "CMS27"
    private const val VERSION = 0x01
    private const val KDF_ID_PBKDF2_SHA256 = 0x01
    private const val CIPHER_ID_AES_256_GCM = 0x01
    const val DEFAULT_ITERATIONS = 600_000
    private const val SALT_LEN = 16
    private const val NONCE_LEN = 12
    private const val TAG_LEN_BIT = 128
    private val HEADER_LEN = 5 + 1 + 1 + 1 + 3 + 1 + SALT_LEN + 1 + NONCE_LEN // 41
    private val MIN_PAYLOAD_LEN = HEADER_LEN + 16

    /** 内置密钥（普通模式） */
    const val BUILTIN_KEY = "passwd-cms_2027-buildin-clcwpwqi-267164699"

    class DecryptionFailedException : Exception("DECRYPTION_FAILED")
    class InvalidPayloadException : Exception("INVALID_PAYLOAD")

    private val secureRandom = SecureRandom()

    // ---- Base64（标准，含填充），API 26+ 原生支持 ----
    private fun bytesToBase64(bytes: ByteArray): String =
        java.util.Base64.getEncoder().encodeToString(bytes)

    private fun base64ToBytes(b64: String): ByteArray =
        java.util.Base64.getDecoder().decode(b64.trim())

    private fun buildHeader(iterations: Int, salt: ByteArray, nonce: ByteArray): ByteArray {
        val h = ByteArray(HEADER_LEN)
        var p = 0
        System.arraycopy(MAGIC, 0, h, p, MAGIC.size); p += MAGIC.size
        h[p++] = VERSION.toByte()
        h[p++] = KDF_ID_PBKDF2_SHA256.toByte()
        h[p++] = CIPHER_ID_AES_256_GCM.toByte()
        h[p++] = (iterations ushr 16).toByte()
        h[p++] = (iterations ushr 8).toByte()
        h[p++] = iterations.toByte()
        h[p++] = SALT_LEN.toByte()
        System.arraycopy(salt, 0, h, p, SALT_LEN); p += SALT_LEN
        h[p++] = NONCE_LEN.toByte()
        System.arraycopy(nonce, 0, h, p, NONCE_LEN)
        return h
    }

    /**
     * 手工实现 PBKDF2-HMAC-SHA256（RFC 2898 / PKCS#5 v2.1）。
     * 口令一律按 UTF-8 编码为字节后参与运算，避免不同 Android 密码学 Provider
     * 对 char[] 口令的窄化编码差异，确保与 Web 端 WebCrypto 字节级一致。
     */
    private fun pbkdf2HmacSha256(
        password: ByteArray,
        salt: ByteArray,
        iterations: Int,
        dkLen: Int = 32,
    ): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(password, "HmacSHA256"))
        val hLen = 32
        val out = ByteArray(dkLen)
        var offset = 0
        var blockIndex = 1
        while (offset < dkLen) {
            val indexBytes = byteArrayOf(
                (blockIndex ushr 24).toByte(),
                (blockIndex ushr 16).toByte(),
                (blockIndex ushr 8).toByte(),
                blockIndex.toByte(),
            )
            mac.update(salt)
            var u = mac.doFinal(indexBytes) // U1 = PRF(P, S || INT32_BE(i))
            val t = u.copyOf()
            var j = 1
            while (j < iterations) {
                u = mac.doFinal(u) // doFinal 后 Mac 自动回到带密钥初始状态
                for (k in 0 until hLen) t[k] = (t[k].toInt() xor u[k].toInt()).toByte()
                j++
            }
            val copyLen = minOf(hLen, dkLen - offset)
            System.arraycopy(t, 0, out, offset, copyLen)
            offset += copyLen
            blockIndex++
        }
        Arrays.fill(password, 0.toByte())
        return out
    }

    private fun deriveKey(passphrase: String, salt: ByteArray, iterations: Int): SecretKeySpec {
        val keyBytes = pbkdf2HmacSha256(passphrase.toByteArray(Charsets.UTF_8), salt, iterations)
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * 加密。salt/nonce/iterations 参数仅供测试向量复现，正常调用使用默认值。
     */
    @JvmOverloads
    fun encrypt(
        plaintext: String,
        passphrase: String,
        iterations: Int = DEFAULT_ITERATIONS,
        salt: ByteArray? = null,
        nonce: ByteArray? = null,
    ): String {
        val realSalt = salt ?: ByteArray(SALT_LEN).also { secureRandom.nextBytes(it) }
        val realNonce = nonce ?: ByteArray(NONCE_LEN).also { secureRandom.nextBytes(it) }
        val header = buildHeader(iterations, realSalt, realNonce)
        val key = deriveKey(passphrase, realSalt, iterations)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LEN_BIT, realNonce))
        cipher.updateAAD(header)
        val ct = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return bytesToBase64(header + ct)
    }

    private class Parsed(
        val iterations: Int,
        val salt: ByteArray,
        val nonce: ByteArray,
        val header: ByteArray,
        val ciphertext: ByteArray,
    )

    private fun parse(bytes: ByteArray): Parsed {
        if (bytes.size < MIN_PAYLOAD_LEN) throw InvalidPayloadException()
        if (!Arrays.copyOfRange(bytes, 0, MAGIC.size).contentEquals(MAGIC)) {
            throw InvalidPayloadException()
        }
        if (bytes[5] != VERSION.toByte()) throw InvalidPayloadException()
        if (bytes[6] != KDF_ID_PBKDF2_SHA256.toByte()) throw InvalidPayloadException()
        if (bytes[7] != CIPHER_ID_AES_256_GCM.toByte()) throw InvalidPayloadException()
        val iterations =
            ((bytes[8].toInt() and 0xFF) shl 16) or
                ((bytes[9].toInt() and 0xFF) shl 8) or
                (bytes[10].toInt() and 0xFF)
        val saltLen = bytes[11].toInt() and 0xFF
        val saltStart = 12
        val nonceLenPos = saltStart + saltLen
        val nonceLen = bytes[nonceLenPos].toInt() and 0xFF
        val nonceStart = nonceLenPos + 1
        val headerEnd = nonceStart + nonceLen
        if (bytes.size < headerEnd + 16) throw InvalidPayloadException()
        return Parsed(
            iterations = iterations,
            salt = bytes.copyOfRange(saltStart, saltStart + saltLen),
            nonce = bytes.copyOfRange(nonceStart, nonceStart + nonceLen),
            header = bytes.copyOfRange(0, headerEnd),
            ciphertext = bytes.copyOfRange(headerEnd, bytes.size),
        )
    }

    /**
     * 解密。
     * @throws DecryptionFailedException 密钥错误或数据被篡改
     * @throws InvalidPayloadException 输入不是合法的 CMS-2027 封装格式
     */
    fun decrypt(payload: String, passphrase: String): String {
        val bytes = try {
            base64ToBytes(payload)
        } catch (e: IllegalArgumentException) {
            throw InvalidPayloadException()
        }
        val parsed = parse(bytes)
        val key = deriveKey(passphrase, parsed.salt, parsed.iterations)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LEN_BIT, parsed.nonce))
            cipher.updateAAD(parsed.header)
            String(cipher.doFinal(parsed.ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            throw DecryptionFailedException()
        }
    }

    /** 判断文本是否为 CMS-2027 封装格式（不验证密钥） */
    fun isCms2027Payload(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        return try {
            parse(base64ToBytes(text))
            true
        } catch (e: Exception) {
            false
        }
    }
}
