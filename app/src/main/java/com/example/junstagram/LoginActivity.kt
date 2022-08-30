package com.example.junstagram

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.junstagram.MyApplication.Companion.auth
import com.example.junstagram.databinding.ActivityLoginBinding
import com.facebook.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    var googleSignInClient : GoogleSignInClient? = null
    lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        //일반 로그인
        callbackManager = CallbackManager.Factory.create()

        binding.signText.setOnClickListener { // 가입하기 클릭 시 실행
            val intent: Intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            login()
        }

        val getResultForGoogleSignIn = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                Log.d("gio", "ok.......")
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            MyApplication.email = account.email
                            moveMainPage(auth.currentUser)
                        } else {
                            Toast.makeText(
                                baseContext, "로그인 실패1",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }catch (e:ApiException){
                Toast.makeText(
                    baseContext, "로그인 실패2",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("gio", e.toString())
            }
        }

        binding.googleLoginText.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("326545122611-ipdg53jpceqqqgtrfdtr1ph6e9e107f3.apps.googleusercontent.com")
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInIntent = googleSignInClient?.signInIntent
            getResultForGoogleSignIn.launch(signInIntent)
        }

        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth.currentUser)
    }

    fun login(){
        val email: String = binding.idEdittext.text.toString()
        val password: String = binding.pwEdittext.text.toString()
        if (email != "" && password != "") {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        if (MyApplication.checkAuth()) {
                            binding.idEdittext.text.clear()
                            binding.pwEdittext.text.clear()
                            MyApplication.email = email
                            moveMainPage(auth.currentUser)
                        } else {
                            Toast.makeText(
                                baseContext,
                                "전송된 메일로 이메일 인증이 되지 않았습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        binding.pwEdittext.text.clear()
                        Toast.makeText(
                            baseContext, "로그인 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(baseContext, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    fun moveMainPage(user: FirebaseUser?){
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}