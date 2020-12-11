package com.github.meonix.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {
    //Create extreme type variable and
    private var receiverUserID: String? = null
    private var senderUserID: String? = null
    private var Current_State: String? = null
    private var userProfileImage: CircleImageView? = null
    private var userProfileName: TextView? = null
    private var userProfileStatus: TextView? = null
    private var SendMessageRequestButton: Button? = null
    private var DeclineMessageRequestButton: Button? = null
    private var mToolbar: Toolbar? = null
    private var backGround_visit_image: ImageView? = null

    private var mAuth: FirebaseAuth? = null
    private var UserRef: DatabaseReference? = null
    private var ChatRequestRef: DatabaseReference? = null
    private var ContactRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = FirebaseAuth.getInstance()
        UserRef = FirebaseDatabase.getInstance().reference.child("Users")
        ChatRequestRef = FirebaseDatabase.getInstance().reference.child("Chat Requests")
        ContactRef = FirebaseDatabase.getInstance().reference.child("Contacts")

        receiverUserID = intent.extras!!.get("visit_user_id")!!.toString()

        senderUserID = mAuth!!.currentUser!!.uid  //take user ID of user in the database


        backGround_visit_image = findViewById<View>(R.id.background_visit_image) as ImageView
        userProfileImage = findViewById<View>(R.id.visit_profile_image) as CircleImageView
        userProfileName = findViewById<View>(R.id.visit_user_name) as TextView
        userProfileStatus = findViewById<View>(R.id.visit_profile_status) as TextView
        SendMessageRequestButton = findViewById<View>(R.id.send_message_request_button) as Button
        DeclineMessageRequestButton = findViewById<View>(R.id.decline_message_request_button) as Button
        mToolbar = findViewById<View>(R.id.visit_friends_toolbar) as Toolbar

        Current_State = "new"

        ReTriveUserInfo()
        //This is combined with android:parentActivityName=".FindFriendsActivity" in the AndroidManifest.xml
        // we  will have position to return to FindFriendsActivity in the toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)


    }

    private fun ReTriveUserInfo() {
        UserRef!!.child(receiverUserID!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //check if userID exists and image also exists
                // we load data form database and change data in app
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image") && dataSnapshot.hasChild("BackGround_Image")) {
                    val userImage = dataSnapshot.child("image").value!!.toString()
                    val userName = dataSnapshot.child("name").value!!.toString()
                    val userStatus = dataSnapshot.child("status").value!!.toString()
                    supportActionBar!!.title = userName                //set Title is the name of user
                    val Background = dataSnapshot.child("BackGround_Image").value!!.toString()

                    //                    Picasso.get().load(Background).placeholder(R.drawable.profile).fit().into(backGround_visit_image);
                    Picasso.get().load(Background).placeholder(R.drawable.profile).into(backGround_visit_image)
                    Picasso.get().load(userImage).placeholder(R.drawable.profile).into(userProfileImage)

                    userProfileName!!.text = userName
                    userProfileStatus!!.text = userStatus

                    ManageChatRequest()

                } else if (dataSnapshot.exists() && dataSnapshot.hasChild("image") && !dataSnapshot.hasChild("BackGround_Image")) {
                    val userImage = dataSnapshot.child("image").value!!.toString()
                    val userName = dataSnapshot.child("name").value!!.toString()
                    val userStatus = dataSnapshot.child("status").value!!.toString()
                    supportActionBar!!.title = userName                //set Title is the name of user
                    Picasso.get().load(userImage).placeholder(R.drawable.profile).into(userProfileImage)

                    userProfileName!!.text = userName
                    userProfileStatus!!.text = userStatus

                    ManageChatRequest()
                } else {
                    val userName = dataSnapshot.child("name").value!!.toString()
                    val userStatus = dataSnapshot.child("status").value!!.toString()
                    supportActionBar!!.title = userName//set Title is the name of user
                    userProfileName!!.text = userName
                    userProfileStatus!!.text = userStatus

                    ManageChatRequest()
                }//When user don't have profile picture but user ID exists
                // We do not change them's profile picture and we set them's name and status
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun ManageChatRequest() {
        //The code below is used to when current user click user received the request button will be change Cancel Chat Request
        ChatRequestRef!!.child(senderUserID!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserID!!))
                        //check if uri of user receiver,which is child of Chat Request and has been exists
                        {
                            val request_type = dataSnapshot.child(receiverUserID!!).child("request_type").value!!.toString()
                            if (request_type == "sent") {
                                Current_State = "request_send"
                                SendMessageRequestButton!!.text = "Cancel Chat Request"
                            } else if (request_type == "received") {
                                Current_State = "request_received"
                                SendMessageRequestButton!!.text = "Accept Chat Request"

                                DeclineMessageRequestButton!!.visibility = View.VISIBLE
                                DeclineMessageRequestButton!!.isEnabled = true
                                DeclineMessageRequestButton!!.setOnClickListener { CancelChatRequest() }
                            }
                        } else {
                            ContactRef!!.child(senderUserID!!)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID!!)) {
                                                Current_State = "friends"
                                                SendMessageRequestButton!!.text = "Remove this contact"
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {

                                        }
                                    })
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
        //check if userID of current user not equal to userID (which is current user is looking for)
        if (senderUserID != receiverUserID) {
            SendMessageRequestButton!!.setOnClickListener {
                SendMessageRequestButton!!.isEnabled = false
                if (Current_State == "new") {
                    SendChatRequest()
                }
                if (Current_State == "request_send") {
                    CancelChatRequest()
                }
                if (Current_State == "request_received") {
                    AcceptChatRequest()
                }
                if (Current_State == "friends") {
                    RemoveSpecifitContact()
                }
            }

        } else {
            SendMessageRequestButton!!.visibility = View.VISIBLE
        }//if current userID find friend and They choose their profile we have to Visible button send message

    }

    private fun RemoveSpecifitContact() {
        ContactRef!!.child(senderUserID!!).child(receiverUserID!!)
                .removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        ContactRef!!.child(receiverUserID!!).child(senderUserID!!)
                                .removeValue()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        SendMessageRequestButton!!.isEnabled = true
                                        Current_State = "new"
                                        SendMessageRequestButton!!.text = "Send Message"

                                        DeclineMessageRequestButton!!.visibility = View.INVISIBLE
                                        DeclineMessageRequestButton!!.isEnabled = false
                                    }
                                }
                    }
                }
    }

    private fun AcceptChatRequest() {
        //Create child of Contact tree saved and then Remove request of senderUser and receiverUser
        // We do that the same time in method AcceptChatRequest()
        ContactRef!!.child(senderUserID!!).child(receiverUserID!!)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        ContactRef!!.child(receiverUserID!!).child(senderUserID!!)
                                .child("Contacts").setValue("Saved")
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        ChatRequestRef!!.child(senderUserID!!).child(receiverUserID!!)
                                                .removeValue().addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        ChatRequestRef!!.child(receiverUserID!!).child(senderUserID!!)
                                                                .removeValue().addOnCompleteListener {
                                                                    SendMessageRequestButton!!.isEnabled = true
                                                                    Current_State = "friends"
                                                                    SendMessageRequestButton!!.text = "Remove this Contact"
                                                                    DeclineMessageRequestButton!!.visibility = View.INVISIBLE
                                                                    DeclineMessageRequestButton!!.isEnabled = false
                                                                }
                                                    }
                                                }
                                    }
                                }
                    }
                }


    }

    private fun CancelChatRequest() {
        //Remove the Chat Request tree in the database when user click cancel request
        ChatRequestRef!!.child(senderUserID!!).child(receiverUserID!!)
                .removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        ChatRequestRef!!.child(receiverUserID!!).child(senderUserID!!)
                                .removeValue()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        SendMessageRequestButton!!.isEnabled = true
                                        Current_State = "new"
                                        SendMessageRequestButton!!.text = "Send Message"

                                        DeclineMessageRequestButton!!.visibility = View.INVISIBLE
                                        DeclineMessageRequestButton!!.isEnabled = false
                                    }
                                }
                    }
                }

    }

    private fun SendChatRequest() {
        //senderUserID is uid of current user (senderUserID=mAuth.getCurrentUser().getUid();)
        // And write this uid to Chat Requests Tree (the tree data at database)
        //receiverUserID is uid of current user looking for (receiverUserID = getIntent().getExtras().get("visit_user_id").toString();)
        //And then write uid of current user looking for to Chat Request Tree (that's child of current user's uid
        ChatRequestRef!!.child(senderUserID!!).child(receiverUserID!!)
                .child("request_type").setValue("sent")     //write "sent" in the child of uid ()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //When write uid current user and write child of this uid,we write another tree (tree of user receive the request)
                        // That's the same the Tree of current's uid
                        ChatRequestRef!!.child(receiverUserID!!).child(senderUserID!!)
                                .child("request_type").setValue("received")//write received in the tree of user receive the request
                                .addOnCompleteListener { task ->
                                    run {
                                        if (task.isSuccessful) {
                                            SendMessageRequestButton!!.isEnabled = true
                                            Current_State = "request_send"
                                            SendMessageRequestButton!!.text = "Cancel Chat Request"
                                        }
                                    }
                                }

                    }
                }
    }
}
