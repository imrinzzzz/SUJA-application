/**
 * Wipu     Kumthong            6088095
 * Pada     Kanchanapinpong     6088079
 * Thanirin Trironnarith        6088122
 *
 * Signup.kt deals with the user's registration
 */
package com.example.crowdsourcing

import android.os.Bundle
import android.view.LayoutInflater
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.mikhaellopez.circularimageview.CircularImageView
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.popup_noti.view.*
import java.util.*

class Signup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth     // FirebaseAuth instance declaration
    private lateinit var filePath: Uri          // used to store the selected image location uri
    private val PICK_IMAGE_REQUEST = 2          // tells the system what return-code to use when the invoked Activity completes
    private var count = 0;                      // used to count how many times, images were selected

    // SELECT ARTIST OR SUPPORTER
    fun artist_select(view: View) {
        support_btn.isEnabled = true;
        artist_btn.isEnabled = false;
        artist_btn.setBackgroundResource(R.drawable.click)
        support_btn.setBackgroundResource(R.drawable.unclick)
        Log.e("wow", "nice");
    }
    // SELECT ARTIST OR SUPPORTER
    fun support_select(view: View) {
        support_btn.isEnabled = false;
        artist_btn.isEnabled = true;
        support_btn.setBackgroundResource(R.drawable.click)
        artist_btn.setBackgroundResource(R.drawable.unclick)
    }

    /**
     * @param: savedInstanceState: Bundle?
     * When the page is called, it will begin at this fun
     *
     * if the fields are filled correctly, go to "createAccount" fun
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()       // FirebaseAuth instance instantiation

        // get ID, password, confirm_pass from the XML file
        val id = findViewById<TextView>(R.id.id)
        val username = findViewById<TextView>(R.id.username)
        val pass = findViewById<TextView>(R.id.password)
        val con_pass = findViewById<TextView>(R.id.confirm_pass)
        val confirm = findViewById<TextView>(R.id.confirm)
        // select image
        val select_pic = findViewById<CircularImageView>(R.id.img_pick_btn)
        select_pic.setOnClickListener() {
            imagePicker()
        }

        confirm.setOnClickListener() {
            val msg1: String = id.text.toString()
            val user: String = username.text.toString();
            val msg2: String = pass.text.toString()
            val msg3: String = con_pass.text.toString()
            findViewById<CircularImageView>(R.id.img_pick_btn)
            if (msg1.trim().isEmpty() || user.trim().isEmpty() || msg2.trim().isEmpty() || msg3.trim().isEmpty() ) {
                Toast.makeText(this@Signup, R.string.fill_all, Toast.LENGTH_SHORT).show()
            //select_pic.get
            } else if (msg2.length < 6) {
                Toast.makeText(this@Signup, R.string.pass_contain, Toast.LENGTH_SHORT).show()
            } else if (count == 0) {
                Toast.makeText(this@Signup, R.string.fill_pic, Toast.LENGTH_SHORT).show()
            }
            else if(artist_btn.isEnabled && support_btn.isEnabled)
            {
                Toast.makeText(this@Signup, R.string.select_type, Toast.LENGTH_SHORT).show()
            }
            else {
                // check if password and confirm password is the same
                if (msg2 == msg3) {
                    // type = 1 : artist || type = 2 : supporter
                    var type = 1;                           // set default -> type = 1 = artist
                    if (artist_btn.isEnabled) type = 2      // change type = 2 = supporter if true
                    createAccount(msg1, user, msg2, type)   // call function createAccount
                    back()
                } else {
                    Toast.makeText(this@Signup, R.string.pass_not_match, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    /*********************************** Firebase ***************************************************/
    public override fun onStart() {
        super.onStart()
    }

    /**
     * @param: email: String, username: String, password: String, type: Int
     *
     * create user with FirebaseAuth function
     * when it's created successfully, call "putUserToDB" fun
     */
    private fun createAccount(email: String, username: String, password: String, type: Int) {
        auth.createUserWithEmailAndPassword(email, password)            // use FirebaseAuth function to create new user
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val userAuth = auth.currentUser         // get currentUser
                    /* call function "putUserToDB" and pass currentUser,
                    their profile picture url, and type of user */
                    putUserToDB(userAuth!!, username, type)
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, R.string.authen_fail, Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * @param: userAuth: FirebaseUser,username: String, type: Int
     *
     * put user's information into Firebase Firestore
     * upload user's image to Firebase storage
     */
    private fun putUserToDB(userAuth: FirebaseUser,username: String, type: Int) {
        val storageRef = FirebaseStorage.getInstance().reference    // FirebaseStorage reference
        val ref = storageRef.child("pfp/" + UUID.randomUUID().toString())
        val uploadTask = ref.putFile(filePath)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception.let { throw it!! }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Upload image: ${task.result.toString()}")
                Log.d(TAG,task.result.toString())
                val db = Firebase.firestore                 // instantiate Firestore
                val user = hashMapOf(                      // map user information to each variable
                    "pfp" to task.result.toString(),
                    "type" to type,
                    "userId" to userAuth.uid,
                    "email" to userAuth.email,
                    "username" to username
                )
                Log.d("pic",task.result.toString())
                db.collection("users").document(userAuth.uid)
                    .set(user)              // put user information into userDB
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            } else {
                Log.w(TAG, "upload image failed!", task.exception)
            }
        }.addOnFailureListener { e -> Log.w(TAG, "upload image failed!", e) }
    }
    /*********************************** Firebase (end) ***************************************************/


    /*********************************** Image Cropping ***************************************************/
    /**
     * reference: https://inducesmile.com/kotlin-source-code/how-to-crop-image-in-android-kotlin/
     * start activity crop picture after get picture from gallery
     */
    private fun imagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra("crop", "true")
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            PICK_IMAGE_REQUEST
        )
    }

    /**
     * reference: https://inducesmile.com/kotlin-source-code/how-to-crop-image-in-android-kotlin/
     * this fun lets user select image and ask permission to use gallery (if not permitted yet)
     *
     * if image selected, it will let user crop the image (the "crop" library is imported)
     * if image is successfully selected and cropped, the image location uri will be put to "filePath" global variable
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null || data.data == null) {
                return
            }
            CropImage.activity(data.data!!).start(this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            val image_set = findViewById<CircularImageView>(R.id.img_pick_btn)
            val remove = findViewById<ImageView>(R.id.addicon)
            remove.visibility = View.INVISIBLE
            if (resultCode == RESULT_OK) {
                count++;
                /*Set image */
                image_set.setImageURI(result.getUri())
                /* gathering data to db*/
                if (data != null) {
                    Log.d(this.toString(), "Select image successfully!")
                    filePath = result.uri
                } else Log.w(this.toString(), "select image failed")
                /*if error*/
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.getError()
                Toast.makeText(this@Signup, error.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /*********************************** Image Cropping (end) ***************************************************/
    /**
     * a singleton in which its members can be accessed directly via the name of the containing class
     */
    companion object {
        private const val TAG = "FIREBASE !!!"
    }

    /*************************************** Pop up ******************************************************/
    /**
     * make a popup box
     */
    private fun back() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup_noti, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
        //show dialog
        val mAlertDialog = mBuilder.show()
        mDialogView.back_login.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }
}
