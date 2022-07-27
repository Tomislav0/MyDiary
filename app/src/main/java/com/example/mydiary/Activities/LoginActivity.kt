package com.example.mydiary.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import com.example.mydiary.R

import com.example.mydiary.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var registerTV : TextView
    lateinit var forgotPasswordTV:TextView
    lateinit var emailET : EditText
    lateinit var passwordET : EditText
    lateinit var loginBtn : Button
    lateinit var progressBar : ProgressBar

    lateinit var Auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        registerTV  = findViewById(R.id.registerLoginTV)
        forgotPasswordTV = findViewById(R.id.forgotPasswordTV)
        emailET = findViewById(R.id.emailLoginET)
        passwordET = findViewById(R.id.passwordLoginET)
        loginBtn = findViewById(R.id.submitLoginBtn)
        progressBar = findViewById(R.id.progressLoginPB)

        Auth = Firebase.auth

        registerTV.setOnClickListener(this)
        forgotPasswordTV.setOnClickListener(this)
        loginBtn.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.registerLoginTV ->{
                val intent = Intent(this, RegistrationActivity::class.java)
                startActivity(intent)
            }
            R.id.forgotPasswordTV ->{
                val intent = Intent(this, ForgotPasswordActivity::class.java)
                startActivity(intent)
            }
            R.id.submitLoginBtn ->{
                loginSubmit()
            }
        }
    }

    private fun loginSubmit() {
        var email = emailET.text.toString().trim()
        var password = passwordET.text.toString().trim()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailET.setError("Email"+ Constants.isInvalidErrorMessage)
            emailET.requestFocus()
            return
        }

        if(password.isEmpty()){
            passwordET.setError("Password"+ Constants.isRequiredErrorMessage)
            passwordET.requestFocus()
            return
        }

        progressBar.visibility = View.VISIBLE

        Auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
            if(it.isSuccessful){
                var user = Auth.currentUser
                if(user!!.isEmailVerified){
                    Toast.makeText(this,"Successfully authenticated!",Toast.LENGTH_LONG).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    val prefs = getSharedPreferences(
                        Constants.sharedPreferencesStorageKey,
                        MODE_PRIVATE
                    )
                    val editor = prefs?.edit()

                    editor?.putString(Constants.userEmailStorageKey,email)
                    editor?.putString(Constants.userPasswordStorageKey,password)

                    editor?.commit()


                    startActivity(intent)
                }
                else{
                    Toast.makeText(this,"Please check your email to confirm registration!",Toast.LENGTH_LONG).show()
                }
                progressBar.visibility = View.INVISIBLE
            }else{
                Toast.makeText(this,"Failed to sign in!\nPlease check your credentials.",Toast.LENGTH_LONG).show()
                progressBar.visibility = View.INVISIBLE
            }
        }

    }
}