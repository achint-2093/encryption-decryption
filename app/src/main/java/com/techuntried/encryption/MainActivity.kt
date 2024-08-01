package com.techuntried.encryption

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.techuntried.encryption.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import javax.crypto.spec.SecretKeySpec


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val selectFileCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                intent?.let {
                    Toast.makeText(this, "not null", Toast.LENGTH_SHORT).show()
                    val uri = it.data
                    uri?.let { uriNotNull ->
                        try {
                            val content = readFileContent(uriNotNull)
                            binding.inputText.setText(content)
                        } catch (e: Exception) {
                            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }


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

    @Throws(IOException::class)
    private fun readFileContent(uri: Uri): String {
        val stringBuilder = StringBuilder()
        val inputStream = contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))

        var line: String?
        while ((reader.readLine().also { line = it }) != null) {
            stringBuilder.append(line)
            stringBuilder.append("\n") // Append newline character for each line
        }

        reader.close()
        return stringBuilder.toString()
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
        binding.pasteKey.setOnClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val pasteText = clipData.getItemAt(0).text.toString()
                binding.keyText.setText(pasteText)
            }
        }

        binding.selectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("*/*")
            val mimeTypes = arrayOf("text/plain", "application/json")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            selectFileCallback.launch(intent)
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