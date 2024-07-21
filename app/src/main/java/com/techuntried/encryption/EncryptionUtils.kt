package com.techuntried.encryption

import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class EncryptionUtils(private val secretKey: SecretKey) {

    companion object {
        private const val ALGORITHM = "AES"
        private const val BLOCK_MODE = "CBC"
        private const val PADDING = "PKCS5Padding"
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }

    private val encryptCipher = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, secretKey)
    }

    private fun decryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        }
    }

    fun encrypt(bytes: ByteArray, outputStream: OutputStream): ByteArray {
        val encryptedBytes = encryptCipher.doFinal(bytes)
        val iv = encryptCipher.iv

        val ivSizeBuffer = ByteBuffer.allocate(4).putInt(iv.size).array()
        val encryptedBytesSizeBuffer = ByteBuffer.allocate(4).putInt(encryptedBytes.size).array()

        outputStream.use {
            it.write(ivSizeBuffer)
            it.write(iv)
            it.write(encryptedBytesSizeBuffer)
            it.write(encryptedBytes)
        }

        return encryptedBytes
    }

    fun decrypt(inputStream: InputStream): ByteArray {
        return inputStream.use {
            val ivSizeBuffer = ByteArray(4)
            it.read(ivSizeBuffer)
            val ivSize = ByteBuffer.wrap(ivSizeBuffer).int

            val iv = ByteArray(ivSize)
            it.read(iv)

            val encryptedBytesSizeBuffer = ByteArray(4)
            it.read(encryptedBytesSizeBuffer)
            val encryptedBytesSize = ByteBuffer.wrap(encryptedBytesSizeBuffer).int

            val encryptedBytes = ByteArray(encryptedBytesSize)
            it.read(encryptedBytes)

            decryptCipherForIv(iv).doFinal(encryptedBytes)
        }
    }
}
