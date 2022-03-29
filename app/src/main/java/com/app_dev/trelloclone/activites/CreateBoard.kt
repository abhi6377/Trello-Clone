package com.app_dev.trelloclone.activites

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app_dev.trelloclone.R
import com.app_dev.trelloclone.firebase.firestoreClass
import com.app_dev.trelloclone.models.Board
import com.app_dev.trelloclone.utils.Constants
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException

class CreateBoard : BaseActivity() {
    private var mSelectedImageFileUri: Uri?=null
    private var mBoardImageURL:String=""

    private lateinit var mUserName:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()

        board_create_button.setOnClickListener {
            if(mSelectedImageFileUri!=null){
                uploadboardimage()
            }else{
                showProgessDialog("Please Wait..")
                createBoard()

            }
        }

        if(intent.hasExtra(Constants.NAME)){
            mUserName=intent.getStringExtra(Constants.NAME)!!
        }

        board_image.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
            {
                Constants.showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    private fun createBoard(){
        val assignedUserArrayList:ArrayList<String> = ArrayList()
        assignedUserArrayList.add((getcurrentuserid()))



        var board=Board(
            board_name.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUserArrayList
        )

        firestoreClass().createBoard(this,board)

    }

    private fun uploadboardimage(){
        showProgessDialog("Please Wait..")

            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE"+System.currentTimeMillis()
                        +"."+Constants.getFileExtension(this,mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapshot->
                Log.e(
                    "Board Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri->
                    Log.i("Downloadable Image URL",uri.toString())
                    mBoardImageURL=uri.toString()

                    createBoard()

                }
            }.addOnFailureListener {
                    exception->
                Toast.makeText(this,exception.message,Toast.LENGTH_LONG).show()

                hideProgressBar()
            }


    }

    fun boardCreatedSuccessfully(){
        hideProgressBar()

        setResult(Activity.RESULT_OK)
        finish()
    }


    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_create_board)

        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.go_back)
        }

        toolbar_create_board.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty()&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }
        }else{
            Toast.makeText(this,"Oops! You just denied the permission for storage.You can allow it from settings.",
                Toast.LENGTH_SHORT).show()

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
                    .placeholder(R.drawable.ss)
                    .into(board_image)

            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }



}