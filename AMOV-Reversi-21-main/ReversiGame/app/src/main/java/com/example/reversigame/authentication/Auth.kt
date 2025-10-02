package com.example.reversigame.authentication

import android.app.PendingIntent.getActivity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.reversigame.MainActivity
import com.example.reversigame.Perfil
import com.example.reversigame.R
import com.example.reversigame.databinding.ActivityAuthBinding
import com.example.reversigame.val_s
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*
import androidx.core.app.ActivityCompat.startActivityForResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

import com.google.android.gms.tasks.Task
import android.net.Uri








class Auth : AppCompatActivity(){
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var google_web_id = "481791993578-b4e6rt681ung2ohmk4vqmvphg01osaqv.apps.googleusercontent.com"
    private lateinit var b : ActivityAuthBinding
    private val TAG = "Auth"
    var RC_SIGN_IN = 0

    var personName = ""
    var personEmail = ""
    var personId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        b = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(b.root)

        //FirebaseMessageService - Chaves
        intent.extras?.apply {
            for(k in keySet()) {
                Log.i(TAG, "Extras: $k -> ${get(k)}")
            }
        }

        auth = Firebase.auth

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(google_web_id)
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)

        b.signInButton.setOnClickListener {
            val signInIntent: Intent = googleSignInClient.getSignInIntent()
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        b.signOutButton.setOnClickListener {
            googleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    showUser(null)
                }
        }

        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            personName = acct.displayName.toString()
            personEmail = acct.email.toString()
            personId = acct.id.toString()
            //val personPhoto = acct.photoUrl;

            Toast.makeText(applicationContext, "Nome:${personName}",Toast.LENGTH_SHORT).show()
        }


    }

    override fun onStart() {
        super.onStart()
        showUser(auth.currentUser)
    }

    fun showUser(user : FirebaseUser?) {
        val str = when (user) {
            null -> "No authenticated user"
            else -> "User: ${user.email}"
        }
        //tvStatus.text = str
        Log.i(TAG,str)
    }

    fun createUserWithEmail(email:String,password:String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(this) {
                Log.i(TAG, "createUser: success")
                showUser(auth.currentUser)
            }
            .addOnFailureListener(this) { e ->
                Log.i(TAG, "createUser: failure ${e.message}")
                showUser(null)
            }
    }

    fun signInWithEmail(email:String,password:String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(this) {
                Log.d(TAG, "signInWithEmail: success")
                showUser(auth.currentUser)
            }
            .addOnFailureListener(this) { e->
                Log.d(TAG, "signInWithEmail: failure ${e.message}")
                showUser(null)
            }
    }

    fun signOut() {
        if (auth.currentUser != null) {
            auth.signOut()
        }
        showUser(auth.currentUser)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener(this) { result ->
                Log.d(TAG, "signInWithCredential:success")
                showUser(auth.currentUser)
            }
            .addOnFailureListener(this) { e ->
                Log.d(TAG, "signInWithCredential:failure ${e.message}")
                showUser(auth.currentUser)
            }
    }

    fun onAutenticarEmail(view: View) {
        if(b.edEmail.text.toString() != "" && b.edPassword.text.toString() != ""){
            signInWithEmail(b.edEmail.text.toString(),b.edPassword.text.toString())
            Perfil.emailstr = b.edEmail.text.toString()
        }
    }
    fun onRegistarEmail(view: View) {
        if(b.edEmail.text.toString() != "" && b.edPassword.text.toString() != ""){
            createUserWithEmail(b.edEmail.text.toString(),b.edPassword.text.toString())
            Perfil.emailstr = b.edEmail.text.toString()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            showUser(auth.currentUser)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            showUser(null)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_principal,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.MnCreditos -> {
                val toast = Toast.makeText(applicationContext, "Criado por: Mauro Jesus & Pedro Ramos & Rúben Almeida", Toast.LENGTH_LONG)
                toast.show()
            }
            R.id.MnLinguagem -> {
                var locale = Locale(val_s)
                if(val_s == "pt"){
                    locale = Locale(val_s)
                    val_s = "en"
                }else{
                    locale = Locale(val_s)
                    val_s = "pt"
                }
                val config: Configuration = resources.configuration
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)
                val refresh = Intent(
                    this,
                    Auth::class.java
                )
                startActivity(refresh)
            }
        }
        return super.onOptionsItemSelected(item)
    }


}