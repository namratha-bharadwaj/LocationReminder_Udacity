package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class AuthenticationViewModel: ViewModel() {

    val authenticationState = FirebaseUserLiveData().map {
        if(it != null)
            AuthState.AUTHENTICATED
        else
            AuthState.NOT_AUTHENTICATED
    }

}