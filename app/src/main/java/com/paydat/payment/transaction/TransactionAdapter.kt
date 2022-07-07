package com.paydat.payment.transaction

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.paydat.R
import com.paydat.base.BaseRecyclerAdapter
import com.paydat.data.entities.model.payment.transction.TransactionResponse
import com.paydat.data.remote.payment.card.ProductStatus
import com.paydat.data.repositories.payment.transaction.TransactionStatus
import com.paydat.data.repositories.user.UserType
import com.paydat.databinding.ItemSalesBinding
import com.paydat.util.DateTimeUtils
import com.paydat.util.setTextViewColor
import com.paydat.util.setUnderLine
import java.text.DecimalFormat

class TransactionAdapter(
    private val context: Context,
    private val accountType: String,
    private val callback: Callback
) :
    BaseRecyclerAdapter<TransactionAdapter.ViewHolder>() {
    private lateinit var binding: ItemSalesBinding
    private var list = ArrayList<TransactionResponse>()
    private val formatter = DecimalFormat("0.00")

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionAdapter.ViewHolder {
        binding = ItemSalesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: TransactionAdapter.ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: ArrayList<TransactionResponse>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            setIsRecyclable(false)
        }

        fun bind(data: TransactionResponse, position: Int) {
//            itemView.setOnClickListener {
//                if (accountType == UserType.USER_TYPE_INDIVIDUAL.type) {
//                    if (data.receiptUrl.isNotEmpty()) {
//                        val i = Intent(Intent.ACTION_VIEW)
//                        i.data = Uri.parse(data.receiptUrl)
//                        context.startActivity(i)
//                    }
//                }
//            }

            binding.apply {
                data.let {

                    if (accountType == UserType.USER_TYPE_MERCHANT.type) {
                        binding.tvStatus.setOnClickListener {
                            callback.updateStatus(data, position)
                        }
                    }

                    if (it.paymentDate.isNotBlank()) {
                        tvDate.text =
                            context.getString(R.string.date) + " " + DateTimeUtils.convertDateTime(
                                it.paymentDate
                            )
                    }
                    tvId.text = context.getString(R.string.transaction_id) + " " + it.transactionId
                    tvItem.text = context.getString(R.string.item) + " " + it.description
                    tvPrice.text =
                        context.getString(R.string.price) + " $" + if (accountType == UserType.USER_TYPE_MERCHANT.type) {
                            formatter.format(
                                it.amount
                            )
                        } else {
                            formatter.format(it.total_amount)
                        } + " (" + if (data.taxInclude) context.getString(
                            R.string.tax_includes
                        ) else context.getString(
                            R.string.tax_excludes
                        ) + ")"

                    tvStatus.apply {
                        if (!TextUtils.isEmpty(it.isCollect)) {
                            when (it.isCollect) {
                                ProductStatus.PRODUCT_STATUS_COLLECTION.type -> {
                                    setTextViewColor(
                                        context,
                                        TransactionStatus.TRANSACTION_PICK_UP.color
                                    )
                                    tvAddress.text =
                                        context.getString(R.string.pickup_address) + " " + it.pickupAddress
                                }

                                ProductStatus.PRODUCT_STATUS_COLLECTED.type -> {
                                    setTextViewColor(
                                        context,
                                        TransactionStatus.TRANSACTION_PICK_UP.color
                                    )
                                    tvAddress.text =
                                        context.getString(R.string.pickup_address) + " " + it.pickupAddress
                                }

                                ProductStatus.PRODUCT_STATUS_TO_SHIP.type -> {
                                    setTextViewColor(
                                        context,
                                        TransactionStatus.TRANSACTION_SHIP_TO.color
                                    )
                                    tvAddress.text =
                                        context.getString(R.string.shiping_address) + " " + it.shippingAddress
                                }

                                ProductStatus.PRODUCT_STATUS_SHIPPED.type -> {
                                    setTextViewColor(
                                        context,
                                        TransactionStatus.TRANSACTION_SHIP_TO.color
                                    )
                                    tvAddress.text =
                                        context.getString(R.string.shiping_address) + " " + it.shippingAddress
                                }
                            }
                            text = it.isCollect
                        }
                    }

                    tvPaymentStatus.apply {
                        text = it.status
                        setUnderLine()
                        when (it.status) {
                            TransactionStatus.TRANSACTION_SUCCEED.type -> {
                                text = TransactionStatus.TRANSACTION_SUCCEED.text
                                setTextViewColor(
                                    context,
                                    TransactionStatus.TRANSACTION_SUCCEED.color
                                )
                            }

                            TransactionStatus.TRANSACTION_CANCELED.type -> {
                                text = TransactionStatus.TRANSACTION_CANCELED.text
                                setTextViewColor(
                                    context,
                                    TransactionStatus.TRANSACTION_CANCELED.color
                                )
                            }

                            TransactionStatus.TRANSACTION_PROCESSING.type -> {
                                text = TransactionStatus.TRANSACTION_PROCESSING.text
                                setTextViewColor(
                                    context,
                                    TransactionStatus.TRANSACTION_PROCESSING.color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

interface Callback {
    fun updateStatus(data: TransactionResponse, position: Int)
}