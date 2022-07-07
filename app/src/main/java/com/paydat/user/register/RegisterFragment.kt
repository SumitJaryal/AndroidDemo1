package com.paydat.user.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.paydat.R
import com.paydat.base.BaseFragment
import com.paydat.data.entities.model.login.LoginRequest
import com.paydat.databinding.FragmentRegisterBinding
import com.paydat.domain.BaseUseCase
import com.paydat.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class RegisterFragment : BaseFragment<FragmentRegisterBinding>() {
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private var type: String? = null

    override val viewBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRegisterBinding
        get() = FragmentRegisterBinding::inflate

    override fun onViewBindingCreated(
        view: View,
        binding: FragmentRegisterBinding,
        savedInstanceState: Bundle?
    ) {
        auth = Firebase.auth
        type = arguments?.getString(Constants.USER_TYPE)
        listeners()
        viewModel.stateFlow.onEach { state ->
            handle(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handle(state: RegisterState) {
        when {
            state.isLoading -> {
                progressDialog.showLoadingDialog(requireActivity(), null)
            }
            state.registerSuccess -> {
                progressDialog.dismissLoadingDialog()
                when (state.responseType) {
                    BaseUseCase.ResponseType.REGISTER -> {
                        state.response?.getData()?.let {
                            val token = it.token
//                            if (preferenceManager.accountType == UserType.USER_TYPE_MERCHANT.type) {
//                                viewModel.createAccount(token)
//                            } else {
//                                showErrorMessage(InvalidVerification, "")
//                            }
                            showErrorMessage(InvalidVerification, "")
                        }
                    }
                    BaseUseCase.ResponseType.STRIPE_ACCOUNT -> {
                        state.responseStripe?.getData()?.let {
                            val url = it.url
                            if (url.isNotEmpty()) {
                                val i = Intent(Intent.ACTION_VIEW)
                                i.data = Uri.parse(url)
                                startActivity(i)
                            }
                        }
                        navigateSingleTop(Paths.URI_LOGIN)
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

    private fun listeners() {
        binding.buttonSignup.setOnClickListener {
            binding.apply {
                if (checkConnection()) {
                    val selectedId = radioGroup.checkedRadioButtonId
                    val radioButton = radioGroup.findViewById<RadioButton>(selectedId)

                    val accountType = radioButton.text
                    val firstName = inputLayoutFirstName.editText?.text.toString().trim()
                    val lastName = inputLayoutLastName.editText?.text.toString().trim()
                    val email = inputLayoutEmail.editText?.text.toString().trim()
                    val password = inputLayoutPassword.editText?.text.toString().trim()
                    val confirmPassword =
                        inputLayoutConfirmPassword.editText?.text.toString().trim()

                    if (firstName.isEmpty()) {
                        showErrorMessage(InvalidName, "")
                    } else if (email.isEmpty()) {
                        showErrorMessage(InvalidEmail, "")
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        showErrorMessage(InvalidEmailFormat, "")
                    } else if (password.isEmpty()) {
                        showErrorMessage(InvalidPassword, "")
                    } else if (password.length < 6) {
                        showErrorMessage(InvalidPasswordLength, "")
                    } else if (!isValidPassword(password)) {
                        showErrorMessage(InvalidPasswordFormat, "")
                    } else if (confirmPassword.isEmpty()) {
                        showErrorMessage(InvalidConfirmPassword, "")
                    } else if (password != confirmPassword) {
                        showErrorMessage(PasswordMismatch, "")
                    } else {
                        val name: String = if (lastName.isNotEmpty()) {
                            "$firstName $lastName"
                        } else {
                            firstName
                        }
                        progressDialog.showLoadingDialog(requireActivity(), null)
                        callFirebase(email, password, name, accountType as String)
                    }
                } else {
                    showErrorMessage(NetworkError, "")
                }
            }
        }
    }

    private fun showErrorMessage(error: UserRegisterError, msg: String) {
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
            InvalidPasswordFormat -> getString(R.string.login_invalid_password_format)
            FirebaseError -> msg
            else -> getString(R.string.unknown_error)
        }

        val dialog = AlertDialogs(
            requireContext(),
            getString(R.string.dialog_info),
            message,
            isPositiveButton = true,
            isCancelButton = false,
            positive_button_text = getString(R.string.btn_ok),
            listener = object : AlertDialogsListener {

                override fun onClickClose() {
                }

                override fun onClickPositive() {
                    if (error == InvalidVerification) {
//                        val bundle = Bundle()
//                        bundle.putString(Constants.USER_TYPE, type)
//                        navigateSingleTop(Paths.ID_LOGIN, bundle)
                        navigateSingleTop(Paths.URI_WELCOME)
                    }
                }
            }
        )
        dialog.show()
    }

    private fun callFirebase(email: String, password: String, name: String, accountType: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user, name, accountType)
                } else {
                    showErrorMessage(FirebaseError, task.exception?.message.toString())
                    progressDialog.dismissLoadingDialog()
                }
            }
    }

    private fun updateUI(user: FirebaseUser?, name: String, accountType: String) {
        preferenceManager.accountType(accountType)
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener {
                if (it.isSuccessful) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }
                    user.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                user.getIdToken(true)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val idToken: String = task.result?.token.toString()
                                            viewModel.register(
                                                LoginRequest(
                                                    idToken,
                                                    type,
                                                    false
                                                )
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
                                progressDialog.dismissLoadingDialog()
                                showErrorMessage(FirebaseError, task.exception?.message.toString())
                            }
                        }
                } else {
                    showErrorMessage(FirebaseError, it.exception?.message.toString())
                    progressDialog.dismissLoadingDialog()
                }
            }
        } else {
            progressDialog.dismissLoadingDialog()
            showErrorMessage(InvalidUser, "")
        }
    }
}