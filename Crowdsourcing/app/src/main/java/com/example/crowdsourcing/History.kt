/**
 * Wipu     Kumthong            6088095
 * Pada     Kanchanapinpong     6088079
 * Thanirin Trironnarith        6088122
 *
 * History.kt is a class involves the transactions made
 */

package com.example.crowdsourcing

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.android.synthetic.main.activity_history.*
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

class History : AppCompatActivity() {

    /**
     * ExampleItem : data class stored items to display in RecyclerList (in History page)
     */
    data class ExampleItem(
        val imageResource: Uri,
        val text1: String,
        val text2: String,
        val text3: String,
        val status: Boolean
    ) //create data class hold data

    private val list = ArrayList<ExampleItem>()         // ArrayList used in "getUserInfo" fun

    /**
     * @param: savedInstanceState: Bundle?
     * When the page is called, it will begin at this fun
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)       // set page to activity_history.xml
        getType()       // call to display recycler view list
    }

    /**
      * a void fun that is used to get the type (artist, support) of the current user
      * it then calls "getUserInfo" fun
     **/
    private fun getType() {
        val auth = FirebaseAuth.getInstance()           // get Firebase Auth instance
        val db = FirebaseFirestore.getInstance()    // get Firestore instance

        db.collection("users").document(auth.currentUser!!.uid)
            .get()   // get current user from Firestore
            .addOnSuccessListener { task ->
                val type = task.data!!.getValue("type").toString()
                var userType: String = "payerId"
                var userType2 = "payeeId"
                if (type == "1") {
                    userType = "payeeId"
                    userType2 = "payerId"
                    Log.d(TAG, "successfully get user's type")
                } else Log.d(TAG, "successfully get user's type")
                getUserInfo(userType, userType2)
            }
            .addOnFailureListener { e -> Log.e(TAG, "fail to get user's type", e) }
    }

    /**
     * @param: type: String, type2: String
     * a void fun that loops every transaction in "transaction"
     * if it's successful, call fun "dumsacclist"
     * it will pass the document reference, profile picture, and username
    **/
    private fun getUserInfo(type: String, type2: String) {
        if (!list.isEmpty()) {
            list.clear()
        }
        Log.d(TAG, "Type: $type")
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        db.collection("transaction")
            .get().addOnCompleteListener { documents ->
                if (documents.isSuccessful) { //get data from fire store
                    for (document in documents.result!!) {
                        val docRef = document.data.getValue(type) as DocumentReference
                        val docRef2 = document.data.getValue(type2) as DocumentReference
                        if (docRef.id == auth.currentUser?.uid.toString()) {
                            db.collection("users").document(docRef2.id)
                                .get().addOnSuccessListener { user ->
                                    if (user != null &&user.exists()) {
                                        val username = user.data!!.getValue("username")
                                        val pfp = user.data!!.getValue("pfp").toString()
                                        dumsaclist(document, pfp, username as String)
                                        Log.d(TAG, "get user information complete!")
                                    } else {
                                        Log.d(TAG, "user is null")
                                        val pfp =
                                            "https://firebasestorage.googleapis.com/v0/b/crowdsourcing-c40cb.appspot.com/o/3600x3600-white-solid-color-background.jpg?alt=media&token=b10c2d03-003f-47f4-97d4-735d7a7aa7d7"
                                        dumsaclist(document, pfp, "[deleted]")
                                    }
                                }.addOnFailureListener { e -> Log.e(TAG, "error:", e) }
                        }
                    }
                }
            }
    }

    /**
     * @param: document: QueryDocumentSnapshot, uPicture: String, uId: String
     * this is where the information of a transaction is put into the "ExampleItem"
     * then the list will be displayed in the RecyclerView
     */
    private fun dumsaclist(document: QueryDocumentSnapshot, uPicture: String, uId: String) {

        val datetime = document.getDate("datetime")
        val pattern = "M/d/y HH:mm"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date: String = simpleDateFormat.format(datetime)
        val amount = document.data.getValue("amount").toString()
        val status = document.data.getValue("status")
        val item = ExampleItem(
            Uri.parse(uPicture),
            uId,
            date.toString(),
            amount,
            status as Boolean
        ) // keep as data class
        list += item //contain in list
        val sortedList = list.sortedWith(compareByDescending { it.text2 })
        recycler_view_history.adapter =
            Adapter(sortedList) //send to set component in recycler_view_history
        recycler_view_history.layoutManager = LinearLayoutManager(this)
        recycler_view_history.setHasFixedSize(true)
    }

    /**
     * a singleton in which its members can be accessed directly via the name of the containing class
     */
    companion object {
        private const val TAG = "FIREBASE !!!!"
    }
}