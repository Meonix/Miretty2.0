package com.github.meonix.chatapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.HashMap

import de.hdodenhof.circleimageview.CircleImageView

class SettingActivity : AppCompatActivity() {

    private var UdateAccountSettings: Button? = null
    private var backgroundProfileImage: ImageView? = null
    private var userName: EditText? = null
    private var userStatus: EditText? = null
    private var userProfileImage: CircleImageView? = null
    private var UriImagebackground: Uri? = null
    private var mToolbar: Toolbar? = null
    private var currentUserID: String? = null
    private var mAuth: FirebaseAuth? = null
    private var RootRef: DatabaseReference? = null
    private var storage: FirebaseStorage? = null
    private var UserProfileImageRef: StorageReference? = null
    private var UserBackGroundImage: StorageReference? = null
    private var loadingBar: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        RootRef = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()
        UserProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")
        UserBackGroundImage = FirebaseStorage.getInstance().reference.child("BackGround Images")

        InitializeFields()

        userName!!.visibility = View.INVISIBLE


        UdateAccountSettings!!.setOnClickListener { UpdateSetting() }


        RetrieveUserInto()

        setSupportActionBar(mToolbar)
        mToolbar!!.title = "Setting"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        userProfileImage!!.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GalleryPick)
        }

        backgroundProfileImage!!.setOnClickListener {
            val GIntent = Intent()
            GIntent.action = Intent.ACTION_GET_CONTENT
            GIntent.type = "image/*"
            startActivityForResult(GIntent, MyPick)
        }

    }

    override fun onStart() {
        super.onStart()
        RetrieveUserInto()
    }

    private fun InitializeFields() {
        UdateAccountSettings = findViewById<View>(R.id.update_settings_buttton) as Button
        userName = findViewById<View>(R.id.set_user_name) as EditText
        mToolbar = findViewById<View>(R.id.setting_toolbar) as Toolbar
        userStatus = findViewById<View>(R.id.set_profile_status) as EditText
        userProfileImage = findViewById<View>(R.id.set_profile_image) as CircleImageView
        backgroundProfileImage = findViewById<View>(R.id.background_profile_image) as ImageView
        loadingBar = ProgressDialog(this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MyPick && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            UriImagebackground = data.data

            loadingBar!!.setTitle("Set BackGround Image")
            loadingBar!!.setMessage("Please wait ,your  backGround image is updating....")
            loadingBar!!.setCanceledOnTouchOutside(false)
            loadingBar!!.show()

            if (UriImagebackground != null) {
                val red = UserBackGroundImage!!.child(currentUserID!! + ".jpg")
                red.putBytes(compressImage(UriImagebackground!!)!!.toByteArray()).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@SettingActivity, "Background Image uploaded Successfully...", Toast.LENGTH_SHORT).show()
                        val downloadUrl = task.result.downloadUrl!!.toString()
                        RootRef!!.child("Users").child(currentUserID!!).child("BackGround_Image").setValue(downloadUrl).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@SettingActivity, "Image save in Database Successfully.....", Toast.LENGTH_SHORT).show()
                                loadingBar!!.dismiss()
                            } else {
                                val message = task.exception!!.toString()
                                Toast.makeText(this@SettingActivity, "Error$message", Toast.LENGTH_SHORT).show()
                                loadingBar!!.dismiss()
                            }
                        }
                    } else {
                        val message = task.exception!!.toString()
                        Toast.makeText(this@SettingActivity, "Error :$message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        if (requestCode == GalleryPick && resultCode == Activity.RESULT_OK && data != null) {

            val ImageUri = data.data
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON).start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                val result = CropImage.getActivityResult(data)

                loadingBar!!.setTitle("Set Profile Image")
                loadingBar!!.setMessage("Please wait ,your  profile image is updating....")
                loadingBar!!.setCanceledOnTouchOutside(false)
                loadingBar!!.show()

                val resultUri = result.uri

                val filePath = UserProfileImageRef!!.child(currentUserID!! + ".jpg")

                filePath.putBytes(compressImage(resultUri)!!.toByteArray()).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@SettingActivity, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show()
                        val downloadUrl = task.result.downloadUrl!!.toString()
                        RootRef!!.child("Users").child(currentUserID!!).child("image").setValue(downloadUrl).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@SettingActivity, "Image save in Database Successfully.....", Toast.LENGTH_SHORT).show()
                                loadingBar!!.dismiss()
                            } else {
                                val message = task.exception!!.toString()
                                Toast.makeText(this@SettingActivity, "Error$message", Toast.LENGTH_SHORT).show()
                                loadingBar!!.dismiss()
                            }
                        }
                    } else {
                        val message = task.exception!!.toString()
                        Toast.makeText(this@SettingActivity, "Error :$message", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }


    }

    private fun UpdateSetting() {
        val setUserName = userName!!.text.toString()
        val setStatus = userStatus!!.text.toString()
        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(setStatus)) {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show()
        } else {
            RootRef!!.child("Users").child(currentUserID!!).child("name").setValue(setUserName).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    SendUserToMainActivity()
                    Toast.makeText(this@SettingActivity, "Name Updated Successfully..", Toast.LENGTH_SHORT).show()
                } else {
                    val message = task.exception!!.toString()
                    Toast.makeText(this@SettingActivity, "Error$message", Toast.LENGTH_SHORT).show()
                }
            }
            RootRef!!.child("Users").child(currentUserID!!).child("status").setValue(setStatus).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    SendUserToMainActivity()
                    Toast.makeText(this@SettingActivity, "status Updated Successfully..", Toast.LENGTH_SHORT).show()
                } else {
                    val message = task.exception!!.toString()
                    Toast.makeText(this@SettingActivity, "Error$message", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }


    private fun RetrieveUserInto() {
        RootRef!!.child("Users").child(currentUserID!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("status") && dataSnapshot.hasChild("image") && dataSnapshot.hasChild("BackGround_Image")) {
                    val retriveUserName = dataSnapshot.child("name").value!!.toString()
                    val retriveStatus = dataSnapshot.child("status").value!!.toString()
                    val retriveProfileImage = dataSnapshot.child("image").value!!.toString()
                    val retriveBackground = dataSnapshot.child("BackGround_Image").value!!.toString()

                    userName!!.setText(retriveUserName)
                    userStatus!!.setText(retriveStatus)
                    Picasso.get().load(retriveProfileImage).into(userProfileImage)
                    Picasso.get().load(retriveBackground).into(backgroundProfileImage)
                } else if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("status")
                        && dataSnapshot.hasChild("image") && !dataSnapshot.hasChild("BackGround_Image")) {
                    val retriveUserName = dataSnapshot.child("name").value!!.toString()
                    val retriveStatus = dataSnapshot.child("status").value!!.toString()
                    val retriveProfileImage = dataSnapshot.child("image").value!!.toString()

                    userName!!.setText(retriveUserName)
                    userStatus!!.setText(retriveStatus)
                    Picasso.get().load(retriveProfileImage).into(userProfileImage)
                } else if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("status")
                        && !dataSnapshot.hasChild("image") && dataSnapshot.hasChild("BackGround_Image")) {
                    val retriveUserName = dataSnapshot.child("name").value!!.toString()
                    val retriveStatus = dataSnapshot.child("status").value!!.toString()
                    val retriveBackground = dataSnapshot.child("BackGround_Image").value!!.toString()

                    userName!!.setText(retriveUserName)
                    userStatus!!.setText(retriveStatus)
                    Picasso.get().load(retriveBackground).into(backgroundProfileImage)
                } else if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("status") && !dataSnapshot.hasChild("image") && !dataSnapshot.hasChild("BackGround_Image")) {
                    val retriveUserName = dataSnapshot.child("name").value!!.toString()
                    val retriveStatus = dataSnapshot.child("status").value!!.toString()

                    userName!!.setText(retriveUserName)
                    userStatus!!.setText(retriveStatus)
                } else {
                    userName!!.visibility = View.VISIBLE
                    Toast.makeText(this@SettingActivity, "Please set & update your profile information.....", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }


    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@SettingActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        startActivity(mainIntent)
        finish()
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false)
    }

    fun compressImage(image: Uri): ByteArrayOutputStream? {
        var original: Bitmap? = null
        try {
            original = MediaStore.Images.Media.getBitmap(this.contentResolver, image)
        } catch (e: IOException) {
            return null
        }

        val height = original!!.height
        var width = original.width
        if (height > 2048) {
            width = (width * (2048.0f / height)).toInt()
            original = getResizedBitmap(original, width, 2048)
        }

        val out = ByteArrayOutputStream()
        original.compress(Bitmap.CompressFormat.JPEG, 47, out)
        return out
    }

    companion object {

        private val GalleryPick = 1
        private val MyPick = 2
    }
}
