package com.paydat.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.paydat.R
import com.paydat.base.BaseRecyclerAdapter
import com.paydat.data.entities.model.main.NavigationDrawerModel
import com.paydat.data.entities.model.main.NavigationType
import com.paydat.databinding.ItemMenuActionBinding
import com.paydat.databinding.ItemMenuSideBarProfileBinding
import com.paydat.util.makeBackgroundSelector
import com.paydat.util.makeTextSelector

class NavigationDrawerAdapter(
    private val list: List<NavigationDrawerModel>,
    private val context: Context,
    private val callbacks: Callback
) : BaseRecyclerAdapter<RecyclerView.ViewHolder>() {
    private lateinit var itemBinding: ItemMenuActionBinding
    private lateinit var headerBinding: ItemMenuSideBarProfileBinding

    companion object {
        const val TYPE_ITEMS = 1
        const val TYPE_HEADER = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {

            TYPE_HEADER -> {
                headerBinding = ItemMenuSideBarProfileBinding.inflate(inflater, parent, false)
                HeaderHolder(headerBinding.root)
            }
            else -> {
                itemBinding = ItemMenuActionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                Holder(itemBinding, context, callbacks)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (list[position].type) {
            NavigationType.TYPE_HEADER -> {}
            NavigationType.TYPE_ITEMS -> {
                (holder as Holder).bind(list[position])
            }
        }
    }

    override fun getItemCount(): Int = list.size

    inner class Holder(
        private val itemBinding: ItemMenuActionBinding,
        private val context: Context,
        private var callbacks: Callback
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(listBean: NavigationDrawerModel) {
            itemBinding.apply {
                ContextCompat.getColor(context, R.color.base).apply {
                    parent.background = makeBackgroundSelector(this)
                    parent.background = makeTextSelector(this)
                }
                itemName.text = context.resources.getString(listBean.stringResourceId)
                itemIcon.setImageResource(listBean.imageResourceId)
                parent.setOnClickListener {
                    callbacks.onDrawerItemClick(
                        absoluteAdapterPosition,
                        listBean
                    )
                }
            }

        }
    }

    inner class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun getItemViewType(position: Int): Int {
        val type = when (list[position].type) {
            NavigationType.TYPE_HEADER -> TYPE_HEADER
            NavigationType.TYPE_ITEMS -> TYPE_ITEMS
        }
        return type
    }

    interface Callback {
        fun onDrawerItemClick(position: Int, item: NavigationDrawerModel)
    }
}