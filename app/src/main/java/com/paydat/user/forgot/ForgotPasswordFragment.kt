package com.paydat.user.forgot

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.paydat.R
import com.paydat.base.BaseFragment
import com.paydat.databinding.FragmentForgotPasswordBinding
import com.paydat.util.AlertDialogs
import com.paydat.util.AlertDialogsListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordFragment : BaseFragment<FragmentForgotPasswordBinding>(), View.OnClickListener {
    private lateinit var auth: FirebaseAuth
    override val viewBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentForgotPasswordBinding
        get() = FragmentForgotPasswordBinding::inflate

    override fun onViewBindingCreated(
        view: View,
        binding: FragmentForgotPasswordBinding,
        savedInstanceState: Bundle?
    ) {
        auth = Firebase.auth
        listeners()
    }

    private fun listeners() {
        binding.btnReset.setOnClickListener(this)
        binding.buttonLogin.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        binding.apply {
            when (p0) {
                buttonLogin -> activity?.onBackPressed()
                btnReset -> {
                    val email = binding.inputLayoutEmail.editText?.text.toString().trim()
                    if (email.isEmpty()) {
                        showErrorMessage(InvalidEmail, "")
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        showErrorMessage(InvalidEmailFormat, "")
                    } else {
                        progressDialog.showLoadingDialog(requireActivity(), null)
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    progressDialog.dismissLoadingDialog()
                                    showErrorMessage(ForgotSuccess, "")
                                } else {
                                    progressDialog.dismissLoadingDialog()
                                    showErrorMessage(
                                        SomethingWrong,
                                        task.exception?.message.toString().trim()
                                    )
                                }
                            }.addOnFailureListener {
                                progressDialog.dismissLoadingDialog()
                                showErrorMessage(
                                    SomethingWrong,
                                    it.message.toString().trim()
                                )
                            }
                    }
                }
            }
        }
    }

    private fun showErrorMessage(error: ForgotPasswordError, text: String) {
        val message = when (error) {
            InvalidEmailFormat -> getString(R.string.invalid_email)
            InvalidEmail -> getString(R.string.empty_email)
            ForgotSuccess -> getString(R.string.success_forgot_password)
            SomethingWrong -> text
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
                    if (error == ForgotSuccess) {
                        activity?.onBackPressed()
                    }
                }
            }
        )
        dialog.show()
    }
}