package com.github.meonix.chatapp


import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
class ContactsFragment : Fragment() {

    private var ContactsView: View? = null
    private var mycontactlist: RecyclerView? = null
    private var ContactsRef: DatabaseReference? = null
    private var UserRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUserID: String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false)

        mycontactlist = ContactsView!!.findViewById<View>(R.id.contact_list) as RecyclerView
        mycontactlist!!.layoutManager = LinearLayoutManager(context)
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid


        UserRef = FirebaseDatabase.getInstance().reference.child("Users")
        ContactsRef = FirebaseDatabase.getInstance().reference.child("Contacts").child(currentUserID!!)
        return ContactsView
    }

    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<ContactsModel>()
                .setQuery(ContactsRef!!, ContactsModel::class.java!!)
                .build()
        val adapter = object : FirebaseRecyclerAdapter<ContactsModel, ContactsViewHolder>(options) {
            override fun onBindViewHolder(holder: ContactsViewHolder, position: Int, model: ContactsModel) {
                val userID = getRef(position).key //Take each all the key of Contacts tree

                UserRef!!.child(userID).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.hasChild("image")) {
                            val profileiamge = dataSnapshot.child("image").value!!.toString()
                            val userName = dataSnapshot.child("name").value!!.toString()
                            val userStatus = dataSnapshot.child("status").value!!.toString()

                            holder.username.text = userName
                            holder.userStatus.text = userStatus
                            Picasso.get().load(profileiamge).into(holder.profileImage)
                        } else {
                            val userName = dataSnapshot.child("name").value!!.toString()
                            val userStatus = dataSnapshot.child("status").value!!.toString()
                            holder.username.text = userName
                            holder.userStatus.text = userStatus
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ContactsViewHolder {
                val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.users_display_layout, viewGroup, false)
                return ContactsViewHolder(view)
            }
        }
        mycontactlist!!.adapter = adapter
        adapter.startListening()
    }

    inner class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var username: TextView
        internal var userStatus: TextView
        internal var profileImage: CircleImageView

        init {
            username = itemView.findViewById(R.id.user_profile_name)
            userStatus = itemView.findViewById(R.id.user_status)
            profileImage = itemView.findViewById(R.id.users_profile_image)
        }
    }


}// Required empty public constructor

