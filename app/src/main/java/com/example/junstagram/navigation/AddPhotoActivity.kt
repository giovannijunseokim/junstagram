package com.example.junstagram.navigation

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.junstagram.databinding.ActivityAddPhotoBinding
import com.example.junstagram.navigation.model.ContentDataModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*


class AddPhotoActivity : AppCompatActivity() {
    var storage : FirebaseStorage? = null
    var timeStamp : String? = null
    var auth : FirebaseAuth? = null
    var db : FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        val binding = ActivityAddPhotoBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        var photoUri : Uri? = null
        var launcher = registerForActivityResult(
            ActivityResultContracts.GetContent()) {
            if(it!=null){photoUri = it
            binding.addPhotoImage.setImageURI(it)
            }
            else{
                Toast.makeText(this,"사진 파일을 선택하지 않았거나, 오류가 발생했습니다.",Toast.LENGTH_SHORT)
                finish()
            }
        }.launch("image/*")

        binding.addPhotoUploadBtn.setOnClickListener{
            photoUri?.let { it -> uploadImageWithTextToFirebase(it) }
        }

    }
    fun uploadImageWithTextToFirebase(uri : Uri){
        var fileName = "IMAGE_" + timeStamp + "_.png"
        val binding = ActivityAddPhotoBinding.inflate(layoutInflater)

        var storageRef = storage!!.reference.child("images")?.child(fileName)

        storageRef?.putFile(uri!!)?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener{ uri ->
                var contentDataModel = ContentDataModel()
                contentDataModel.imageUri = uri.toString()
                contentDataModel.uid = auth?.currentUser?.uid
                contentDataModel.userId = auth?.currentUser?.email
                contentDataModel.explain = binding.addPhotoEditExplain.text.toString()
                contentDataModel.timeStamp = System.currentTimeMillis()

                db?.collection("images")?.document()?.set(contentDataModel)
                Toast.makeText(this, "사진이 업로드 되었습니다.", Toast.LENGTH_SHORT)

                setResult(Activity.RESULT_OK)
                finish()
                }
        }
    }
}