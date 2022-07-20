package com.example.junstagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.junstagram.R
import com.example.junstagram.databinding.ActivityAddPhotoBinding
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    val binding = ActivityAddPhotoBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        storage = FirebaseStorage.getInstance()
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        binding.addPhotoUploadBtn.setOnClickListener{
            var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){   //이미지 식별값 일치
            if(resultCode == Activity.RESULT_OK){   //결과값이 사진을 선택한 때일 때
                //이미지 받는 함수
                photoUri = data?.data
                binding.addPhotoImage.setImageURI(photoUri)
            }
        }
        else{
            // 뒤로가기 등을 눌러 취소하였을 때
            finish()
        }
    }

}