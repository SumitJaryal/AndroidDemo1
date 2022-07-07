package com.paydat.user.profile.update

import androidx.lifecycle.viewModelScope
import com.paydat.domain.BaseUseCase
import com.paydat.domain.auth.AuthUseCase
import com.paydat.domain.logout.LogoutUseCase
import com.paydat.domain.user.UserUseCase
import com.paydat.util.ReduxViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val logoutUseCase: LogoutUseCase
) : ReduxViewModel<ProfileState>(ProfileState()) {
    private val uiActions = MutableSharedFlow<ProfileViewAction>()

    init {
        handleLogoutEvent()
        handleSwitchAccountEvent()
    }

    private fun handleSwitchAccountEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<UpdateTaxStatus>()
                .flatMapLatest {
                    authUseCase.updateTaxStatus(it.status)
                }
                .collectAndSetState { result ->
                    when (result) {
                        UserUseCase.Outcome.Loading -> {
                            copy(isLoading = true, uiError = null, isSuccess = false)
                        }
                        else -> {
                            if (result is UserUseCase.Outcome.Response) {
                                copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    responseType = BaseUseCase.ResponseType.TAX_STATUS
                                )
                            } else {
                                copy(
                                    isLoading = false,
                                    isSuccess = false,
                                    responseType = BaseUseCase.ResponseType.TAX_STATUS,
                                    uiError = result.asError(),
                                )
                            }
                        }
                    }
                }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            uiActions.emit(LogoutUser(""))
        }
    }

    fun switchAccount(status: Boolean) {
        viewModelScope.launch {
            uiActions.emit(UpdateTaxStatus(status))
        }
    }

    private fun handleLogoutEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<LogoutUser>()
                .flatMapLatest {
                    logoutUseCase.logout()
                }
                .collectAndSetState { result ->
                    when (result) {
                        LogoutUseCase.Outcome.Loading -> {
                            copy(isLoading = true, uiError = null)
                        }
                        else -> {
                            if (result is LogoutUseCase.Outcome.Response) {
                                copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    responseLogout = result.asResponse(),
                                    responseType = BaseUseCase.ResponseType.DELETE_LOCAL_DATABASE,
                                    responseTime = Date().time.toString()
                                )
                            } else {
                                copy(
                                    isLoading = false,
                                    isSuccess = false,
                                    responseLogout = false,
                                    uiError = result.asError(),
                                    responseType = BaseUseCase.ResponseType.ERROR
                                )
                            }
                        }
                    }
                }
        }
    }
}