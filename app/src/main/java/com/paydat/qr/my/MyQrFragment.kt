package com.paydat.qr.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paydat.base.BaseFragment
import com.paydat.databinding.FragmentMyQrBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyQrFragment : BaseFragment<FragmentMyQrBinding>() {

    override val viewBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyQrBinding
        get() = FragmentMyQrBinding::inflate

    override fun onViewBindingCreated(
        view: View,
        binding: FragmentMyQrBinding,
        savedInstanceState: Bundle?
    ) {
        binding.apply {
            preferenceManager.userId.let {
                tvId.text = "ID: $it"
                imageViewQr.setImageBitmap(getQrCodeBitmap(it))
            }
        }
    }
}