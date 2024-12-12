package com.capstone.fruitsguard.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.capstone.fruitsguard.ProfileViewModel
import com.capstone.fruitsguard.R
import com.capstone.fruitsguard.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            profileViewModel.uploadProfilePhoto(uri)
            binding.imgViewProfile.setImageURI(uri) // Tampilkan sementara
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")
        storage = FirebaseStorage.getInstance().reference

        // Periksa status pengguna
        checkUserStatus()

        // Observasi LiveData dari ViewModel
        observeViewModel()

        // Muat data pengguna
        profileViewModel.loadUserProfile()
        profileViewModel.loadProfilePhoto()

        // Pilih gambar untuk mengubah foto profil
        binding.imgViewChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Tombol Logout
        binding.buttonLogout.setOnClickListener {
            signOut()
        }
    }

    private fun checkUserStatus() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            // Tidak login, arahkan ke LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut() // Logout dari Firebase
        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(this@ProfileActivity)
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Toast.makeText(this@ProfileActivity, "Berhasil logout", Toast.LENGTH_SHORT).show()

            // Arahkan ke LoginActivity dan hapus ProfileActivity dari stack
            val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }


    private fun observeViewModel() {
        // Observasi nama lengkap dari ViewModel
        profileViewModel.fullName.observe(this, Observer { name ->
            binding.tvFullNameProfile.text = name
        })

        // Observasi URL foto dari ViewModel
        profileViewModel.imageUrl.observe(this, Observer { photoUrl ->
            Glide.with(this)
                .load(photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache gambar
                .placeholder(R.drawable.ic_profile)
                .into(binding.imgViewProfile)
        })

        // Observasi pesan toast dari ViewModel
        profileViewModel.toastMessage.observe(this, Observer { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                // Reset pesan setelah ditampilkan untuk menghindari pengulangan
                profileViewModel.clearToastMessage()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home-> {
                onBackPressed() // Menangani tombol back
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}