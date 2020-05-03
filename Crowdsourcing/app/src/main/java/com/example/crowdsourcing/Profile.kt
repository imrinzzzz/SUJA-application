/**
 * Wipu     Kumthong            6088095
 * Pada     Kanchanapinpong     6088079
 * Thanirin Trironnarith        6088122
 *
 * Profile.kt is for user to check their profile
 * It allows user to edit their profile picture, username, and password
 *
 */
package com.example.crowdsourcing

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.popup_noti.view.*
import java.util.*

class Profile : AppCompatActivity() {

    private lateinit var filePath: Uri          // used to store the selected image location uri
    private val PICK_IMAGE_REQUEST = 2          // tells the system what return-code to use when the invoked Activity completes
    private var count = 0;                      // used to count how many times, images were selected

    /**
     * @param: savedInstanceState: Bundle?
     * When the page is called, it will begin at this fun
     *
     * calls "get_user" fun
     * if the user clicks edit profile, it will call "imagePicker" fun
     * if the user clicks update, it will call "update" fun
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)       //set layout of kotlin file

        val update = findViewById<TextView>(R.id.update)        //get component from xml
        val edit_profile = findViewById<ImageView>(R.id.edit_profile)
        get_user(findViewById(R.id.username_display), findViewById(R.id.img_pick_btn))

        edit_profile.setOnClickListener { //listen for click
            imagePicker() //sent to image picker
            count++
        }
        update.setOnClickListener{ //listen for click
            update() //sent to update function
        }
    }

    /************************************* dialog *****************************************/
    /**
     * @param: string: String
     *
     * display dialog according to user's activity
     * if there is the parameter passed is not null, call "put" fun
     */
    private fun dialog_confirm(string: String) {   //notify client confirm change
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup_noti, null) //bind with layout
        mDialogView.back_login.text = getString(R.string.confirm) //change text from layout
        mDialogView.login_success.text = getString(R.string.confirm_update)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
        //show dialog
        mBuilder.show()

