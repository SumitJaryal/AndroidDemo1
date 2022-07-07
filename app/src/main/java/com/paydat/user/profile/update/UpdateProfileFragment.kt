package com.paydat.user.profile.update

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.paydat.R
import com.paydat.base.BaseFragment
import com.paydat.data.repositories.user.UserType
import com.paydat.databinding.FragmentUpdateProfileBinding
import com.paydat.domain.BaseUseCase
import com.paydat.main.LogoutCallback
import com.paydat.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@AndroidEntryPoint
class UpdateProfileFragment : BaseFragment<FragmentUpdateProfileBinding>() {
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private var callback: LogoutCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = context as LogoutCallback
        } catch (e: Exception) {
            Log.d(TAG, "onAttach: ${e.message}")
        }
    }

    override fun onDetach() {
        super.onDetach()
        try {
            callback = null
        } catch (e: Exception) {
            Log.d(TAG, "onAttach: ${e.message}")
        }
    }

    override val viewBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUpdateProfileBinding
        get() = FragmentUpdateProfileBinding::inflate

    override fun onViewBindingCreated(
        view: View,
        binding: FragmentUpdateProfileBinding,
        savedInstanceState: Bundle?
    ) {
        auth = Firebase.auth

        viewModel.stateFlow.onEach { state ->
            handle(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        setData()
        listener()
    }

    private fun handle(state: ProfileState) {
        when {
            state.isLoading -> {
                progressDialog.showLoadingDialog(requireActivity(), null)
            }
            state.isSuccess -> {
                progressDialog.dismissLoadingDialog()
                when (state.responseType) {
                    BaseUseCase.ResponseType.TAX_STATUS -> {

                    }
                    else -> {}
                }
            }

            state.uiError != null -> {
                progressDialog.dismissLoadingDialog()
                showErrorMessage(state.uiError, "")
            }
        }
    }

    private fun listener() {
        binding.apply {
            buttonUpdate.setOnClickListener {
                val name = inputLayoutName.editText?.text.toString().trim()
                val email = inputLayoutEmail.editText?.text.toString().trim()
                val password = inputLayoutPassword.editText?.text.toString().trim()
                val confirmPassword =
                    inputLayoutConfirmPassword.editText?.text.toString().trim()

                if (name.isEmpty()) {
                    showErrorMessage(InvalidName, "")
                } else if (password.isNotEmpty() || confirmPassword.isNotEmpty()) {
                    when {
                        password.isEmpty() -> {
                            showErrorMessage(InvalidPassword, "")
                        }
                        password.length < 6 -> {
                            showErrorMessage(InvalidPasswordLength, "")
                        }
                        !isValidPassword(password) -> {
                            showErrorMessage(InvalidPasswordFormat, "")
                        }
                        confirmPassword.isEmpty() -> {
                            showErrorMessage(InvalidConfirmPassword, "")
                        }
                        password != confirmPassword -> {
                            showErrorMessage(PasswordMismatch, "")
                        }
                        else -> updateUser(name, email, password, true)
                    }
                } else {
                    updateUser(name, email, password, false)
                }
            }
            buttonReset.setOnClickListener {
                showErrorMessage(
                    ResetError,
                    ""
                )
            }

            btnBusinessDetail.setOnClickListener {
                navigation(Paths.URI_BUSINESS)
            }

            sbTax.setOnCheckedChangeListener { _, status ->
                viewModel.switchAccount(status)
            }
        }
    }

    private fun updateUser(name: String, email: String, password: String, status: Boolean) {
        progressDialog.showLoadingDialog(requireActivity(), null)
        val user = auth.currentUser

        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }
        user?.updateProfile(profileUpdates)?.addOnCompleteListener {
            if (it.isSuccessful) {
                if (status) {
                    val user1 = auth.currentUser
                    user1?.updatePassword(password)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                preferenceManager.updateProfile(name, email)
                                progressDialog.dismissLoadingDialog()
                                showErrorMessage(
                                    UpdateSuccess,
                                    ""
                                )
                            } else {
                                progressDialog.dismissLoadingDialog()
                                showErrorMessage(
                                    FirebaseError,
                                    task.exception?.message.toString()
                                )
                            }
                        }
                } else {
                    preferenceManager.updateProfile(name, email)
                    progressDialog.dismissLoadingDialog()
                    showErrorMessage(
                        UpdateSuccess,
                        ""
                    )
                }
            } else {
                progressDialog.dismissLoadingDialog()
                showErrorMessage(
                    FirebaseError,
                    it.exception?.message.toString()
                )
            }
        }
    }

    private fun setData() {
        binding.apply {
            inputLayoutName.editText?.setText(preferenceManager.name)
            inputLayoutEmail.editText?.setText(preferenceManager.email)

            if (preferenceManager.accountType == UserType.USER_TYPE_MERCHANT.type) {
                sbTax.visibility = View.VISIBLE
                btnBusinessDetail.visibility = View.VISIBLE
            } else {
                sbTax.visibility = View.GONE
                btnBusinessDetail.visibility = View.GONE
            }
            sbTax.isChecked = preferenceManager.taxInclude
        }
    }

    private fun showErrorMessage(error: UserProfileError, msg: String) {
        val message = when (error) {
            InvalidName -> getString(R.string.empty_name)
            InvalidEmailFormat -> getString(R.string.invalid_email)
            InvalidEmail -> getString(R.string.empty_email)
            InvalidPassword -> getString(R.string.empty_password)
            InvalidConfirmPassword -> getString(R.string.empty_confirm_password)
            PasswordMismatch -> getString(R.string.mismatch_password)
            InvalidPasswordLength -> getString(R.string.invalid_password_length)
            NetworkError -> getString(R.string.network_connection)
            InvalidUser -> getString(R.string.user_not_found)
            InvalidVerification -> getString(R.string.pending_verification)
            UpdateSuccess -> getString(R.string.user_updated_successfully)
            InvalidPasswordFormat -> getString(R.string.login_invalid_password_format)
            ResetSuccess -> getString(R.string.success_forgot_password)
            ResetError -> getString(R.string.are_you_sure_reset_password)
            FirebaseError -> msg
            else -> getString(R.string.unknown_error)
        }

        val dialog = AlertDialogs(
            requireContext(),
            getString(R.string.dialog_info),
            message,
            isPositiveButton = true,
            isCancelButton = error == ResetError,
            positive_button_text = getString(R.string.btn_ok),
            cancel_button_text = getString(R.string.cancel),
            listener = object : AlertDialogsListener {

                override fun onClickClose() {
                }

                override fun onClickPositive() {
                    if (error == UpdateSuccess) {
                        callback?.userUpdate(true)
                        activity?.onBackPressed()
                    } else if (error == ResetError) {
                        resetPassword()
                    }
                }
            }
        )
        dialog.show()
    }

    private fun resetPassword() {
        val email = binding.inputLayoutEmail.editText?.text.toString().trim()
        progressDialog.showLoadingDialog(requireActivity(), null)
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                progressDialog.dismissLoadingDialog()
                if (task.isSuccessful) {
                    val text = "We have sent you an email on $email to reset the Password."
                    showErrorMessage(FirebaseError, text)
                } else {
                    showErrorMessage(
                        FirebaseError,
                        task.exception?.message.toString().trim()
                    )
                }
            }.addOnFailureListener {
                progressDialog.dismissLoadingDialog()
                showErrorMessage(
                    FirebaseError,
                    it.message.toString().trim()
                )
            }
    }

}