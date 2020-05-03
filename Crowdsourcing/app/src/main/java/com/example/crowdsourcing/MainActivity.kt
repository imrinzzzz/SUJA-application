/**
 * Wipu     Kumthong            6088095
 * Pada     Kanchanapinpong     6088079
 * Thanirin Trironnarith        6088122
 *
 * MainActivity.kt is the first page when the app starts
 * The rest will go from here
 */
package com.example.crowdsourcing

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.util.*

class MainActivity : AppCompatActivity() {

    /**
     * @param: savedInstanceState: Bundle?
     * When the page is called, it will begin at this fun
     *
     * If the user click sign up, it will go to "Signup.xml"
     * If the user click log in, it will go to "check"
     * If the user click change language, it will go to "change" fun
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) //set layout of this kotlin file to link

        val login =
            findViewById<TextView>(R.id.login) //declare variable to get data from xml component
        val signup = findViewById<TextView>(R.id.signup)
        val change_lang = findViewById<ImageView>(R.id.flag)
        val flag = findViewById<ImageView>(R.id.flag)

        signup.setOnClickListener {  //if click signup start the next activity call signup
            // start your next activity
            startActivity(Intent(this, Signup::class.java))
        }

        if (Locale.getDefault().toString() == "th_TH" || Locale.getDefault()
                .toString() == "th"
        ) {  // to check which language have been display and what flag should be set according to language
            flag.setBackgroundResource(R.drawable.ic_eng_flag)
        } else if (Locale.getDefault().toString() == "en_US" || Locale.getDefault()
                .toString() == "en"
        ) {
            flag.setBackgroundResource(R.drawable.ic_thai_flag)
        }

        login.setOnClickListener {//if press login check for authentication if user have logged in but no sign out
            check()
        }

        change_lang.setOnClickListener {
            change() // change language function when click the flag
        }
    }

    /********************************* change language ****************************/
    /**
     * when user click change language
     * it will call "setLocate" fun
     */
    private fun change() {
        if (Locale.getDefault().toString() == "th_TH" || Locale.getDefault().toString() == "th") {
            setLocate("en") //if language is Thai change the language to eng
            recreate()
            //flag.setBackgroundResource(R.drawable.ic_thai_flag)
        } else if (Locale.getDefault().toString() == "en" || Locale.getDefault()
                .toString() == "en_US"
        ) {
            setLocate("th")// if language eng change to thai
            recreate()
            //flag.setBackgroundResource(R.drawable.ic_eng_flag)
        }
    }

    /**
     * @param: Lang: String?
     *
     * it will configure the app' language according to the parameter passed
     */
    private fun setLocate(Lang: String?) {
        val languageCode = Lang
        val config = resources.configuration
        val locale = Locale(languageCode) //get locale of this language

        Locale.setDefault(locale) //set new locale or new language
        config.locale = locale
        resources.updateConfiguration(
            config,
            resources.displayMetrics
        ) //update config to new resource

        recreate() //recreate the app
    }
    /********************************* change language ****************************/

    /********************************* check authentication ****************************/
    /**
     * check if user is logged in (is authenticated and haven't logged out)
     * if logged in: check type and send to their prospective XML pages
     *      artist: Timeline_artist.xml
     *      support: Timeline.xml
     * if not looged in: send to "Login.xml"
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
                        val type = document.data?.getValue("type").toString() //get type of user
                        if (type == "1")  //if type 1 he is artist go to artist timeline
                        {
                            startActivity(Intent(this, Timeline_artist::class.java))
                        } else //else supporter go to supporter timeline
                        {
                            startActivity(Intent(this, Timeline::class.java))
                        }
                    }
                }
        } else { // none user found go to login
            startActivity(Intent(this, Login::class.java))
        }
    }
    /********************************* check authentication ****************************/

}