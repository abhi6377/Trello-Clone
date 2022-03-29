package com.app_dev.trelloclone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app_dev.trelloclone.R
import com.app_dev.trelloclone.models.Board
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_create_board.view.*
import kotlinx.android.synthetic.main.item_boards.view.*

open class BoardItemsAdapter(private val context: Context, private var list:ArrayList<Board>):
RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private var onClickListener:OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_boards,parent,false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
        if(holder is MyViewHolder){
            Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ss)
                    .into(holder.itemView.iv_board_image)

            holder.itemView.t_name.text=model.name
            holder.itemView.t_createdby.text="Created by: ${model.createdBy}"

            holder.itemView.setOnClickListener {
                if(onClickListener!=null)
                {
                    onClickListener!!.onClick(position,model)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener{
        fun onClick(position: Int,model:Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener
    }

    private class MyViewHolder(view:View):RecyclerView.ViewHolder(view)

}