package com.paydat.user.register

import com.paydat.data.entities.model.base.DataModel
import com.paydat.data.entities.model.login.LoginRequest
import com.paydat.data.entities.model.login.LoginResponse
import com.paydat.data.entities.model.payment.stripe.StripeAccountResponse
import com.paydat.domain.BaseUseCase
import com.paydat.domain.user.UserUseCase

data class RegisterState(
    val isLoading: Boolean = false,
    val registerSuccess: Boolean = false,
    val uiError: UserRegisterError? = null,
    val response: DataModel<LoginResponse>? = null,
    val responseStripe: DataModel<StripeAccountResponse>? = null,
    val responseType: BaseUseCase.ResponseType = BaseUseCase.ResponseType.COMMON,
)

sealed class UserRegisterError
object InvalidPassword : UserRegisterError()
object InvalidPasswordLength : UserRegisterError()
object InvalidConfirmPassword : UserRegisterError()
object PasswordMismatch : UserRegisterError()
object InvalidEmail : UserRegisterError()
object InvalidName : UserRegisterError()
object InvalidEmailFormat : UserRegisterError()
object InvalidUser : UserRegisterError()
object NetworkError : UserRegisterError()
object UnknownError : UserRegisterError()
object InvalidVerification : UserRegisterError()
object FirebaseError : UserRegisterError()
object InvalidPasswordFormat : UserRegisterError()

fun UserUseCase.Outcome.asError(): UserRegisterError? {
    return when (this) {
        UserUseCase.Outcome.InvalidPassword -> InvalidPassword
        UserUseCase.Outcome.InvalidEmail -> InvalidEmail
        UserUseCase.Outcome.InvalidUser -> InvalidUser
        UserUseCase.Outcome.NetworkError -> NetworkError
        UserUseCase.Outcome.UnknownError -> UnknownError
        UserUseCase.Outcome.InvalidCredentials -> InvalidUser
        UserUseCase.Outcome.InvalidEmailFormat -> InvalidEmailFormat
        UserUseCase.Outcome.InvalidName -> InvalidName
        UserUseCase.Outcome.PasswordMismatch -> PasswordMismatch
        UserUseCase.Outcome.InvalidPasswordLength -> InvalidPasswordLength
        UserUseCase.Outcome.InvalidConfirmPassword -> InvalidConfirmPassword
        UserUseCase.Outcome.InvalidVerification -> InvalidVerification
        UserUseCase.Outcome.FirebaseError -> FirebaseError
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

sealed class UserRegisterAction
data class UserRegister(val request: LoginRequest) : UserRegisterAction()
data class CreateStripeAccount(val token: String) : UserRegisterAction()