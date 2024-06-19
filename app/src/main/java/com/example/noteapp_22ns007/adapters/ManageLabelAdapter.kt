package com.example.noteapp_22ns007.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp_22ns007.R
import com.example.noteapp_22ns007.Utils
import com.example.noteapp_22ns007.databinding.ManageLabelItemBinding
import com.example.noteapp_22ns007.fragments.ManageLabelFragment
import com.example.noteapp_22ns007.model.database.entities.Label
import com.example.noteapp_22ns007.model.viewmodels.LabelViewModel

class ManageLabelAdapter(
    private var labels: MutableList<Label>,
    private val labelViewModel: LabelViewModel,
    private val f: ManageLabelFragment
) : RecyclerView.Adapter<ManageLabelAdapter.ManageLabelViewHolder>() {
    private val viewHolderList = mutableListOf<ManageLabelViewHolder>()
    private var updateLabelSuccessLiveData: LiveData<Boolean>? = null

    inner class ManageLabelViewHolder(val binding: ManageLabelItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(label: Label) {
            binding.editTextManageLabel.setText(label.name)

            binding.editTextManageLabel.setOnFocusChangeListener { it, hasFocus ->
                if (hasFocus) {
                    it.setBackgroundResource(R.drawable.z_note_item_background)
                    binding.iconManageLabel.setImageResource(R.drawable.z_ic_delete_note)
                    binding.iconEditManageLabel.setImageResource(R.drawable.z_ic_check)
                } else {
                    it.setBackgroundResource(0)
                    binding.iconManageLabel.setImageResource(R.drawable.z_ic_label)
                    binding.iconEditManageLabel.setImageResource(R.drawable.z_ic_edit)
                }
            }

            binding.iconEditManageLabel.setOnClickListener {
                if (binding.editTextManageLabel.isFocused) {
                    val oldLabelName = labels[adapterPosition].name
                    val currentLabelName = binding.editTextManageLabel.text.toString()

                    if(currentLabelName == oldLabelName) {
                        binding.editTextManageLabel.clearFocus()
                        return@setOnClickListener
                    }

                    val newLabel = Label(label.labelId, currentLabelName)

                    labelViewModel.update(newLabel)

                    updateLabelSuccessLiveData = labelViewModel.updateResult
                    updateLabelSuccessLiveData?.observe(f.viewLifecycleOwner) {
                        success -> if (!success) {
                            Utils.notification(f.view, "Nhãn \"$currentLabelName\" đã tồn tại") {}
                            binding.editTextManageLabel.setText(oldLabelName)
                        } else {
                            Utils.notification(f.view, "Cập nhật thành công") {}
                        }
                        updateLabelSuccessLiveData!!.removeObservers(f.viewLifecycleOwner)
                        return@observe
                    }
                } else {
                    binding.editTextManageLabel.requestFocus()
                }
            }

            binding.iconManageLabel.setOnClickListener {
                if (binding.editTextManageLabel.isFocused) {
                    val labelToDelete = labels[adapterPosition]

                    showDeleteConfirmationDialog(
                        onConfirm = {
                            labelViewModel.deleteLabelById(labelToDelete.labelId!!)
                            Utils.notification(f.view, "Đã xóa nhãn \"${labelToDelete.name}\"") {}
                        },
                        onCancel = {}
                    )
                }
            }
        }

        private fun showDeleteConfirmationDialog(
            onConfirm: () -> Unit,
            onCancel: () -> Unit
        ) {
            val context = binding.root.context
            AlertDialog.Builder(context).apply {
                setTitle("Xác nhận xóa nhãn")
                setMessage("\nNhãn này sẽ bị xóa khỏi toàn bộ ghi chú và danh sách nhãn.\n\nBạn có chắc chắn muốn xóa nhãn này không?")
                setPositiveButton("Xóa") { _, _ ->
                    onConfirm()
                }
                setNegativeButton("Hủy") { dialog, _ ->
                    onCancel()
                    dialog.dismiss()
                }
                create()
                show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageLabelViewHolder {
        val binding = ManageLabelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ManageLabelViewHolder(binding)
        viewHolderList.add(viewHolder)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ManageLabelViewHolder, position: Int) {
        holder.bind(labels[position])
    }

    override fun getItemCount(): Int = labels.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateLabels(newLabels: List<Label>) {
        labels = newLabels.toMutableList()
        notifyDataSetChanged()
    }
}