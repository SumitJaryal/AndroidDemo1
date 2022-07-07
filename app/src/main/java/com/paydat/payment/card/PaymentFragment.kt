package com.paydat.payment.card

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.paydat.R
import com.paydat.base.BaseFragment
import com.paydat.data.entities.model.payment.card.CardDetail
import com.paydat.data.entities.model.payment.card.CardResponse
import com.paydat.data.entities.model.payment.card.DeleteCardRequest
import com.paydat.data.entities.model.payment.pay.CreateChargeRequest
import com.paydat.data.entities.model.payment.pay.CreateChargeResponse
import com.paydat.data.entities.model.qr.SellItemQr
import com.paydat.data.remote.payment.card.ProductStatus
import com.paydat.databinding.FragmentPaymentBinding
import com.paydat.domain.BaseUseCase
import com.paydat.main.LogoutCallback
import com.paydat.payment.pay.PaymentDoneDialog
import com.paydat.payment.pay.PaymentListener
import com.paydat.util.*
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.Token
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt
import kotlin.random.Random

@AndroidEntryPoint
class PaymentFragment : BaseFragment<FragmentPaymentBinding>(), View.OnClickListener,
    CardAdapter.Callback {
    private val viewModel: CardViewModel by viewModels()
    private var callback: LogoutCallback? = null
    private lateinit var data: SellItemQr
    private var type: String? = null
    private var isCard: Boolean = true
    private var cardId: String = ""
//    private lateinit var charge: PaymentSetting

    private lateinit var adapter: CardAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = context as LogoutCallback
        } catch (e: Throwable) {
            Log.d(TAG, "onAttach: ${e.message}")
        }
    }

    override fun onDetach() {
        super.onDetach()
        try {
            callback = null
        } catch (e: Throwable) {
            Log.d(TAG, "onAttach: ${e.message}")
        }
    }

    override val viewBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPaymentBinding
        get() = FragmentPaymentBinding::inflate

    override fun onViewBindingCreated(
        view: View,
        binding: FragmentPaymentBinding,
        savedInstanceState: Bundle?
    ) {
        data = Gson().fromJson(arguments?.getString(Constants.SCAN_MSG), SellItemQr::class.java)
        type = arguments?.getString(Constants.ORDER_DELIVER_TYPE)

        setAdapter()
        initialized()
        listeners()

        viewModel.getCard()
        viewModel.stateFlow.onEach { state ->
            handle(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handle(state: CardViewState) {
        when {
            state.isLoading -> {
                progressDialog.showLoadingDialog(requireActivity(), null)
            }
            state.isSuccess -> {
                progressDialog.dismissLoadingDialog()
                when (state.responseType) {
                    BaseUseCase.ResponseType.ADD_CARD, BaseUseCase.ResponseType.GET_CARD -> {
                        state.response?.getData()?.let {
                            val data = it as CardResponse
                            val list = data.cardList
//                            charge = data.settings
                            try {
                                adapter.setList(list as ArrayList<CardDetail>)
                            } catch (e: Throwable) {
                            }
                        }
                    }
                    BaseUseCase.ResponseType.PAYMENT -> {
                        state.responseCharge?.getData().let {
                            val data = it as CreateChargeResponse
                            val dialog = PaymentDoneDialog(
                                context = requireContext(),
                                title = data.description,
                                amount = data.amount,
                                commission = data.commission,
                                gst = this.data.gst.toDouble(),
                                listener = object : PaymentListener {
                                    override fun clickPositive() {
                                        navigateSingleTop(Paths.URI_SALES)
                                    }
                                }
                            )
                            dialog.show()
                        }
                    }
                    BaseUseCase.ResponseType.DELETE_CARD -> {
                        val id = state.responseDelete?.getData()?.id
                        adapter.removeData(id)
                    }
                    else -> {}
                }
            }

            state.uiError != null -> {
                progressDialog.dismissLoadingDialog()
                showErrorMessage(state.uiError, "")
            }
        }
    }

    private fun setAdapter() {
        adapter = CardAdapter(requireContext(), this)
        binding.rvCard.adapter = adapter
    }

    private fun initialized() {
    }

    private fun listeners() {
        binding.buttonPay.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.buttonPay -> {
                if (isCard) {
                    addPayment()
                } else {
                    if (cardId.isNotEmpty()) {
                        progressDialog.showLoadingDialog(requireActivity(), null)
                        viewModel.payment(
                            CreateChargeRequest(
                                cardId = cardId,
                                isCollect = if (data.available == 0) ProductStatus.PRODUCT_STATUS_COLLECTION.type else ProductStatus.PRODUCT_STATUS_TO_SHIP.type,
                                shippingAddress = data.shippingAddress,
                                productId = data._id
                            )
                        )
                    } else {
                        showErrorMessage(InvalidCard, "")
                    }
                }
            }
        }
    }

    private fun addPayment() {
        val card = binding.cardView.cardParams
        if (card != null) {
            progressDialog.showLoadingDialog(requireActivity(), null)
            val stripe = Stripe(requireContext(), Config.PUBLISHABLE_KEY)
            stripe.createCardToken(
                card,
                callback = object : ApiResultCallback<Token> {
                    override fun onError(e: Exception) {
                        showErrorMessage(InvalidStripeToken, e.message.toString())
                        progressDialog.dismissLoadingDialog()
                    }

                    override fun onSuccess(result: Token) {
                        viewModel.payment(
                            CreateChargeRequest(
                                sourceToken = result.id,
                                isCollect = if (data.available == 0) ProductStatus.PRODUCT_STATUS_COLLECTION.type else ProductStatus.PRODUCT_STATUS_TO_SHIP.type,
                                shippingAddress = data.shippingAddress,
                                productId = data._id
                            )
                        )
                        progressDialog.dismissLoadingDialog()
                    }
                }
            )
        } else {
            showErrorMessage(InvalidCard, "")
        }
    }

    private fun showErrorMessage(error: CardError, text: String) {
        val message = when (error) {
            TokenExpire -> getString(R.string.title_session_expire)
            InvalidCard -> getString(R.string.empty_card)
            InvalidStripeToken -> text
            InvalidData -> getString(R.string.merchant_error)
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

    override fun onCardClick(data: CardDetail, position: Int) {
        if (data.isChecked) {
            isCard = false
            cardId = data.id
        } else {
            isCard = true
            cardId = ""
        }
    }

    override fun onCardDelete(data: CardDetail, pos: Int) {
        val dialog = AlertDialogs(
            requireContext(),
            getString(R.string.dialog_info),
            getString(R.string.are_you_sure_delete_card),
            isPositiveButton = true,
            isCancelButton = true,
            positive_button_text = getString(R.string.btn_ok),
            cancel_button_text = getString(R.string.cancel),
            listener = object : AlertDialogsListener {

                override fun onClickClose() {
                }

                override fun onClickPositive() {
                    viewModel.deleteCard(DeleteCardRequest(data.id))
                }
            }
        )
        dialog.show()
    }

    private fun randomMerchantID(): String = List(17) {
        (('a'..'z') + ('_') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")

    private fun randomAmount(): Double {
        return (Random.nextDouble(100.0, 3000.0) * 100.0).roundToInt() / 100.0 + 5.0
    }

    private fun finalAmount(amt: String): Double {
        return ((amt.toDouble() * 100.0).roundToInt() / 100.0)
    }

//    private fun finalAmountAfterCommission(): Double {
//        charge.apply {
//            val amt = ((data.amount.toDouble() * 100.0).roundToInt() / 100.0)
//            return if (type == ChargeType.CHARGE_TYPE_FIXED.type) {
//                amount
//            } else {
//                (amount * amt) / 100
//            }
//        }
//    }
}