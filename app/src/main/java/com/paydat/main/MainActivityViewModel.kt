package com.paydat.main

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
class MainActivityViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val logoutUseCase: LogoutUseCase
) : ReduxViewModel<MainViewState>(MainViewState()) {
    private val uiActions = MutableSharedFlow<MainViewAction>()

    init {
        handleLogoutEvent()
        handleSwitchAccountEvent()
        handleScreenEvent()
    }

    private fun handleSwitchAccountEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<SwitchAccount>()
                .flatMapLatest {
                    authUseCase.switchAccount(it.accountType)
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
                                    responseType = BaseUseCase.ResponseType.SWITCH_ACCOUNT,
                                    responseTime = Date().time.toString()
                                )
                            } else {
                                copy(
                                    isLoading = false,
                                    isSuccess = false,
                                    responseType = BaseUseCase.ResponseType.SWITCH_ACCOUNT,
                                    uiError = result.asError(),
                                )
                            }
                        }
                    }
                }
        }
    }

    fun screenHandler() {
        viewModelScope.launch {
            uiActions.emit(Screen(""))
        }
    }

    fun logOut() {
        viewModelScope.launch {
            uiActions.emit(LogoutUser(""))
        }
    }

    fun switchAccount(accountType: String) {
        viewModelScope.launch {
            uiActions.emit(SwitchAccount(accountType))
        }
    }

    private fun handleScreenEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<Screen>()
                .flatMapLatest {
                    authUseCase.checkUserStatus()
                }.collectAndSetState { result ->
                    when (result) {
                        AuthUseCase.Outcome.ShowWelcome -> {
                            copy(mainNavigation = Welcome)
                        }
                        AuthUseCase.Outcome.ShowLogin -> {
                            copy(mainNavigation = Login)
                        }
                        AuthUseCase.Outcome.ShowForgotPassword -> {
                            copy(mainNavigation = ForgotPassword)
                        }
                        AuthUseCase.Outcome.ShowRegister -> {
                            copy(mainNavigation = Register)
                        }
                        AuthUseCase.Outcome.ShowHome -> {
                            copy(mainNavigation = Home)
                        }
                        AuthUseCase.Outcome.ShowSale -> {
                            copy(mainNavigation = Sale)
                        }
                        AuthUseCase.Outcome.ShowScan -> {
                            copy(mainNavigation = Scan)
                        }
                        AuthUseCase.Outcome.ShowAuthorise -> {
                            copy(mainNavigation = Authorise)
                        }
                        AuthUseCase.Outcome.ShowMyQr -> {
                            copy(mainNavigation = MyQr)
                        }
                        AuthUseCase.Outcome.ShowSell -> {
                            copy(mainNavigation = Sell)
                        }
                        AuthUseCase.Outcome.ShowGenerateQr -> {
                            copy(mainNavigation = GenerateQr)
                        }
                        else -> {
                            copy(
                                isLoading = false,
                                isSuccess = false,
                                uiError = result.asError()
                            )
                        }
                    }
                }
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