package com.paydat.user.register

import androidx.lifecycle.viewModelScope
import com.paydat.domain.BaseUseCase
import com.paydat.domain.user.UserUseCase
import com.paydat.util.ReduxViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.paydat.data.entities.model.login.LoginRequest as LoginRequest1

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ReduxViewModel<RegisterState>(RegisterState()) {
    private val uiActions = MutableSharedFlow<UserRegisterAction>()

    init {
        handleRegisterEvent()
        handleStripeAccountEvent()
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
                            copy(isLoading = true, uiError = null)
                        }
                        else -> {
                            if (result is UserUseCase.Outcome.Response) {
                                copy(
                                    isLoading = false,
                                    registerSuccess = true,
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

    private fun handleRegisterEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<UserRegister>()
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
                                registerSuccess = false,
                                uiError = result.asError()
                            )
                        }

                        else -> {
                            if (result is UserUseCase.Outcome.Response) {
                                copy(
                                    isLoading = false,
                                    registerSuccess = true,
                                    response = result.asLoginResponse(),
                                    responseType = BaseUseCase.ResponseType.REGISTER
                                )
                            } else
                                copy(
                                    isLoading = false,
                                    uiError = result.asError(),
                                    responseType = BaseUseCase.ResponseType.REGISTER
                                )
                        }
                    }
                }
        }
    }

    fun register(loginRequest: LoginRequest1) {
        viewModelScope.launch {
            uiActions.emit(UserRegister(loginRequest))
        }
    }

    fun createAccount(token: String) {
        viewModelScope.launch {
            uiActions.emit(CreateStripeAccount(token))
        }
    }
}