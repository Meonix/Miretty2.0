package com.github.meonix.chatapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

import com.github.meonix.chatapp.adapter.AdapterMessage
import com.github.meonix.chatapp.model.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap

class GroupChatActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null
    private var SendMessageButton: ImageButton? = null
    private var userMessageInput: EditText? = null
    private var recyclerView: RecyclerView? = null
    private var mAuth: FirebaseAuth? = null
    private var UserRef: DatabaseReference? = null
    private var GroupNameRef: DatabaseReference? = null
    private var GroupMessageKeyRef: DatabaseReference? = null
    private var NoitificationRef: DatabaseReference? = null
    private var currentGroupName: String? = null
    private var currentUserID: String? = null
    private var currentUserName: String? = null
    private var currentDate: String? = null
    private var currentTime: String? = null

    private val listMessage = ArrayList<MessageModel>()
    private var messageAdapter: AdapterMessage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)


        currentGroupName = intent.extras!!.get("groupName")!!.toString()
        Toast.makeText(this@GroupChatActivity, currentGroupName, Toast.LENGTH_SHORT).show()

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        UserRef = FirebaseDatabase.getInstance().reference.child("Users")
        GroupNameRef = FirebaseDatabase.getInstance().reference.child("Groups").child(currentGroupName!!)
        NoitificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")

        InitializeFields()

        GetUserInfo()
        recyclerView!!.adapter = messageAdapter
        SendMessageButton!!.setOnClickListener {
            SaveMessageInfoToDatabase()
            recyclerView!!.post { recyclerView!!.smoothScrollToPosition(recyclerView!!.adapter!!.itemCount) }
            userMessageInput!!.setText("")
        }
        userMessageInput!!.setOnClickListener { recyclerView!!.post { recyclerView!!.smoothScrollToPosition(recyclerView!!.adapter!!.itemCount) } }
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        recyclerView!!.layoutManager = linearLayoutManager
        listMessage.clear()
        GroupNameRef!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, s: String?) {
                val message = dataSnapshot!!.getValue<MessageModel>(MessageModel::class.java)
                listMessage.add(message!!)
                messageAdapter!!.notifyDataSetChanged()
                recyclerView!!.post { recyclerView!!.smoothScrollToPosition(recyclerView!!.adapter!!.itemCount) }

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun InitializeFields() {
        mToolbar = findViewById(R.id.group_chat_bar_layout)
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = currentGroupName

        SendMessageButton = findViewById(R.id.send_message_button)
        userMessageInput = findViewById(R.id.input_group_message)
        recyclerView = findViewById(R.id.groupChatRecyclerDisplay)
        messageAdapter = AdapterMessage(listMessage)
    }

    private fun GetUserInfo() {
        UserRef!!.child(currentUserID!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").value!!.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }


    @SuppressLint("SimpleDateFormat")
    private fun SaveMessageInfoToDatabase() {
        val message = userMessageInput!!.text.toString()
        val messageKey = GroupNameRef!!.push().key
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show()
        } else {
            val calForDate = Calendar.getInstance()
            val currentDateFormat = SimpleDateFormat("MM dd,yyy")
            currentDate = currentDateFormat.format(calForDate.time)

            val calForTime = Calendar.getInstance()
            val currentTimeFormat = SimpleDateFormat("hh:mm ss a")
            currentTime = currentTimeFormat.format(calForTime.time)

            val groupMessageKey = HashMap<String, Any>()
            GroupNameRef!!.updateChildren(groupMessageKey)

            GroupMessageKeyRef = GroupNameRef!!.child(messageKey)
            val messageInfoMap = HashMap<String, Any>()
            messageInfoMap["uid"] = currentUserID.toString()
            messageInfoMap["name"] = currentUserName.toString()
            messageInfoMap["message"] = message
            messageInfoMap["date"] = currentDate.toString()
            messageInfoMap["time"] = currentTime.toString()
            GroupMessageKeyRef!!.updateChildren(messageInfoMap)


            val chatnotificationHasmap = HashMap<String, String>()
            chatnotificationHasmap["from"] = currentUserID.toString()
            chatnotificationHasmap["type"] = "request"
            //            NoitificationRef.child("rciFFlsy3zWbSgBcgrC5k1o2nbt1").push().setValue(chatnotificationHasmap)
            //                    .addOnCompleteListener(new OnCompleteListener<Void>() {
            //                        @Override
            //                        public void onComplete(@NonNull Task<Void> task) {
            //                            if (task.isSuccessful()) {
            //
            //                            }
            //                        }
            //                    });
        }
    }

    override fun onStart() {
        super.onStart()
        recyclerView!!.post { recyclerView!!.smoothScrollToPosition(recyclerView!!.adapter!!.itemCount) }
    }
}
