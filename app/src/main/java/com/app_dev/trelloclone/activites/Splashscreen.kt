package com.app_dev.trelloclone.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.app_dev.trelloclone.R
import com.app_dev.trelloclone.firebase.firestoreClass

class Splashscreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splashscreen)

         window.setFlags(
             WindowManager.LayoutParams.FLAG_FULLSCREEN,
             WindowManager.LayoutParams.FLAG_FULLSCREEN
         )



        Handler().postDelayed({

            var currentUserId=firestoreClass().getCUrrentUserId()
            if(currentUserId.isNotEmpty())
                startActivity(Intent(this,MainActivity::class.java))
            else{
                startActivity(Intent(this, intro::class.java))
            }

            finish()
        },3300
        )
    }
}