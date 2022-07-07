package com.paydat.payment.card

import androidx.lifecycle.viewModelScope
import com.paydat.data.entities.model.payment.card.DeleteCardRequest
import com.paydat.data.entities.model.payment.pay.CreateChargeRequest
import com.paydat.domain.BaseUseCase
import com.paydat.domain.payment.card.CardUseCase
import com.paydat.util.ReduxViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val cardUseCase: CardUseCase
) : ReduxViewModel<CardViewState>(CardViewState()) {
    private val uiActions = MutableSharedFlow<CardAction>()

    init {
        handleAddCardEvent()
        handlePaymentEvent()
        handleCardListEvent()
        handleDeleteCardEvent()
    }

    private fun handlePaymentEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<Payment>()
                .flatMapLatest {
                    cardUseCase.payment(it.data)
                }
                .collectAndSetState { result ->

                    when (result) {
                        CardUseCase.Outcome.Loading -> {
                            copy(
                                isLoading = true,
                                uiError = null,
                                isSuccess = false,
                                responseType = BaseUseCase.ResponseType.PAYMENT
                            )
                        }
                        else -> {
                            if (result is CardUseCase.Outcome.Response) {
                                copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    responseType = BaseUseCase.ResponseType.PAYMENT,
                                    responseCharge = result.asChargeResponse(),
                                    uiError = null
                                )
                            } else {
                                copy(
                                    isLoading = false,
                                    uiError = result.asError(),
                                    isSuccess = false,
                                    responseType = BaseUseCase.ResponseType.PAYMENT,
                                )
                            }

                        }
                    }
                }
        }
    }

    private fun handleAddCardEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<AddCard>()
                .flatMapLatest {
                    cardUseCase.addCard(it.stripeToken)
                }
                .collectAndSetState { result ->

                    when (result) {
                        CardUseCase.Outcome.Loading -> {
                            copy(
                                isLoading = true,
                                uiError = null,
                                isSuccess = false,
                                responseType = BaseUseCase.ResponseType.ADD_CARD
                            )
                        }
                        else -> {
                            if (result is CardUseCase.Outcome.Response) {
                                copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    responseType = BaseUseCase.ResponseType.ADD_CARD,
                                    response = result.asResponse(),
                                    uiError = null
                                )
                            } else {
                                copy(
                                    isLoading = false,
                                    uiError = result.asError(),
                                    isSuccess = false,
                                    responseType = BaseUseCase.ResponseType.ADD_CARD,
                                )
                            }

                        }
                    }
                }
        }
    }

    private fun handleCardListEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<GetCard>()
                .flatMapLatest {
                    cardUseCase.getCard()
                }
                .collectAndSetState { result ->

                    when (result) {
                        CardUseCase.Outcome.Loading -> {
                            copy(
                                isLoading = true,
                                uiError = null,
                                isSuccess = false,
                                responseType = BaseUseCase.ResponseType.GET_CARD
                            )
                        }
                        else -> {
                            if (result is CardUseCase.Outcome.Response) {
                                copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    responseType = BaseUseCase.ResponseType.GET_CARD,
                                    response = result.asResponse(),
                                    uiError = null
                                )
                            } else {
                                copy(
                                    isLoading = false,
                                    uiError = result.asError(),
                                    isSuccess = false,
                                    responseType = BaseUseCase.ResponseType.GET_CARD,
                                )
                            }

                        }
                    }
                }
        }
    }

    private fun handleDeleteCardEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<DeleteCard>()
                .flatMapLatest {
                    cardUseCase.deleteCard(it.data)
                }
                .collectAndSetState { result ->
                    when (result) {
                        CardUseCase.Outcome.Loading -> {
                            copy(
                                isLoading = true,
                                uiError = null,
                                isSuccess = false,
                                responseType = BaseUseCase.ResponseType.DELETE_CARD
                            )
                        }
                        else -> {
                            if (result is CardUseCase.Outcome.Response) {
                                copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    responseType = BaseUseCase.ResponseType.DELETE_CARD,
                                    responseDelete = result.asDeleteResponse(),
                                    uiError = null
                                )
                            } else {
                                copy(
                                    isLoading = false,
                                    uiError = result.asError(),
                                    isSuccess = false,
                                    responseType = BaseUseCase.ResponseType.DELETE_CARD,
                                )
                            }

                        }
                    }
                }
        }
    }

    fun addCard(stripeToken: String) {
        viewModelScope.launch {
            uiActions.emit(AddCard(stripeToken))
        }
    }

    fun payment(data: CreateChargeRequest) {
        viewModelScope.launch {
            uiActions.emit(Payment(data))
        }
    }

    fun getCard() {
        viewModelScope.launch {
            uiActions.emit(GetCard(""))
        }
    }

    fun deleteCard(data: DeleteCardRequest) {
        viewModelScope.launch {
            uiActions.emit(DeleteCard(data))
        }
    }

}