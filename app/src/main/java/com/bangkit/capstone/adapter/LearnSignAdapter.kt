package com.bangkit.capstone.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.capstone.R
import com.bangkit.capstone.model.LearnSign
import com.bangkit.capstone.ui.LearnSignDetailActivity

class LearnSignAdapter(private var listLearnSign: ArrayList<LearnSign>) : RecyclerView.Adapter<LearnSignAdapter.ListViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_learn_sign, parent, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int = listLearnSign.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
//        holder.imgPhoto.setImageResource(listLearnSign[position].photo)
        holder.tvLearnSign.text = listLearnSign[position].alphabet

        holder.itemView.setOnClickListener {
            val intentDetail = Intent(holder.itemView.context, LearnSignDetailActivity::class.java)
            intentDetail.putExtra(LearnSignDetailActivity.key_learn_sign, listLearnSign[holder.adapterPosition])
            holder.itemView.context.startActivity(intentDetail)
        }
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val imgPhoto: ImageView = itemView.findViewById(R.id.ivSignLaguage)
        val tvLearnSign: TextView = itemView.findViewById(R.id.learn_alphabet)
    }

//    fun setFilteredList(list: List<LearnSign>){
//        this.listLearnSign = listLearnSign
//        notifyDataSetChanged()
//
//    }
}