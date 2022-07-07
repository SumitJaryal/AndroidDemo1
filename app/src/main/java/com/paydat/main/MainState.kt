package com.paydat.main

import com.paydat.domain.BaseUseCase
import com.paydat.domain.auth.AuthUseCase
import com.paydat.domain.logout.LogoutUseCase
import com.paydat.domain.user.UserUseCase

data class MainViewState(
    val mainNavigation: MainNavigation? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val uiError: MainViewError? = null,
    val response: Boolean = false,
    val responseType: BaseUseCase.ResponseType = BaseUseCase.ResponseType.COMMON,
    val responseLogout: Boolean = false,
    val responseTime: String = ""
)

sealed class MainViewError
object NetworkError : MainViewError()
object UnknownError : MainViewError()
object TokenExpire : MainViewError()

fun AuthUseCase.Outcome.asError(): MainViewError? {
    return when (this) {
        AuthUseCase.Outcome.NetworkError -> NetworkError
        AuthUseCase.Outcome.TokenExpire -> TokenExpire
        else -> null
    }
}

fun UserUseCase.Outcome.asError(): MainViewError? {
    return when (this) {
        UserUseCase.Outcome.NetworkError -> NetworkError
        UserUseCase.Outcome.TokenExpire -> TokenExpire
        else -> null
    }
}

fun AuthUseCase.Outcome.asResponse(): Boolean {
    if (this is AuthUseCase.Outcome.Response) {
        return this.response as Boolean
    }
    return false
}

fun LogoutUseCase.Outcome.asResponse(): Boolean {
    if (this is LogoutUseCase.Outcome.Response) {
        return this.response as Boolean
    }
    return false
}

fun LogoutUseCase.Outcome.asError(): MainViewError? {
    return when (this) {
        LogoutUseCase.Outcome.NetworkError -> NetworkError
        LogoutUseCase.Outcome.UnknownError -> UnknownError
        else -> null
    }
}

sealed class MainNavigation
object Welcome : MainNavigation()
object Login : MainNavigation()
object Register : MainNavigation()
object Home : MainNavigation()
object Sale : MainNavigation()
object Scan : MainNavigation()
object Authorise : MainNavigation()
object MyQr : MainNavigation()
object Sell : MainNavigation()
object GenerateQr : MainNavigation()
object ForgotPassword : MainNavigation()

sealed class MainViewAction
data class LogoutUser(val nothing: String) : MainViewAction()
data class SwitchAccount(val accountType: String) : MainViewAction()
data class Screen(val nothing: String) : MainViewAction()

