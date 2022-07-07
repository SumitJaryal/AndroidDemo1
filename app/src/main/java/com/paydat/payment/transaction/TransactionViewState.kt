package com.paydat.payment.transaction

import com.paydat.data.entities.model.base.BaseData
import com.paydat.data.entities.model.base.DataModel
import com.paydat.data.entities.model.base.ListModel
import com.paydat.data.entities.model.payment.transction.TransactionResponse
import com.paydat.domain.BaseUseCase
import com.paydat.domain.payment.transaction.TransactionUseCase

data class TransactionViewState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val uiError: TransactionError? = null,
    val response: ListModel<TransactionResponse>? = null,
    val responseUpdate: DataModel<BaseData>? = null,
    val responseType: BaseUseCase.ResponseType = BaseUseCase.ResponseType.COMMON
)

sealed class TransactionError
object NetworkError : TransactionError()
object UnknownError : TransactionError()
object NoRecordFound : TransactionError()
object TokenExpire : TransactionError()
object InvalidData : TransactionError()

fun TransactionUseCase.Outcome.asError(): TransactionError {
    return when (this) {
        TransactionUseCase.Outcome.NoRecordFound -> NoRecordFound
        TransactionUseCase.Outcome.NetworkError -> NetworkError
        TransactionUseCase.Outcome.UnknownError -> UnknownError
        TransactionUseCase.Outcome.TokenExpire -> TokenExpire
        TransactionUseCase.Outcome.InvalidData -> InvalidData

        else -> UnknownError
    }
}

fun TransactionUseCase.Outcome.asResponse(): ListModel<TransactionResponse>? {
    if (this is TransactionUseCase.Outcome.Response) {
        return this.response as ListModel<TransactionResponse>
    }
    return null
}

fun TransactionUseCase.Outcome.asUpdateStatusResponse(): DataModel<BaseData>? {
    if (this is TransactionUseCase.Outcome.Response) {
        return this.response as DataModel<BaseData>
    }
    return null
}

sealed class TransactionAction
data class Transaction(val nothing: String) : TransactionAction()
data class UpdateStatus(val id: String, val status: String) : TransactionAction()