package com.app_dev.trelloclone.activites

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.app_dev.trelloclone.R
import com.app_dev.trelloclone.adapter.CardListItemAdapter
import com.app_dev.trelloclone.adapter.CardMemberListItemsAdapter
import com.app_dev.trelloclone.dialogs.LabelColorListDialog
import com.app_dev.trelloclone.dialogs.MembersListDialog
import com.app_dev.trelloclone.firebase.firestoreClass
import com.app_dev.trelloclone.models.*
import com.app_dev.trelloclone.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_my_profile.toolbar_my_profile
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails:Board
    private var mTaskListPosition=-1
    private var mCardPosition=-1
    private var mSelectedColor=""
    private lateinit var mMembersDetailsList:ArrayList<User>
    private var mSelectedDueDateMilliSeconds:Long=0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentdata()
        setupActionBar()
        et_name_card_details.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)

        et_name_card_details.setSelection(et_name_card_details.text.toString().length)


        mSelectedColor= mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }

        btn_update_card_details.setOnClickListener {
            if(et_name_card_details.text.toString().isNotEmpty())
                updateCardDetails()
            else
                Toast.makeText(this,"Please enter a card name",Toast.LENGTH_SHORT).show()
        }

        tv_select_label_color.setOnClickListener {
            labelColorsListDialog()
        }

        tv_select_members.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()

        mSelectedDueDateMilliSeconds=mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate

        if(mSelectedDueDateMilliSeconds >0){
            val simpleDateFormat=SimpleDateFormat("dd//MM//yyyy",Locale.ENGLISH)
            val selectedDate=simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            tv_select_due_date.text=selectedDate
        }

        tv_select_due_date.setOnClickListener {
            showDataPicker()
        }

    }

    fun addUpdateTaskListSuccess(){
        hideProgressBar()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_card_details_activity)

        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.go_back)
            actionBar.title=mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
        }

        toolbar_card_details_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun colorsList():ArrayList<String>{
        val colorList:ArrayList<String> = ArrayList()
        colorList.add("#43C86F")
        colorList.add("#0C90F1")
        colorList.add("#F72400")
        colorList.add("#7A8089")
        colorList.add("#D57C1D")
        colorList.add("#770000")
        colorList.add("#0022F8")

        return colorList
    }

    private fun setColor(){
        tv_select_label_color.text=""
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_delete_card->{
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getIntentdata(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails=intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }

        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition=intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }

        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition=intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)


        }

        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailsList=intent.getParcelableArrayListExtra(
                Constants.BOARD_MEMBERS_LIST
            )!!
        }

    }

    private fun membersListDialog(){
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        if (cardAssignedMembersList.size > 0) {

              for (i in mMembersDetailsList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMembersDetailsList[i].id == j) {
                        mMembersDetailsList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mMembersDetailsList.indices) {
                mMembersDetailsList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(
            this@CardDetailsActivity,
            mMembersDetailsList,
            "Select Member"
        ) {
            override fun onItemSelected(user: User, action: String) {

                if (action == Constants.SELECT) {
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(
                            user.id
                        )
                    ) {
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(
                            user.id
                        )
                    }
                } else {
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(
                        user.id
                    )

                    for (i in mMembersDetailsList.indices) {
                        if (mMembersDetailsList[i].id == user.id) {
                            mMembersDetailsList[i].selected = false
                        }
                    }
                }

                setupSelectedMembersList()
            }
        }
        listDialog.show()

    }

    private fun setupSelectedMembersList() {

        // Assigned members of the Card.
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        // A instance of selected members list.
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        // Here we got the detail list of members and add it to the selected members list as required.
        for (i in mMembersDetailsList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMembersDetailsList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        mMembersDetailsList[i].id,
                        mMembersDetailsList[i].image
                    )

                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {

            // This is for the last item to show.
            selectedMembersList.add(SelectedMembers("", ""))

            tv_select_members.visibility = View.GONE
            rv_selected_members_list.visibility = View.VISIBLE

            rv_selected_members_list.layoutManager = GridLayoutManager(this@CardDetailsActivity, 6)
            val adapter =
                CardMemberListItemsAdapter(this@CardDetailsActivity, selectedMembersList, true)
            rv_selected_members_list.adapter = adapter
            adapter.setOnClickListener(object :
                CardMemberListItemsAdapter.OnClickListener {
                override fun onClick() {
                    membersListDialog()
                }

            })
        } else {
            tv_select_members.visibility = View.VISIBLE
            rv_selected_members_list.visibility = View.GONE
        }
    }

    private fun updateCardDetails(){
        val card=Card(et_name_card_details.text.toString(),
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
                        mSelectedColor,mSelectedDueDateMilliSeconds
            )

        val taskList:ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition]=card

        showProgessDialog("Please Wait..")
        firestoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)

    }

    private fun alertDialogForDeleteCard(cardName:String){
        val builder= AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $cardName ?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){
                dialogueInterface,which->
            dialogueInterface.dismiss()

                this.deleteCard()

        }
        builder.setNegativeButton("No"){
                dialogueInterface,which->
            dialogueInterface.dismiss()
        }
        val alertDialog: AlertDialog =builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteCard(){
        val cardList:ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardList.removeAt(mCardPosition)

        val taskList:ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        taskList[mTaskListPosition].cards=cardList

        showProgessDialog("Please Wait..")
        firestoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)

    }

    private fun labelColorsListDialog(){
        val colorsList:ArrayList<String> = colorsList()

        val listDialog= object : LabelColorListDialog(
            this,
            colorsList,
            "Select Label Color",mSelectedColor
        ){
            override fun onItemSelected(color: String) {
                mSelectedColor=color
                setColor()
            }

        }
        listDialog.show()

    }

    private fun showDataPicker() {

        val c = Calendar.getInstance()
        val year =
                c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day


        val dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                           val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"

                         val sMonthOfYear =
                            if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                    val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"

                    tv_select_due_date.text = selectedDate

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                    val theDate = sdf.parse(selectedDate)


                    mSelectedDueDateMilliSeconds = theDate!!.time
                },
                year,
                month,
                day
        )
        dpd.show()
    }

}
