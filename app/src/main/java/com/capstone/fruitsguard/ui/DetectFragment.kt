package com.capstone.fruitsguard.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.capstone.fruitsguard.data.retrofit.ApiClient
import com.capstone.fruitsguard.data.remote.PredictResponse
import com.capstone.fruitsguard.R
import com.capstone.fruitsguard.databinding.FragmentDetectBinding
import com.capstone.fruitsguard.getImageUri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetectFragment : Fragment() {

    private var _binding: FragmentDetectBinding? = null
    private val binding get() = _binding!!
    private var currentImageUri: Uri? = null
    private var originalImageUri: Uri? = null
    private var selectedFruit: String? = null
    private var isFromResultActivity = false

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast("Permission request granted")
            } else {
                showToast("Permission request denied")
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.btnGallery.setOnClickListener {
            startGallery()
        }

        binding.btnCamera.setOnClickListener {
            startCamera()
        }


        val options = mutableListOf("Pilih Buah")
        options.addAll(resources.getStringArray(R.array.fruit_array))
        val spinnerOptions = binding.spinnerOptions

        spinnerOptions.setItems(options)

        val fruitDescriptions = resources.getStringArray(R.array.fruit_descriptions)

        spinnerOptions.setOnItemSelectedListener { _, position, _, item ->
            if (position == 0) {

            } else {
                selectedFruit = item.toString()
                if (options.contains("Pilih Buah")) {
                    options.remove("Pilih Buah")
                    spinnerOptions.setItems(options)
                    spinnerOptions.selectedIndex = position - 1
                }
            }
            Log.d("DetectFragment", "Selected fruit: $selectedFruit")
        }

        binding.btnDetect.setOnClickListener {
            if (selectedFruit.isNullOrEmpty()) {
                showToast("Pilih jenis buah terlebih dahulu")
                return@setOnClickListener
            }
            val fruitIndex = options.indexOf(selectedFruit)
            val description = fruitDescriptions.getOrNull(fruitIndex) ?: "Deskripsi tidak tersedia"
            currentImageUri?.let { uri ->
                analyzeImage(uri, selectedFruit!!, description)
            } ?: showToast("Pilih gambar terlebih dahulu")
        }
    }


    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCamera() {
        currentImageUri = getImageUri(requireContext())
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            originalImageUri = uri
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }
    @SuppressLint("Recycle")
    private fun analyzeImage(imageUri: Uri, selectedFruit: String, description: String) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val requestBody = inputStream?.readBytes()?.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = requestBody?.let {
                MultipartBody.Part.createFormData("input", "image.jpg", it)
            }

            if (imagePart != null) {
                val call = ApiClient.instance.uploadImage(imagePart)
                call.enqueue(object : Callback<PredictResponse> {
                    override fun onResponse(call: Call<PredictResponse>, response: Response<PredictResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val result = response.body()?.hasil
                            moveToResultActivity(result, selectedFruit, description)
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("API Error", "Error Body: $errorBody")
                            showToast("Gagal memproses: $errorBody")
                        }
                    }

                    override fun onFailure(call: Call<PredictResponse>, t: Throwable) {
                        Log.e("API Failure", "Error: ${t.message}")
                        showToast("Gagal terhubung: ${t.message}")
                    }
                })
            } else {
                showToast("Gagal membaca gambar")
            }
        } catch (e: Exception) {
            Log.e("AnalyzeImage Error", "Exception: ${e.message}")
            showToast("Terjadi kesalahan: ${e.message}")
        }
    }


    private fun moveToResultActivity(hasil: String?, selectedFruit: String, description: String) {
        isFromResultActivity = true
        val intent = Intent(requireContext(), ResultActivity::class.java).apply {
            putExtra("hasil", hasil)
            putExtra("imageUri", currentImageUri.toString())
            putExtra("selectedFruit", selectedFruit)
            putExtra("description", description)
        }
        startActivity(intent)
    }


    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            try {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.previewImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("Image Load Error", "Error loading image: ${e.message}")
                showToast("Gagal memuat gambar")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (isFromResultActivity) {
            resetView()
            isFromResultActivity = false
        }
    }

    private fun resetView() {
        // Reset preview image
        binding.previewImageView.setImageResource(R.drawable.image_preview) // Gambar default

        // Reset spinner
        val options = mutableListOf("Pilih Buah")
        options.addAll(resources.getStringArray(R.array.fruit_array))
        binding.spinnerOptions.setItems(options)
        binding.spinnerOptions.selectedIndex = 0

        // Reset state variables
        currentImageUri = null
        originalImageUri = null
        selectedFruit = null
    }

    companion object {
        private val REQUIRED_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

