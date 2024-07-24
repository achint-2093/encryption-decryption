package com.techuntried.encryption

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
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

        binding.scrollView.setOnScrollChangeListener { v, _, scrollY, oldScrollX, oldScrollY ->
            val totalHeight = binding.scrollView.getChildAt(0).height - binding.scrollView.height
            binding.fabUp.visibility = if (scrollY > 100) View.VISIBLE else View.GONE
            binding.fabDown.visibility =
                if (scrollY < totalHeight - 100) View.VISIBLE else View.GONE
        }

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
        binding.keyText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement this method
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the TextView with the character count
                val charCount = s?.length ?: 0
                binding.keyCharCount.text = "Character count: $charCount"
            }

            override fun afterTextChanged(s: Editable?) {
                // No need to implement this method
            }
        })

    }


    private fun setOnClickListener() {
        binding.fabUp.setOnClickListener {
            binding.scrollView.smoothScrollTo(0, 0)
        }
        binding.fabDown.setOnClickListener {
            binding.scrollView.smoothScrollTo(0, binding.scrollView.getChildAt(0).height)
        }
        binding.clearButton.setOnClickListener {
            binding.inputText.setText("")
            binding.outputText.setText("")
        }
        binding.openLocation.setOnClickListener {
            Toast.makeText(this, "not implemented", Toast.LENGTH_SHORT).show()
        }

        binding.encryptButton.setOnClickListener {
            val customKey = getKey()
            customKey?.let {
                val keyBytes = customKey.toByteArray(Charsets.UTF_8)
                val secretKey = SecretKeySpec(keyBytes, "AES")
                try {
                    val encryptionUtils = EncryptionUtils(secretKey)
                    val file = File(filesDir, getFileName())
                    val fileOutputStream = FileOutputStream(file)
                    val encryptedText =
                        encryptionUtils.encrypt(getInput().encodeToByteArray(), fileOutputStream)
                    binding.outputText.setText(encryptedText.decodeToString())
                } catch (e: Exception) {
                    binding.outputText.setText(e.message)
                }
            }// Replace with your actual key
        }

        binding.decryptButton.setOnClickListener {
            val customKey = getKey()
            customKey?.let {
                try {
                    val keyBytes = customKey.toByteArray(Charsets.UTF_8)
                    val secretKey = SecretKeySpec(keyBytes, "AES")
                    val encryptionUtils = EncryptionUtils(secretKey)
                    val file = File(filesDir, getFileName())
                    val fileInputStream = FileInputStream(file)
                    val decryptedText = encryptionUtils.decrypt(fileInputStream)
                    binding.outputText.setText(decryptedText.decodeToString())
                } catch (e: Exception) {
                    binding.outputText.setText(e.message)
                }
            }


        }

        binding.copyOutput.setOnClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Encrypted Text", binding.outputText.text)
            clipboardManager.setPrimaryClip(clipData)
        }

        binding.decryptAssetButton.setOnClickListener {
            val customKey = getKey()
            customKey?.let {
                try {
                    val keyBytes = customKey.toByteArray(Charsets.UTF_8)
                    val secretKey = SecretKeySpec(keyBytes, "AES")
                    val encryptionUtils = EncryptionUtils(secretKey)
                    val inputStream = assets.open("encrypted_file.json")
                    val decryptedText = encryptionUtils.decrypt(inputStream)
                    binding.outputText.setText(decryptedText.decodeToString())
                } catch (e: Exception) {
                    binding.outputText.setText(e.message)
                }
            }
        }

        binding.generateKey.setOnClickListener {
            val characters =
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#%^*()-_=+[{]}|;:,<.>/?~"
            val key = (1..32)
                .map { characters.random() }
                .joinToString("")
            binding.keyText.setText(key)
        }
        binding.clearKey.setOnClickListener {
            binding.keyText.setText("")
        }
        binding.copyKey.setOnClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Key", binding.keyText.text)
            clipboardManager.setPrimaryClip(clipData)
        }
    }

    private fun getInput(): String {
        return binding.inputText.text.toString()
    }

    private fun getFileName(): String {
        val fileName = binding.fileName.text.toString()
        if (fileName.isNotEmpty()) {
            return fileName
        } else {
            Toast.makeText(this, "using encrypted_file.txt", Toast.LENGTH_SHORT).show()
            return "encrypted_file.txt"
        }
    }

    private fun getKey(): String? {
        val key = binding.keyText.text.toString()
        if (key.length == 32) {
            return key
        } else {
            Toast.makeText(this, "key length must be 32", Toast.LENGTH_SHORT).show()
            return null
        }
    }


}