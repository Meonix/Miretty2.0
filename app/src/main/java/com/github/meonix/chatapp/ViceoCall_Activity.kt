package com.github.meonix.chatapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast

import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration

class ViceoCall_Activity : AppCompatActivity() {

    private var mRtcEngine: RtcEngine? = null

    //variable is needed Drag FrameLayout(your Front camera when Video call)
    private var mainLayout: ViewGroup? = null
    private var image: FrameLayout? = null
    private var xDelta: Int = 0
    private var yDelta: Int = 0

    // Handle SDK Events
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            runOnUiThread {
                // set first remote user to the main bg video container
                setupRemoteVideoStream(uid)
            }
        }

        // remote user has left channel
        override fun onUserOffline(uid: Int, reason: Int) { // Tutorial Step 7
            runOnUiThread { onRemoteUserLeft() }
        }

        // remote user has toggled their video
        override fun onUserMuteVideo(uid: Int, toggle: Boolean) { // Tutorial Step 10
            runOnUiThread { onRemoteUserVideoToggle(uid, toggle) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viceo_call_)
        //Set OntouchListener to drag FrameLayout(caution : Framelayout have to placed in Relativelayout)

        mainLayout = findViewById(R.id.main)
        image = findViewById(R.id.floating_video_container)
        image!!.setOnTouchListener(onTouchListener())
        //OnTouchListener will be define below

        //check permission
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initAgoraEngine()
        }

        findViewById<View>(R.id.audioBtn).visibility = View.GONE // set the audio button hidden
        findViewById<View>(R.id.leaveBtn).visibility = View.GONE // set the leave button hidden
        findViewById<View>(R.id.videoBtn).visibility = View.GONE // set the video button hidden
    }

    private fun onTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { view, event ->
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()

            when (event.action and MotionEvent.ACTION_MASK) {

                MotionEvent.ACTION_DOWN -> {
                    val lParams = view.layoutParams as RelativeLayout.LayoutParams

                    xDelta = x - lParams.leftMargin
                    yDelta = y - lParams.topMargin
                }

                MotionEvent.ACTION_UP -> Toast.makeText(this@ViceoCall_Activity,
                        "Position is changed", Toast.LENGTH_SHORT)
                        .show()

                MotionEvent.ACTION_MOVE -> {
                    val layoutParams = view
                            .layoutParams as RelativeLayout.LayoutParams
                    layoutParams.leftMargin = x - xDelta
                    layoutParams.topMargin = y - yDelta
                    layoutParams.rightMargin = 0
                    layoutParams.bottomMargin = 0
                    view.layoutParams = layoutParams
                }
            }
            mainLayout!!.invalidate()
            true
        }
    }

    private fun initAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, getString(R.string.agora_app_id), mRtcEventHandler)
        } catch (e: Exception) {
            Log.e(LOG_TAG, Log.getStackTraceString(e))

            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }

        setupSession()
    }

    private fun setupSession() {
        mRtcEngine!!.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)

        mRtcEngine!!.enableVideo()

        mRtcEngine!!.setVideoEncoderConfiguration(VideoEncoderConfiguration(VideoEncoderConfiguration.VD_1920x1080, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT))
    }

    private fun setupLocalVideoFeed() {

        // setup the container for the local user
        val videoContainer = findViewById<FrameLayout>(R.id.floating_video_container)
        val videoSurface = RtcEngine.CreateRendererView(baseContext)
        videoSurface.setZOrderMediaOverlay(true)
        videoContainer.addView(videoSurface)
        mRtcEngine!!.setupLocalVideo(VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun setupRemoteVideoStream(uid: Int) {
        // setup ui element for the remote stream
        val videoContainer = findViewById<FrameLayout>(R.id.bg_video_container)
        // ignore any new streams that join the session
        if (videoContainer.childCount >= 1) {
            return
        }

        val videoSurface = RtcEngine.CreateRendererView(baseContext)
        videoContainer.addView(videoSurface)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, uid))
        mRtcEngine!!.setRemoteSubscribeFallbackOption(io.agora.rtc.Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY)

    }

    fun onAudioMuteClicked(view: View) {
        val btn = view as ImageView
        if (btn.isSelected) {
            btn.isSelected = false
            btn.setImageResource(R.drawable.audio_toggle_btn)
        } else {
            btn.isSelected = true
            btn.setImageResource(R.drawable.audio_toggle_active_btn)
        }

        mRtcEngine!!.muteLocalAudioStream(btn.isSelected)
    }

    fun onVideoMuteClicked(view: View) {
        val btn = view as ImageView
        if (btn.isSelected) {
            btn.isSelected = false
            btn.setImageResource(R.drawable.video_toggle_btn)
        } else {
            btn.isSelected = true
            btn.setImageResource(R.drawable.video_toggle_active_btn)
        }

        mRtcEngine!!.muteLocalVideoStream(btn.isSelected)

        val container = findViewById<FrameLayout>(R.id.floating_video_container)
        container.visibility = if (btn.isSelected) View.GONE else View.VISIBLE
        val videoSurface = container.getChildAt(0) as SurfaceView
        videoSurface.setZOrderMediaOverlay(!btn.isSelected)
        videoSurface.visibility = if (btn.isSelected) View.GONE else View.VISIBLE
    }

    // join the channel when user clicks UI button
    fun onjoinChannelClicked(view: View) {
        mRtcEngine!!.joinChannel(null, "test-channel", "Extra Optional Data", 0) // if you do not specify the uid, Agora will assign one.
        setupLocalVideoFeed()
        findViewById<View>(R.id.joinBtn).visibility = View.GONE // set the join button hidden
        findViewById<View>(R.id.audioBtn).visibility = View.VISIBLE // set the audio button hidden
        findViewById<View>(R.id.leaveBtn).visibility = View.VISIBLE // set the leave button hidden
        findViewById<View>(R.id.videoBtn).visibility = View.VISIBLE // set the video button hidden
    }

    fun onLeaveChannelClicked(view: View) {
        leaveChannel()
        removeVideo(R.id.floating_video_container)
        removeVideo(R.id.bg_video_container)
        findViewById<View>(R.id.joinBtn).visibility = View.VISIBLE // set the join button visible
        findViewById<View>(R.id.audioBtn).visibility = View.GONE // set the audio button hidden
        findViewById<View>(R.id.leaveBtn).visibility = View.GONE // set the leave button hidden
        findViewById<View>(R.id.videoBtn).visibility = View.GONE // set the video button hidden
    }

    private fun leaveChannel() {
        mRtcEngine!!.leaveChannel()
    }

    private fun removeVideo(containerID: Int) {
        val videoContainer = findViewById<FrameLayout>(containerID)
        videoContainer.removeAllViews()
    }

    private fun onRemoteUserVideoToggle(uid: Int, toggle: Boolean) {
        val videoContainer = findViewById<FrameLayout>(R.id.bg_video_container)

        val videoSurface = videoContainer.getChildAt(0) as SurfaceView
        videoSurface.visibility = if (toggle) View.GONE else View.VISIBLE

        // add an icon to let the other user know remote video has been disabled
        if (toggle) {
            val noCamera = ImageView(this)
            noCamera.setImageResource(R.drawable.video_disabled)
            videoContainer.addView(noCamera)
        } else {
            val noCamera = videoContainer.getChildAt(1) as ImageView
            if (noCamera != null) {
                videoContainer.removeView(noCamera)
            }
        }
    }

    private fun onRemoteUserLeft() {
        removeVideo(R.id.bg_video_container)
    }


    fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(LOG_TAG, "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(this,
                        permission) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    REQUESTED_PERMISSIONS,
                    requestCode)
            return false
        }
        return true
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode)

        when (requestCode) {
            PERMISSION_REQ_ID -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG_TAG, "Need permissions " + Manifest.permission.RECORD_AUDIO + "/" + Manifest.permission.CAMERA)
                }
                initAgoraEngine()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }

    fun showLongToast(msg: String) {
        this.runOnUiThread { Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show() }
    }

    companion object {

        // Permissions
        private val PERMISSION_REQ_ID = 22
        private val REQUESTED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)

        private val LOG_TAG = MainActivity::class.java!!.getSimpleName()
    }

}
