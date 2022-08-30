package com.example.junstagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.junstagram.databinding.ActivityLoginBinding
import com.example.junstagram.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {

    lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySignInBinding.inflate(layoutInflater)
        binding.loginText.setOnClickListener{ // 로그인 클릭 시 실행
            finish()
        }

        binding.signInBtn.setOnClickListener {
            val email: String = binding.edit1.text.toString()
            val name: String = binding.edit2.text.toString()
            val nickname: String = binding.edit3.text.toString()
            val password: String = binding.edit4.text.toString()
            MyApplication.auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){ task ->
                binding.edit1.text.clear()
                binding.edit2.text.clear()
                binding.edit3.text.clear()
                binding.edit4.text.clear()
                if(task.isSuccessful){
                    MyApplication.auth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener { sendTask ->
                            if (sendTask.isSuccessful){
                                Toast.makeText(baseContext,"회원가입에 " +
                                        "성공하셨습니다. 전송된 메일을 " +
                                        "확인해주세요.",Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(baseContext, "메일 전송 실패",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                }else{
                    Toast.makeText(baseContext,"회원가입 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        setContentView(binding.root)
    }
}