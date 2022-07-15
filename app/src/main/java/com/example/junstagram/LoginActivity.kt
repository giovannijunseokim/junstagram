package com.example.junstagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.junstagram.databinding.ActivityLoginBinding
import com.facebook.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider


class LoginActivity : AppCompatActivity() {
    lateinit var auth :FirebaseAuth
    val GOOGLE_REQUEST_CODE = 99
    val TAG = "googleLogin"
    lateinit var callbackManager: CallbackManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(getApplicationContext())
        AppEventsLogger.activateApp(this)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        auth = Firebase.auth
        callbackManager = CallbackManager.Factory.create()
        setContentView(binding.root)

        binding.signText.setOnClickListener{ // 가입하기 클릭 시 실행
            val intent: Intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener{
            val email:String = binding.idEdittext.text.toString()
            val password:String = binding.pwEdittext.text.toString()
            MyApplication.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){ task ->
                    binding.idEdittext.text.clear()
                    binding.pwEdittext.text.clear()
                    if(task.isSuccessful){
                        if (MyApplication.checkAuth()){
                            MyApplication.email = email
                            val intent = Intent(this,MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(baseContext,
                            "전송된 메일로 이메일 인증이 되지 않았습니다.",
                            Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(baseContext,"로그인 실패",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.fbLoginText.setOnClickListener{
            facebookLogin()
        }
    }
    private fun facebookLogin(){
        LoginManager.getInstance()
            .logInWithReadPermissions(this, listOf("public_profile","email"))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    handleFBToken(result?.accessToken)
                }
                override fun onCancel() {}
                override fun onError(error: FacebookException?) {}
            })
    }
    private fun handleFBToken(token : AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "로그인 성공")
                    val user = auth!!.currentUser
                    loginSuccess()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }
    private fun loginSuccess(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }
}