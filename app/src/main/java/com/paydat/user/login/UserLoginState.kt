package com.paydat.user.login

import com.paydat.data.entities.model.base.DataModel
import com.paydat.data.entities.model.login.LoginRequest
import com.paydat.data.entities.model.login.LoginResponse
import com.paydat.data.entities.model.payment.stripe.StripeAccountResponse
import com.paydat.domain.BaseUseCase
import com.paydat.domain.user.UserUseCase

data class UserLoginState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val uiError: UserLoginError? = null,
    val response: DataModel<LoginResponse>? = null,
    val responseStripe: DataModel<StripeAccountResponse>? = null,
    val responseType: BaseUseCase.ResponseType = BaseUseCase.ResponseType.COMMON,
)

sealed class UserLoginError
object InvalidPassword : UserLoginError()
object InvalidPasswordLength : UserLoginError()
object InvalidConfirmPassword : UserLoginError()
object PasswordMismatch : UserLoginError()
object InvalidEmail : UserLoginError()
object InvalidName : UserLoginError()
object InvalidEmailFormat : UserLoginError()
object InvalidUser : UserLoginError()
object FirebaseError : UserLoginError()
object NetworkError : UserLoginError()
object UnknownError : UserLoginError()
object InvalidVerification : UserLoginError()

fun UserUseCase.Outcome.asError(): UserLoginError? {
    return when (this) {
        UserUseCase.Outcome.InvalidPassword -> InvalidPassword
        UserUseCase.Outcome.InvalidEmail -> InvalidEmail
        UserUseCase.Outcome.InvalidUser -> InvalidUser
        UserUseCase.Outcome.CouldNotLogin -> UnknownError
        UserUseCase.Outcome.NetworkError -> NetworkError
        UserUseCase.Outcome.InvalidCredentials -> InvalidUser
        UserUseCase.Outcome.InvalidEmailFormat -> InvalidEmailFormat
        UserUseCase.Outcome.InvalidName -> InvalidName
        UserUseCase.Outcome.InvalidName -> PasswordMismatch
        UserUseCase.Outcome.InvalidConfirmPassword -> InvalidConfirmPassword
        UserUseCase.Outcome.InvalidPasswordLength -> InvalidPasswordLength
        UserUseCase.Outcome.InvalidVerification -> InvalidVerification
        UserUseCase.Outcome.FirebaseError -> FirebaseError
        UserUseCase.Outcome.UnknownError -> UnknownError
        else -> null
    }
}

fun UserUseCase.Outcome.asLoginResponse(): DataModel<LoginResponse>? {
    if (this is UserUseCase.Outcome.Response) {
        return this.response as DataModel<LoginResponse>
    }
    return null
}

fun UserUseCase.Outcome.asStripeResponse(): DataModel<StripeAccountResponse>? {
    if (this is UserUseCase.Outcome.Response) {
        return this.response as DataModel<StripeAccountResponse>
    }
    return null
}

sealed class UserLoginAction
data class UserLogin(val request: LoginRequest) : UserLoginAction()
data class CreateStripeAccount(val token: String) : UserLoginAction()