package com.app_dev.trelloclone.activites

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app_dev.trelloclone.R
import com.app_dev.trelloclone.adapter.BoardItemsAdapter
import com.app_dev.trelloclone.firebase.firestoreClass
import com.app_dev.trelloclone.models.Board
import com.app_dev.trelloclone.models.User
import com.app_dev.trelloclone.utils.Constants
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.item_boards.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE:Int=11
        const val CREATE_BOARD_REQUEST_CODE:Int=12
    }

    private lateinit var mUserName:String
    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab_create_board.setOnClickListener {
            val intent=Intent(this,CreateBoard::class.java)

            intent.putExtra(Constants.NAME,mUserName)

            //getResult2.launch(Intent(this,CreateBoard::class.java))
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

        setUpActionBar()
        nav_view.setNavigationItemSelectedListener(this)

        mSharedPreferences=this.getSharedPreferences(Constants.TRELLO_PREFERENCES,Context.MODE_PRIVATE)
         val tokenUpdated=mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)

        if(tokenUpdated)
        {
            showProgessDialog("Please Wait..")
            firestoreClass().signInUser(this,true)
        }else {
            FirebaseInstallations.getInstance()
                .getToken(true).addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                    updateFCMToken(instanceIdResult.token)
                }
        }


        firestoreClass().signInUser(this,true)


    }


    fun populate_boardListToUI(boardList:ArrayList<Board>){

       hideProgressBar()

        if(boardList.size>0){
            rv_boards_list.visibility=View.VISIBLE
            no_boards_available.visibility=View.GONE

            rv_boards_list.layoutManager=LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)

            val adapter=BoardItemsAdapter(this,boardList)
            rv_boards_list.adapter=adapter
            //Log.i("POPUI:", "Board adapter size: ${adapter.itemCount}")

            adapter.setOnClickListener(object :BoardItemsAdapter.OnClickListener{
                override fun onClick(position:Int,model:Board){

                    val intent=Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)

                    startActivity(intent)
                }
            })

        }else{
            rv_boards_list.visibility=View.GONE
            no_boards_available.visibility=View.VISIBLE
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }

    }

    private fun toggleDrawer() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            doubleBacktoexit()
        }
    }

    fun updateNavigationUserDetails(user:User,readBoardsList:Boolean) {
        hideProgressBar()
        mUserName=user.name

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_user_image)

        tv_username.text=user.name

        if(readBoardsList){
            showProgessDialog("Please Wait..")
            firestoreClass().getBoardList(this)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode== MY_PROFILE_REQUEST_CODE){
            firestoreClass().signInUser(this)
        }else if(resultCode== Activity.RESULT_OK && requestCode== CREATE_BOARD_REQUEST_CODE){
            firestoreClass().getBoardList(this)
        }
        else{
            Log.e("Cancelled","Cancelled")
        }
    }




    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                //Toast.makeText(this, "my profile", Toast.LENGTH_SHORT).show()
                startActivityForResult(Intent(this,MyProfile::class.java), MY_PROFILE_REQUEST_CODE)

            //                getResult.launch(Intent(this,MyProfile::class.java))
            }

            R.id.nav_sign_out -> {
//               FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, intro::class.java)
                AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Sign Out")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->

                        FirebaseAuth.getInstance().signOut()
                        mSharedPreferences.edit().clear().apply()

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }).setNegativeButton("No", null).show()

            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)

        return  true
    }

    fun tokenUpdateSuccess(){
        hideProgressBar()
        val editor:SharedPreferences.Editor=mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()
        showProgessDialog("Please Wait..")
        firestoreClass().signInUser(this,true)
    }

    private fun updateFCMToken(token:String){
        val userHashMap= HashMap<String,Any>()
        userHashMap[Constants.FCM_TOKEN]=token
        showProgessDialog("Please Wait..")
        firestoreClass().updateUserProfileData(this,userHashMap)
    }

}
