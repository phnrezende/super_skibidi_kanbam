package com.pedro.task.util

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pedro.task.databinding.BottomSheetBinding

fun Fragment.showBottomSheet(
    titleButton: Int,
    titleDialog: Int,
    message: String,
    onClick: () -> Unit
) {
    val dialog = BottomSheetDialog(requireContext())
    val binding = BottomSheetBinding.inflate(LayoutInflater.from(requireContext()))

    binding.textViewTitle.setText(titleDialog)
    binding.textViewMessage.text = message
    binding.buttonOk.setText(titleButton)

    binding.buttonOk.setOnClickListener {
        onClick()
        dialog.dismiss()
    }

    dialog.setContentView(binding.root)
    dialog.show()
}