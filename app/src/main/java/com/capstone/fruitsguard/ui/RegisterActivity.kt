package com.capstone.fruitsguard.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.capstone.fruitsguard.R
import com.capstone.fruitsguard.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        binding.btnSignUp.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.editTextPasswordRegister.text.toString().trim()

            if (validateInput(fullName, email, password)) {
                registerUser(fullName, email, password)
            }
        }

        binding.loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun validateInput(fullName: String, email: String, password: String): Boolean {
        return when {
            fullName.isEmpty() -> {
                binding.etFullName.error = "Nama lengkap harus diisi"
                false
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Email harus diisi"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Format email tidak valid"
                false
            }
            password.isEmpty() -> {
                binding.editTextPasswordRegister.error = "Password harus diisi"
                false
            }
            password.length < 8 -> {
                binding.editTextPasswordRegister.error = "Password minimal 8 karakter"
                false
            }
            else -> true
        }
    }

    private fun registerUser(fullName: String, email: String, password: String) {
        showLoading(true)
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val uid = user?.uid ?: ""
                val userMap = mapOf(
                    "fullName" to fullName,
                    "email" to email,
                    "password" to password
                )

                database.child(uid).setValue(userMap)
                    .addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                            showLoading(false)
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            showLoading(false)
                            Toast.makeText(this, "Database error: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
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
}