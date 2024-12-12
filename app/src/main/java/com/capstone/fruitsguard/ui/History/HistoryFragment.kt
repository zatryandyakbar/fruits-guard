package com.capstone.fruitsguard.ui.History

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.capstone.fruitsguard.data.database.AppDatabase
import com.capstone.fruitsguard.databinding.FragmentHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var adapter: HistoryAdapter

    private val database by lazy {
        Room.databaseBuilder(
            requireContext().applicationContext, // Gunakan application context
            AppDatabase::class.java,
            "result_data"
        ).build()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        // Inisialisasi RecyclerView dan adapter
        adapter = HistoryAdapter()
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }

        //Tambahkan Listener
        setupDeleteListener()

        // Load data dari database
        loadHistoryData()

        return binding.root
    }

    private fun setupDeleteListener() {
        adapter.setOnDeleteClickListener { scanResult ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                database.scanResultDao().deleteScanResultById(scanResult.id)
                val updateList = database.scanResultDao().getAllScanResults()
                withContext(Dispatchers.Main) {
                    adapter.submitList(updateList)
                    Toast.makeText(requireContext(), "History berhasil dihapus!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadHistoryData() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val scanResults = database.scanResultDao().getAllScanResults()
            withContext(Dispatchers.Main) {
                adapter.submitList(scanResults)

                binding.tvEmptyHistory.visibility = if (scanResults.isEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Tutup database jika perlu
        database.close()
    }
}