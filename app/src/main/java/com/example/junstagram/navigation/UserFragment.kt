package com.example.junstagram.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.service.autofill.UserData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.junstagram.LoginActivity
import com.example.junstagram.MainActivity
import com.example.junstagram.MyApplication
import com.example.junstagram.MyApplication.Companion.email
import com.example.junstagram.R
import com.example.junstagram.navigation.GridFragment.GridAdapter.CustomViewHoler
import com.example.junstagram.navigation.model.ContentDataModel
import com.example.junstagram.navigation.model.FollowDataModel
import com.example.junstagram.navigation.model.UserDataModel
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment(){
    lateinit var uid : String
    lateinit var userId : String
    lateinit var fragmentView : View
    lateinit var currentUserUid : String
    lateinit var email : String
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView =
            LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
        uid = arguments?.getString("destinationUid")!! // 현재 내가 보고있는 계정
        currentUserUid = MyApplication.auth.currentUser?.uid!! // 로그인 한 계정
        if (uid == currentUserUid) {
            //내 페이지
            fragmentView.user_followBtnSignOut?.text = "로그아웃"
            fragmentView.user_messageBtn.visibility = View.GONE
            fragmentView.user_followBtnSignOut?.setOnClickListener {
                startActivity(Intent(activity, LoginActivity::class.java))
                activity?.finish()
                MyApplication.auth.signOut()
            }
        } else {
            //다른 유저의 페이지
            var mainactivity = (activity as MainActivity)
            mainactivity.toolbarBackBtn.setOnClickListener {
                mainactivity.bottomNavigation.selectedItemId = R.id.actionHome
            }
            mainactivity.toolbarBackBtn.visibility = View.VISIBLE
            fragmentView.user_followBtnSignOut.setOnClickListener {
                requestFollow()
            }
        }

        fragmentView.user_RecyclerView?.adapter = UserAdapter()
        fragmentView.user_RecyclerView?.layoutManager = GridLayoutManager(activity, 3)

        var launcher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            if (it != null) {
                val storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages")
                    .child(currentUserUid)
                storageRef.putFile(it).continueWithTask {
                    return@continueWithTask storageRef.downloadUrl
                }.addOnSuccessListener { uri ->
                    var map = HashMap<String, Any>()
                    map["imageUri"] = uri.toString()
                    MyApplication.db.collection("profileImages")
                        .document(MyApplication.auth.currentUser!!.uid)
                        .set(map)
                }
            } else {
                Toast.makeText(context, "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT)
            }
        }
        if (uid == currentUserUid) {
            fragmentView.user_profile.setOnClickListener {
                launcher.launch("image/*")
            }
        }
        getProfileImage()
        getFollowerAndFollowing()

        return fragmentView
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    inner class UserAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDataModels : ArrayList<ContentDataModel> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()
        var userDataModel = UserDataModel()
        init {
            MyApplication.db.collection("images").whereEqualTo("uid",uid).addSnapshotListener {
                    querySnapshot, exception ->
                if (querySnapshot == null) {
                    return@addSnapshotListener
                }
                for (snapshot in querySnapshot.documents) {
                    contentDataModels.add(snapshot.toObject(ContentDataModel::class.java)!!)
                }
                fragmentView.user_tvPostCount.text = contentDataModels.size.toString()
            }
            MyApplication.db.collection("userInformation").whereEqualTo("uid",uid).addSnapshotListener{
                    snapshot, error ->
                if (snapshot == null){
                    return@addSnapshotListener
                }
                for (data in snapshot.documents) {
                    userDataModel = (data.toObject(UserDataModel::class.java)!!)
                }
                fragmentView.user_name.text = userDataModel.name
                fragmentView.user_nickname.text = "@" + userDataModel.nickname
                fragmentView.user_description.text = userDataModel.description
                if(userDataModel.description == "")
                    fragmentView.user_description.visibility = View.GONE
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
        MyApplication.db.collection("profileImages").document(uid)
            .addSnapshotListener { documentSnapshot, error ->
                if (documentSnapshot == null) return@addSnapshotListener
                if (documentSnapshot.data != null){
                    val uri = documentSnapshot.data!!["imageUri"]
                    Glide.with(this).load(uri).apply(RequestOptions().circleCrop()).into(fragmentView.user_profile)
                }
            }
    }

    fun requestFollow(){
        // 내 계정의 FDM에서 일어나는 일
        var tsDocFollowing = MyApplication.db.collection("users").document(currentUserUid)
        MyApplication.db.runTransaction { transaction ->
            var followDataModel = transaction.get(tsDocFollowing).toObject(FollowDataModel::class.java) // 정보 FDM로 받아옴
            if (followDataModel == null){ // null이라는 건, 지금까지 팔로우 한 사람이 없어 db에 users 컬렉션이 없다는 뜻. 새로 만들어 주자.
                followDataModel = FollowDataModel()
                followDataModel.followingCount = 1
                followDataModel.followings[uid] = true

                transaction.set(tsDocFollowing,followDataModel)
                return@runTransaction
            }
            if(followDataModel.followings.containsKey(uid)){ // 내가 팔로우 했음. 다시 눌렀으니 팔로우 해제
                followDataModel.followingCount -= 1
                followDataModel.followings.remove(uid)
            }else{ // 내가 팔로우 아직 안했음. 팔로우 ㄱㄱ
                followDataModel.followingCount += 1
                followDataModel.followings[uid] = true
            }
            transaction.set(tsDocFollowing,followDataModel)
            return@runTransaction
        }

        // 남의 계정의 FDM에서 일어나는 일
        var tsDocFollower = MyApplication.db.collection("users").document(uid)
        MyApplication.db.runTransaction { transaction ->
            var followDataModel = transaction.get(tsDocFollower).toObject(FollowDataModel::class.java)
            if(followDataModel == null){
                followDataModel = FollowDataModel()
                followDataModel!!.followerCount = 1
                followDataModel!!.followers[currentUserUid] = true

                transaction.set(tsDocFollower, followDataModel!!)
                return@runTransaction
            }
            if(followDataModel!!.followers.containsKey(currentUserUid)){ // 내가 팔로우 했음. 다시 눌렀으니 팔로우 해제
                followDataModel!!.followerCount -= 1
                followDataModel!!.followers.remove(currentUserUid)
            }else{ // 내가 팔로우 아직 안했음. 팔로우 ㄱㄱ
                followDataModel!!.followerCount += 1
                followDataModel!!.followers[currentUserUid] = true
            }
            transaction.set(tsDocFollower,followDataModel!!)
            return@runTransaction
        }
    }

    fun getFollowerAndFollowing(){
        MyApplication.db.collection("users").document(uid).addSnapshotListener { documentSnapshot, error ->
            if(documentSnapshot == null) return@addSnapshotListener
            var followDataModel = documentSnapshot.toObject(FollowDataModel::class.java)
            if (followDataModel?.followingCount != null){ // 팔로잉 가져오기
                fragmentView.user_tvFollowingCount.text = followDataModel?.followingCount?.toString()
            }
            if (followDataModel?.followerCount != null){ // 팔로워 가져오기
                context
                fragmentView.user_tvFollowerCount.text = followDataModel?.followerCount?.toString()
                if (followDataModel.followers.containsKey(currentUserUid)){
                    fragmentView.user_followBtnSignOut.text = "팔로잉"
                    // 색 변하게 하는 법.....
                    fragmentView.user_followBtnSignOut.setBackgroundColor(ContextCompat.getColor(
                        requireContext(), R.color.colorLightGray))
                }else{
                    if (uid != currentUserUid){
                        fragmentView.user_followBtnSignOut.text = "팔로우"
                        fragmentView.user_followBtnSignOut.setBackgroundColor(ContextCompat.getColor(
                            requireContext(), R.color.blue))
                    }
                }
            }
        }
    }
}