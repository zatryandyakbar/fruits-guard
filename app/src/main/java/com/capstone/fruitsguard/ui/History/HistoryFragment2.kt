package com.capstone.fruitsguard.ui.History

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.capstone.fruitsguard.R
import com.capstone.fruitsguard.data.database.AppDatabase
import com.capstone.fruitsguard.databinding.FragmentHistory2Binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment2 : Fragment() {

    private lateinit var binding: FragmentHistory2Binding
    private lateinit var adapter: HistoryAdapter
    private lateinit var recyclerView: RecyclerView
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
        binding = FragmentHistory2Binding.inflate(inflater, container, false)

        // Inisialisasi RecyclerView dan adapter
        adapter = HistoryAdapter()
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment2.adapter
        }

        // Load data dari database
        loadHistoryData()

        return binding.root
    }

    private fun loadHistoryData() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val scanResults = database.scanResultDao().getAllScanResults()
            withContext(Dispatchers.Main) {
                adapter.submitList(scanResults)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Tutup database jika perlu
        database.close()
    }
}