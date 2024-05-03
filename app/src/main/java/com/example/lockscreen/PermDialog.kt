package com.example.lockscreen

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.lockscreen.databinding.RequestPermDlgBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PermDialog : DialogFragment(R.layout.request_perm_dlg)
{
    private lateinit var mainBinding : RequestPermDlgBinding
    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog
    {
        super.onCreateDialog(savedInstanceState)

        mainBinding = RequestPermDlgBinding.inflate(layoutInflater)
        val dlg = MaterialAlertDialogBuilder(requireContext())
        dlg.setView(mainBinding.root)
        return dlg.create()
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        mainBinding.settingsBtn.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            dismiss()
        }
        mainBinding.cancelPermBtn.setOnClickListener { dismiss() }

        super.onViewCreated(view, savedInstanceState)
    }
}