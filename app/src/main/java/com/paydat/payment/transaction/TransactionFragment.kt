package com.paydat.payment.transaction

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.paydat.R
import com.paydat.base.BaseFragment
import com.paydat.data.entities.model.payment.transction.TransactionResponse
import com.paydat.data.remote.payment.card.ProductStatus
import com.paydat.databinding.FragmentSalesBinding
import com.paydat.domain.BaseUseCase
import com.paydat.main.LogoutCallback
import com.paydat.util.AlertDialogs
import com.paydat.util.AlertDialogsListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class TransactionFragment : BaseFragment<FragmentSalesBinding>(), Callback {
    private lateinit var adapter: TransactionAdapter
    private val viewModel: TransactionViewModel by viewModels()
    private var callback: LogoutCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = context as LogoutCallback
        } catch (e: Exception) {
            Log.d(TAG, "onAttach: ${e.message}")
        }
    }

    override fun onDetach() {
        super.onDetach()
        try {
            callback = null
        } catch (e: Exception) {
            Log.d(TAG, "onAttach: ${e.message}")
        }
    }

    override val viewBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSalesBinding
        get() = FragmentSalesBinding::inflate

    override fun onViewBindingCreated(
        view: View,
        binding: FragmentSalesBinding,
        savedInstanceState: Bundle?
    ) {
        setAdapter()

        viewModel.transaction()
        viewModel.stateFlow.onEach { state ->
            handle(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handle(state: TransactionViewState) {
        when {
            state.isLoading -> {
                progressDialog.showLoadingDialog(requireActivity(), null)
            }
            state.isSuccess -> {
                progressDialog.dismissLoadingDialog()
                when (state.responseType) {
                    BaseUseCase.ResponseType.TRANSACTION -> {
                        val list = state.response?.getListData() as ArrayList<TransactionResponse>
//                        list.sortByDescending { it.paymentDate }
                        adapter.setList(list)
                    }

                    BaseUseCase.ResponseType.UPDATE_STATUS -> {
                        viewModel.transaction()
                    }
                    else -> {}
                }
            }

            state.uiError != null -> {
                progressDialog.dismissLoadingDialog()
                showErrorMessage(state.uiError)
            }
        }
    }

    private fun showErrorMessage(error: TransactionError?) {
        val message = when (error) {
            TokenExpire -> getString(R.string.title_session_expire)
            NetworkError -> getString(R.string.network_connection)
            else -> getString(R.string.unknown_error)
        }

        val dialog = AlertDialogs(
            requireContext(),
            getString(R.string.dialog_info),
            message,
            isPositiveButton = true,
            isCancelButton = false,
            positive_button_text = getString(R.string.btn_ok),
            listener = object : AlertDialogsListener {

                override fun onClickClose() {
                }

                override fun onClickPositive() {
                    if (error == TokenExpire) {
                        callback?.logoutUser(true)
                    }
                }
            }
        )
        dialog.show()
    }

    private fun setAdapter() {
        adapter = TransactionAdapter(requireContext(), preferenceManager.accountType, this)
        binding.rvSale.adapter = adapter
        binding.rvSale.setHasFixedSize(true)
    }

    override fun updateStatus(data: TransactionResponse, position: Int) {
        when (data.isCollect) {
            ProductStatus.PRODUCT_STATUS_COLLECTION.type -> {
                viewModel.updateStatus(data.id, ProductStatus.PRODUCT_STATUS_COLLECTED.type)
            }

            ProductStatus.PRODUCT_STATUS_TO_SHIP.type -> {
                viewModel.updateStatus(data.id, ProductStatus.PRODUCT_STATUS_SHIPPED.type)
            }
        }
    }
}