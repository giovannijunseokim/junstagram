package com.example.junstagram

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.junstagram.navigation.model.ContentDataModel
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentActivity : AppCompatActivity() {
    var contentUid : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        contentUid = intent.getStringExtra("contentUid")

        commentBtn_send.setOnClickListener {
            changeViewToComment()
        }

        commentRecyclerView.adapter = CommentRecyclerViewAdapter()
        commentRecyclerView.layoutManager = LinearLayoutManager(this)

    }
    inner class CommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var comments : ArrayList<ContentDataModel.Comment> = arrayListOf() // 댓글 담을 리스트
        init {
            MyApplication.db.collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timeStamp")
                .addSnapshotListener{snapshot, e ->
                    comments.clear()
                    if(snapshot == null){
                        return@addSnapshotListener
                    }

                    for (snapshot in snapshot.documents!!){
                        comments.add(snapshot.toObject(ContentDataModel.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }
        private inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            view.commentViewItem_textView_comment.text = comments[position].comment
            view.commentViewItem_textView_profile.text = comments[position].userId

            MyApplication.db
                .collection("profileImages")
                .document(comments[position].uid!!)
                .get().addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        var url = task.result!!["image"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions()
                            .circleCrop()).into(view.commentViewItem_imageView_profile)
                    }
                }
        }

        override fun getItemCount(): Int {
            return comments.size
        }

    }
    fun changeViewToComment(){
        var comment = ContentDataModel.Comment()
        comment.userId = MyApplication.auth.currentUser?.email
        comment.uid = MyApplication.auth.currentUser?.uid
        comment.comment = commentEditText_message.text.toString()
        comment.timeStamp = System.currentTimeMillis()

        MyApplication.db.collection("images").document(contentUid!!).collection("comments").document()
            .set(comment)

        commentEditText_message.text.clear()
    }
}