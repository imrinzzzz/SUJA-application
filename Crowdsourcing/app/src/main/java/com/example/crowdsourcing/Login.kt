/**
 * Wipu     Kumthong            6088095
 * Pada     Kanchanapinpong     6088079
 * Thanirin Trironnarith        6088122
 *
 * Login.kt is a class when user chose to log in
 * It will check if said user exists in FirebaseAuth
 */

package com.example.crowdsourcing

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class Login : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth        //create firebase authentication

    /**
     * @param: savedInstanceState: Bundle?
     * When the page is called, it will begin at this fun
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) //set layout
        val login = findViewById<TextView>(R.id.login_login) //var for login button

        mAuth = FirebaseAuth.getInstance()          // get current Authenticated user

        login.setOnClickListener {
            val email_id = findViewById<EditText>(R.id.login_id) //create var of edit text
            val password_id = findViewById<EditText>(R.id.login_pass)
            val email: String = email_id.text.toString() // edit text input to string
            val password: String = password_id.text.toString()

            if (email.isEmpty()) { //check if email empty require input
                Log.d(TAG, "Email was empty!")
                Toast.makeText(this@Login, R.string.fill_email, Toast.LENGTH_SHORT)
                    .show() //notify user
            }
            else if (password.isEmpty()) {// check for password if empty notify user
                Toast.makeText(this@Login, R.string.fill_pass, Toast.LENGTH_SHORT)
                    .show() //notify user
            }
            else {
                /**
                 * use a Firebase Auth function to check Login
                 * if logged in successfully, go to fun "check"
                 */
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task -> //sign in with email and pass to check user
                        if (!task.isSuccessful) { //if task unable to find or error
                            if (password.length < 6) { //password less than 6
                                //login_passwordEditText.error = "Please check your password. Password must have minimum 6 characters."
                                Log.d(TAG, "Enter password less than 6 characters.")
                                Toast.makeText(
                                    this@Login,
                                    R.string.pass_contain,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } else { //fail with some error
                                Log.d(TAG, "Authentication Failed: " + task.exception!!.message)
                                Toast.makeText(
                                    this@Login,
                                    "Authentication Failed: " + task.exception!!.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            //Sign in successfully
                            Toast.makeText(this@Login, R.string.login_success, Toast.LENGTH_SHORT)
                                .show()
                            check()
                            //startActivity(Intent(this, Timeline::class.java))
                        }
                    }
            }
        }
    }

    /**
     * check which type (artist, supporter) of user it is
     * send the user to their prospective XML page
     * Artist: go to "Timeline_artist.xml"
     * Supporter: go to "Timeline.xml"
     */
    private fun check() {
        val auth: FirebaseAuth = FirebaseAuth.getInstance() // FirebaseAuth instance instantiation
        val currentUser = auth.currentUser //get current user from firebase
        val currentFirebaseUser = currentUser?.uid.toString() //user id from firebase to string
        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        if (currentUser != null) {                                     // if user's already logged in
            db.collection("users").document(currentFirebaseUser).get()
                .addOnSuccessListener { document ->
                    if (document != null) { //if can firestore can find user
                        //Log.d(TAG, "DocumentSnapshot data: "+ task.result!!.data)
                        val type = document.data?.getValue("type").toString() //get type of user
                        if (type == "1")  //if type 1 he is artist go to artist timeline
                        {
                            startActivity(Intent(this, Timeline_artist::class.java))
                        } else //else supporter go to supporter timeline
                        {
                            // Log.d("Type",document.data?.getValue("type").toString())
                            startActivity(Intent(this, Timeline::class.java))
                        }
                    }
                }
        }
    }

    /**
     * a singleton in which its members can be accessed directly via the name of the containing class
     */
    companion object {
        private const val TAG = "Login Activity: "
    }
}
