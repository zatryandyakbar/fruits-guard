package com.capstone.fruitsguard.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.capstone.fruitsguard.data.retrofit.ApiClient
import com.capstone.fruitsguard.data.remote.PredictResponse
import com.capstone.fruitsguard.R
import com.capstone.fruitsguard.data.database.AppDatabase
import com.capstone.fruitsguard.data.database.ScanResultEntity
import com.capstone.fruitsguard.databinding.FragmentDetectBinding
import com.capstone.fruitsguard.getImageUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class DetectFragment : Fragment() {

    private var _binding: FragmentDetectBinding? = null
    private val binding get() = _binding!!
    private var currentImageUri: Uri? = null
    private var originalImageUri: Uri? = null
    private var selectedFruit: String? = null
    private var isFromResultActivity = false

    // Tambahkan instance Room Database
    private lateinit var appDatabase: AppDatabase

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast("Permission request granted")
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
        appDatabase = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "result_data"
        ).build()
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
        showLoading(true)
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

                            // Simpan hasil analisis ke dalam Room Database
                            saveAnalysisResult(selectedFruit, result ?: "Tidak Diketahui", imageUri)
                            moveToResultActivity(result, selectedFruit, description)
                        } else {
                            val errorBody = response.errorBody()?.string()
                            showLoading(false)
                            Log.e("API Error", "Error Body: $errorBody")
                            showToast("Gagal memproses: $errorBody")
                        }
                    }

                    override fun onFailure(call: Call<PredictResponse>, t: Throwable) {
                        Log.e("API Failure", "Error: ${t.message}")
                        showToast("Gagal terhubung: ${t.message}")
                        showLoading(false)
                    }
                })
            } else {
                showToast("Gagal membaca gambar")
                showLoading(false)
            }
        } catch (e: Exception) {
            Log.e("AnalyzeImage Error", "Exception: ${e.message}")
            showToast("Terjadi kesalahan: ${e.message}")
            showLoading(false)
        }
    }

    private fun saveAnalysisResult(fruit: String, result: String, imageUri: Uri) {
        // Gunakan viewModelScope atau lifecycleScope
        showLoading(false)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val color = if (result == "Segar") Color.GREEN else Color.RED
                val savedImageFile = saveImageToInternalStorage(imageUri)
                val analysisResult = ScanResultEntity(
                    fruitName = fruit,
                    result = result,
                    imageUrl = savedImageFile.absolutePath,
                    color = color
                )
                appDatabase.scanResultDao().insertScanResult(analysisResult)
                withContext(Dispatchers.Main) {
                    // Tambahkan notifikasi atau feedback jika perlu
                    Toast.makeText(requireContext(), "Hasil analisis disimpan", Toast.LENGTH_SHORT).show()
                }
                Log.d("DetectFragment", "Hasil analisis disimpan di Room Database.")
            } catch (e: Exception) {
                Log.e("DetectFragment", "Gagal menyimpan hasil analisis", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal menyimpan hasil", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImageToInternalStorage(imageUri: Uri): File {
        val context = requireContext()
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val fileName = "fruit_${System.currentTimeMillis()}.jpg"

        val outputFile = File(context.filesDir, fileName)
        inputStream?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return outputFile
    }

    private fun moveToResultActivity(hasil: String?, selectedFruit: String, description: String) {
        isFromResultActivity = true
        val intent = Intent(requireContext(), ResultActivity::class.java).apply {
            putExtra("hasil", hasil)
            putExtra("imageUri", currentImageUri.toString())
            putExtra("selectedFruit", selectedFruit)
            putExtra("description", description)
            val color = if (hasil == "Segar") Color.GREEN else Color.RED
            putExtra("resultColor", color)
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

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.visibility = View.VISIBLE
            binding.loadingAnimationOverlay.playAnimation()
        } else {
            binding.loadingOverlay.visibility = View.GONE
            binding.loadingAnimationOverlay.cancelAnimation()
        }
    }

    companion object {
        private val REQUIRED_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
