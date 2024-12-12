package com.capstone.fruitsguard.data.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "result_data")
@Parcelize
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fruitName: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String,// Tambahkan URL gambar jika diperlukan
    val color: Int
) : Parcelable
