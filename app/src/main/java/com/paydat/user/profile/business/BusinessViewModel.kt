package com.paydat.user.profile.business

import androidx.lifecycle.viewModelScope
import com.paydat.data.entities.model.user.BusinessRequest
import com.paydat.domain.BaseUseCase
import com.paydat.domain.auth.AuthUseCase
import com.paydat.domain.logout.LogoutUseCase
import com.paydat.domain.user.UserUseCase
import com.paydat.user.profile.update.UpdateTaxStatus
import com.paydat.util.ReduxViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BusinessViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val logoutUseCase: LogoutUseCase
) : ReduxViewModel<BusinessStateView>(BusinessStateView()) {
    private val uiActions = MutableSharedFlow<BusinessViewAction>()

    init {
        handleBusinessDetailEvent()
    }

    private fun handleBusinessDetailEvent() {
        viewModelScope.launch {
            uiActions.filterIsInstance<UpdateBusinessDetail>()
                .flatMapLatest {
                    authUseCase.addBusiness(it.businessRequest)
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
                                    responseType = BaseUseCase.ResponseType.ADD_BUSINESS
                                )
                            } else {
                                copy(
                                    isLoading = false,
                                    isSuccess = false,
                                    responseType = BaseUseCase.ResponseType.ADD_BUSINESS,
                                    uiError = result.asError(),
                                )
                            }
                        }
                    }
                }
        }
    }

    fun businessDetail(businessRequest: BusinessRequest) {
        viewModelScope.launch {
            uiActions.emit((UpdateBusinessDetail(businessRequest)))
        }
    }
}