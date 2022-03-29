package com.app_dev.trelloclone.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.app_dev.trelloclone.R
import com.app_dev.trelloclone.firebase.firestoreClass
import com.app_dev.trelloclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.sql.Struct

class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        setupActionBar()
    }


    fun userRegisteredSuccess(){

        Toast.makeText(
                this,
                "Registered Successfully.Verification link sent to your Email.",
                Toast.LENGTH_LONG
        ).show()

        startActivity(Intent(this, LoginActivity::class.java))
        //Toast.makeText(this,"You have registered successfully",Toast.LENGTH_SHORT).show()
        hideProgressBar()

        finish()

    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_signup_activity)

        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.go_back)
        }

        toolbar_signup_activity.setNavigationOnClickListener {
            onBackPressed()
        }

        sign_up_button.setOnClickListener {
            registeruser()
        }
    }

    private  fun validateForm(name:String,email:String,password:String):Boolean
    {

        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                false
            }

            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter a Email")
                false
            }

            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            }
            else->{
                true
            }
        }

    }


    private fun registeruser()
    {
        val name:String=signup_name.text.toString()
        val email:String=signup_email.text.toString()
        val password:String=signup_password.text.toString()

        if(validateForm(name,email,password)) {


            showProgessDialog("Please Wait..")

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    //hideProgressBar()

                    if (task.isSuccessful) {

                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registerEmail = firebaseUser.email!!

                        val user=User(firebaseUser.uid,name,registerEmail)
                        firestoreClass().registerUser(this@SignUpActivity,user)

                        firebaseUser.sendEmailVerification()

                        signup_name.setText("")
                        signup_email.setText("")
                        signup_password.setText("")







                    } else {
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()

                    }
                }
        }


        //Toast.makeText(this,"Register successful",Toast.LENGTH_SHORT).show()



    }



}