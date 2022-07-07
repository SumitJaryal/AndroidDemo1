package com.paydat.user.profile.business

import com.paydat.data.entities.model.base.DataModel
import com.paydat.data.entities.model.login.LoginResponse
import com.paydat.data.entities.model.user.BusinessRequest
import com.paydat.domain.BaseUseCase
import com.paydat.domain.logout.LogoutUseCase
import com.paydat.domain.user.UserUseCase

data class BusinessStateView(
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val uiError: BusinessError? = null,
    val response: DataModel<LoginResponse>? = null,
    val responseType: BaseUseCase.ResponseType = BaseUseCase.ResponseType.COMMON,
)

sealed class BusinessError
object NetworkError : BusinessError()
object UnknownError : BusinessError()
object InvalidTaxId : BusinessError()
object InvalidBankName : BusinessError()
object InvalidAccountNo : BusinessError()
object InvalidBusinessNature : BusinessError()


fun UserUseCase.Outcome.asError(): BusinessError? {
    return when (this) {
        UserUseCase.Outcome.NetworkError -> NetworkError
        UserUseCase.Outcome.UnknownError -> UnknownError
        UserUseCase.Outcome.InvalidTaxId -> InvalidTaxId
        UserUseCase.Outcome.InvalidBankName -> InvalidBankName
        UserUseCase.Outcome.InvalidAccountNo -> InvalidAccountNo
        UserUseCase.Outcome.InvalidBusinessNature -> InvalidBusinessNature
        else -> null
    }
}
sealed class BusinessViewAction
data class UpdateBusinessDetail(val businessRequest: BusinessRequest) : BusinessViewAction()