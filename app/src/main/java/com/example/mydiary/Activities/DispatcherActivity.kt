package com.example.mydiary.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mydiary.Fragments.AccountFragment
import com.example.mydiary.Fragments.DiaryFragment
import com.example.mydiary.Fragments.StatisticsFragment
import com.example.mydiary.R
import com.example.mydiary.constants.Constants
import com.example.mydiary.constants.Constants.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class DispatcherActivity : AppCompatActivity() {
    lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_passowrd)

        auth = Firebase.auth

        var activityClass :Class<*>

        val prefs = getSharedPreferences(
            sharedPreferencesStorageKey,
            MODE_PRIVATE
        )

        if(auth.currentUser == null){
            var userEmail = prefs.getString(userEmailStorageKey,null)
            var userPassword = prefs.getString(userEmailStorageKey,null)
            activityClass = if(userEmail!=null && userPassword!=null){
                auth.signInWithEmailAndPassword(userEmail,userPassword)
                HomeActivity::class.java
            }else{
                LoginActivity::class.java
            }

        }
        else{
            activityClass = HomeActivity::class.java
        }

        startActivity(Intent(this, activityClass))
        finish()
    }

}
