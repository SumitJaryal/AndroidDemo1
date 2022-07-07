package com.paydat.user.login

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.paydat.R
import com.paydat.base.BaseFragment
import com.paydat.data.entities.model.login.LoginRequest
import com.paydat.data.repositories.user.UserType
import com.paydat.databinding.FragmentLoginBinding
import com.paydat.domain.BaseUseCase
import com.paydat.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(), View.OnClickListener {

    @Inject
    lateinit var permissionManager: PermissionManager
    private val viewModel: UserLoginViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private var type: String? = null

    override val viewBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLoginBinding
        get() = FragmentLoginBinding::inflate

    private lateinit var uri: Uri

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            navigateSingleTop(uri)
        } else {
            val dialog = context?.let {
                AlertDialogs(
                    it,
                    getString(R.string.dialog_info),
                    getString(R.string.camera_permission_desc),
                    isPositiveButton = true,
                    isCancelButton = false,
                    positive_button_text = getString(R.string.btn_ok),
                    cancel_button_text = getString(R.string.cancel),
                    listener = object : AlertDialogsListener {

                        override fun onClickClose() {
                        }

                        override fun onClickPositive() {

                        }
                    }
                )
            }
            dialog?.show()
        }
    }

    override fun onViewBindingCreated(
        view: View,
        binding: FragmentLoginBinding,
        savedInstanceState: Bundle?
    ) {
        auth = Firebase.auth
        type = arguments?.getString(Constants.USER_TYPE)
        listener()

        viewModel.stateFlow.onEach { state ->
            handle(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun listener() {
        binding.buttonLogin.setOnClickListener(this)
        binding.buttonForgot.setOnClickListener(this)
        binding.buttonSignup.setOnClickListener(this)
    }

    private fun handle(state: UserLoginState) {
        when {
            state.isLoading -> {
                progressDialog.showLoadingDialog(requireActivity(), null)
            }
            state.loginSuccess -> {
                progressDialog.dismissLoadingDialog()
                when (state.responseType) {
                    BaseUseCase.ResponseType.LOGIN -> {
                        state.response?.getData()?.let { it ->
                            val token = it.token
                            val userData = it.userDetail
                            val status = it.stripe_accountStatus
                            userData.apply {
                                if (preferenceManager.accountType == UserType.USER_TYPE_INDIVIDUAL.type) {
                                    checkPermission(Paths.URI_SCAN)
                                } else if (preferenceManager.accountType == UserType.USER_TYPE_MERCHANT.type) {
//                                    if (stripe_accountId.isEmpty() && status.isEmpty() || status == StripeStatus.STRIPE_STATUS_RESTRICTED.type) {
//                                        val dialog = AlertDialogs(
//                                            requireContext(),
//                                            getString(R.string.dialog_info),
//                                            getString(R.string.setup_stripe_account),
//                                            isPositiveButton = true,
//                                            isCancelButton = true,
//                                            positive_button_text = getString(R.string.btn_ok),
//                                            cancel_button_text = getString(R.string.cancel),
//                                            listener = object : AlertDialogsListener {
//
//                                                override fun onClickClose() {
//                                                }
//
//                                                override fun onClickPositive() {
//                                                    viewModel.createAccount(token)
//                                                }
//                                            }
//                                        )
//                                        dialog.show()
//                                    } else {
//                                        navigateSingleTop(Paths.URI_SELL)
//                                    }
                                    navigateSingleTop(Paths.URI_SELL)
                                }
                            }
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

    private fun showErrorMessage(error: UserLoginError, msg: String) {
        val message = when (error) {
            InvalidEmailFormat -> getString(R.string.invalid_email)
            InvalidEmail -> getString(R.string.empty_email)
            InvalidPasswordLength -> getString(R.string.invalid_password_length)
            InvalidPassword -> getString(R.string.empty_password)
            NetworkError -> getString(R.string.network_connection)
            InvalidUser -> getString(R.string.user_not_found)
            InvalidVerification -> getString(R.string.pending_verification)
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

                }
            }
        )
        dialog.show()
    }

    override fun onClick(p0: View?) {
        binding.apply {
            when (p0) {
                buttonForgot -> navigation(Paths.URI_FORGOT)
                buttonSignup -> navigation(Paths.URI_REGISTER)

                buttonLogin -> {
                    binding.apply {
                        if (checkConnection()) {
                            val email = inputLayoutEmail.editText?.text.toString().trim()
                            val password = inputLayoutPassword.editText?.text.toString().trim()

                            if (email.isEmpty()) {
                                showErrorMessage(InvalidEmail, "")
                            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                showErrorMessage(InvalidEmailFormat, "")
                            } else if (password.isEmpty()) {
                                showErrorMessage(InvalidPassword, "")
                            } else if (password.length < 6) {
                                showErrorMessage(InvalidPasswordLength, "")
                            } else {
                                progressDialog.showLoadingDialog(requireActivity(), null)
                                callFirebase(email, password)
                            }
                        } else {
                            showErrorMessage(NetworkError, "")
                        }
                    }
                }
            }
        }
    }

    private fun callFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    showErrorMessage(FirebaseError, task.exception?.message.toString())
                    progressDialog.dismissLoadingDialog()
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            if (user.isEmailVerified) {
                user.getIdToken(true)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val idToken: String = task.result?.token.toString()
                            progressDialog.dismissLoadingDialog()
                            viewModel.login(LoginRequest(idToken, type, true))
                        } else {
                            showErrorMessage(FirebaseError, task.exception?.message.toString())
                            progressDialog.dismissLoadingDialog()
                        }
                    }
            } else {
                val dialog = AlertDialogs(
                    requireContext(),
                    getString(R.string.dialog_info),
                    getString(R.string.pending_verification),
                    isPositiveButton = true,
                    isCancelButton = true,
                    positive_button_text = getString(R.string.btn_ok),
                    cancel_button_text = getString(R.string.resend),
                    listener = object : AlertDialogsListener {

                        override fun onClickClose() {
                            user.sendEmailVerification().addOnCompleteListener { task ->
//                                if (task.isSuccessful) {
//                                    Log.d("=======", "onClickClose: Done")
//                                } else {
//                                    Log.d("=======", "onClickClose: ${task.exception?.message.toString()}")
//                                }
                            }
                        }

                        override fun onClickPositive() {

                        }
                    }
                )
                dialog.show()
                progressDialog.dismissLoadingDialog()
            }
        } else {
            progressDialog.dismissLoadingDialog()
            showErrorMessage(InvalidUser, "")
        }
    }

    private fun checkPermission(uri: Uri) {
        if (permissionManager.canTakeCameraPhoto) {
            navigateSingleTop(uri)
        } else {
            this.uri = uri
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onResume() {
        super.onResume()
        if (progressDialog == null) {
            progressDialog = ProgressDialog()
        }
    }

    override fun onStop() {
        super.onStop()
        progressDialog.dismissLoadingDialog()
    }
}