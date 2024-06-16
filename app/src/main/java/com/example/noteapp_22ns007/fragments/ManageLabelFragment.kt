package com.example.noteapp_22ns007.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.noteapp_22ns007.MainActivity
import com.example.noteapp_22ns007.R
import com.example.noteapp_22ns007.databinding.FragmentManageLabelBinding
import com.example.noteapp_22ns007.model.viewmodels.LabelViewModel

class ManageLabelFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private var _binding: FragmentManageLabelBinding? = null
    private val binding get() = _binding!!

    private lateinit var labelViewModel: LabelViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageLabelBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity

        initializeViewElements(binding)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeViewElements(binding: FragmentManageLabelBinding) {
        binding.manageLabelBtnBack.setOnClickListener {
            mainActivity.hideManageLabelFragment()
        }

        labelViewModel = (activity as MainActivity).labelViewModel
        labelViewModel.getAllLabels().observe(viewLifecycleOwner) {
            labels ->

        }
    }
}