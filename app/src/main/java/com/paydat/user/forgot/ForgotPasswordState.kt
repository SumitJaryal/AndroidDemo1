package com.paydat.user.forgot

import com.paydat.domain.user.UserUseCase

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val registerSuccess: Boolean = false,
    val uiError: ForgotPasswordError? = null,
)

sealed class ForgotPasswordError
object InvalidEmail : ForgotPasswordError()
object InvalidEmailFormat : ForgotPasswordError()
object ForgotSuccess : ForgotPasswordError()
object SomethingWrong : ForgotPasswordError()

fun UserUseCase.Outcome.asError(): ForgotPasswordError? {
    return when (this) {
        UserUseCase.Outcome.InvalidEmail -> InvalidEmail
        UserUseCase.Outcome.InvalidEmailFormat -> InvalidEmailFormat
        UserUseCase.Outcome.ForgotSuccess -> ForgotSuccess
        UserUseCase.Outcome.SomethingWrong -> SomethingWrong
        else -> null
    }
}