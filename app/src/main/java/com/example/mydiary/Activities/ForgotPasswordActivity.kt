package com.example.mydiary.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import com.example.mydiary.R
import com.example.mydiary.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var auth:FirebaseAuth

    lateinit var loginTV : TextView
    lateinit var emailET : EditText
    lateinit var resetPasswordBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_passowrd)

        auth = Firebase.auth

        loginTV = findViewById(R.id.loginForgotPasswordTV)
        emailET = findViewById(R.id.emailForgotPasswordET)
        resetPasswordBtn = findViewById(R.id.submitResetPasswordBtn)

        loginTV.setOnClickListener(this)
        resetPasswordBtn.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.loginForgotPasswordTV ->{
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            R.id.submitResetPasswordBtn ->{
                submitResetPassword()
            }
        }
    }

    private fun submitResetPassword() {
        var email = emailET.text.toString()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailET.setError("Email"+ Constants.isInvalidErrorMessage)
            emailET.requestFocus()
            return
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener{it->

            if(it.isSuccessful){
                Toast.makeText(this,"Please check your email.",Toast.LENGTH_LONG).show()
                var intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }else{
                emailET.text = null
                Toast.makeText(this,"Invalid email. Please check your credentials.",Toast.LENGTH_LONG).show()
            }
        }
    }

}