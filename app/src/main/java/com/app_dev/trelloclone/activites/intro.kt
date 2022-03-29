package com.app_dev.trelloclone.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app_dev.trelloclone.R
import com.app_dev.trelloclone.firebase.firestoreClass
import kotlinx.android.synthetic.main.activity_intro.*

class intro : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)


        btn_sign_up.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btn_sign_in.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }


}