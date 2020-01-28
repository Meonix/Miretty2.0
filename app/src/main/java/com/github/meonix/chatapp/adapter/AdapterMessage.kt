package com.github.meonix.chatapp.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.github.meonix.chatapp.R
import com.github.meonix.chatapp.model.MessageModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

import de.hdodenhof.circleimageview.CircleImageView

 public class AdapterMessage( val messageList: List<MessageModel>) : RecyclerView.Adapter<AdapterMessage.ViewHolder>() {
    private var mAuth: FirebaseAuth? = null
    private val UserRef = FirebaseDatabase.getInstance().reference.child("Users")

    inner class ViewHolder(var layout: View) : RecyclerView.ViewHolder(layout) {
        var cvAvatar: CircleImageView
        var message_time: TextView
        var message_user: TextView
        var message_text: TextView
        var message_date: TextView

        init {
            cvAvatar = layout.findViewById(R.id.cvAvatar)
            message_text = layout.findViewById(R.id.message_text)
            message_time = layout.findViewById(R.id.message_time)
            message_user = layout.findViewById(R.id.message_user)
            message_date = layout.findViewById(R.id.message_date)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        //        View v = inflater.inflate(R.layout.item_chat_message, parent, false);
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        mAuth = FirebaseAuth.getInstance()
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val time = messageList[position].time
        viewHolder.message_user.text = messageList[position].name
        viewHolder.message_text.text = messageList[position].message
        viewHolder.message_date.text = messageList[position].date
        viewHolder.message_time.text = time
        // TODO: set avater
        val uid = messageList[position].uid

        UserRef.child(messageList[position].uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                    val userImage = dataSnapshot.child("image").value!!.toString()
                    Picasso.get().load(userImage).placeholder(R.drawable.profile).into(viewHolder.cvAvatar)
                } else {
                    Picasso.get().load(R.drawable.profile).placeholder(R.drawable.profile).into(viewHolder.cvAvatar)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })


        //        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        ////            storageReference.child("Profile Images/" + uid + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
        ////                @Override
        ////                public void onSuccess(Uri uri) {
        ////                    Picasso.get().load(uri).into(viewHolder.cvAvatar);
        ////                }
        ////            }).addOnFailureListener(new OnFailureListener() {
        ////                @Override
        ////                public void onFailure(@NonNull Exception exception) {
        ////                    // Handle any errors
        ////                }
        ////            });
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

}