package com.paydat.qr.generate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.paydat.R
import com.paydat.base.BaseFragment
import com.paydat.data.entities.model.qr.SellItemQr
import com.paydat.data.repositories.user.AvailabilityType
import com.paydat.databinding.FragmentGenerateQrBinding
import com.paydat.util.APPlINK
import com.paydat.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

@AndroidEntryPoint
class GenerateQrFragment : BaseFragment<FragmentGenerateQrBinding>() {

    private val formatter = DecimalFormat("0.00")

    override val viewBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGenerateQrBinding
        get() = FragmentGenerateQrBinding::inflate

    override fun onViewBindingCreated(
        view: View,
        binding: FragmentGenerateQrBinding,
        savedInstanceState: Bundle?
    ) {

        val details = Gson().fromJson(
            arguments?.getString(Constants.QR_GENERATE_DATA),
            SellItemQr::class.java
        )

        binding.apply {
            tvMerchantId.text = getString(R.string.merchant_id) + " " + details.receiverId
            tvDesc.text = getString(R.string.description) + " " + details.description
            val amt = details.amount.toDouble()
            tvAmt.text = getString(R.string.amount) + formatter.format(amt)
            val gst = details.gst.toDouble()
            tvGst.text = getString(R.string.gst) + formatter.format(gst)
            textView.text = "Merchant Name: ${preferenceManager.name}"
            tvPickupAddress.text = "${getString(R.string.pickup_address)} ${details.pickupAddress}"
            tvPickup.text =
                getString(R.string.pickup) + " " + if (details.available == AvailabilityType.AVAILABILITY_PICKUP.type || details.available == AvailabilityType.AVAILABILITY_PICKUP_SHIP.type) getString(
                    R.string.available
                ) else getString(
                    R.string.unavailable
                )
            tvShipping.text =
                getString(R.string.shipping) + " " + if (details.available == AvailabilityType.AVAILABILITY_SHIP.type || details.available == AvailabilityType.AVAILABILITY_PICKUP_SHIP.type) getString(
                    R.string.available
                ) else getString(
                    R.string.unavailable
                )
            if (details.available == AvailabilityType.AVAILABILITY_PICKUP.type || details.available == AvailabilityType.AVAILABILITY_PICKUP_SHIP.type) {
                tvPickupAddress.visibility = View.VISIBLE
            } else {
                tvPickupAddress.visibility = View.GONE
            }
//            imageViewQr.setImageBitmap(details._id?.let { getQrCodeBitmap(APPlINK.LIVE.url + it) })
            imageViewQr.setImageBitmap(details._id?.let { getQrCodeBitmap(it) })
        }
    }
}