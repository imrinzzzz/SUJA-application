/**
 * Wipu     Kumthong            6088095
 * Pada     Kanchanapinpong     6088079
 * Thanirin Trironnarith        6088122
 *
 * Post.kt is a class that deals with artist's post creation
 */

package com.example.crowdsourcing

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.mikhaellopez.circularimageview.CircularImageView
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.popup_noti.view.*
import java.util.*


class Post : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth     // FirebaseAuth instance declaration
    private lateinit var filePath: Uri          // used to store the selected image location uri
    private val PICK_IMAGE_REQUEST = 2          // tells the system what return-code to use when the invoked Activity completes
    private var count = 0;                      // used to count how many times, images were selected

    /**
     * @param: savedInstanceState: Bundle?
     * When the page is called, it will begin at this fun
     *
     * if the user selects post, it will go to "confirm" fun
     * if the user selects add photo, it will go to "imagePicker" fun
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post) // set layout

        val post = findViewById<TextView>(R.id.post) //create variable from xml
        val add_photo = findViewById<LinearLayout>(R.id.add_photo)
        val image_set: ImageView = findViewById<ImageView>(R.id.result)
        val profile = findViewById<CircularImageView>(R.id.newpost_profile)
        val username = findViewById<TextView>(R.id.new_post_username)
        image_set.visibility = View.INVISIBLE //if no picture set invisible

        Profile().get_user(username, profile)
        val caption = findViewById<EditText>(R.id.caption)
        caption.text.toString()

        post.setOnClickListener {
            confirm()   //add to db
        }

        add_photo.setOnClickListener {
            imagePicker()   //pick image from gallery
        }
    }

    /**
     * reference: https://inducesmile.com/kotlin-source-code/how-to-crop-image-in-android-kotlin/
     * send picture to crop by using package from outside
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
            if (data == null || data.data == null) { return }
            count = 0
            CropImage.activity(data.data!!).start(this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            val image_set: ImageView = findViewById<ImageView>(R.id.result)
            if (resultCode == RESULT_OK) {
                count++
                /*Set image */
                image_set.setImageURI(result.uri)
                image_set.visibility = View.VISIBLE
                /* gathering data to db*/
                if (data != null) {
                    Log.d(this.toString(), "Select image successfully!")
                    filePath = result.uri
                } else Log.w(this.toString(), "select image failed")
            /*if error*/
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this@Post, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * this fun will check if user have filled everything and select an image before creation of the post
     * if everything is ok, put the post to Firebase Firestore called "post"
     */
    fun confirm() {
        val caption = findViewById<EditText>(R.id.caption)
        val string = caption.text.toString()
        if (count == 0) {
            Toast.makeText(this@Post, R.string.fill_pic, Toast.LENGTH_SHORT).show()
        } else if (string.isEmpty()) {
            Toast.makeText(this@Post, R.string.fill_all, Toast.LENGTH_SHORT).show()
        } else {
            val storageRef = FirebaseStorage.getInstance().reference    // FirebaseStorage reference
            val ref = storageRef.child("post/" + UUID.randomUUID().toString())
            val uploadTask = ref.putFile(filePath)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception.let { throw it!! }
                    }
                    return@Continuation ref.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Upload image: ${task.result.toString()}")
                        Log.d(TAG, task.result.toString())

                        /* This is where we put the post to Firebase Firestore */
                        val user_id = FirebaseAuth.getInstance().currentUser?.uid
                        val db = Firebase.firestore
                        val userRef: DocumentReference =
                            db.collection("users").document(user_id.toString())
                        val id: String = db.collection("post").document().getId()
                        val post = hashMapOf(
                            "datetime" to Calendar.getInstance().time,
                            "image" to task.result.toString(),
                            "text" to string,
                            "userId" to userRef
                        )
                        db.collection("post").document(id)
                            .set(post)              // put user information into userDB
                            .addOnSuccessListener {
                                Log.d("post db", "DocumentSnapshot successfully written!")
                            }.addOnFailureListener { e ->
                                Log.e("post db", "Error writing document", e) }
                        dialog()    // show dialog
                    }
                }.addOnFailureListener { e -> Log.w(TAG, "upload image failed!", e) }
        }
    }

    /**
     * This fun creates a dialog that responds to the user's activity
     */
    fun dialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup_noti, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()
        mDialogView.login_success.text = "Post successfully"
        mDialogView.back_login.text = "Back to Timeline"
        //show dialog
        mDialogView.back_login.setOnClickListener {
            mAlertDialog.dismiss()
            startActivity(Intent(this, Timeline_artist::class.java))
        }
    }

    /**
     * a singleton in which its members can be accessed directly via the name of the containing class
     */
    companion object {
        private const val TAG = "FIREBASE !!!!"
    }

}