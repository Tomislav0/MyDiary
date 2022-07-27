package com.example.mydiary.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mydiary.Activities.LoginActivity
import com.example.mydiary.R
import com.example.mydiary.constants.Constants
import com.example.mydiary.constants.Constants.lastActivityStorageKey
import com.example.mydiary.models.NoteBM
import com.example.mydiary.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class AccountFragment : Fragment(R.layout.fragment_account), View.OnClickListener {
    lateinit var auth: FirebaseAuth
    lateinit var database : FirebaseDatabase

    lateinit var gson : Gson

    lateinit var logoutBtn: Button
    lateinit var emailET :EditText
    lateinit var nameET :EditText
    lateinit var saveBtn : Button

    lateinit var fetchedUser:User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance(Constants.BASE_URL)

        gson = Gson()
        logoutBtn= view.findViewById(R.id.logoutBtn)
        emailET= view.findViewById(R.id.emailAccountET)
        nameET= view.findViewById(R.id.nameAccountET)
        saveBtn= view.findViewById(R.id.accountSubmitBtn)

        logoutBtn.setOnClickListener(this)
        saveBtn.setOnClickListener(this)

        fetchUserData()
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.logoutBtn ->{

                auth.signOut()

                val intent = Intent(activity, LoginActivity::class.java)
                activity?.startActivity(intent)
                activity?.finish()
            }
            R.id.accountSubmitBtn -> {
                saveUserData()
            }
        }

    }

    private fun saveUserData() {
        val name = nameET.text.toString()
        if(name.isEmpty()){
            Toast.makeText(context,"Name input cannot be empty.",Toast.LENGTH_SHORT).show()
            return
        }
        fetchedUser = User(name,fetchedUser.email,fetchedUser.gender,fetchedUser.date,fetchedUser.password)

        database.getReference("Users").child(auth.currentUser!!.uid).setValue(fetchedUser).addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context,"Data successfully updated.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserData() {
        database.getReference("Users").child(auth.currentUser!!.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val toJson = gson.toJson(it.result.value)
                fetchedUser = gson.fromJson(toJson, User::class.java)
                emailET.setText(fetchedUser.email)
                nameET.setText(fetchedUser.name)
            }
        }
    }


}