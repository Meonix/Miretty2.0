package com.github.meonix.chatapp

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.github.meonix.chatapp.RequestsFragment.RequestsViewHolder
import com.github.meonix.chatapp.model.ContactsModel
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


/**
 * A simple [Fragment] subclass. */
class RequestsFragment : Fragment() {

    private var RequestsFragmentView: View? = null
    private var myRequestsList: RecyclerView? = null
    private var ChatRequestRef: DatabaseReference? = null
    private var UserRef: DatabaseReference? = null
    private var ContactsRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null  //use mAuth to take current user
    private var currentUserID: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        RequestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false)

        myRequestsList = RequestsFragmentView!!.findViewById<View>(R.id.chat_request_list) as RecyclerView
        myRequestsList!!.layoutManager = LinearLayoutManager(context)

        ContactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")
        ChatRequestRef = FirebaseDatabase.getInstance().reference.child("Chat Requests")
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid  //get current UserID
        UserRef = FirebaseDatabase.getInstance().reference.child("Users")
        return RequestsFragmentView
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<ContactsModel>()
                .setQuery(ChatRequestRef!!.child(currentUserID!!), ContactsModel::class.java!!)
                .build()
        val adapter = object : FirebaseRecyclerAdapter<ContactsModel, RequestsViewHolder>(options) {
            override fun onBindViewHolder(holder: RequestsViewHolder, position: Int, model: ContactsModel) {
                val visit_user_id = getRef(position).key

                val list_user_id = getRef(position).key

                val getTypeRef = getRef(position).child("request_type").ref

                getTypeRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val type = dataSnapshot.value!!.toString()
                            if (type == "received") {
                                holder.itemView.findViewById<View>(R.id.request_accept_btn).visibility = View.VISIBLE
                                holder.itemView.findViewById<View>(R.id.request_cancel_btn).visibility = View.VISIBLE
                                UserRef!!.child(list_user_id).addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        if (dataSnapshot.hasChild("image")) {
                                            val requestUserName = dataSnapshot.child("name").value!!.toString()

                                            val requestUserStatus = dataSnapshot.child("status").value!!.toString()
                                            val requestProfileImage = dataSnapshot.child("image").value!!.toString()

                                            holder.userName.text = requestUserName
                                            holder.userStatus.text = requestUserStatus
                                            Picasso.get().load(requestProfileImage).into(holder.profileImage)
                                        } else {
                                            val requestUserName = dataSnapshot.child("name").value!!.toString()
                                            val requestUserStatus = dataSnapshot.child("status").value!!.toString()

                                            holder.userName.text = requestUserName
                                            holder.userStatus.text = requestUserStatus
                                        }
                                        holder.itemView.setOnClickListener {
                                            val profileIntent = Intent(activity, ProfileActivity::class.java)
                                            profileIntent.putExtra("visit_user_id", visit_user_id) //Send this ID to the ProfileActivity
                                            startActivity(profileIntent)
                                        }
                                        holder.AcceptButton.setOnClickListener {
                                            ContactsRef!!.child(currentUserID!!).child(list_user_id)
                                                    .child("Contacts").setValue("Saved").addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            ContactsRef!!.child(list_user_id).child(currentUserID!!)
                                                                    .child("Contacts").setValue("Saved").addOnCompleteListener { task ->
                                                                        if (task.isSuccessful) {
                                                                            ChatRequestRef!!.child(currentUserID!!).child(list_user_id)
                                                                                    .removeValue().addOnCompleteListener { task ->
                                                                                        if (task.isSuccessful) {
                                                                                            ChatRequestRef!!.child(list_user_id).child(currentUserID!!)
                                                                                                    .removeValue().addOnCompleteListener { task ->
                                                                                                        if (task.isSuccessful) {
                                                                                                            Toast.makeText(context, "Add Friend successfully", Toast.LENGTH_SHORT).show()
                                                                                                        }
                                                                                                    }
                                                                                        }
                                                                                    }
                                                                        }
                                                                    }
                                                        }
                                                    }
                                        }
                                        holder.CancelButton.setOnClickListener {
                                            ChatRequestRef!!.child(currentUserID!!).child(list_user_id)
                                                    .removeValue().addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            ChatRequestRef!!.child(list_user_id).child(currentUserID!!)
                                                                    .removeValue().addOnCompleteListener { task ->
                                                                        if (task.isSuccessful) {
                                                                            Toast.makeText(context, "The request has been rejected", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                        }
                                                    }
                                        }

                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {

                                    }
                                })
                            } else {
                                holder.itemView.findViewById<View>(R.id.request_cancel_btn).visibility = View.VISIBLE
                                UserRef!!.child(list_user_id).addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        if (dataSnapshot.hasChild("image")) {
                                            val requestUserName = dataSnapshot.child("name").value!!.toString()

                                            val requestUserStatus = dataSnapshot.child("status").value!!.toString()
                                            val requestProfileImage = dataSnapshot.child("image").value!!.toString()

                                            holder.userName.text = requestUserName
                                            holder.userStatus.text = requestUserStatus
                                            Picasso.get().load(requestProfileImage).into(holder.profileImage)
                                        } else {
                                            val requestUserName = dataSnapshot.child("name").value!!.toString()
                                            val requestUserStatus = dataSnapshot.child("status").value!!.toString()

                                            holder.userName.text = requestUserName
                                            holder.userStatus.text = requestUserStatus
                                        }
                                        holder.itemView.setOnClickListener {
                                            val profileIntent = Intent(activity, ProfileActivity::class.java)
                                            profileIntent.putExtra("visit_user_id", visit_user_id) //Send this ID to the ProfileActivity
                                            startActivity(profileIntent)
                                        }




                                        holder.CancelButton.setOnClickListener {
                                            ChatRequestRef!!.child(currentUserID!!).child(list_user_id)
                                                    .removeValue().addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            ChatRequestRef!!.child(list_user_id).child(currentUserID!!)
                                                                    .removeValue().addOnCompleteListener { task ->
                                                                        if (task.isSuccessful) {
                                                                            Toast.makeText(context, "The request has been rejected", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                        }
                                                    }
                                        }

                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {

                                    }
                                })
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RequestsViewHolder {
                val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.users_display_layout, viewGroup, false)
                return RequestsViewHolder(view)
            }
        }
        myRequestsList!!.adapter = adapter
        adapter.startListening()

    }

    class RequestsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var userName: TextView
        internal var userStatus: TextView
        internal var profileImage: CircleImageView
        internal var AcceptButton: Button
        internal var CancelButton: Button

        init {

            userName = itemView.findViewById(R.id.user_profile_name)
            userStatus = itemView.findViewById(R.id.user_status)
            profileImage = itemView.findViewById(R.id.users_profile_image)
            AcceptButton = itemView.findViewById(R.id.request_accept_btn)
            CancelButton = itemView.findViewById(R.id.request_cancel_btn)
        }
    }
}// Required empty public constructor