        val user = FirebaseAuth.getInstance().currentUser
        mDialogView.back_login.setOnClickListener { //to confirm update of info
            if(string.isNotEmpty()) {
                user!!.updatePassword(string).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("update", "update password complete")
                    } else {
                        Log.e("update", "unable to update password ")
                    }
                }
            }
            put()
            startActivity(Intent(this, Timeline::class.java))

        }
    }
    /************************************* dialog *****************************************/
    /**
     * put the updated information to Firebase Firestore
     */
    private fun put() {
        val username = findViewById<EditText>(R.id.username_display).text.toString()
        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser?.uid.toString() //user id
        val db = FirebaseFirestore.getInstance() // instance firebase

        db.collection("users").document(currentFirebaseUser) //find user data from firestore
            .update( mapOf("username" to username))
        if (count > 0) {
            val storageRef = FirebaseStorage.getInstance().reference    // FirebaseStorage reference
            val ref = storageRef.child("pfp/" + UUID.randomUUID().toString())
            val uploadTask = ref.putFile(filePath)

            /* Put image to Firebase Storage */
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception.let { throw it!! }
                }
                return@Continuation ref.downloadUrl
            /* If successful, put the updated information (including image) to Firestor */
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("img", "Upload image successfully!")
                    db.collection("users").document(currentFirebaseUser) //find user data from firestore
                        .update( mapOf("pfp" to task.result.toString()))
                } else {
                    Log.w("img", "upload image failed!", task.exception)
                }
            }.addOnFailureListener { e -> Log.w("img", "upload image failed!", e) }
        }

        /* When done, go back to prospective page of each user */
        db.collection("users").document(currentFirebaseUser) //find user data from firestore
            .get().addOnSuccessListener { document ->
                if (document != null) {      //if data found. IT SHOULD
                    val type= document.data?.getValue("type").toString() //get image file
                    if(type == "1") {
                        startActivity(Intent(this, Timeline_artist::class.java))
                    } else {
                        startActivity(Intent(this, Timeline::class.java))
                    }
                }
            }
    }

    /************************************* to update *****************************************/
    /**
     * check if every field is filled correctly
     * if yes, go to "dialog_confirm" fun
     */
    private fun update(){

        val old_pass_xml = findViewById<EditText>(R.id.old_pass) //declare xml variable
        val new_pass_xml = findViewById<EditText>(R.id.new_password)
        val confirm_pass_xml = findViewById<EditText>(R.id.confirm_pass)

        val old_pass: String = old_pass_xml.text.toString() //get text from edittext to string
        val new_pass: String = new_pass_xml.text.toString()
        val confirm_new: String = confirm_pass_xml.text.toString()

        if(old_pass.isEmpty() && new_pass.isEmpty() && confirm_new.isEmpty()) {
            Toast.makeText(this@Profile,R.string.fill_all,Toast.LENGTH_SHORT).show()
        } else if(new_pass.length < 6) {   //pass must not less than 6
            Toast.makeText(this@Profile,R.string.pass_contain, Toast.LENGTH_SHORT).show()
        } else if(new_pass != confirm_new) {   //new pass must be equal to confirmation pass
            Toast.makeText(this@Profile,R.string.pass_not_match, Toast.LENGTH_SHORT).show()
        } else{ //confirm dialog
            dialog_confirm(confirm_new)
        }
    }
    /************************************* to update *****************************************/

    /************************************* get user data *****************************************/
    /**
     * this fun is for displaying user information on profile page
     */
    fun get_user(user: TextView,pic: CircularImageView){ //get user data
        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser?.uid.toString() //user id
        val db = FirebaseFirestore.getInstance() // instance firebase
        db.collection("users").document(currentFirebaseUser) //find user data from firestore
            .get().addOnSuccessListener { document ->
                if (document != null) { //if data found. IT SHOULD
                    val profile= document.data?.getValue("pfp").toString() //get image file
                    val uri = Uri.parse(profile) //change to uri
                    user.text = document.getString("username").toString().capitalize() //username with capital letter
                    Picasso.get().load(uri).into(pic)   // display picture (imported library)
                }
            }
    }
    /************************************* get user data *****************************************/

    /************************************** let user crop new image ***************************************/
    /**
     * reference: https://inducesmile.com/kotlin-source-code/how-to-crop-image-in-android-kotlin/
     * try to call image crop page from import package
     */
    private fun imagePicker() {
        val intent = Intent() //change image of profile
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra("crop", "true")
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            PICK_IMAGE_REQUEST
        ) //sent to crop from import
    }

    /**
     * reference: https://inducesmile.com/kotlin-source-code/how-to-crop-image-in-android-kotlin/
     * this fun lets user select image and ask permission to use gallery (if not permitted yet)
     *
     * if image selected, it will let user crop the image (the "crop" library is imported)
     * if image is successfully selected and cropped, the image location uri will be put to "filePath" global variable
     */
    override fun onActivityResult(requestCode: Int,resultCode: Int,data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)  //unable to access gallery
        {
            if (data == null || data.data == null) {
                return //data is null
            }
            CropImage.activity(data.data!!).start(this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) { //if crop proceed and gain data
            val result = CropImage.getActivityResult(data) //image file
            val image_set = findViewById<CircularImageView>(R.id.img_pick_btn) // component from layout
            if (resultCode == RESULT_OK) { //no error
                if (data != null) {
                    image_set.setImageURI(result.getUri()) //set uri to component show on screen
                    filePath = result.uri
                    Log.d(this.toString(), "Select image successfully!")
                } else Log.w(this.toString(), "select image failed")
                /*if error*/
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) { //toast reason of error from crop
                val error = result.getError()
                Toast.makeText(this@Profile, error.message, Toast.LENGTH_SHORT) //toast out error
                    .show()
            }
        }
    }
    /************************************** let user crop new image ***************************************/

}
