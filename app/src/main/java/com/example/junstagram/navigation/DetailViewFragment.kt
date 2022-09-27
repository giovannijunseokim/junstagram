package com.example.junstagram.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.junstagram.CommentActivity
import com.example.junstagram.MyApplication
import com.example.junstagram.R
import com.example.junstagram.databinding.FragmentDetailBinding
import com.example.junstagram.databinding.ItemHomefeedBinding
import com.example.junstagram.navigation.model.ContentDataModel
import com.example.junstagram.navigation.model.UserDataModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.item_homefeed.*

class DetailViewFragment : Fragment(){


    override fun onCreateView(inflater: LayoutInflater , container: ViewGroup?, savedInstanceState: Bundle?)
    : View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,
        container,false)

        val fragmentDetailBinding = FragmentDetailBinding.inflate(layoutInflater)
        val manager = LinearLayoutManager(activity)
        manager.reverseLayout = true
        manager.stackFromEnd = true

        fragmentDetailBinding.detailViewFragmentRecyclerView.adapter = DetailViewAdapter()
        fragmentDetailBinding.detailViewFragmentRecyclerView.layoutManager = manager

        return fragmentDetailBinding.root
    }
    @SuppressLint("NotifyDataSetChanged")
    inner class DetailViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDataModels : ArrayList<ContentDataModel> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()
        var userDataModel = UserDataModel()
        var uid : String? = null
        var email : String? = null

        init {
            MyApplication.db.collection("images").orderBy("timeStamp").addSnapshotListener { snapshot, error ->
                contentDataModels.clear()
                contentUidList.clear()

                if (snapshot == null) return@addSnapshotListener
                for(snapshot in snapshot.documents){
                    var item = snapshot.toObject(ContentDataModel::class.java)
                    contentDataModels.add(item!!)
                    contentUidList.add(snapshot.id)
                    notifyDataSetChanged()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            uid = FirebaseAuth.getInstance().currentUser?.uid
            return MyViewHolder(ItemHomefeedBinding.inflate(layoutInflater, parent, false))
        }

        inner class MyViewHolder(val binding: ItemHomefeedBinding) : RecyclerView.ViewHolder(binding.root)

        override fun getItemCount(): Int { return contentDataModels.size }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val holder = holder as MyViewHolder
            val data = contentDataModels.get(position)

            holder.binding.run {

                Glide.with(holder.itemView.context).load(data.imageUri).into(holder.binding.homeFeedImage)
                homeFeedExplainText.text = data.explain
                homeFeedFavoriteCounterText.text = "좋아요 " + data.favoriteCount + "개"
                //Glide.with(holder.itemView.context).load(data.imageUri).into(holder.binding.homeFeedProfileImage)
                homeFeedFavoriteIcon.setOnClickListener { favoriteEvent(position) }
                // 유저 이름 가져오기
                MyApplication.db.collection("userInformation").whereEqualTo("uid",uid)?.addSnapshotListener{
                        snapshot, error ->
                    if (snapshot == null){
                        return@addSnapshotListener
                    }
                    for (data in snapshot!!.documents) {
                        userDataModel = (data.toObject(UserDataModel::class.java)!!)
                        homeFeed_userName.text = userDataModel.nickname
                        homeFeed_profileText.text = userDataModel.nickname
                    }
                }

                if(contentDataModels!![position].favorites.containsKey(uid)){
                    // 버튼 클릭 돼있음
                    homeFeedFavoriteIcon.setImageResource(R.drawable.ic_favorite)
                }else{
                    // 버튼 클릭 안 돼있음
                    homeFeedFavoriteIcon.setImageResource(R.drawable.ic_favorite_border)
                }

                holder.binding.homeFeedProfileImage.setOnClickListener {
                    changeViewToUserFragment(position)
                }
                holder.binding.homeFeedProfileText.setOnClickListener {
                    changeViewToUserFragment(position)
                }
                holder.binding.homeFeedUserName.setOnClickListener {
                    changeViewToUserFragment(position)
                }
                holder.binding.homeFeedCommentIcon.setOnClickListener {
                    val intent = Intent(it.context, CommentActivity::class.java)
                    intent.putExtra("contentUid", contentUidList[position])
                    startActivity(intent)
                }

            }

        }

        fun favoriteEvent(position : Int){
            var tsDoc = MyApplication.db?.collection("images")?.document(contentUidList[position])
            MyApplication.db?.runTransaction{ transaction ->
                val contentDTO = transaction.get(tsDoc!!).toObject(ContentDataModel::class.java)

                if (contentDTO!!.favorites.containsKey(uid)){
                    // 버튼 클릭 돼있음
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! - 1
                    contentDTO?.favorites!!.remove(uid)
                }else{
                    //버튼 클릭 안 돼있음
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! + 1
                    contentDTO?.favorites!![uid!!] = true
                }
                transaction.set(tsDoc, contentDTO)
            }
        }

        fun changeViewToUserFragment(position:Int){
            var fragment = UserFragment()
            var bundle = Bundle()
            bundle.putString("destinationUid", contentDataModels[position].uid)
            fragment.arguments = (bundle)
            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.mainContent, fragment)?.commit()
        }
    }


}