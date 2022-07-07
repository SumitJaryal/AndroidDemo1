package com.paydat.payment.card

import com.paydat.data.entities.model.base.DataModel
import com.paydat.data.entities.model.payment.card.CardResponse
import com.paydat.data.entities.model.payment.card.DeleteCardRequest
import com.paydat.data.entities.model.payment.card.DeleteCardResponse
import com.paydat.data.entities.model.payment.pay.CreateChargeRequest
import com.paydat.data.entities.model.payment.pay.CreateChargeResponse
import com.paydat.domain.BaseUseCase
import com.paydat.domain.payment.card.CardUseCase

data class CardViewState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val uiError: CardError? = null,
    val response: DataModel<CardResponse>? = null,
    val responseDelete: DataModel<DeleteCardResponse>? = null,
    val responseCharge: DataModel<CreateChargeResponse>? = null,
    val responseType: BaseUseCase.ResponseType = BaseUseCase.ResponseType.COMMON
)

sealed class CardError
object NetworkError : CardError()
object UnknownError : CardError()
object NoRecordFound : CardError()
object TokenExpire : CardError()
object InvalidCard : CardError()
object InvalidStripeToken : CardError()
object InvalidToken : CardError()
object InvalidData : CardError()

fun CardUseCase.Outcome.asError(): CardError {
    return when (this) {
        CardUseCase.Outcome.NoRecordFound -> NoRecordFound
        CardUseCase.Outcome.NetworkError -> NetworkError
        CardUseCase.Outcome.UnknownError -> UnknownError
        CardUseCase.Outcome.TokenExpire -> TokenExpire
        CardUseCase.Outcome.InvalidCard -> InvalidCard
        CardUseCase.Outcome.InvalidStripeToken -> InvalidStripeToken
        CardUseCase.Outcome.InvalidToken -> InvalidToken
        CardUseCase.Outcome.InvalidData -> InvalidData

        else -> UnknownError
    }
}

fun CardUseCase.Outcome.asResponse(): DataModel<CardResponse>? {
    if (this is CardUseCase.Outcome.Response) {
        return this.response as DataModel<CardResponse>
    }
    return null
}

fun CardUseCase.Outcome.asDeleteResponse(): DataModel<DeleteCardResponse>? {
    if (this is CardUseCase.Outcome.Response) {
        return this.response as DataModel<DeleteCardResponse>
    }
    return null
}

fun CardUseCase.Outcome.asChargeResponse(): DataModel<CreateChargeResponse>? {
    if (this is CardUseCase.Outcome.Response) {
        return this.response as DataModel<CreateChargeResponse>
    }
    return null
}

sealed class CardAction
data class AddCard(val stripeToken: String) : CardAction()
data class Payment(val data: CreateChargeRequest) : CardAction()
data class GetCard(val nothing: String) : CardAction()
data class DeleteCard(val data: DeleteCardRequest) : CardAction()