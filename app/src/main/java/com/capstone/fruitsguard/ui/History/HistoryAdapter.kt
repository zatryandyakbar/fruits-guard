package com.capstone.fruitsguard.ui.History

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.fruitsguard.R
import com.capstone.fruitsguard.data.database.ScanResultEntity
import com.capstone.fruitsguard.databinding.ItemHistoryListBinding
import java.io.File


class HistoryAdapter : ListAdapter<ScanResultEntity, HistoryAdapter.ViewHolder>(ScanResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scanResult = getItem(position) // Menggunakan getItem untuk mengambil data
        holder.bind(scanResult)
    }

    inner class ViewHolder(private val binding: ItemHistoryListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(scanResult: ScanResultEntity) {
            binding.apply {
                tvNamaBuahHistory.text = scanResult.fruitName
                tvResultBuahHistory.text = scanResult.result
                tvResultBuahHistory.setTextColor(scanResult.color)

                // Gunakan Glide dengan error handling
                Glide.with(imgPhotoHistory.context)
                    .load(File(scanResult.imageUrl))
                    .error(R.drawable.image_preview) // Tambahkan placeholder gambar
                    .placeholder(R.drawable.image_preview)
                    .into(imgPhotoHistory)

                imgDeleteHistory.setOnClickListener {
                    onDeleteClickListener?.invoke(scanResult)
                }
            }
        }
    }

    //Tambahkan property untuk listener
    private var onDeleteClickListener: ((ScanResultEntity) -> Unit)? = null

    //Fungsi untuk setListener
    fun setOnDeleteClickListener(listener: (ScanResultEntity) -> Unit) {
        onDeleteClickListener = listener
    }

    // DiffCallback untuk membandingkan item dan memutuskan perubahan data
    class ScanResultDiffCallback : DiffUtil.ItemCallback<ScanResultEntity>() {
        override fun areItemsTheSame(oldItem: ScanResultEntity, newItem: ScanResultEntity): Boolean {
            return oldItem.id == newItem.id // Pastikan id unik di setiap item
        }

        override fun areContentsTheSame(oldItem: ScanResultEntity, newItem: ScanResultEntity): Boolean {
            return oldItem == newItem
        }
    }
}