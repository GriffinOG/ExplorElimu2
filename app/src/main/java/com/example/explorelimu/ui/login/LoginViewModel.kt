package com.example.explorelimu.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.explorelimu.data.login.LoginRepository
import com.example.explorelimu.data.login.Result

import com.example.explorelimu.R

class LoginViewModel(private val application: Application, val loginRepository: LoginRepository) : ViewModel() {

    private val _form = MutableLiveData<LoginFormState>()
    val formState: LiveData<LoginFormState> = _form

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(application, username, password)

        if (result is Result.Success) {
            Log.d("LoginViewModel", "result is Success")
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun register(email: String, username: String, school: String?, isEducator: Boolean, password: String){
        // can be launched in a separate asynchronous job
        val result = loginRepository.register(application, email, username, school!!, isEducator, password)

        if (result is Result.Error){
            _loginResult.value = LoginResult(error = R.string.login_failed)
        } else if (result is Result.RegSuccess){
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(application)
            prefs.edit()
                    .putString("xmpp_jid", email.split("@").toTypedArray()[0])
                    .putString("xmpp_password", password)
                .putBoolean("just_registered", true)
                    .apply()
            login(email, password)
        }
    }

    fun loginDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _form.value = LoginFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _form.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _form.value = LoginFormState(isDataValid = true)
        }
    }

    fun regDataChanged(email: String, username: String, school: String?, password: String, passwordConfirm: String) {
        if (!isEmailValid(email)) {
            _form.value = LoginFormState(emailError = R.string.invalid_email)
        } else if (!isUsernameValid(username)) {
            _form.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isSchoolSelected(school)) {
            _form.value = LoginFormState(schoolSelectionError = R.string.school_not_selected)
        } else if (!isPasswordValid(password)) {
            _form.value = LoginFormState(passwordError = R.string.invalid_password)
        } else if (!doesPasswordConfirmMatch(password, passwordConfirm))  {
            _form.value = LoginFormState(passwordConfirmError = R.string.invalid_password_confirmation)
        } else {
            _form.value = LoginFormState(isDataValid = true)
        }
    }

    // A username validation check
    private fun isEmailValid(email: String): Boolean {
        return if (email.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            email.isNotBlank()
        }
    }

    private fun isUsernameValid(username: String): Boolean {
        return username.isNotEmpty()
    }

    private fun isSchoolSelected(school: String?): Boolean {
        return !school.isNullOrEmpty()
    }

    // A password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    //A password confirm validation check
    private fun doesPasswordConfirmMatch(password: String, passwordConfirm: String): Boolean {
        return passwordConfirm == password
    }
}