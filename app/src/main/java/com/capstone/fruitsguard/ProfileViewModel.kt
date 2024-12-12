package com.capstone.fruitsguard

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
    private val storage: StorageReference = FirebaseStorage.getInstance().reference


    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> get() = _fullName

    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: LiveData<String?> get() = _imageUrl

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> get() = _toastMessage

    // Function to load user profile data
    fun loadUserProfile() {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            database.child(uid).get().addOnSuccessListener { snapshot ->
                val name = snapshot.child("fullName").value?.toString() ?: "Unknown User"
                // Update LiveData with new values
                _fullName.value = name
            }.addOnFailureListener {
                _fullName.value = "Failed to load name"
            }
        } else {
            _fullName.value = "User not logged in"
        }
    }

    // Function to load user profile photo
    fun loadProfilePhoto() {
        val currentUser = auth.currentUser
        _imageUrl.value = currentUser?.photoUrl?.toString()
        val uid = currentUser?.uid

        if (uid != null) {
            database.child(uid).get().addOnSuccessListener { snapshot ->
                val image = snapshot.child("imageUrl").value?.toString() ?: ""
                // Update LiveData with new values
                _imageUrl.value = image
            }.addOnFailureListener {
                _imageUrl.value = ""
            }
        } else {
            _imageUrl.value = ""
        }
    }

    // Function to upload profile photo
    fun uploadProfilePhoto(uri: Uri) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid ?: return

        val photoRef = storage.child("assets/$uid.jpg")

        photoRef.putFile(uri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveProfilePhotoUrl(downloadUri.toString())
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
                _toastMessage.value = "Gagal upload Photo: ${exception.message}"
            }
    }

    // Fungsi untuk membersihkan pesan toast setelah ditampilkan
    fun clearToastMessage() {
        _toastMessage.value = null
    }

    // Save URL of the uploaded photo to Firebase
    private fun saveProfilePhotoUrl(imageUrl: String) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid ?: return

        database.child(uid).child("imageUrl").setValue(imageUrl)
            .addOnSuccessListener {
                // Handle success
                _toastMessage.value = "Profile photo updated!"
            }
            .addOnFailureListener { exception ->
                _toastMessage.value = "Failed to save photo URL: ${exception.message}"
            }
    }
}