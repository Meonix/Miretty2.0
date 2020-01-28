package com.github.meonix.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

import java.util.concurrent.TimeUnit

class PhoneLoginActivity : AppCompatActivity() {


    private var SendVerificationCodeButton: Button? = null
    private var Verifybutton: Button? = null
    private var InputPhoneNumber: EditText? = null
    private var InputVerificationCode: EditText? = null
    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null

    private var mAuth: FirebaseAuth? = null
    private var loadingBar: ProgressDialog? = null

    private var mVerificationId: String? = null
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)

        mAuth = FirebaseAuth.getInstance()


        SendVerificationCodeButton = findViewById<View>(R.id.send_ver_code_button) as Button
        Verifybutton = findViewById<View>(R.id.send_verify_button) as Button
        InputPhoneNumber = findViewById<View>(R.id.phone_number_input) as EditText
        InputVerificationCode = findViewById<View>(R.id.verification_code_input) as EditText
        loadingBar = ProgressDialog(this)
        SendVerificationCodeButton!!.setOnClickListener {
            SendVerificationCodeButton!!.visibility = View.INVISIBLE
            InputPhoneNumber!!.visibility = View.INVISIBLE

            Verifybutton!!.visibility = View.VISIBLE
            InputPhoneNumber!!.visibility = View.VISIBLE


            val phoneNumber = InputPhoneNumber!!.text.toString()
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this@PhoneLoginActivity, "Phone number is required...", Toast.LENGTH_SHORT).show()

            } else {
                loadingBar!!.setTitle("Phone Verification")
                loadingBar!!.setMessage("Please wait,while we are authenticating your phone...")
                loadingBar!!.setCanceledOnTouchOutside(false)
                loadingBar!!.show()
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber, // Phone number to verify
                        60, // Timeout duration
                        TimeUnit.SECONDS, // Unit of timeout
                        this@PhoneLoginActivity, // Activity (for callback binding)
                        callbacks!!)        // OnVerificationStateChangedCallbacks
            }
        }



        Verifybutton!!.setOnClickListener {
            SendVerificationCodeButton!!.visibility = View.INVISIBLE
            InputPhoneNumber!!.visibility = View.INVISIBLE

            val verificationCoce = InputVerificationCode!!.text.toString()

            if (TextUtils.isEmpty(verificationCoce)) {
                Toast.makeText(this@PhoneLoginActivity, "Please write verification code first....", Toast.LENGTH_SHORT).show()
            } else {

                loadingBar!!.setTitle("Verification Code")
                loadingBar!!.setMessage("Please wait,while we are verifying verification code...")
                loadingBar!!.setCanceledOnTouchOutside(false)
                loadingBar!!.show()

                val credential = PhoneAuthProvider.getCredential(mVerificationId!!, verificationCoce)
                signInWithPhoneAuthCredential(credential)
            }
        }


        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                loadingBar!!.dismiss()
                Toast.makeText(this@PhoneLoginActivity, "Invalid Phone Number,Please enter correct phone number with your country code...", Toast.LENGTH_SHORT).show()
                SendVerificationCodeButton!!.visibility = View.VISIBLE
                InputPhoneNumber!!.visibility = View.VISIBLE

                Verifybutton!!.visibility = View.INVISIBLE
                InputPhoneNumber!!.visibility = View.INVISIBLE

            }

            override fun onCodeSent(verificationId: String?,
                                    token: PhoneAuthProvider.ForceResendingToken?) {

                mVerificationId = verificationId
                mResendToken = token

                loadingBar!!.dismiss()

                Toast.makeText(this@PhoneLoginActivity, "Code has been sent ,pleaset check and verify..", Toast.LENGTH_SHORT).show()

                SendVerificationCodeButton!!.visibility = View.INVISIBLE
                InputPhoneNumber!!.visibility = View.INVISIBLE

                Verifybutton!!.visibility = View.VISIBLE
                InputPhoneNumber!!.visibility = View.VISIBLE

            }


        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        loadingBar!!.dismiss()
                        Toast.makeText(this@PhoneLoginActivity, "Congratulation , you're logged in successfully....", Toast.LENGTH_SHORT).show()
                        SendUerToMainActivity()
                    } else {
                        val message = task.exception!!.toString()
                        Toast.makeText(this@PhoneLoginActivity, "Error :$message", Toast.LENGTH_SHORT).show()

                    }
                }
    }

    private fun SendUerToMainActivity() {
        val mainIntent = Intent(this@PhoneLoginActivity, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }


}
