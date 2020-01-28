package com.github.meonix.chatapp

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast

import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

import java.io.File
import java.io.FileOutputStream

class ZoomImage : AppCompatActivity() {
    private var linkImage: String? = null
    private var ZoomImageView: PhotoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //hide Status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)


        setContentView(R.layout.activity_zoom_image)
        ZoomImageView = findViewById(R.id.pvZoomImage)
        linkImage = intent.extras!!.get("imageURL")!!.toString()
        Picasso.get().load(linkImage).into(ZoomImageView)


        ZoomImageView!!.setOnLongClickListener {
            registerForContextMenu(ZoomImageView)
            ZoomImageView!!.showContextMenu()
            unregisterForContextMenu(ZoomImageView)
            true
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.zoom_image_options, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.downloadImage -> {
                Picasso.get()
                        .load(linkImage)
                        .into(object : Target {
                            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                                try {
                                    Toast.makeText(this@ZoomImage, "Image has been Downloaded....", Toast.LENGTH_SHORT).show()
                                    // Get the directory for the user's public pictures directory.
                                    val file = File(Environment.getExternalStoragePublicDirectory(
                                            Environment.DIRECTORY_PICTURES), "Miretty")
                                    file.mkdir()
                                    val externalFile = File(file, System.currentTimeMillis().toString() + ".jpg")

                                    val out = FileOutputStream(externalFile)
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)

                                    out.flush()
                                    out.close()
                                } catch (e: Exception) {
                                    // some action
                                    Toast.makeText(this@ZoomImage, "Error " + e.message, Toast.LENGTH_LONG).show()
                                }

                            }

                            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {

                                Toast.makeText(this@ZoomImage, "Error " + e.message, Toast.LENGTH_LONG).show()
                            }

                            override fun onPrepareLoad(placeHolderDrawable: Drawable) {

                            }
                        }
                        )
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }
}

