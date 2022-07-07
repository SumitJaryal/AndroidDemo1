package com.paydat.user.profile.update

import com.paydat.data.entities.model.base.DataModel
import com.paydat.data.entities.model.login.LoginResponse
import com.paydat.domain.BaseUseCase
import com.paydat.domain.logout.LogoutUseCase
import com.paydat.domain.user.UserUseCase

data class ProfileState(
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val registerSuccess: Boolean = false,
    val uiError: UserProfileError? = null,
    val response: DataModel<LoginResponse>? = null,
    val responseType: BaseUseCase.ResponseType = BaseUseCase.ResponseType.COMMON,
    val responseLogout: Boolean = false,
    val responseTime: String = ""
)

sealed class UserProfileError
object InvalidPassword : UserProfileError()
object InvalidCurrentPassword : UserProfileError()
object InvalidPasswordLength : UserProfileError()
object InvalidConfirmPassword : UserProfileError()
object PasswordMismatch : UserProfileError()
object InvalidEmail : UserProfileError()
object InvalidName : UserProfileError()
object InvalidEmailFormat : UserProfileError()
object InvalidUser : UserProfileError()
object NetworkError : UserProfileError()
object UnknownError : UserProfileError()
object InvalidVerification : UserProfileError()
object FirebaseError : UserProfileError()
object UpdateSuccess : UserProfileError()
object InvalidPasswordFormat : UserProfileError()
object ResetSuccess : UserProfileError()
object ResetError : UserProfileError()

fun UserUseCase.Outcome.asError(): UserProfileError? {
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
        UserUseCase.Outcome.InvalidCurrentPassword -> InvalidCurrentPassword
        UserUseCase.Outcome.ResetSuccess -> ResetSuccess
        UserUseCase.Outcome.ResetError -> ResetError
        else -> null
    }
}

fun LogoutUseCase.Outcome.asResponse(): Boolean {
    if (this is LogoutUseCase.Outcome.Response) {
        return this.response as Boolean
    }
    return false
}

fun LogoutUseCase.Outcome.asError(): UserProfileError? {
    return when (this) {
        LogoutUseCase.Outcome.NetworkError -> NetworkError
        LogoutUseCase.Outcome.UnknownError -> UnknownError
        else -> null
    }
}

sealed class ProfileViewAction
data class LogoutUser(val nothing: String) : ProfileViewAction()
data class UpdateTaxStatus(val status: Boolean) : ProfileViewAction()