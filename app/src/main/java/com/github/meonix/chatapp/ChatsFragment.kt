package com.github.meonix.chatapp


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

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


    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<ContactsModel>()
                .setQuery(ChatRef!!, ContactsModel::class.java!!)
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
