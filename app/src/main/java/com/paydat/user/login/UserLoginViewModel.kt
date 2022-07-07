package com.paydat.user.login

import androidx.lifecycle.viewModelScope
import com.paydat.data.entities.model.login.LoginRequest
import com.paydat.domain.BaseUseCase
import com.paydat.domain.user.UserUseCase
import com.paydat.util.ReduxViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserLoginViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ReduxViewModel<UserLoginState>(UserLoginState()) {
    private val uiActions = MutableSharedFlow<UserLoginAction>()

    init {
        handleLoginEvent()
        handleStripeAccountEvent()
    }

    private fun handleLoginEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<UserLogin>()
                .flatMapLatest {
                    userUseCase.login(it.request)
                }
                .collectAndSetState { result ->
                    when (result) {
                        UserUseCase.Outcome.Loading -> {
                            copy(isLoading = true, uiError = null)
                        }
                        UserUseCase.Outcome.InvalidCredentials -> {
                            copy(
                                isLoading = false,
                                loginSuccess = false,
                                uiError = result.asError(),
                                responseType = BaseUseCase.ResponseType.LOGIN
                            )
                        }

                        else -> {
                            if (result is UserUseCase.Outcome.Response) {
                                copy(
                                    isLoading = false,
                                    loginSuccess = true,
                                    response = result.asLoginResponse(),
                                    responseType = BaseUseCase.ResponseType.LOGIN
                                )
                            } else
                                copy(isLoading = false, uiError = result.asError(), loginSuccess = false)
                        }
                    }
                }
        }
    }

    private fun handleStripeAccountEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<CreateStripeAccount>()
                .flatMapLatest {
                    userUseCase.createStripeAccount(it.token)
                }
                .collectAndSetState { result ->
                    when (result) {
                        UserUseCase.Outcome.Loading -> {
                            copy(isLoading = true, uiError = null, loginSuccess = false)
                        }
                        else -> {
                            if (result is UserUseCase.Outcome.Response) {
                                copy(
                                    isLoading = false,
                                    loginSuccess = true,
                                    responseStripe = result.asStripeResponse(),
                                    responseType = BaseUseCase.ResponseType.STRIPE_ACCOUNT
                                )
                            } else
                                copy(
                                    isLoading = false,
                                    uiError = result.asError(),
                                    responseType = BaseUseCase.ResponseType.STRIPE_ACCOUNT
                                )
                        }
                    }
                }
        }
    }

    fun login(loginRequest: LoginRequest) {
        viewModelScope.launch {
            uiActions.emit(UserLogin(loginRequest))
        }
    }

    fun createAccount(token: String) {
        viewModelScope.launch {
            uiActions.emit(CreateStripeAccount(token))
        }
    }
}