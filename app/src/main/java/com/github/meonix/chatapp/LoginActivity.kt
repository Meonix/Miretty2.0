package com.github.meonix.chatapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.mbms.MbmsErrors
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var loadingBar: ProgressDialog? = null
    private var userRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        handleOnClick()
    }

    private fun AllowUserToLogin() {
        val email = login_email.text.toString()
        val password = login_password.text.toString()
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email....", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password....", Toast.LENGTH_SHORT).show()
        } else {
            loadingBar?.setTitle("Sign In")
            loadingBar?.setMessage("Please wait....")
            loadingBar?.setCanceledOnTouchOutside(true)
            loadingBar?.show()

            mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUserID = mAuth?.currentUser?.uid
                    val deviceToken = FirebaseInstanceId.getInstance().token

                    userRef?.child(currentUserID)?.child("device_token")
                            ?.setValue(deviceToken)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    sendUserToMainActivity()
                                    Toast.makeText(this@LoginActivity, "Logged in  Successful....", Toast.LENGTH_SHORT).show()
                                    loadingBar?.dismiss()
                                }
                            }
                } else {
                    val message = task.exception?.toString()
                    Toast.makeText(this@LoginActivity, "Error :$message", Toast.LENGTH_SHORT).show()
                    loadingBar?.dismiss()
                }
            }
        }
    }

    private fun handleOnClick() {
        loadingBar = ProgressDialog(this)
        need_new_account_link.setOnClickListener { sendUserToRegisterActivity() }
        login_button.setOnClickListener { AllowUserToLogin() }
        phone_login_button.setOnClickListener {
            val phoneLoginIntent = Intent(this@LoginActivity, PhoneLoginActivity::class.java)
            startActivity(phoneLoginIntent)
        }
    }


    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        startActivity(mainIntent)
        finish()
    }

    private fun sendUserToRegisterActivity() {
        val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(registerIntent)
    }
}
