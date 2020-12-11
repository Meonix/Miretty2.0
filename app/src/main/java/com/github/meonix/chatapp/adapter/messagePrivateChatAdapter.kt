package com.github.meonix.chatapp.adapter

import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView

import com.github.meonix.chatapp.R
import com.github.meonix.chatapp.ZoomImage
import com.github.meonix.chatapp.model.MessagesChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

import java.io.IOException

import de.hdodenhof.circleimageview.CircleImageView

class messagePrivateChatAdapter(private val userMessageList: List<MessagesChatModel>) : RecyclerView.Adapter<messagePrivateChatAdapter.MessageViewHolder>() {
    private val mAuth: FirebaseAuth
    private var usersRef: DatabaseReference? = null
    private var mPlayer: MediaPlayer? = null
    private var mStartPlaying: Boolean = false
    private var playvideo: Boolean = false

    inner class MessageViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var avatarRevceiver: CircleImageView
        var receiverMessageCard: CardView
        var senderMessageCard: CardView
        var PrivateMessageTime: TextView
        var PrivateMessageDate: TextView
        var receiverPrivateMessage: TextView
        var senderPrivateMessage: TextView
        var audioOfSender: ImageButton
        var audioOfReceiver: ImageButton
        var senderImage: ImageView
        var receiverImage: ImageView
        var senderVideo: VideoView
        var receiverVideo: VideoView

