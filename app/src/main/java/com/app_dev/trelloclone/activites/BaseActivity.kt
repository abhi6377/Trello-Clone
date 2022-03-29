package com.app_dev.trelloclone.activites

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.app_dev.trelloclone.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.dialog_progress.*

open class BaseActivity : AppCompatActivity() {

    private  var doubleBackToExitPressedOnce=false
    private  lateinit var mProgessDialogue:Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

     fun showProgessDialog(text:String){
         mProgessDialogue= Dialog(this)

         mProgessDialogue.setContentView(R.layout.dialog_progress)
         mProgessDialogue.tv_progress_text.text=text
         mProgessDialogue.show()
     }

    fun hideProgressBar()
    {
        mProgessDialogue.dismiss()
    }

    fun getcurrentuserid():String{
        return  FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubleBacktoexit(){
        if(doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce=true
        Toast.makeText(this,resources.getString(R.string.please_click_back_again_to_exit),
        Toast.LENGTH_SHORT).show()

//        if user presses the back button within 2 sec
        Handler().postDelayed({doubleBackToExitPressedOnce=false},2000)

    }


    //to display an error message at the bottom:snackbar
    fun showErrorSnackBar(message:String)
    {
        val snackBar=Snackbar.make(findViewById(android.R.id.content),message,Snackbar.LENGTH_LONG)

        val snackBarview=snackBar.view
        snackBarview.setBackgroundColor(ContextCompat.getColor(this,R.color.snackbar_color))

        snackBar.show()
    }

}