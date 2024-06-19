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
    private lateinit var labelAdapter: LabelAdapter

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

        labelAdapter = LabelAdapter(emptyList())

        binding.labelRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = labelAdapter
        }

        labelViewModel.getAllLabels().observe(viewLifecycleOwner) {
            labels -> labelAdapter.updateLabel(labels)
        }

        binding.btnOpenManageLabel.setOnClickListener {
            mainActivity.hideKeyBoardForSearchBar()
            mainActivity.displayManageLabelFragment(/*ManageLabelFragment()*/)
        }
    }
}