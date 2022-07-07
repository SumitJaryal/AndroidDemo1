package com.paydat.payment.transaction

import androidx.lifecycle.viewModelScope
import com.paydat.domain.BaseUseCase
import com.paydat.domain.payment.transaction.TransactionUseCase
import com.paydat.util.ReduxViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionUseCase: TransactionUseCase
) : ReduxViewModel<TransactionViewState>(TransactionViewState()) {
    private val uiActions = MutableSharedFlow<TransactionAction>()

    init {
        handleTransactionListEvent()
        handleUpdateStatusEvent()
    }

    private fun handleUpdateStatusEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<UpdateStatus>()
                .flatMapLatest {
                    transactionUseCase.updateStatus(it.id, it.status)
                }
                .collectAndSetState { result ->
                    when (result) {
                        TransactionUseCase.Outcome.Loading -> {
                            copy(
                                isLoading = true,
                                uiError = null,
                                isSuccess = false,
                                responseType = BaseUseCase.ResponseType.UPDATE_STATUS
                            )
                        }
                        else -> {
                            if (result is TransactionUseCase.Outcome.Response) {
                                copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    responseType = BaseUseCase.ResponseType.UPDATE_STATUS,
                                    responseUpdate = result.asUpdateStatusResponse(),
                                    uiError = null
                                )
                            } else {
                                copy(
                                    isLoading = false,
                                    uiError = result.asError(),
                                    isSuccess = false,
                                    responseType = BaseUseCase.ResponseType.UPDATE_STATUS,
                                )
                            }

                        }
                    }
                }
        }
    }

    private fun handleTransactionListEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<Transaction>()
                .flatMapLatest {
                    transactionUseCase.transaction()
                }
                .collectAndSetState { result ->
                    when (result) {
                        TransactionUseCase.Outcome.Loading -> {
                            copy(
                                isLoading = true,
                                uiError = null,
                                isSuccess = false,
                                responseType = BaseUseCase.ResponseType.TRANSACTION
                            )
                        }
                        else -> {
                            if (result is TransactionUseCase.Outcome.Response) {
                                copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    responseType = BaseUseCase.ResponseType.TRANSACTION,
                                    response = result.asResponse(),
                                    uiError = null
                                )
                            } else {
                                copy(
                                    isLoading = false,
                                    uiError = result.asError(),
                                    isSuccess = false,
                                    responseType = BaseUseCase.ResponseType.TRANSACTION,
                                )
                            }

                        }
                    }
                }
        }
    }

    fun transaction() {
        viewModelScope.launch {
            uiActions.emit(Transaction(""))
        }
    }

    fun updateStatus(id: String, status: String) {
        viewModelScope.launch {
            uiActions.emit(UpdateStatus(id, status))
        }
    }

}