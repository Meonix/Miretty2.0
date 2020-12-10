package com.github.meonix.chatapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaRecorder
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.github.meonix.chatapp.adapter.messagePrivateChatAdapter

import com.github.meonix.chatapp.model.MessagesChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap

import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.custom_chat_bar.*

class ChatActivity : AppCompatActivity() {
    private var messageReceiverID: String = ""
    private var messageReceiverName: String = ""
    private var messageUserImage: String = ""
    private var messageSenderID: String = ""
    private var currentDate: String = ""
    private var currentTime: String = ""
    private var currentUser: String = ""
    private var RootRef: DatabaseReference? = null
    private var AudioRef: DatabaseReference? = null
    private var MessageAudioRef: DatabaseReference? = null
    private var ImageRef: DatabaseReference? = null
    private var MessageImageRef: DatabaseReference? = null
    private var NoitificationRef: DatabaseReference? = null
    private var ChatToolbar: Toolbar? = null
    private var mAuth: FirebaseAuth? = null
    private var audioPrivateChat: StorageReference? = null
    private var ImagePrivateChat: StorageReference? = null
    private var videoPrivateChat: StorageReference? = null
    private val messagelist = mutableListOf<MessagesChatModel>()
    private var mRecorder: MediaRecorder? = null
    private var permissionToRecordAccepted = false
    private var mProgress: ProgressDialog? = null
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var UriImageMessage: Uri? = null
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        messageReceiverID = intent.extras!!.get("visit_user_id")!!.toString()
        messageReceiverName = intent.extras!!.get("visit_user_name")!!.toString()
        messageUserImage = intent.extras!!.get("userImage")!!.toString()

        mAuth = FirebaseAuth.getInstance()
        audioPrivateChat = FirebaseStorage.getInstance().reference.child("Audio Messages")
        ImagePrivateChat = FirebaseStorage.getInstance().reference.child("Image Message")
        videoPrivateChat = FirebaseStorage.getInstance().reference.child("Video Messages")
        NoitificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")
        messageSenderID = mAuth!!.currentUser!!.uid

        currentUser = messageSenderID!!
        mProgress = ProgressDialog(this)