        init {
            receiverMessageCard = itemview.findViewById(R.id.receiver_message_card)
            senderMessageCard = itemview.findViewById(R.id.sender_message_card)
            avatarRevceiver = itemview.findViewById(R.id.private_message_image)
            audioOfSender = itemview.findViewById(R.id.audioOfSender)
            audioOfReceiver = itemview.findViewById(R.id.audioOfReceiver)

            PrivateMessageTime = itemview.findViewById(R.id.private_message_time)
            PrivateMessageDate = itemview.findViewById(R.id.private_message_date)
            receiverPrivateMessage = itemview.findViewById(R.id.receiver_private_message)

            senderPrivateMessage = itemview.findViewById(R.id.sender_private_message)

            senderImage = itemview.findViewById(R.id.imageOfSender)
            receiverImage = itemview.findViewById(R.id.imageOfReceiver)

            senderVideo = itemview.findViewById(R.id.videoOfSender)
            receiverVideo = itemview.findViewById(R.id.videoOfReceiver)
        }
    }

    init {
        this.mAuth = FirebaseAuth.getInstance()
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MessageViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.private_message_layout, viewGroup, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(messageViewHolder: MessageViewHolder, i: Int) {
        val messageSenderID = mAuth.currentUser!!.uid
        val messages = userMessageList[i]
        val fromUserID = messages.from_uid
        val fromMessageType = messages.type

        usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(fromUserID!!)

        messageViewHolder.receiverMessageCard.setOnClickListener {
            if (messageViewHolder.PrivateMessageTime.visibility == View.INVISIBLE) {
                messageViewHolder.PrivateMessageTime.visibility = View.VISIBLE
                messageViewHolder.PrivateMessageDate.visibility = View.VISIBLE
            } else {
                messageViewHolder.PrivateMessageTime.visibility = View.INVISIBLE
                messageViewHolder.PrivateMessageDate.visibility = View.INVISIBLE
            }
        }
        messageViewHolder.senderMessageCard.setOnClickListener {
            if (messageViewHolder.PrivateMessageTime.visibility == View.INVISIBLE) {
                messageViewHolder.PrivateMessageTime.visibility = View.VISIBLE
                messageViewHolder.PrivateMessageDate.visibility = View.VISIBLE
            } else {
                messageViewHolder.PrivateMessageTime.visibility = View.INVISIBLE
                messageViewHolder.PrivateMessageDate.visibility = View.INVISIBLE
            }
        }

        mStartPlaying = true
        messageViewHolder.audioOfSender.setOnClickListener {
            try {
                onPlay(mStartPlaying, messages.message)
                if (mStartPlaying) {
                    messageViewHolder.audioOfSender.setImageResource(R.drawable.pausebtn)
                } else {
                    messageViewHolder.audioOfSender.setImageResource(R.drawable.continuebtn)
                }
                mStartPlaying = !mStartPlaying
            } catch (e: Exception) {
                // TODO: handle exception
            }
        }
        messageViewHolder.audioOfReceiver.setOnClickListener {
            try {
                onPlay(mStartPlaying, messages.message)
                if (mStartPlaying) {
                    messageViewHolder.audioOfReceiver.setImageResource(R.drawable.pausebtn)
                } else {
                    messageViewHolder.audioOfReceiver.setImageResource(R.drawable.continuebtn)
                }
                mStartPlaying = !mStartPlaying
            } catch (e: Exception) {
                // TODO: handle exception
            }
        }


        usersRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    val receiverImage = dataSnapshot.child("image").value!!.toString()
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile).into(messageViewHolder.avatarRevceiver)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        if (fromMessageType == "text") {
            messageViewHolder.audioOfSender.visibility = View.GONE
            messageViewHolder.audioOfReceiver.visibility = View.GONE
            messageViewHolder.receiverImage.visibility = View.GONE
            messageViewHolder.senderImage.visibility = View.GONE
            messageViewHolder.receiverVideo.visibility = View.GONE
            messageViewHolder.senderVideo.visibility = View.GONE

            messageViewHolder.receiverMessageCard.visibility = View.GONE
            messageViewHolder.avatarRevceiver.visibility = View.INVISIBLE
            messageViewHolder.senderMessageCard.visibility = View.INVISIBLE

            messageViewHolder.PrivateMessageTime.visibility = View.INVISIBLE
            messageViewHolder.PrivateMessageDate.visibility = View.INVISIBLE

            if (fromUserID == messageSenderID) {
                messageViewHolder.senderMessageCard.visibility = View.VISIBLE
                //                messageViewHolder.senderMessageCard.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.senderPrivateMessage.visibility = View.VISIBLE
                messageViewHolder.senderPrivateMessage.setTextColor(Color.WHITE)
                messageViewHolder.senderPrivateMessage.text = messages.message
                messageViewHolder.PrivateMessageTime.text = messages.time
                messageViewHolder.PrivateMessageDate.text = messages.date

            } else {

                messageViewHolder.PrivateMessageTime.text = messages.time
                messageViewHolder.PrivateMessageDate.text = messages.date
                messageViewHolder.senderMessageCard.visibility = View.GONE
                messageViewHolder.receiverMessageCard.visibility = View.VISIBLE

                messageViewHolder.avatarRevceiver.visibility = View.VISIBLE

                //                messageViewHolder.receiverMessageCard.setBackgroundResource(R.drawable.receiver_message_layout);

                messageViewHolder.receiverPrivateMessage.visibility = View.VISIBLE
                messageViewHolder.receiverPrivateMessage.setTextColor(Color.BLACK)
                messageViewHolder.receiverPrivateMessage.text = messages.message
            }
        } else if (fromMessageType == "audio") {
            messageViewHolder.receiverMessageCard.visibility = View.INVISIBLE
            messageViewHolder.avatarRevceiver.visibility = View.INVISIBLE
            messageViewHolder.senderMessageCard.visibility = View.INVISIBLE
            messageViewHolder.audioOfSender.visibility = View.INVISIBLE
            messageViewHolder.audioOfReceiver.visibility = View.INVISIBLE

            messageViewHolder.senderPrivateMessage.visibility = View.GONE
            messageViewHolder.receiverPrivateMessage.visibility = View.GONE
            messageViewHolder.receiverImage.visibility = View.GONE
            messageViewHolder.senderImage.visibility = View.GONE
            messageViewHolder.receiverVideo.visibility = View.GONE
            messageViewHolder.senderVideo.visibility = View.GONE

            messageViewHolder.PrivateMessageTime.visibility = View.INVISIBLE
            messageViewHolder.PrivateMessageDate.visibility = View.INVISIBLE
            if (fromUserID == messageSenderID) {
                messageViewHolder.senderMessageCard.visibility = View.VISIBLE
                messageViewHolder.senderMessageCard.setBackgroundResource(R.drawable.sender_message_layout)
                messageViewHolder.audioOfSender.visibility = View.VISIBLE

                messageViewHolder.PrivateMessageTime.text = messages.time
                messageViewHolder.PrivateMessageDate.text = messages.date

            } else {

                messageViewHolder.PrivateMessageTime.text = messages.time
                messageViewHolder.PrivateMessageDate.text = messages.date
                messageViewHolder.senderMessageCard.visibility = View.INVISIBLE
                messageViewHolder.receiverMessageCard.visibility = View.VISIBLE
                messageViewHolder.audioOfReceiver.visibility = View.VISIBLE

                messageViewHolder.avatarRevceiver.visibility = View.VISIBLE

                messageViewHolder.receiverMessageCard.setBackgroundResource(R.drawable.receiver_message_layout)
                messageViewHolder.avatarRevceiver.visibility = View.VISIBLE

            }
        } else if (fromMessageType == "image") {
            messageViewHolder.senderPrivateMessage.visibility = View.GONE
            messageViewHolder.receiverPrivateMessage.visibility = View.GONE
            messageViewHolder.audioOfSender.visibility = View.GONE
            messageViewHolder.audioOfReceiver.visibility = View.GONE
            messageViewHolder.receiverVideo.visibility = View.GONE
            messageViewHolder.senderVideo.visibility = View.GONE

            messageViewHolder.receiverMessageCard.visibility = View.INVISIBLE
            messageViewHolder.avatarRevceiver.visibility = View.INVISIBLE
            messageViewHolder.senderMessageCard.visibility = View.INVISIBLE
            messageViewHolder.receiverImage.visibility = View.INVISIBLE
            messageViewHolder.senderImage.visibility = View.INVISIBLE

            messageViewHolder.PrivateMessageTime.visibility = View.INVISIBLE
            messageViewHolder.PrivateMessageDate.visibility = View.INVISIBLE

            val urlImage = messages.message
            if (fromUserID == messageSenderID) {
                messageViewHolder.senderMessageCard.visibility = View.VISIBLE
                //                messageViewHolder.senderMessageCard.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.senderImage.visibility = View.VISIBLE

                Picasso.get().load(urlImage).into(messageViewHolder.senderImage)
                messageViewHolder.PrivateMessageTime.text = messages.time
                messageViewHolder.PrivateMessageDate.text = messages.date
                messageViewHolder.senderMessageCard.setOnClickListener { v ->
                    val ZoomImage = Intent(v.context, ZoomImage::class.java)
                    ZoomImage.putExtra("imageURL", urlImage)
                    v.context.startActivity(ZoomImage)
                }
            } else {
                messageViewHolder.receiverImage.visibility = View.VISIBLE
                messageViewHolder.PrivateMessageTime.text = messages.time
                messageViewHolder.PrivateMessageDate.text = messages.date
                messageViewHolder.senderMessageCard.visibility = View.INVISIBLE
                messageViewHolder.receiverMessageCard.visibility = View.VISIBLE

                Picasso.get().load(urlImage).into(messageViewHolder.receiverImage)
                messageViewHolder.avatarRevceiver.visibility = View.VISIBLE

                //                messageViewHolder.receiverMessageCard.setBackgroundResource(R.drawable.receiver_message_layout);
                messageViewHolder.avatarRevceiver.visibility = View.VISIBLE
                messageViewHolder.receiverMessageCard.setOnClickListener { v ->
                    val ZoomImage = Intent(v.context, ZoomImage::class.java)
                    ZoomImage.putExtra("imageURL", urlImage)
                    v.context.startActivity(ZoomImage)
                }
            }
        } else if (fromMessageType == "video") {
            messageViewHolder.senderPrivateMessage.visibility = View.GONE
            messageViewHolder.receiverPrivateMessage.visibility = View.GONE
            messageViewHolder.receiverImage.visibility = View.GONE
            messageViewHolder.senderImage.visibility = View.GONE
            messageViewHolder.audioOfSender.visibility = View.GONE
            messageViewHolder.audioOfReceiver.visibility = View.GONE

            messageViewHolder.receiverMessageCard.visibility = View.INVISIBLE
            messageViewHolder.avatarRevceiver.visibility = View.INVISIBLE
            messageViewHolder.senderMessageCard.visibility = View.INVISIBLE
            messageViewHolder.receiverVideo.visibility = View.INVISIBLE
            messageViewHolder.senderVideo.visibility = View.INVISIBLE

            messageViewHolder.PrivateMessageTime.visibility = View.INVISIBLE
            messageViewHolder.PrivateMessageDate.visibility = View.INVISIBLE

            playvideo = true
            if (fromUserID == messageSenderID) {
                messageViewHolder.senderMessageCard.visibility = View.VISIBLE
                messageViewHolder.senderMessageCard.setBackgroundResource(R.drawable.sender_message_layout)
                messageViewHolder.senderVideo.visibility = View.VISIBLE

                val uriVideo = messages.message
                val uri = Uri.parse(uriVideo)

                messageViewHolder.senderVideo.setVideoURI(uri)
                messageViewHolder.senderVideo.requestFocus()
                messageViewHolder.senderMessageCard.setOnClickListener {
                    try {
                        if (playvideo) {
                            messageViewHolder.senderVideo.start()
                        } else {
                            messageViewHolder.senderVideo.pause()
                        }
                        playvideo = !playvideo
                    } catch (e: Exception) {
                        // TODO: handle exception
                    }
                }

                messageViewHolder.PrivateMessageTime.text = messages.time
                messageViewHolder.PrivateMessageDate.text = messages.date

            } else {
                messageViewHolder.receiverImage.visibility = View.VISIBLE
                messageViewHolder.PrivateMessageTime.text = messages.time
                messageViewHolder.PrivateMessageDate.text = messages.date
                messageViewHolder.senderMessageCard.visibility = View.INVISIBLE
                messageViewHolder.receiverMessageCard.visibility = View.VISIBLE
                messageViewHolder.receiverVideo.visibility = View.VISIBLE

                val uriVideo = messages.message
                val uri = Uri.parse(uriVideo)

                messageViewHolder.receiverVideo.setVideoURI(uri)
                messageViewHolder.receiverVideo.requestFocus()
                messageViewHolder.receiverMessageCard.setOnClickListener {
                    try {
                        if (playvideo) {
                            messageViewHolder.receiverVideo.start()
                        } else {
                            messageViewHolder.receiverVideo.pause()
                        }
                        playvideo = !playvideo
                    } catch (e: Exception) {
                        // TODO: handle exception
                    }
                }

                messageViewHolder.avatarRevceiver.visibility = View.VISIBLE

                messageViewHolder.receiverMessageCard.setBackgroundResource(R.drawable.receiver_message_layout)
                messageViewHolder.avatarRevceiver.visibility = View.VISIBLE

            }
        }
    }


    private fun onPlay(start: Boolean, mFileName: String?) {
        if (start) {
            startPlaying(mFileName)
        } else {
            stopPlaying()
        }
    }

    private fun startPlaying(mFileName: String?) {
        mPlayer = MediaPlayer()
        try {
            mPlayer!!.setDataSource(mFileName)
            mPlayer!!.prepare()
            mPlayer!!.start()
        } catch (e: IOException) {

        }

    }

    private fun stopPlaying() {
        mPlayer!!.release()
        mPlayer = null
    }

    override fun getItemCount(): Int {
        return userMessageList.size
    }


}
