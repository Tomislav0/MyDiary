package com.example.mydiary.Activities

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import com.example.mydiary.R
import com.example.mydiary.constants.Constants.*
import com.example.mydiary.helpers.DateHelper
import com.example.mydiary.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity(), View.OnClickListener{
    @Inject
    lateinit var dateHelper:DateHelper

    private lateinit var auth: FirebaseAuth;
    private lateinit var database :FirebaseDatabase

    lateinit var registerBtn : Button
    lateinit var spinner :Spinner
    lateinit var dateTV : TextView
    lateinit var loginTV : TextView
    lateinit var nameET : EditText
    lateinit var emailET :EditText
    lateinit var passwordET :EditText
    lateinit var passwordRepeatET :EditText
    lateinit var progressBar : ProgressBar
    lateinit var calendar: Calendar

    var year by Delegates.notNull<Int>()
    var month by Delegates.notNull<Int>()
    var day by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance(BASE_URL)

        progressBar = findViewById(R.id.progressRegisterPB)
        registerBtn = findViewById(R.id.submitRegisterBtn)
        spinner = findViewById(R.id.genderSpinner)
        dateTV = findViewById(R.id.dateTV)
        loginTV = findViewById(R.id.loginRegistrationTV)
        nameET = findViewById(R.id.nameRegistrationET)
        emailET = findViewById(R.id.emailRegistrationET)
        passwordET = findViewById(R.id.firstPasswordRegstrationET)
        passwordRepeatET = findViewById(R.id.repeatPasswordRegstrationET)

        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)
        ArrayAdapter.createFromResource(
            this,
            R.array.genders,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        dateTV.setOnClickListener(this)
        loginTV.setOnClickListener(this)
        registerBtn.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.loginRegistrationTV ->{
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            R.id.dateTV ->{
                val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, myear, mmonth, mdayOfMonth ->
                    var fixedmonth = mmonth+1
                    dateTV.setText( mdayOfMonth.toString() + "."+ fixedmonth.toString() + "." + myear.toString() + "." )
                    year = myear
                    month = mmonth
                    day = mdayOfMonth
                    calendar.set(myear,mmonth,mdayOfMonth)
                }, year, month, day)
                datePickerDialog.show()
            }
            R.id.submitRegisterBtn ->{
                registerSubmit()
            }

        }
    }

    private fun registerSubmit(){
        var name = nameET.text.toString()
        var email = emailET.text.toString().trim()
        var date = dateHelper.convertToStringFormat(calendar.time)
        var gender = spinner.selectedItem.toString()
        var passwordFirst = passwordET.text.toString().trim()
        var passwordSecond = passwordRepeatET.text.toString().trim()

        if(name.isEmpty()){
            nameET.setError("Name"+ isRequiredErrorMessage)
            nameET.requestFocus()
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailET.setError("Email"+ isInvalidErrorMessage)
            emailET.requestFocus()
            return
        }
        if(date.isEmpty()){
            dateTV.setError("Date"+ isRequiredErrorMessage)
            dateTV.requestFocus()
            return
        }else{
            dateTV.error = null
        }

        if(passwordFirst.isEmpty()){
            passwordET.setError("Password"+ isRequiredErrorMessage)
            passwordET.requestFocus()
            return
        }
        if(passwordSecond.isEmpty()){
            passwordRepeatET.setError("Repeat password"+ isRequiredErrorMessage)
            passwordRepeatET.requestFocus()
            return
        }
        if(passwordFirst.length < 6){
            passwordET.setError(passwordErrorMessage)
            passwordET.requestFocus()
            return
        }
        if(!passwordSecond.equals(passwordFirst)){
            passwordRepeatET.setError(passwordsNotMatchErrorMessage)
            passwordRepeatET.requestFocus()
            return
        }

        progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email,passwordFirst)
            .addOnCompleteListener{
                if(it.isSuccessful) {
                    var user = User(name, email, gender, date, passwordFirst)
                    database.getReference("Users")
                        .child(auth.currentUser!!.uid)
                        .setValue(user)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                var currUser = auth.currentUser
                                currUser!!.sendEmailVerification()
                                Toast.makeText(this, registerSuccessfulMessage, Toast.LENGTH_LONG)
                                   .show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, registerErrorMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }
                            progressBar.visibility = View.INVISIBLE
                        }

                }else{
                    Toast.makeText(this, registerErrorMessage, Toast.LENGTH_SHORT)
                        .show()
                    progressBar.visibility = View.INVISIBLE
                }
            }
    }


}