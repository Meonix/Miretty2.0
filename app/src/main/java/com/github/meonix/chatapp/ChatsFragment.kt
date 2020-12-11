package com.github.meonix.chatapp


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.github.meonix.chatapp.model.ContactsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

import de.hdodenhof.circleimageview.CircleImageView


/**
 * A simple [Fragment] subclass.
 */
class ChatsFragment : Fragment() {
    private var PrivateChatView: View? = null
    private var chatList: RecyclerView? = null
    private var ChatRef: DatabaseReference? = null
    private var UserRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUserID: String? = null
    companion object {
        /*  Permission request code to draw over other apps  */
        private const val DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE = 1222
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        PrivateChatView = inflater.inflate(R.layout.fragment_chats2, container, false)
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        ChatRef = FirebaseDatabase.getInstance().reference.child("Contacts").child(currentUserID!!)
        UserRef = FirebaseDatabase.getInstance().reference.child("Users")
        chatList = PrivateChatView!!.findViewById(R.id.chats_list)
        chatList!!.layoutManager = LinearLayoutManager(context)
        return PrivateChatView
    }

    fun createFloatingWidget(view: View?, usersIDs: String, Name: String, userImage: String) {
        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context?.packageName}"))
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE)
        }  //If permission is granted start floating widget service
        else{
            startFloatingWidgetService(usersIDs,Name,userImage)
        }
    }
    private fun startFloatingWidgetService(usersIDs: String, Name: String, userImage: String) {
        val intent = Intent(context, FloatingWidgetService::class.java)
        intent.putExtra("visit_user_id", usersIDs)
        intent.putExtra("visit_user_name", Name)
        intent.putExtra("userImage", userImage)

        context?.startService(intent)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE) {
            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) //If permission granted start floating widget service
                Toast.makeText(context, getString(R.string.permission_success), Toast.LENGTH_SHORT).show()
//                startFloatingWidgetService(usersIDs, Name, s)
            else  //Permission is not available then display toast
                Toast.makeText(context,
                        resources.getString(R.string.draw_other_app_permission_denied),
                        Toast.LENGTH_SHORT).show()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<ContactsModel>()
                .setQuery(ChatRef!!, ContactsModel::class.java)
                .build()
        val adapter = object : FirebaseRecyclerAdapter<ContactsModel, ChatsViewHolder>(options) {
            override fun onBindViewHolder(holder: ChatsViewHolder, position: Int, model: ContactsModel) {
                val usersIDs = getRef(position).key

                val Image = arrayOf("default_image")
                UserRef!!.child(usersIDs).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")) {
                                Image[0] = dataSnapshot.child("image").value!!.toString()
                                Picasso.get().load(Image[0]).into(holder.profileImageView)
                            }
                            val Name = dataSnapshot.child("name").value!!.toString()
                            val Status = dataSnapshot.child("status").value!!.toString()
                            holder.userName.text = Name
                            holder.userStatus.text = "Last seen: " + "\n" + "date" + "time"

                            holder.itemView.setOnClickListener {
                                val chatItem = Intent(context, ChatActivity::class.java)
                                chatItem.putExtra("visit_user_id", usersIDs)
                                chatItem.putExtra("visit_user_name", Name)
                                chatItem.putExtra("userImage", Image[0])
                                startActivity(chatItem)
                            }
                            holder.itemView.setOnLongClickListener {
                                Toast.makeText(context, "long click", Toast.LENGTH_SHORT).show()
                                createFloatingWidget(it,usersIDs,Name,Image[0])
                                return@setOnLongClickListener true
                            }
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ChatsViewHolder {
                val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.users_display_layout, viewGroup, false)
                return ChatsViewHolder(view)
            }
        }

        chatList!!.adapter = adapter
        adapter.startListening()

    }


    class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val profileImageView: CircleImageView
        internal val userStatus: TextView
        internal val userName: TextView

        init {
            profileImageView = itemView.findViewById(R.id.users_profile_image)
            userName = itemView.findViewById(R.id.user_profile_name)
            userStatus = itemView.findViewById(R.id.user_status)
        }
    }
}// Required empty public constructor
