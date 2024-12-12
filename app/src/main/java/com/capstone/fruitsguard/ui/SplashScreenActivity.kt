package com.capstone.fruitsguard.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.capstone.fruitsguard.MainActivity
import com.capstone.fruitsguard.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")
        // Delay SplashScreen untuk beberapa detik
        Handler().postDelayed({
            checkUserLogin()
        }, 2000) // 2 detik delay

    }

    private fun checkUserLogin() {
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            // User sudah login, ambil data tambahan dari Firebase Database
            val userId = currentUser.uid
            database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val fullName = snapshot.child("fullName").getValue(String::class.java)
                        // Pindah ke halaman utama (HomeActivity) jika sudah login
                        val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                        intent.putExtra("fullName", fullName) // Kirim data jika perlu
                        startActivity(intent)
                        finish()
                    } else {
                        // Jika data user tidak ditemukan, logout dan kembali ke LoginActivity
                        firebaseAuth.signOut()
                        startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error jika diperlukan
                }
            })
        } else {
            // Jika user belum login, arahkan ke LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}