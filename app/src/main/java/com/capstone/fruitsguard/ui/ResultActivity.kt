package com.capstone.fruitsguard.ui

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.fruitsguard.R
import com.capstone.fruitsguard.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)
        val hasil = intent.getStringExtra("hasil")
        val namaBuah = intent.getStringExtra("selectedFruit")
        val description = intent.getStringExtra("description")

        binding.namaBuah.text = "$namaBuah"
        binding.kematangan.text = "$hasil"
        if (imageUri != null) {
            binding.resultImage.setImageURI(imageUri)
        }
        binding.manfaat.text = "$description"
    }
}