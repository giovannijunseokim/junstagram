package com.example.junstagram.navigation.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.junstagram.R
import com.example.junstagram.databinding.ItemHomefeedBinding
import com.example.junstagram.navigation.model.ContentDataModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    var contentDataModels : ArrayList<ContentDataModel> = arrayListOf()
    var contentUidList : ArrayList<String> = arrayListOf()
    var db = FirebaseFirestore.getInstance()
    var storage = FirebaseStorage.getInstance()

    init {
        db?.collection("images")?.orderBy("timeStamp")?.addSnapshotListener { snapshot, error ->
            contentDataModels.clear()
            contentUidList.clear()
            for(ss in snapshot!!.documents){
                var item = ss.toObject(ContentDataModel::class.java)
                contentDataModels.add(item!!)
                contentUidList.add(ss.id)
            }
            notifyDataSetChanged()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ItemHomefeedBinding.inflate(layoutInflater))
    }

    class MyViewHolder(val binding: ItemHomefeedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int { return contentDataModels.size }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as MyViewHolder
        val data = contentDataModels.get(position)
        holder.binding.run {
            homeFeedProfileText.text = data.userId
            homeFeedUserName.text = data.userId
            Glide.with(holder.itemView.context).load(data.imageUri).into(holder.binding.homeFeedImage)
            homeFeedExplainText.text = data.explain
            homeFeedFavoriteCounterText.text = "좋아요 " + data.favoriteCount + "개"
            //Glide.with(holder.itemView.context).load(data.imageUri).into(holder.binding.homeFeedProfileImage)
        }
    }

}