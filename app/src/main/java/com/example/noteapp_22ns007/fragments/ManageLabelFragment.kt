package com.example.noteapp_22ns007.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp_22ns007.MainActivity
import com.example.noteapp_22ns007.Utils
import com.example.noteapp_22ns007.adapters.ManageLabelAdapter
import com.example.noteapp_22ns007.databinding.FragmentManageLabelBinding
import com.example.noteapp_22ns007.model.database.entities.Label
import com.example.noteapp_22ns007.model.viewmodels.LabelViewModel

class ManageLabelFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private var _binding: FragmentManageLabelBinding? = null
    private val binding get() = _binding!!

    private lateinit var labelViewModel: LabelViewModel
    private lateinit var manageLabelAdapter: ManageLabelAdapter
    private var labels: MutableList<Label> = mutableListOf()
    private var addLabelSuccessLiveData: LiveData<Boolean>? = null

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

        binding.addLabelBtn.setOnClickListener {
            val newLabelName = binding.addLabelEdt.text.toString()
            if(newLabelName.isBlank()) {
                Utils.notification(view, "Nhãn không được trống", "") {}
                return@setOnClickListener
            }

            labelViewModel.insert(Label(null, newLabelName))

            addLabelSuccessLiveData = labelViewModel.insertResult
            addLabelSuccessLiveData?.observe(viewLifecycleOwner) { success ->
                if (!success) {
                    Utils.notification(view, "Nhãn \"$newLabelName\" đã tồn tại", "") {}
                } else {
                    Utils.notification(view, "Đã thêm nhãn \"$newLabelName\"", "") {}
                    binding.addLabelEdt.text = null
                }
                addLabelSuccessLiveData!!.removeObservers(viewLifecycleOwner)
            }
        }

        labelViewModel = mainActivity.labelViewModel

        manageLabelAdapter = ManageLabelAdapter(labels, labelViewModel, this)
        binding.manageLabelRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = manageLabelAdapter
        }

        labelViewModel = (activity as MainActivity).labelViewModel
        labelViewModel.getAllLabels().observe(viewLifecycleOwner) {
            labels -> manageLabelAdapter.updateLabels(labels)
        }
    }

    fun clearFocus(): Boolean {
        var focusCleared = false

        for (i in 0 until binding.manageLabelRecyclerView.childCount) {
            val viewHolder = binding.manageLabelRecyclerView.findViewHolderForAdapterPosition(i) as? ManageLabelAdapter.ManageLabelViewHolder

            viewHolder?.let {
                if (it.binding.editTextManageLabel.isFocused) {
                    it.binding.editTextManageLabel.setText(manageLabelAdapter.labels[viewHolder.adapterPosition].name)
                    it.binding.editTextManageLabel.clearFocus()
                    focusCleared = true
                }
            }
        }

        return focusCleared
    }
}