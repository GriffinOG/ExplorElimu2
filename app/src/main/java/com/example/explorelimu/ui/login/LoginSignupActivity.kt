package com.example.explorelimu.ui.login

import android.Manifest
import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.render.MainActivity
import com.example.explorelimu.ModelListActivity
import com.example.explorelimu.R
import com.example.explorelimu.xmpp.RoosterConnectionService
import com.google.android.material.textfield.TextInputEditText

class LoginSignupActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var loginViewModel: LoginViewModel
    private var selectedSchool: String? = null
    private var isEducator: Boolean = false

    private lateinit var email: TextInputEditText
    private lateinit var password: TextInputEditText

    private lateinit var emailStr: String
    private lateinit var passwordStr: String

    var PERMISSION_ALL = 1
    var PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS)

    var bound = false

    val br: BroadcastReceiver = object :BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == RoosterConnectionService.UI_AUTHENTICATED) {
                val mainActivityIntent = Intent(
                    this@LoginSignupActivity,
                    com.example.explorelimu.MainActivity::class.java
                )
                startActivity(mainActivityIntent)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout_login_signup)

        val loginForm = findViewById<ConstraintLayout>(R.id.login_form)
        val regForm = findViewById<ConstraintLayout>(R.id.reg_form)
        email = findViewById(R.id.email)
        val username = findViewById<TextInputEditText>(R.id.username)
        val schoolsList = findViewById<Spinner>(R.id.schools_list)
//        val educatorCheckBox = findViewById<CheckBox>(R.id.educator_cb)
        password = findViewById(R.id.password)
        val passwordConfirm = findViewById<TextInputEditText>(R.id.password_confirm)
        val login = findViewById<Button>(R.id.login)
        val register = findViewById<Button>(R.id.register)
        val loading = findViewById<ProgressBar>(R.id.loading)
        val unregisteredTextView = findViewById<TextView>(R.id.unregistered_tv)
        val accountExistsTextView = findViewById<TextView>(R.id.account_exists_tv)

        schoolsList.onItemSelectedListener = this
        ArrayAdapter.createFromResource(
            this,
            R.array.schools_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            schoolsList.adapter = adapter
        }

        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(application))
            .get(LoginViewModel::class.java)

        loginViewModel.formState.observe(this@LoginSignupActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.emailError != null) {
                email.error = getString(loginState.emailError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginSignupActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                Log.d("LoginSignup", "Should update")
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        email.afterTextChanged {
            loginViewModel.loginDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    email.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        updateLoginDetails()
                        loginViewModel.login(
                            email.text.toString(),
                            password.text.toString()
                        )
                    }
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                updateLoginDetails()
                loginViewModel.login(email.text.toString(), password.text.toString())
            }
        }

        unregisteredTextView.setOnClickListener {
            email = findViewById(R.id.reg_email)
            password = findViewById(R.id.reg_password)

            loginForm.visibility = View.INVISIBLE
            regForm.visibility = View.VISIBLE

            username.afterTextChanged {
                loginViewModel.regDataChanged(
                    email.text.toString(),
                    username.text.toString(),
                    selectedSchool,
                    password.text.toString(),
                    passwordConfirm.text.toString()
                )
            }

            passwordConfirm.apply {
                afterTextChanged {
                    loginViewModel.regDataChanged(
                        email.text.toString(),
                        username.text.toString(),
                        selectedSchool,
                        password.text.toString(),
                        passwordConfirm.text.toString()
                    )
                }

                setOnEditorActionListener { _, actionId, _ ->
                    when (actionId) {
                        EditorInfo.IME_ACTION_DONE -> {
                            updateLoginDetails()
                            loginViewModel.register(
                                    email.text.toString(),
                                    username.text.toString(),
                                    selectedSchool,
                                    isEducator,
                                    password.text.toString()
                            )
                        }
                    }
                    false
                }

                register.setOnClickListener {
                    loading.visibility = View.VISIBLE
                    updateLoginDetails()
                    loginViewModel.register(
                        email.text.toString(),
                        username.text.toString(),
                        selectedSchool,
                        isEducator,
                        password.text.toString()
                    )
                }
            }
        }

        accountExistsTextView.setOnClickListener {
            email = findViewById(R.id.email)
            password = findViewById(R.id.password)

            regForm.visibility = View.INVISIBLE
            loginForm.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(br, IntentFilter(RoosterConnectionService.UI_AUTHENTICATED))
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(br)
    }

    private fun updateUiWithUser(model: LoggedInUserView) {

        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience

//        Toast.makeText(
//            applicationContext,
//            "$welcome $displayName",
//            Toast.LENGTH_LONG
//        ).show()

        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(
            this
        )
        prefs.edit().putBoolean("xmpp_logged_in", true).apply()
//        finish()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedSchool = parent!!.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    fun onCheckboxClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked
            when (view.id) {
                R.id.educator_cb -> {
                    if (checked) {
                        isEducator = true
                    } else {
                        // Remove the meat
                    }
                }
            }
        }
    }

    private fun updateLoginDetails(){
        emailStr = email.text.toString()
        passwordStr = password.text.toString()

    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}