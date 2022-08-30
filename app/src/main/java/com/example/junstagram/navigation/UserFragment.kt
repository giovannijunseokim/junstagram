package com.example.junstagram.navigation

import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.marginRight
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.junstagram.LoginActivity
import com.example.junstagram.MainActivity
import com.example.junstagram.MyApplication
import com.example.junstagram.R
import com.example.junstagram.navigation.model.ContentDataModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment(){
    lateinit var uid : String
    lateinit var fragmentView : View
    lateinit var currentUserUid : String
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container,false)
        uid = arguments?.getString("destinationUid")!!
        currentUserUid = MyApplication.auth.currentUser?.uid!!
        if(uid == currentUserUid){
            //내 페이지
            fragmentView.user_followBtnSignOut?.text = "로그아웃"
            fragmentView.user_messageBtn.visibility = View.GONE
            fragmentView.user_followBtnSignOut?.setOnClickListener {
                startActivity(Intent(activity, LoginActivity::class.java))
                activity?.finish()
                MyApplication.auth.signOut()
            }
        }else{
            //다른 유저의 페이지
            var mainactivity = (activity as MainActivity)
            mainactivity.toolbarBackBtn.setOnClickListener {
                mainactivity.bottomNavigation.selectedItemId = R.id.actionHome
            }
            mainactivity.toolbarBackBtn.visibility = View.VISIBLE
        }

        fragmentView.user_RecyclerView?.adapter = UserAdapter()
        fragmentView.user_RecyclerView?.layoutManager = GridLayoutManager(activity, 3)

        var launcher = registerForActivityResult(
            ActivityResultContracts.GetContent()) {
            if(it!=null){
                val storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages")
                    .child(MyApplication.auth.currentUser!!.uid)
                storageRef.putFile(it).continueWithTask{
                    return@continueWithTask storageRef.downloadUrl
                }.addOnSuccessListener { uri ->
                    var map = HashMap<String,Any>()
                    map ["imageUri"] = uri.toString()
                    MyApplication.db.collection("profileImages").document(MyApplication.auth.currentUser!!.uid)
                        .set(map)
                }
            }else{Toast.makeText(context,"이미지를 선택하지 않았습니다.",Toast.LENGTH_SHORT)}
        }
        fragmentView.user_profile.setOnClickListener {
            launcher.launch("image/*")
        }
        getProfileImage()

        return fragmentView
    }
    inner class UserAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDataModels : ArrayList<ContentDataModel> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {
            MyApplication.db.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener{
                    querySnapshot, db ->
                if(querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents){
                    contentDataModels.add(snapshot.toObject(ContentDataModel::class.java)!!)
                }
                fragmentView.user_name.text = contentDataModels[0].userId
                fragmentView.user_tvPostCount.text = contentDataModels.size.toString()
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = fragmentView.resources.displayMetrics.widthPixels / 3
            val imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHoler(imageView)
        }

        inner class CustomViewHoler(var imageView: ImageView) : RecyclerView.ViewHolder(imageView) {}

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHoler).imageView
            Glide.with(holder.itemView.context).load(contentDataModels[position].imageUri)
                .apply(RequestOptions().centerCrop()).into(imageView)
        }

        override fun getItemCount(): Int {
            return contentDataModels.size
        }
    }
    fun getProfileImage(){
        MyApplication.db.collection("profileImages").document(MyApplication.auth.currentUser!!.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if (documentSnapshot == null) return@addSnapshotListener
                if (documentSnapshot.data != null){
                    val uri = documentSnapshot.data!!["imageUri"]
                    Glide.with(this).load(uri).apply(RequestOptions().circleCrop()).into(fragmentView.user_profile)
                }
            }
    }
}