package com.anotherdimension.bpl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private val RC_SIGN_GOOGLE: Int = 1
    private lateinit var googleSignInOptions: GoogleSignInOptions
    private lateinit var googleButton: ImageButton
    private lateinit var facebookButton: ImageButton
    private lateinit var buttonContainer: LinearLayout
    private lateinit var progressIndicator: ProgressBar
    private lateinit var googleSignInClient: GoogleSignInClient
    private var showButtons: Boolean = true
    private lateinit var callbackManager: CallbackManager
    private val fbPermissionNeeded: List<String> = listOf("email")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        configureGoogleSignIn()
        setListeners()
    }

    private fun toggleButtons() {
        showButtons = !showButtons
        when (showButtons) {
            true -> {
                progressIndicator.visibility = View.INVISIBLE
                buttonContainer.visibility = View.VISIBLE
            }
            else -> {
                buttonContainer.visibility = View.INVISIBLE
                progressIndicator.visibility = View.VISIBLE
            }
        }
    }


    private fun setListeners() {
        googleButton = google_button
        facebookButton = facebook_button
        buttonContainer = button_container
        progressIndicator = login_progress
        googleButton.setOnClickListener(googleLoginListener)
        facebookButton.setOnClickListener(facebookLoginListener)
    }


    private fun configureGoogleSignIn() {
        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private val googleLoginListener: View.OnClickListener = View.OnClickListener {
        toggleButtons()
        Log.d("Login Attempted", "Google")
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_GOOGLE)
    }

    private val facebookLoginListener: View.OnClickListener = View.OnClickListener {
        toggleButtons()
        Log.d("Login Attempted", "Facebook")

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
                override fun onSuccess(result: LoginResult?) {
                    Log.d("MainActivity", "Facebook token: " + result!!.accessToken.token)
                    getJWT(result.accessToken.token.toString(), "F")
                }

                override fun onCancel() {
                    Log.d("MainActivity", "Failed ")
                    Toast.makeText(applicationContext, "Login cancelled by user", Toast.LENGTH_LONG)
                        .show()
                    toggleButtons()
                }

                override fun onError(error: FacebookException?) {
                    Log.d("MainActivity", "Failed ")
                    Toast.makeText(
                        applicationContext,
                        "Facebook sign in failed:(",
                        Toast.LENGTH_LONG
                    ).show()
                    toggleButtons()
                }
            })

        LoginManager.getInstance().logInWithReadPermissions(this, fbPermissionNeeded)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("RC", requestCode.toString())
        if (requestCode == RC_SIGN_GOOGLE) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("Account token: ", account!!.idToken.toString())
                getJWT(account.idToken.toString(), "G")
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
                toggleButtons()
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getJWT(token: String, type: String) {
        val url: String = resources.getString(R.string.base_url) + "/api/login"
        val queue = RequestQueueSingleton.getInstance(this.applicationContext).requestQueue
        val params: JSONObject = JSONObject()

        params.put("type", type)
        params.put("token", token)

        val request = JsonObjectRequest(Request.Method.POST, url, params,
            Response.Listener { response ->
                Log.d("Response: ", response.toString())
                val success: Boolean = response.getBoolean("success")
                toggleButtons()
                if (success) {
                    val jwt: String = response.getString("token")
                    storeToSharedPreference(jwt)
                    val intent: Intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Server Error", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                Log.d("Error: ", error.toString())
                toggleButtons()
            }
        )

        queue.add(request)
    }

    private fun storeToSharedPreference(accessToken: String) {
        val sharedPreference = getSharedPreferences("BPL", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("token", accessToken)
        editor.apply()
    }
}