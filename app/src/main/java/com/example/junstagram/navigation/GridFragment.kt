package com.example.junstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.junstagram.MyApplication
import com.example.junstagram.R
import com.example.junstagram.navigation.model.ContentDataModel
import kotlinx.android.synthetic.main.fragment_grid.view.*

class GridFragment : Fragment(){
    lateinit var fragmentView : View

    override fun onCreateView(
        inflater: LayoutInflater ,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_grid, container, false)

        fragmentView.gridFragmentRecyclerView.adapter = GridAdapter()

        val manager = GridLayoutManager(activity,3)
        fragmentView.gridFragmentRecyclerView.layoutManager = manager

        return fragmentView
    }

    inner class GridAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDataModels : ArrayList<ContentDataModel> = arrayListOf()

        init {
            MyApplication.db.collection("images")?.addSnapshotListener{
                    querySnapshot, db ->
                if(querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents){
                    contentDataModels.add(snapshot.toObject(ContentDataModel::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val width = fragmentView.resources.displayMetrics.widthPixels / 3
            val imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHoler(imageView)
        }

        inner class CustomViewHoler(var imageView: ImageView) : RecyclerView.ViewHolder(imageView) {}

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val imageView = (holder as CustomViewHoler).imageView
            Glide.with(holder.itemView.context).load(contentDataModels[position].imageUri)
                .apply(RequestOptions().centerCrop()).into(imageView)
        }

        override fun getItemCount(): Int {
            return contentDataModels.size
        }
    }
}