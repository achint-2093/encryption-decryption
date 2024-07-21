package com.techuntried.encryption

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.techuntried.encryption.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_main
        )

        setOnClickListener()
        binding.inputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement this method
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the TextView with the character count
                val charCount = s?.length ?: 0
                binding.inputCharCount.text = "Character count: $charCount"
            }

            override fun afterTextChanged(s: Editable?) {
                // No need to implement this method
            }
        })
        binding.outputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement this method
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the TextView with the character count
                val charCount = s?.length ?: 0
                binding.outputCharCount.text = "Character count: $charCount"
            }

            override fun afterTextChanged(s: Editable?) {
                // No need to implement this method
            }
        })

    }

    private fun setOnClickListener() {
        val customKey = "your-256-bit-secret-key-goes-hee" // Replace with your actual key
        val keyBytes = customKey.toByteArray(Charsets.UTF_8)
        val secretKey = SecretKeySpec(keyBytes, "AES")
        binding.clearButton.setOnClickListener {
            binding.inputText.setText("")
            binding.outputText.setText("")
        }

        binding.encryptButton.setOnClickListener {
            try {
                val encryptionUtils = EncryptionUtils(secretKey)
                val file = File(filesDir, "encrypted_file.txt")
                val fileOutputStream = FileOutputStream(file)
                val encryptedText =
                    encryptionUtils.encrypt(getInput().encodeToByteArray(), fileOutputStream)
                binding.outputText.setText(encryptedText.decodeToString())
            } catch (e: Exception) {
                binding.outputText.setText(e.message)
            }

        }

        binding.decryptButton.setOnClickListener {
            try {
                val encryptionUtils = EncryptionUtils(secretKey)
                val file = File(filesDir, "encrypted_file.txt")
                val fileInputStream = FileInputStream(file)
                val decryptedText = encryptionUtils.decrypt(fileInputStream)
                binding.outputText.setText(decryptedText.decodeToString())
            } catch (e: Exception) {
                binding.outputText.setText(e.message)
            }

        }

        binding.copyOutput.setOnClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Encrypted Text", binding.outputText.text)
            clipboardManager.setPrimaryClip(clipData)
        }

        binding.decryptAssetButton.setOnClickListener {
            try {
                val encryptionUtils = EncryptionUtils(secretKey)

                val inputStream = assets.open("encrypted_file.txt")
                val decryptedText = encryptionUtils.decrypt(inputStream)
                binding.outputText.setText(decryptedText.decodeToString())
            } catch (e: Exception) {
                binding.outputText.setText(e.message)
            }
        }
    }

    private fun getInput(): String {
        return binding.inputText.text.toString()
    }

    private fun getKey(): String {
        return binding.keyText.text.toString()
    }
}