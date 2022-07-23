package com.example.mydiary.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.mydiary.Activities.LoginActivity
import com.example.mydiary.R
import com.example.mydiary.constants.Constants
import com.example.mydiary.constants.Constants.lastActivityStorageKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AccountFragment : Fragment(R.layout.fragment_account), View.OnClickListener {
    lateinit var auth: FirebaseAuth

    lateinit var logoutBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        logoutBtn= view.findViewById(R.id.logoutBtn)

        logoutBtn.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.logoutBtn ->{

                auth.signOut()

                val intent = Intent(activity, LoginActivity::class.java)
                activity?.startActivity(intent)
                activity?.finish()
            }
        }

    }
}