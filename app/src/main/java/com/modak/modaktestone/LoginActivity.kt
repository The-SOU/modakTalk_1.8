package com.modak.modaktestone

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.modak.modaktestone.databinding.ActivityLoginBinding
import com.modak.modaktestone.navigation.PhoneCertificationActivity
import com.modak.modaktestone.navigation.SelectRegionActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.model.OAuthToken
import com.modak.modaktestone.navigation.login.AgreementTermsActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    var auth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
//        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("107280891504-iambicb740bfiffs3ckmcf31fv0rkar8.apps.googleusercontent.com")
            .requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.kakaoLoginButton.setOnClickListener {
            signinEmail()
        }
        binding.emailLoginButton.setOnClickListener {
            signInAndSignUp()
        }

        binding.layout.setOnClickListener {
            hideKeyboard()
        }
    }

    fun hideKeyboard(){
        val view = this.currentFocus
        if(view != null){
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


//    private fun getFirebaseJwt(accessToken: String, type:Int) {
//        val firebaseCustomTokenApi : Retrofit = get()
//
//        val observable: Disposable =
//            firebaseCustomTokenApi
//                .create(FirebaseCustomTokenAPI)
//                .getFirebaseKakaoCustomToken(accessToken)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ i ->     /*??????*/
//                }, { error -> /*??????*/
//                }).addTo(disposables)
//    }

    fun signInAndSignUp() {
        auth?.createUserWithEmailAndPassword(
            binding.emailEdtitext.text.toString(),
            binding.passwordEdtitext.text.toString()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //Creating a user Account
                moveAgreementPage(task.result?.user)
            } else if (!task.exception?.message.isNullOrEmpty()) {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            } else {
                signinEmail()
            }
        }
    }

    fun signinEmail() {
        auth?.signInWithEmailAndPassword(
            binding.emailEdtitext.text.toString(),
            binding.passwordEdtitext.text.toString()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                moveMainpage(task.result?.user)
            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun kakaoLogin() {
// ????????? ?????? callback ??????
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("TAG", "????????? ??????", error)
            } else if (token != null) {
                Log.i("TAG", "????????? ?????? ${token.accessToken}")
            }
        }

// ??????????????? ???????????? ????????? ?????????????????? ?????????, ????????? ????????????????????? ?????????
        if (LoginClient.instance.isKakaoTalkLoginAvailable(this)) {
            LoginClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            LoginClient.instance.loginWithKakaoAccount(this, callback = callback)
        }

    }

    fun moveMainpage(user: FirebaseUser?) {
        if(user != null){
            startActivity(Intent(this, com.modak.modaktestone.MainActivity::class.java))
            finish()
        }
    }

    fun moveAgreementPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, AgreementTermsActivity::class.java))
            finish()
        }
    }

    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }


    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //Login
                moveAgreementPage(task.result?.user)
            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result!!.isSuccess) {
                //??? ?????? ????????????????????? ?????? ??? ????????? ?????????????????? ?????????.
                var account = result.signInAccount
                //?????????
                firebaseAuthWithGoogle(account)

            }
        }
    }


}