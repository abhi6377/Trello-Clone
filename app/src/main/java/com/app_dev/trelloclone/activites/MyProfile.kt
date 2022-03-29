package com.app_dev.trelloclone.activites

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app_dev.trelloclone.R
import com.app_dev.trelloclone.firebase.firestoreClass
import com.app_dev.trelloclone.models.User
import com.app_dev.trelloclone.utils.Constants
import com.app_dev.trelloclone.utils.Constants.PICK_IMAGE_REQUEST_CODE
import com.app_dev.trelloclone.utils.Constants.READ_STORAGE_PERMISSION_CODE

import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.io.IOException

class MyProfile :BaseActivity() {



    private var mSelectedImageFileUri: Uri?=null
    private var mProfileImageURL:String=""
    private lateinit var mUserDetails:User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        firestoreClass().signInUser(this)
        setupActionBar()

        my_profile_image.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
            {
                Constants.showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_CODE)
            }
        }

        update_button.setOnClickListener {
            if(mSelectedImageFileUri!=null){
                uploadUserImage()
            }else{
                showProgessDialog("Please Wait..")
                updateUserProfileData()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if(requestCode== Constants.READ_STORAGE_PERMISSION_CODE){
                if(grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Constants.showImageChooser(this)
                }
            }else{
                Toast.makeText(this,"Oops! You just denied the permission for storage.You can allow it from settings.",Toast.LENGTH_SHORT).show()

            }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode== Constants.PICK_IMAGE_REQUEST_CODE &&data!!.data!=null) {
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(my_profile_image)

            }catch (e:IOException){
                e.printStackTrace()
            }
        }
    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_my_profile)

        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.go_back)
        }

        toolbar_my_profile.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    fun update_profile_details(user: User) {
        mUserDetails=user

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(my_profile_image)

        my_profile_email.setText(user.email)
        My_profile_name.setText(user.name)

        if(user.mobile!=0L){
            my_profile_mobile.setText(user.mobile.toString())
        }



//        update_button.setOnClickListener {
//            user.name=My_profile_name.text.toString()
//
//        }

    }

    private fun updateUserProfileData(){
        val userHashMap=HashMap<String,Any>()

        var anychangesMade=false

        if(mProfileImageURL.isNotEmpty() && mProfileImageURL!=mUserDetails.image){
            userHashMap[Constants.IMAGE]=mProfileImageURL
            anychangesMade=true
        }

        if(My_profile_name.text.toString()!=mUserDetails.name){
            userHashMap[Constants.NAME]=My_profile_name.text.toString()
            anychangesMade=true
        }

        if(my_profile_mobile.text.toString()!=mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE]=my_profile_mobile.text.toString().toLong()
            anychangesMade=true
        }

        if(anychangesMade) {
            firestoreClass().updateUserProfileData(this, userHashMap)

        }


    }

    private  fun uploadUserImage(){
        showProgessDialog("Please Wait..")

        if(mSelectedImageFileUri!=null)
        {
            val sRef:StorageReference=FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE"+System.currentTimeMillis()
                        +"."+Constants.getFileExtension(this,mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot->
                Log.e(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                    Log.i("Downloadable Image URL",uri.toString())
                    mProfileImageURL=uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener {
                exception->
                Toast.makeText(this,exception.message,Toast.LENGTH_LONG).show()
                hideProgressBar()
            }

        }
    }



    fun profileUpdateSuccess(){
        hideProgressBar()

        setResult(Activity.RESULT_OK)

        finish()
    }

}