        RootRef = FirebaseDatabase.getInstance().reference!!
        InitializeFields()
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)


        userName.text = messageReceiverName
        Picasso.get().load(messageUserImage).placeholder(R.drawable.profile).into(userImage)

        sendMessagebutton.setOnClickListener {
            sendMessage()
            privateMessageChat.post {
//                private_message_chat!!.smoothScrollToPosition(private_message_chat!!.adapter!!.itemCount)
                privateMessageChat!!.scrollToPosition(privateMessageChat!!.adapter!!.itemCount)
            }
        }
        messageInputText.setOnClickListener {
            privateMessageChat.post {
//                private_message_chat!!.smoothScrollToPosition(private_message_chat!!.adapter!!.itemCount)
                privateMessageChat.scrollToPosition(privateMessageChat!!.adapter!!.itemCount)
            }
        }

        btnVideoCall.setOnClickListener {
            val findfriendsIntent = Intent(this@ChatActivity, ViceoCall_Activity::class.java)
            startActivity(findfriendsIntent)
        }


        audioMessageButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {

                startRecording()

            } else if (event.action == MotionEvent.ACTION_UP) {

                stopRecording()

            }
            false
        }

        imageMessageButton.setOnClickListener {
            val GIntent = Intent()
            GIntent.action = Intent.ACTION_GET_CONTENT
            GIntent.type = "*/*"
            startActivityForResult(GIntent, MyPick)
        }

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        privateMessageChat.layoutManager = linearLayoutManager

        messagelist.clear()
        RootRef!!.child("Messages").child(messageSenderID).child(messageReceiverID!!).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, s: String?) {
                val message = dataSnapshot!!.getValue<MessagesChatModel>(MessagesChatModel::class.java)
                messagelist.add(message!!)
                privateMessageChat.adapter?.notifyDataSetChanged()
                privateMessageChat.post {
//                    private_message_chat!!.smoothScrollToPosition(private_message_chat!!.adapter!!.itemCount)
                    privateMessageChat.scrollToPosition(messagelist.size-1)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot?, s: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot?) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot?, s: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError?) {

            }
        })

    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MyPick && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            UriImageMessage = data.data!!
            val cr = this.contentResolver
            val Type = cr.getType(UriImageMessage!!)

            if (Type!!.contains("image/")) {
                if (UriImageMessage != null) {

                    ImageRef = RootRef!!.child("Messages")
                            .child(messageSenderID!!).child(messageReceiverID!!).push()
                    val messagePushID = ImageRef!!.key!!
                    val red = ImagePrivateChat!!.child(currentUser!!).child("$messagePushID.jpg")
                    red.putBytes(compressImage(UriImageMessage!!)!!.toByteArray()).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUrl = task.result.downloadUrl!!.toString()


                            //RootRef.child("Users").child(currentUser).child("BackGround_Image").setValue(downloadUrl);
                            MessageImageRef = RootRef!!.child("Messages").child(messageSenderID!!).child(messageReceiverID!!).child(messagePushID)
                            val messageSenderRef = "Messages/$messageSenderID/$messageReceiverID"
                            val messageReceiverRef = "Messages/$messageReceiverID/$messageSenderID"
                            val calForDate = Calendar.getInstance()
                            val currentDateFormat = SimpleDateFormat("MM dd,yyy")
                            currentDate = currentDateFormat.format(calForDate.time)

                            val calForTime = Calendar.getInstance()
                            val currentTimeFormat = SimpleDateFormat("hh:mm ss a")
                            currentTime = currentTimeFormat.format(calForTime.time)
                            val messageTextBody = HashMap<String,Any>()
                            messageTextBody["from_uid"] = messageSenderID
                            messageTextBody["message"] = downloadUrl
                            messageTextBody["date"] = currentDate
                            messageTextBody["time"] = currentTime
                            messageTextBody["type"] = "image"
                            val messageBodyDetails = HashMap<String,Any>()
                            messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
                            messageBodyDetails["$messageReceiverRef/$messagePushID"] = messageTextBody
                            RootRef!!.updateChildren(messageBodyDetails)
                        }
                    }
                }
            } else if (Type.contains("video/")) {
                if (UriImageMessage != null) {
                    ImageRef = RootRef!!.child("Messages")
                            .child(messageSenderID).child(messageReceiverID).push()
                    val messagePushID = ImageRef!!.key
                    val red = videoPrivateChat!!.child(currentUser).child("$messagePushID.mp4")
                    red.putFile(UriImageMessage!!).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUrl = task.result.downloadUrl!!.toString()


                            //RootRef.child("Users").child(currentUser).child("BackGround_Image").setValue(downloadUrl);
                            MessageImageRef = RootRef!!.child("Messages").child(messageSenderID!!).child(messageReceiverID!!).child(messagePushID)
                            val messageSenderRef = "Messages/$messageSenderID/$messageReceiverID"
                            val messageReceiverRef = "Messages/$messageReceiverID/$messageSenderID"
                            val calForDate = Calendar.getInstance()
                            val currentDateFormat = SimpleDateFormat("MM dd,yyy")
                            currentDate = currentDateFormat.format(calForDate.time)

                            val calForTime = Calendar.getInstance()
                            val currentTimeFormat = SimpleDateFormat("hh:mm ss a")
                            currentTime = currentTimeFormat.format(calForTime.time)
                            val messageTextBody = HashMap<String,Any>()
                            messageTextBody["from_uid"] = messageSenderID
                            messageTextBody["message"] = downloadUrl
                            messageTextBody["date"] = currentDate
                            messageTextBody["time"] = currentTime
                            messageTextBody["type"] = "video"
                            val messageBodyDetails = HashMap<String,Any>()
                            messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
                            messageBodyDetails["$messageReceiverRef/$messagePushID"] = messageTextBody
                            RootRef!!.updateChildren(messageBodyDetails)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Operation has been canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionToRecordAccepted) finish()

    }

    private fun startRecording() {
        AudioRef = RootRef!!.child("Messages")
                .child(messageSenderID!!).child(messageReceiverID!!).push()
        val messagePushID = AudioRef!!.key
        mFileName = externalCacheDir!!.absolutePath + "/" + messagePushID + ".mp3"
        mRecorder = MediaRecorder()

        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mRecorder!!.setOutputFile(mFileName)
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        try {
            mRecorder!!.prepare()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }

        mRecorder!!.start()
    }

    private fun stopRecording() {
        mRecorder!!.stop()
        mRecorder!!.release()
        mRecorder = null
        uploadAudio()
    }

    @SuppressLint("SimpleDateFormat")
    private fun uploadAudio() {
        mProgress!!.setMessage("Sending Your Audio....")
        mProgress!!.show()
        AudioRef = RootRef!!.child("Messages")!!
                .child(messageSenderID).child(messageReceiverID).push()!!
        val messagePushID = AudioRef!!.key!!
        val filepath = audioPrivateChat!!.child(currentUser).child("$messagePushID.mp3")
        val uri = Uri.fromFile(File(mFileName))!!

        filepath.putFile(uri).addOnCompleteListener { task ->
            //delete audio file is saved in storage
            val file = File(uri.path!!)
            file.delete()
            if (file.exists()) {
                try {
                    file.canonicalFile!!.delete()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (file.exists()) {
                    applicationContext!!.deleteFile(file.name)
                }
            }


            val audioDownloadUrl = task.result.downloadUrl!!.toString()
            MessageAudioRef = RootRef!!.child("Messages").child(messageSenderID).child(messageReceiverID!!).child(messagePushID)

            val messageSenderRef = "Messages/$messageSenderID/$messageReceiverID"
            val messageReceiverRef = "Messages/$messageReceiverID/$messageSenderID"
            val calForDate = Calendar.getInstance()
            val currentDateFormat = SimpleDateFormat("MM dd,yyy")
            currentDate = currentDateFormat.format(calForDate.time)

            val calForTime = Calendar.getInstance()
            val currentTimeFormat = SimpleDateFormat("hh:mm ss a")
            currentTime = currentTimeFormat.format(calForTime.time)
            val messageTextBody = HashMap<String,Any>()
            messageTextBody["from_uid"] = messageSenderID
            messageTextBody["message"] = audioDownloadUrl
            messageTextBody["date"] = currentDate
            messageTextBody["time"] = currentTime
            messageTextBody["type"] = "audio"
            val messageBodyDetails = HashMap<String,Any>()
            messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
            messageBodyDetails["$messageReceiverRef/$messagePushID"] = messageTextBody
            RootRef!!.updateChildren(messageBodyDetails)
            Toast.makeText(applicationContext, "done", Toast.LENGTH_SHORT).show()
            mProgress!!.dismiss()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (mRecorder != null) {
            mRecorder!!.release()
            mRecorder = null
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun sendMessage() {
        val messageTExt = messageInputText.text.toString()
        if (TextUtils.isEmpty(messageTExt)) {
            Toast.makeText(this, "write your message ....", Toast.LENGTH_SHORT).show()
        } else {
            val messageSenderRef = "Messages/$messageSenderID/$messageReceiverID"
            val messageReceiverRef = "Messages/$messageReceiverID/$messageSenderID"

            val userMessageKeyRef = RootRef!!.child("Messages")
                    .child(messageSenderID!!).child(messageReceiverID!!).push()
            val messagePushID = userMessageKeyRef.key
            val calForDate = Calendar.getInstance()
            val currentDateFormat = SimpleDateFormat("MM dd,yyy")
            currentDate = currentDateFormat.format(calForDate.time)

            val calForTime = Calendar.getInstance()
            val currentTimeFormat = SimpleDateFormat("hh:mm ss a")
            currentTime = currentTimeFormat.format(calForTime.time)
            val messageTextBody = HashMap<String,Any>()
            messageTextBody["from_uid"] = messageSenderID!!
            messageTextBody["message"] = messageTExt
            messageTextBody["date"] = currentDate!!
            messageTextBody["time"] = currentTime!!
            messageTextBody.put("type", "text")
            val messageBodyDetails = HashMap<String,Any>()
            messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
            messageBodyDetails["$messageReceiverRef/$messagePushID"] = messageTextBody
            RootRef!!.updateChildren(messageBodyDetails)

            val chatnotificationHasmap = HashMap<String, String>()
            chatnotificationHasmap["from"] = messageSenderID.toString()
            chatnotificationHasmap["type"] = "request"
            NoitificationRef!!.child(messageReceiverID!!).push()
                    .setValue(chatnotificationHasmap)

            messageInputText.setText("")
        }
    }


    @SuppressLint("InflateParams")
    private fun InitializeFields() {
        ChatToolbar = findViewById(R.id.chat_toolbar)
        setSupportActionBar(ChatToolbar)

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayShowCustomEnabled(true)

        val layoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null)
        actionbar.customView = actionBarView!!


        val messageAdapter = messagePrivateChatAdapter(messagelist)
        privateMessageChat.adapter = messageAdapter

    }

    override fun onStart() {
        super.onStart()
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        privateMessageChat!!.layoutManager = linearLayoutManager
        privateMessageChat!!.post {
//            private_message_chat!!.smoothScrollToPosition(private_message_chat!!.adapter!!.itemCount)
            privateMessageChat!!.scrollToPosition(privateMessageChat!!.adapter!!.itemCount)
        }
    }

    private fun getResizedBitmap(bm: Bitmap?, newWidth: Int?, newHeight: Int?): Bitmap {
        val width = bm!!.width
        val height = bm!!.height
        val scaleWidth = newWidth!!.toFloat() / width
        val scaleHeight = newHeight!!.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false)
    }

    private fun compressImage(image: Uri?): ByteArrayOutputStream? {
        var original: Bitmap?
        try {
            original = MediaStore.Images.Media.getBitmap(this.contentResolver, image!!)
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
        private var mFileName: String? = null
        private val LOG_TAG = "AudioRecordTest"
        private val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private val MyPick = 2
    }
}