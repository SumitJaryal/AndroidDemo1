package com.paydat.user.profile.business

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.paydat.R
import com.paydat.base.BaseFragment
import com.paydat.data.entities.model.user.BusinessRequest
import com.paydat.databinding.FragmentBusinessBinding
import com.paydat.domain.BaseUseCase
import com.paydat.user.profile.update.ProfileViewModel
import com.paydat.util.AlertDialogs
import com.paydat.util.AlertDialogsListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class BusinessFragment : BaseFragment<FragmentBusinessBinding>() {
    private val viewModel: BusinessViewModel by viewModels()

    override val viewBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBusinessBinding
        get() = FragmentBusinessBinding::inflate

    override fun onViewBindingCreated(
        view: View,
        binding: FragmentBusinessBinding,
        savedInstanceState: Bundle?
    ) {

        setData()
        viewModel.stateFlow.onEach { state ->
            handle(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        listener()
    }

    private fun setData() {
        binding.apply {
            inputLayoutTaxId.editText?.setText(preferenceManager.taxId)
            inputLayoutBankName.editText?.setText(preferenceManager.bankName)
            inputLayoutAccountNo.editText?.setText(preferenceManager.accountNo)
            inputLayoutBusinessNature.editText?.setText(preferenceManager.businessNature)

            if (preferenceManager.taxId.isNotEmpty()) {
                inputLayoutTaxId.editText?.isEnabled = false
                inputLayoutBankName.editText?.isEnabled = false
                inputLayoutAccountNo.editText?.isEnabled = false
                inputLayoutBusinessNature.editText?.isEnabled = false
                buttonUpdate.visibility = View.GONE
            } else {
                inputLayoutTaxId.editText?.isEnabled = true
                inputLayoutBankName.editText?.isEnabled = true
                inputLayoutAccountNo.editText?.isEnabled = true
                inputLayoutBusinessNature.editText?.isEnabled = true
                buttonUpdate.visibility = View.VISIBLE
            }
        }
    }

    private fun handle(state: BusinessStateView) {
        when {
            state.isLoading -> {
                progressDialog.showLoadingDialog(requireActivity(), null)
            }
            state.isSuccess -> {
                progressDialog.dismissLoadingDialog()
                when (state.responseType) {
                    BaseUseCase.ResponseType.ADD_BUSINESS -> {
                        val dialog = AlertDialogs(
                            requireContext(),
                            getString(R.string.dialog_info),
                            getString(R.string.success_business_details),
                            isPositiveButton = true,
                            isCancelButton = false,
                            positive_button_text = getString(R.string.btn_ok),
                            listener = object : AlertDialogsListener {

                                override fun onClickClose() {

                                }

                                override fun onClickPositive() {
                                    requireActivity().onBackPressed()
                                }
                            }
                        )
                        dialog.show()
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

    private fun listener() {
        binding.apply {
            buttonUpdate.setOnClickListener {
                val taxId = inputLayoutTaxId.editText?.text.toString().trim()
                val bankName = inputLayoutBankName.editText?.text.toString().trim()
                val accountNo = inputLayoutAccountNo.editText?.text.toString().trim()
                val businessNature = inputLayoutBusinessNature.editText?.text.toString().trim()

                when {
                    taxId.isEmpty() -> {
                        showErrorMessage(InvalidTaxId)
                    }

                    bankName.isEmpty() -> {
                        showErrorMessage(InvalidBankName)
                    }

                    accountNo.isEmpty() -> {
                        showErrorMessage(InvalidAccountNo)
                    }

                    businessNature.isEmpty() -> {
                        showErrorMessage(InvalidBusinessNature)
                    }

                    else -> {
                        viewModel.businessDetail(
                            BusinessRequest(
                                taxId,
                                accountNo,
                                bankName,
                                businessNature
                            )
                        )
                    }
                }
            }
        }
    }

    private fun showErrorMessage(error: BusinessError) {
        val message = when (error) {
            NetworkError -> getString(R.string.network_connection)
            InvalidTaxId -> getString(R.string.error_taxId)
            InvalidBankName -> getString(R.string.error_bank_name)
            InvalidAccountNo -> getString(R.string.error_account_no)
            InvalidBusinessNature -> getString(R.string.error_business_nature)
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

                }
            }
        )
        dialog.show()
    }

}