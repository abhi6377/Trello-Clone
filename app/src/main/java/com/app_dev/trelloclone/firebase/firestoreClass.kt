package com.app_dev.trelloclone.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.app_dev.trelloclone.activites.*
import com.app_dev.trelloclone.models.Board
import com.app_dev.trelloclone.models.User
import com.app_dev.trelloclone.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.lang.reflect.Array.set

open class firestoreClass {

    private  val mfireStore=FirebaseFirestore.getInstance()

    fun registerUser(activity:SignUpActivity,userInfo: User){

        mfireStore.collection(Constants.USERS)
                .document(getCUrrentUserId()).set(userInfo, SetOptions.merge())
                .addOnSuccessListener {
                    activity.userRegisteredSuccess()
                }

    }

    fun createBoard(activity: CreateBoard,board:Board){

        mfireStore.collection(Constants.BOARDS)
            .document().set(board, SetOptions.merge())
            .addOnSuccessListener {

                Toast.makeText(activity,"Board created successfully.",Toast.LENGTH_SHORT).show()
               // Constants.DOCUMENT_ID=mfireStore.collection(Constants.BOARDS).document().id
                activity.boardCreatedSuccessfully()

            }.addOnFailureListener {
                //activity2.hideProgressBar()
                Toast.makeText(activity,"Board creation error.",Toast.LENGTH_SHORT).show()

            }
    }

    fun addUpdateTaskList(activity:Activity,board:Board){
        val taskListHashMAp=HashMap<String,Any>()
        taskListHashMAp[Constants.TASK_LIST]=board.taskList

        mfireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMAp)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,"TaskList updates successfully")
                if(activity is TaskListActivity)
                    activity.addUpdateTaskListSuccess()

                else if(activity is CardDetailsActivity)
                    activity.addUpdateTaskListSuccess()

            }.addOnFailureListener {
                exception->
                if(activity is TaskListActivity)
                    activity.hideProgressBar()
                else if(activity is CardDetailsActivity)
                    activity.hideProgressBar()
                Log.e(activity.javaClass.simpleName,"Error while creating")

            }
    }

    fun updateUserProfileData(activity:Activity,userHashMap:HashMap<String,Any>){
        mfireStore.collection(Constants.USERS).document(getCUrrentUserId()).update(userHashMap)
            .addOnSuccessListener {
                Toast.makeText(activity,"Profile updated Successfully",Toast.LENGTH_SHORT).show()

                when(activity){
                    is MainActivity->{
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfile->
                        activity.profileUpdateSuccess()
                }

            }.addOnFailureListener {
                e->

                if(activity is MyProfile)
                    activity.hideProgressBar()
                if(activity is MainActivity)
                    activity.hideProgressBar()
                Toast.makeText(activity,"Profile update error",Toast.LENGTH_SHORT).show()
            }
    }

    fun getBoardList(activity: MainActivity){
        mfireStore.collection(Constants.BOARDS)
                .whereArrayContains(Constants.ASSIGNED_TO,getCUrrentUserId())
                .get()
                .addOnSuccessListener {
                    document->
                    Log.i(activity.javaClass.simpleName,document.documents.toString())
                    val boardList:ArrayList<Board> = ArrayList()
                    for(i in document.documents){
                        val board=i.toObject(Board::class.java)!!

                            board.documentId=i.id
                            mfireStore.collection(Constants.BOARDS).document(i.id)
                                .update(mapOf(
                                    "documentId" to i.id
                                ))
//
                       // Toast.makeText(activity,"doc id:${board.documentId}",Toast.LENGTH_SHORT).show()

                        boardList.add(board)
                    }

                    activity.populate_boardListToUI(boardList)
                }.addOnFailureListener {

                    activity.hideProgressBar()
                    Toast.makeText(activity,"Recycler view error toast",Toast.LENGTH_SHORT).show()

                }
    }

    fun signInUser(activity:Activity,readBoardsList:Boolean=false){
        mfireStore.collection(Constants.USERS)
            .document(getCUrrentUserId())
            .get().addOnSuccessListener {document ->
                val loggedInUser=document.toObject(User::class.java)!!

                    when(activity){
                        is LoginActivity->{
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity->{
                            activity.updateNavigationUserDetails(loggedInUser,readBoardsList)
                        }
                        is MyProfile->{
                            activity.update_profile_details(loggedInUser)
                        }
                    }


            }.addOnFailureListener {
                e->
                when(activity){
                    is LoginActivity->{
                        activity.hideProgressBar()
                    }
                    is MainActivity->{
                        activity.hideProgressBar()
                    }
                }
            }
    }

    fun getCUrrentUserId():String{
        var currentUser=FirebaseAuth.getInstance().currentUser
          var currentUserId=""
        if(currentUser!=null)
            currentUserId=currentUser.uid
        return  currentUserId
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mfireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document->
                Log.i(activity.javaClass.simpleName,document.toString())

                activity.boardDetails(document.toObject(Board::class.java)!!)

            }.addOnFailureListener {

                activity.hideProgressBar()
                Toast.makeText(activity,"Recycler view error toast",Toast.LENGTH_SHORT).show()

            }
    }

    fun getAssignedMenbersListDetails(
        activity:Activity,assignedTo:ArrayList<String>){
        mfireStore.collection(Constants.USERS)
            .whereIn(Constants.ID,assignedTo)
            .get()
            .addOnSuccessListener {
                document->
                Log.e(activity.javaClass.simpleName,document.documents.toString())

                val usersList:ArrayList<User> = ArrayList()

                for(i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                if(activity is MembersAcitivity)
                    activity.setUpMembersList(usersList)
                else if (activity is TaskListActivity)
                    activity.boardMemberdetailsList(usersList)

            }.addOnFailureListener {
                if(activity is MembersAcitivity)
                    activity.hideProgressBar()
                else if(activity is TaskListActivity)
                    activity.hideProgressBar()

                Toast.makeText(activity,"Error.",Toast.LENGTH_SHORT).show()
            }
    }

    fun getMemberDetails(activity: MembersAcitivity,email:String){
        mfireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener {
                document->
                if(document.documents.size>0){
                    val user=document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressBar()
                    activity.showErrorSnackBar("No such member found")
                }
            }.addOnFailureListener {
                activity.hideProgressBar()
                Toast.makeText(activity,"Error while getting user details",Toast.LENGTH_SHORT).show()
            }
    }

    fun assignMemberToBoard(activity: MembersAcitivity,board:Board,user:User){

        val assignedToHashMAp=HashMap<String,Any>()
        assignedToHashMAp[Constants.ASSIGNED_TO]=board.assignedTo

        mfireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMAp)
            .addOnSuccessListener {
                activity.memberAssignedSuccess(user)
            }.addOnFailureListener {
                activity.hideProgressBar()
                Toast.makeText(activity,"Error!!",Toast.LENGTH_SHORT).show()
            }

    }


}