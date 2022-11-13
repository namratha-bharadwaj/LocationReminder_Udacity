package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */

//https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private val authViewModel by viewModels<AuthenticationViewModel>()
    private val TAG = "LoginFragment"
    private val registerActivityForResult = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        handleSigninResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        observeAuthStateAndSetContent()
        binding.lifecycleOwner = this
    }

    private fun observeAuthStateAndSetContent() {
        authViewModel.authenticationState.observe(this, Observer { authState ->
            when(authState) {
                AuthState.AUTHENTICATED -> {
                    goToRemindersActivity()
                }
                AuthState.NOT_AUTHENTICATED -> {
                    binding.loginSignupButton.setOnClickListener {
                        launchSigninFlow()
                    }
                }
            }
        })
    }

    private fun goToRemindersActivity() {
        this.finish()
        navigateToRemindersActivity()
    }

    private fun navigateToRemindersActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
    }

    fun launchSigninFlow() {
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build())

        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        registerActivityForResult.launch(intent)

    }

    private fun handleSigninResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            //Signin successful
            val user = FirebaseAuth.getInstance().currentUser?.displayName
            Toast.makeText(this, "Signin successful for $user", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Signin successful for user: $user")
            navigateToRemindersActivity()
        } else {
            //Signin failed. Show toast
            Toast.makeText(this, "Signin failed!", Toast.LENGTH_SHORT)
            Log.i(TAG, "Signin failed. ${response?.error?.toString()}")
        }

    }
}
