package com.archik.gpstracker.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Toast
import com.archik.gpstracker.R
import com.archik.gpstracker.databinding.SaveDialogBinding
import com.archik.gpstracker.db.TrackItem

object DialogManager {

  fun showLocEnableDialog(context: Context, listener: Listener) {
    val builder = AlertDialog.Builder(context)
    val dialog = builder.create()

    dialog.setTitle(R.string.location_disabled)
    dialog.setMessage(context.getString(R.string.location_dialog_message))
    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") {
      _, _ -> listener.onClick()
    }
    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") {
      _, _ -> dialog.dismiss()
    }
    dialog.show()
  }

  @SuppressLint("SetTextI18n")
  fun showSaveDialog(context: Context, item: TrackItem?, listener: Listener) {
    val builder = AlertDialog.Builder(context)
    val binding = SaveDialogBinding.inflate(LayoutInflater.from(context), null, false)

    builder.setView(binding.root)

    val dialog = builder.create()

    binding.apply {
      tvTime.text = "${item?.time} s"
      tvSpeed.text = "${item?.velocity} km/h"
      tvDistance.text = "${item?.distance} km"

      bSave.setOnClickListener {
        listener.onClick()
        dialog.dismiss()
      }

      bCancel.setOnClickListener {
        dialog.dismiss()
      }
    }

    // Убираем фон у диалог. окна
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    dialog.show()

  }

  interface Listener {
    fun onClick()
  }
}