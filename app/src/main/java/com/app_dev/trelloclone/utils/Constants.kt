package com.app_dev.trelloclone.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat.startActivityForResult
import com.app_dev.trelloclone.activites.MyProfile
import com.app_dev.trelloclone.utils.Constants.PICK_IMAGE_REQUEST_CODE

object Constants {

    const val USERS: String = "users"
    const val BOARDS:String="boards"
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val ASSIGNED_TO:String="assignedTo"

    const val FCM_TOKEN_UPDATED="fcm_token_updated"
    const val FCM_TOKEN="fcmToken"
    const val TRELLO_PREFERENCES="TrelloClone_prefrences"
    const val FCM_KEY_TITLE="fcm_token_title"
    const val FCM_KEY_MESSAGE="fcm_key_message"
    const val FCM_BASE_URL="https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION="authorization"
    const val FCM_KEY:String="key"
    const val FCM_SERVER_KEY:String="AAAAXQwId0U:APA91bESoJG9qxf_m7AC3rTSoKUW0oxGAXhQax5bAxI6qdS9llVNRYoNaFtJB6Ss8j-L16A-nB0EFS7T1c9aN5Em_hvHZEZ84hGXTq-FC625Wthgr2Qe2Wtj1OgG0I8cYVdf3KvODHcu"
    const val FCM_KEY_DATA:String="data"
    const val FCM_KEY_TO:String="to"


    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2
    var DOCUMENT_ID:String="documentId"
    const val TASK_LIST:String="taskList"
    const val BOARD_DETAIL:String="board_detail"
    const val ID:String="id"
    const val EMAIL:String="email"
    const val TASK_LIST_ITEM_POSITION:String="task_list_item_position"
    const val CARD_LIST_ITEM_POSITION:String="card_list_item_position"
    const val BOARD_MEMBERS_LIST:String="board_members_list"
    const val SELECT:String="Select"
    const val UN_SELECT:String="UnSelect"


    fun showImageChooser(activity: Activity) {
        var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)


    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {

        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))

    }
}