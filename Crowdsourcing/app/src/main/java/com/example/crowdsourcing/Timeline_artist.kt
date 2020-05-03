/**
 * Wipu     Kumthong            6088095
 * Pada     Kanchanapinpong     6088079
 * Thanirin Trironnarith        6088122
 *
 * Timeline_artist.kt is where it deals with the display of posts (artist)
 */
package com.example.crowdsourcing

import android.os.Bundle
import android.view.LayoutInflater
import android.os.Build
import android.widget.*
import ItemListener
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.credit_card.view.*
import kotlinx.android.synthetic.main.activity_timeline_artist.*
import kotlinx.android.synthetic.main.activity_timeline_artist.recycler_view
import kotlinx.android.synthetic.main.donate.view.*
import kotlinx.android.synthetic.main.donate.view.Donate_popup_text
import kotlinx.android.synthetic.main.donate.view.cancel_donate
import kotlinx.android.synthetic.main.donate.view.insert_money
import kotlinx.android.synthetic.main.donate.view.next_donate_popup
import kotlinx.android.synthetic.main.nav_bar_artist.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.android.synthetic.main.pay_success.view.*
import kotlinx.android.synthetic.main.transac_success.view.*
import kotlinx.android.synthetic.main.transfer_money.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Timeline_artist : AppCompatActivity(), ItemListener,
    NavigationView.OnNavigationItemSelectedListener {

    lateinit var toolbar: Toolbar //top bar
    lateinit var drawerLayout: DrawerLayout //overlay nav
    lateinit var navView: NavigationView //nav view
    private lateinit var mHandler: Handler //if refresh in action
    private lateinit var mRunnable: Runnable //call for refresh
    private var doubleBackToExitPressedOnce = false //setting for double back action

    /**
     * ExampleItem2 : data class stored items to display in RecyclerList (in History page)
     */
    data class ExampleItem3(
        val imageResource: Uri,
        val imageResource2: Uri,
        val text1: String,
        val text2: String,
        val text3: String,
        val uId: String
    )

    private val list = ArrayList<ExampleItem3>()            // ArrayList used in "getUserInfo" fun

    /**
     * @param: savedInstanceState: Bundle?
     * When the page is called, it will begin at this fun
     *
     * list RecyclerView list retrieved from Firestore (calling "getPostUserInfo" fun)
     * calls "count_amount" fun
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.nav_bar_artist)
        val history = findViewById<LinearLayout>(R.id.history_artist)
        val transfer = findViewById<TextView>(R.id.artist_transfer)
        val fab = findViewById<FloatingActionButton>(R.id.fab)

        count_amount()

        fab.setOnClickListener {
            startActivity(Intent(this, Post::class.java))
        }

        history.setOnClickListener() {
            startActivity(Intent(this, History::class.java))
        }

        transfer.setOnClickListener {
            val money = findViewById<TextView>(R.id.artist_money)
            if (money.text.toString().toInt() <= 0) {
                Toast.makeText(this, R.string.broke, Toast.LENGTH_SHORT).show()
            } else {
                dialog()
            }
        }

        getPostUserInfo()
        mHandler = Handler()
        itemsswipetorefresh2.setOnRefreshListener {
            mRunnable = Runnable {
                // Update the text view text with a random number
                getPostUserInfo()
                count_amount()
                // Hide swipe to refresh icon animation
                itemsswipetorefresh2.isRefreshing = false
            }
            mHandler.postDelayed(
                mRunnable, (randomInRange(1, 5) * 1000).toLong() // Delay 1 to 5 seconds
            )
        }

        toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.artist_drawer)
        navView = findViewById(R.id.nav_view2)
        Profile().get_user(
            nav_view2.getHeaderView(0).user_nav,
            nav_view2.getHeaderView(0).user_profile
        )
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

    }

    /**
     * @param: item: MenuItem
     *
     * Deals with Navigation bar activities
     * If user clicks delete
     *      - delete the user from the Firestore
     *      - delete the user from the Firebase Auth
     *      - delete the user's post (if any)
     * If user clicks logout, use Firebase Auth
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                startActivity(Intent(this, Profile::class.java))
            }

            R.id.nav_delete -> {
                Log.d("in", "in")
                val db = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance().currentUser
                val uid = auth?.uid.toString()
                db.collection("post").get().addOnCompleteListener { document ->
                    if (document.isSuccessful) {
                        for (doc in document.result!!) {
                            val docRef = doc.data.getValue("userId") as DocumentReference
                            if (docRef.id.toString() == uid) {
                                db.collection("post").document(doc.id).delete()
                            }
                        }
                    }
                }
                db.collection("users").document(uid).delete()
                    .addOnSuccessListener {
                        auth?.delete()?.addOnCompleteListener {
                            Log.d("in", uid)
                            startActivity(Intent(this, Login::class.java))
                        }
                    }
            }

            R.id.nav_logout -> {
                Toast.makeText(this, R.string.log_out, Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut();
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * This will get all the post information + user information (related to the post) from Firestore
     * If successfully retrieved, calls "dumdumlist" fun
     */
    private fun getPostUserInfo() {
        val db = FirebaseFirestore.getInstance()
        if (!list.isEmpty()) {
            list.clear()
        }

        db.collection("post").get().addOnCompleteListener { documents ->
            if (documents.isSuccessful) {
                for (document in documents.result!!) {
                    val datetime = document.getDate("datetime")
                    val pattern = "M/d/y HH:mm"
                    val simpleDateFormat = SimpleDateFormat(pattern)
                    val date: String = simpleDateFormat.format(datetime)
                    val text = document.data.getValue("text").toString()
                    val image = document.data.getValue("image").toString()
                    val docRef = document.data.getValue("userId") as DocumentReference
                    db.collection("users").document(docRef.id).get()
                        .addOnSuccessListener { user ->
                            if (user != null && user.exists()) {
                                Log.d(TAG, "successful getting user! called dumdumlist"+user)
                                val username = user.data!!.getValue("username")
                                val pfp = user.data!!.getValue("pfp").toString()
                                val uId = user.id
                                dumdumlist(username as String, pfp, date, text, image, uId)
                            }
                        }.addOnFailureListener { e -> Log.e(TAG, "failed getting user", e) }
                }
            }
        }
    }

    /**
     * @param:username: String, pfp: String, date: String, text: String, img: String, uId: String
     * This is where the information of the posts is put in the ExampleItem2
     * then the list will be displayed in the RecyclerView
     */
    private fun dumdumlist(username: String, pfp: String, date: String, text: String, img: String, uId: String) {
        Log.d("dumdum", "it's me dumdum")
        val item = ExampleItem3(Uri.parse(pfp), Uri.parse(img), username, date, text, uId)
        list += item
        val sortedlist = list.sortedWith(compareByDescending { it.text2 })
        recycler_view.adapter = AdapterTimeline_artist(sortedlist, this)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)
    }

    /**
     * @param: imageView: CircularImageView, text: String, username: String, img: String
     *
     * When user clicks donate, it will pop up a dialog that lets user fill in the amount
     * if the user doesn't dismiss, call "put_amount" and "credit_card" fun
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onClicked(
        imageView: CircularImageView,
        textView: String,
        username: String,
        img: String
    ) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.donate, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()
        mDialogView.Donate_popup_text.text = textView

        Picasso.get().load(img).into(mDialogView.imageView4)
        mDialogView.Donate_popup_text.text = username
        //show dialog
        mDialogView.next_donate_popup.setOnClickListener {
            val amount = mDialogView.insert_money.text.toString().toIntOrNull()
            mAlertDialog.dismiss()
            put_amount(amount!!, textView)
            credit_card(amount.toString())
        }
        mDialogView.cancel_donate.setOnClickListener() {
            mAlertDialog.dismiss()
        }
    }

    /**
     * It will pop up a dialog that lets user fill in the credit card info
     * if user fills in information, calls "pay_done" fun
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun credit_card(amount: String) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.credit_card, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()

        //show dialog
        mDialogView.confirm_donate.setOnClickListener {
            mAlertDialog.dismiss()
            pay_done(amount)
        }
        mDialogView.back_donate.setOnClickListener() {
            mAlertDialog.dismiss()
        }
    }

    /**
     * It will pop up a dialog that notifies user that the payment is successful
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun pay_done(amount: String) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.pay_success, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()
        mDialogView.paysuccess.text = amount
        //show dialog
        mDialogView.pay_back_home.setOnClickListener() {
            mAlertDialog.dismiss()
        }
    }

    /**
     * when user clicks transfer money, it will pop up a dialog to let user fill in info
     * if user fill in the amount, it will call "put_transac" and "next_dialog" fun
     */
    fun dialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.transfer_money, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()
        val money = findViewById<TextView>(R.id.artist_money)
        mDialogView.transac_money.text = money.text.toString()
        //show dialog
        mDialogView.cancel_donate.setOnClickListener() {
            mAlertDialog.dismiss()
        }
        mDialogView.next_donate_popup.setOnClickListener() {
            val amount = mDialogView.insert_money.text.toString()
//            Log.d(TAG, "Amount = ${amount.toInt()} current money = ${money.toString().toInt()}")
            //Toast.makeText(this, amount, Toast.LENGTH_SHORT).show()

            if(amount.isNotEmpty()) {
                if(money.text.toString().toInt() < amount.toInt()) {
                    Toast.makeText(this, R.string.over, Toast.LENGTH_SHORT).show()
                } else {
                    put_transac(amount)
                    mAlertDialog.dismiss()
                    next_dialog()
                }
            } else {
                Toast.makeText(this, R.string.amount_in, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * it will pop up a dialog notifying the user that the transaction was successful
     * calls "count_amount" fun
     */
    fun next_dialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.transac_success, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()

        //show dialog
        mDialogView.transac_back_home.setOnClickListener() {
            mAlertDialog.dismiss()
            count_amount()
        }
    }

    /**
     * @param: amount: Int
     * put the transaction to the Firebase Firestore named "transaction"
     */
    private fun put_transac(amount: String) {
        val user_id = FirebaseAuth.getInstance().currentUser?.uid
        val db = Firebase.firestore
        val userRef: DocumentReference = db.collection("users").document(user_id.toString())
        val id: String = db.collection("transaction").document().id
        Log.d("in", "in")
        val transac = hashMapOf(
            "amount" to amount,
            "datetime" to Calendar.getInstance().time,
            "payerId" to userRef,
            "payeeId" to userRef,
            "status" to false,
            "transactionId" to id
        )
        db.collection("transaction").document(id)
            .set(transac)              // put user information into userDB
            .addOnSuccessListener { Log.d("post db", "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w("post db", "Error writing document", e) }
    }

    /**
     * calculate the sum of the artist's account (from donation and transaction)
     * it will show the result by calling "transac_show" fun
     */
    fun count_amount() {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user_id = FirebaseAuth.getInstance().currentUser?.uid
        val userRef: DocumentReference = db.collection("users").document(user_id.toString())
        var total = 0;
        db.collection("transaction")
            .get().addOnCompleteListener { documents ->
                if (documents.isSuccessful) { //get data from fire store
                    for (document in documents.result!!) {
                        val docRef = document.data.getValue("payeeId") as DocumentReference
                        val docRef2 = document.data.getValue("payerId") as DocumentReference
                        val docbool2 = document.data.getValue("status").toString()
                        if (docRef.id == auth.currentUser?.uid.toString()) {
                            val amount_transac = document.data.getValue("amount").toString().toIntOrNull()
                            if (amount_transac != null) {
                                if (docbool2 == "false") {
                                    total -= amount_transac
                                    Log.d("in", total.toString())
                                    transac_show(total)
                                } else {
                                    total += amount_transac
                                    Log.d("out", total.toString())
                                    transac_show(total)
                                }

                            }
                        }
                    }
                }
            }
    }

    /**
     * @param: total2: Int
     * show the calculated amount
     */
    private fun transac_show(total2: Int) {
        val show = findViewById<TextView>(R.id.artist_money)
        show.text = total2.toString()
    }

    /**
     * this function will navigate user out of the app if user press back button 2 times
     */
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {

            val a = Intent(Intent.ACTION_MAIN)
            a.addCategory(Intent.CATEGORY_HOME)
            a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(a)
            super.onBackPressed()

            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, R.string.back_exit, Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    /**
     * @param: min: Int, max: Int
     * @return: int
     * Custom method to get a random number from the provided range
     */
    private fun randomInRange(min: Int, max: Int): Int {
        // Define a new Random class
        val r = Random()

        // Get the next random number within range
        // Including both minimum and maximum number
        return r.nextInt((max - min) + 1) + min;
    }

    /**
     * @param: amount: Int, textView: String
     * put the transaction to the Firebase Firestore named "transaction"
     */
    private fun put_amount(amount: Int, textView: String) {
        Log.d(TAG, "userId = $textView")
        val user_id = FirebaseAuth.getInstance().currentUser?.uid
        val db = Firebase.firestore
        val userRef: DocumentReference = db.collection("users").document(user_id.toString())
        val payeeRef: DocumentReference = db.collection("users").document(textView)
        val id: String = db.collection("transaction").document().getId()
        Log.d("in", "in")
        val transac = hashMapOf(
            "amount" to amount,
            "datetime" to Calendar.getInstance().time,
            "payerId" to userRef,
            "payeeId" to payeeRef,
            "status" to true,
            "transactionId" to id
        )
        db.collection("transaction").document(id)
            .set(transac)              // put user information into userDB
            .addOnSuccessListener { Log.d("post db", "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w("post db", "Error writing document", e) }
    }

    /**
     * a singleton in which its members can be accessed directly via the name of the containing class
     */
    companion object {
        private const val TAG = "FIREBASE !!!!"
    }
}