package com.paydat.payment.pay

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import com.paydat.databinding.FragmentPaymentDoneDialogBinding
import java.text.DecimalFormat

interface PaymentListener {
    fun clickPositive()
}

class PaymentDoneDialog(
    context: Context,
    private val title: String,
    private val amount: Double,
    private val commission: Double,
    private val gst: Double,
    private val listener: PaymentListener
) :
    android.app.Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = FragmentPaymentDoneDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        binding.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            setCancelable(false)
            val formatter = DecimalFormat("0.00")
            dialogTitle.text = title
            tvAmt.text = "$ ${formatter.format(amount)} Price"
            tvGst.text = "$ ${formatter.format(gst)} GST"
            tvCommission.text = "$ ${formatter.format(commission)} App Fee"
            tvNet.text = "$ ${formatter.format(amount + commission + gst)} Total"
            btnOk.setOnClickListener {
                dismiss()
                listener.clickPositive()
            }
        }
    }
}