package com.example.noteapp_22ns007.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteapp_22ns007.MainActivity
import com.example.noteapp_22ns007.adapters.LabelAdapter
import com.example.noteapp_22ns007.databinding.FragmentLabelBinding
import com.example.noteapp_22ns007.model.viewmodels.LabelViewModel

class LabelFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private var _binding: FragmentLabelBinding? = null
    private val binding get() = _binding!!

    private lateinit var labelViewModel: LabelViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLabelBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity

        initializeViewElements(binding)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeViewElements(binding: FragmentLabelBinding) {
        labelViewModel = (activity as MainActivity).labelViewModel

        labelViewModel.getAllLabels().observe(viewLifecycleOwner) {
            labels ->
            binding.labelRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            binding.labelRecyclerView.adapter = LabelAdapter(labels)
        }

        binding.btnOpenManageLabel.setOnClickListener {
            mainActivity.hideKeyBoardForSearchBar()
            mainActivity.displayManageLabelFragment()
        }
    }
}