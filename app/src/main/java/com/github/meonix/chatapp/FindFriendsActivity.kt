package com.github.meonix.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.github.meonix.chatapp.FindFriendsActivity.FindFriendViewHolder
import com.github.meonix.chatapp.model.ContactsModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

import de.hdodenhof.circleimageview.CircleImageView

class FindFriendsActivity : AppCompatActivity() {
    private var mToolbar: Toolbar? = null
    private var FindFriendsRecyclerList: RecyclerView? = null
    private var UserRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)

        UserRef = FirebaseDatabase.getInstance().reference.child("Users")

        FindFriendsRecyclerList = findViewById<View>(R.id.find_friends_recycle_list) as RecyclerView
        FindFriendsRecyclerList!!.layoutManager = LinearLayoutManager(this)   //000


        mToolbar = findViewById<View>(R.id.find_friends_toolbar) as Toolbar

        //This is combined with android:parentActivityName=".MainActivity" in the AndroidManifest.xml
        // we  will have position to return to MainActivity in the toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Find Friends"
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<ContactsModel>().setQuery(UserRef!!, ContactsModel::class.java!!).build()

        val adapter = object : FirebaseRecyclerAdapter<ContactsModel, FindFriendViewHolder>(options) {
            override fun onBindViewHolder(holder: FindFriendViewHolder, position: Int, model: ContactsModel) {
                holder.userName.text = model.name
                holder.userStatus.text = model.status
                Picasso.get().load(model.image).placeholder(R.drawable.profile).into(holder.profileImage)

                // add method .placeholder(R.drawable.profile) to when user don't have any picture we will setup default picture is profile in folder drawable

                holder.itemView.setOnClickListener {
                    val visit_user_id = getRef(position).key

                    val profileIntent = Intent(this@FindFriendsActivity, ProfileActivity::class.java)
                    profileIntent.putExtra("visit_user_id", visit_user_id) //Send this ID to the ProfileActivity
                    startActivity(profileIntent)
                }
            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): FindFriendViewHolder {
                val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.users_display_layout, viewGroup, false)
                return FindFriendViewHolder(view)
            }
        }
        FindFriendsRecyclerList!!.adapter = adapter

        adapter.startListening()
    }

    class FindFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var userName: TextView
        internal var userStatus: TextView
        internal var profileImage: CircleImageView

        init {
            userName = itemView.findViewById(R.id.user_profile_name)
            userStatus = itemView.findViewById(R.id.user_status)
            profileImage = itemView.findViewById(R.id.users_profile_image)
        }
    }
}
