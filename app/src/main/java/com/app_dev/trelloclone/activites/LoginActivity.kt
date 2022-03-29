package com.app_dev.trelloclone.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.app_dev.trelloclone.R
import com.app_dev.trelloclone.firebase.firestoreClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.auth.User
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity() {
    private  lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth= FirebaseAuth.getInstance()

        sign_up_button.setOnClickListener {
            sign_in_user()
        }

        forgot_pwd.setOnClickListener {
            startActivity(Intent(this,Forgotpwd::class.java))
        }

        setupActionBar()

    }

    fun signInSuccess(user:com.app_dev.trelloclone.models.User)
    {
        hideProgressBar()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_login_activity)

        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.go_back)
        }

        toolbar_login_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private  fun validateForm(email:String,password:String):Boolean
    {

        return when{

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

    private fun sign_in_user()
    {
        val email:String=et_email.text.toString()
        val password:String=et_password.text.toString()

        if(validateForm(email,password))
        {
            showProgessDialog("Please Wait..")
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this){
                task->
                hideProgressBar()
                if(task.isSuccessful&& auth.currentUser!!.isEmailVerified){
                    Log.d("Signin","SignInWithemail:Success")
                    val user=auth.currentUser

                    firestoreClass().signInUser(this)

                }
                else if(!auth.currentUser!!.isEmailVerified){
                    Toast.makeText(this,"Please verify your Email first",Toast.LENGTH_SHORT).show()
                }

                else{
                    Log.w("Signin","SignInWithEmail:Failure",task.exception)
                    Toast.makeText(this,"Authentication Failed",Toast.LENGTH_SHORT).show()
                }
            }



        }
    }

}