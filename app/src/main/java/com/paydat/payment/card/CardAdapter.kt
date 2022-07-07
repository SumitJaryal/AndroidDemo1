package com.paydat.payment.card

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.paydat.base.BaseRecyclerAdapter
import com.paydat.data.entities.model.payment.card.CardDetail
import com.paydat.databinding.ItemCardBinding

class CardAdapter(
    private val context: Context,
    private val callback: Callback
) :
    BaseRecyclerAdapter<CardAdapter.ViewHolder>() {
    private var row = -1
    private lateinit var binding: ItemCardBinding
    private var list = ArrayList<CardDetail>()

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            setIsRecyclable(false)
        }

        fun bind(position: Int, data: CardDetail) {
            itemView.setOnClickListener {
                row = position
                if (data.isChecked) {
                    data.isChecked = false
                } else {
                    data.isChecked = true
                    for (i in list.indices) {
                        if (i != row) {
                            list[i].isChecked = false
                        }
                    }
                }
                callback.onCardClick(data, position)
                notifyDataSetChanged()
            }

            binding.ivDelete.setOnClickListener {
                callback.onCardDelete(data, position)
            }
            binding.apply {
                if (position == row && data.isChecked) {
                    binding.ivCheck.visibility = View.VISIBLE
                } else {
                    binding.ivCheck.visibility = View.INVISIBLE
                }

                tvCardType.text = data.brand
                tvCardNo.text = "xxxx ${data.last4}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: ArrayList<CardDetail>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun removeData(id: String?) {
        for (i in list.indices) {
            if (list[i].id == id) {
                list.removeAt(i)
                break
            }
        }
        notifyDataSetChanged()
    }

    interface Callback {
        fun onCardClick(data: CardDetail, position: Int)
        fun onCardDelete(data: CardDetail, pos: Int)
    }
}