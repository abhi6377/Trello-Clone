package com.app_dev.trelloclone.activites

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.app_dev.trelloclone.R
import com.app_dev.trelloclone.adapter.TaskListItemAdapter
import com.app_dev.trelloclone.firebase.firestoreClass
import com.app_dev.trelloclone.models.Board
import com.app_dev.trelloclone.models.Card
import com.app_dev.trelloclone.models.Task
import com.app_dev.trelloclone.models.User
import com.app_dev.trelloclone.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_task_list.*

class TaskListActivity :BaseActivity() {

    private lateinit var mBoardDetails:Board
    private lateinit var mBoarddocumentId:String
    lateinit var mAssignedMemberDetails:ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoarddocumentId=intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        //Toast.makeText(this,"board id: ${boardDocumentId}",Toast.LENGTH_SHORT).show()

        showProgessDialog("Please Wait..")
        firestoreClass().getBoardDetails(this,mBoarddocumentId)
    }

//    override fun onResume() {
//        showProgessDialog("Please Wait..")
//        firestoreClass().getBoardDetails(this,mBoarddocumentId)
//        super.onResume()
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode== MEMBERS_REQUEST_CODE || requestCode== CARD_DETAILS_REQUEST_CODE){
            showProgessDialog("Please Wait..")
            firestoreClass().getBoardDetails(this,mBoarddocumentId)
        }

        else{
            Log.e("Cancelled","Cancelled")
        }
    }

    fun cardDetails(taskListPosition:Int, cardPosition:Int){
        val intent=Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMemberDetails)

        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_members->{

                val intent=Intent(this,MembersAcitivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar(title:String)
    {
        setSupportActionBar(toolbar_board)

        val actionBar=supportActionBar

        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.go_back)

        }
        actionBar!!.title=title
        //Toast.makeText(this,"board name: ${title}",Toast.LENGTH_SHORT).show()

        toolbar_board.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun boardDetails(board:Board){
        mBoardDetails=board

        hideProgressBar()
        setupActionBar(board.name)

        showProgessDialog("Please Wait..")
        firestoreClass().getAssignedMenbersListDetails(this,mBoardDetails.assignedTo)

    }

    fun addUpdateTaskListSuccess(){

        firestoreClass().getBoardDetails(this,mBoardDetails.documentId)
    }

    fun createTaskList(taskListName:String){
        val task=Task(taskListName,firestoreClass().getCUrrentUserId())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgessDialog("Please Wait..")
        firestoreClass().addUpdateTaskList(this,mBoardDetails)

    }

    fun updateTaskList(position:Int,listName:String,model:Task){
        val task=Task(listName,model.createdBy)

        mBoardDetails.taskList[position]=task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgessDialog("Please Wait..")
        firestoreClass().addUpdateTaskList(this,mBoardDetails)

    }

    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgessDialog("Please Wait..")
        firestoreClass().addUpdateTaskList(this,mBoardDetails)

    }

    fun addCardtoTaskList(position: Int,cardName:String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        val cardAssignedUsersList:ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(firestoreClass().getCUrrentUserId())

        val card = Card(cardName, firestoreClass().getCUrrentUserId(),cardAssignedUsersList)

        val cardsList=mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )

        mBoardDetails.taskList[position]=task
        showProgessDialog("Please Wait..")
        firestoreClass().addUpdateTaskList(this,mBoardDetails)

    }

    fun boardMemberdetailsList(list:ArrayList<User>){
        mAssignedMemberDetails=list
        hideProgressBar()

        val addTaskList=Task("Add List")
        mBoardDetails.taskList.add(addTaskList)

        rv_task_list.layoutManager=LinearLayoutManager(
                this,LinearLayoutManager.HORIZONTAL,false)
        rv_task_list.setHasFixedSize(true)

        val adapter=TaskListItemAdapter(this,mBoardDetails.taskList)
        rv_task_list.adapter=adapter


    }

    fun updateCardsIntTaskList(taskListPosition: Int,cards:ArrayList<Card>){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        mBoardDetails.taskList[taskListPosition].cards=cards
        showProgessDialog("Please Wait..")
        firestoreClass().addUpdateTaskList(this,mBoardDetails)

    }

    companion object{
        const val MEMBERS_REQUEST_CODE:Int=13
        const val CARD_DETAILS_REQUEST_CODE:Int=14
    }

}