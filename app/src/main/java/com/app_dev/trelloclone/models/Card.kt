package com.app_dev.trelloclone.models

import android.os.Parcel
import android.os.Parcelable

data class Card (
    var name:String="",
    val createdBy:String="",
    val assignedTo:ArrayList<String> = ArrayList(),
    var labelColor:String="",
    var dueDate:Long=0
        ):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
            parcel.readLong()

    )

    override fun describeContents()=0

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel){
        parcel.writeString(name)
        parcel.writeString(createdBy)
        parcel.writeStringList(assignedTo)
        parcel.writeString(labelColor)
        writeLong(dueDate)
    }

    companion object{
        @JvmField
        val CREATOR: Parcelable.Creator<Card> = object : Parcelable.Creator<Card>{
            override fun createFromParcel(p0: Parcel): Card = Card(p0)
            override fun newArray(p0: Int): Array<Card?> = arrayOfNulls(p0)
        }

    }

}