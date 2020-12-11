package com.github.meonix.chatapp

import android.content.DialogInterface
import android.content.Intent
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null
    private var myViewPage: ViewPager? = null
    private var myTablayout: TabLayout? = null
    private var mytabsAccessorAdapter: TabsAccessorAdapter? = null

    private var currentUser: FirebaseUser? = null
    private var mAuth: FirebaseAuth? = null
    private var RootRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser

        RootRef = FirebaseDatabase.getInstance().reference

        mToolbar = findViewById(R.id.main_page_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setTitle("Miretty")

        myViewPage = findViewById(R.id.main_tabs_pager)
        mytabsAccessorAdapter = TabsAccessorAdapter(supportFragmentManager)
        myViewPage!!.adapter = mytabsAccessorAdapter

        //when we go back the main activity  we will back old position
        myViewPage!!.currentItem = myViewPage!!.currentItem

        myTablayout = findViewById(R.id.main_tabs)
        myTablayout!!.setupWithViewPager(myViewPage)

        val ICONS = intArrayOf(R.drawable.chatfragment, R.drawable.groupchatfragment, R.drawable.contactfragment, R.drawable.requestfragment)

        //Get reference to your Tablayout

        myTablayout!!.getTabAt(0)!!.setIcon(ICONS[0])
        myTablayout!!.getTabAt(1)!!.setIcon(ICONS[1])
        myTablayout!!.getTabAt(2)!!.setIcon(ICONS[2])
        myTablayout!!.getTabAt(3)!!.setIcon(ICONS[3])
    }

    override fun onStart() {
        super.onStart()
        if (currentUser == null) {
            SendUserToLoginActivity()
        } else {
            VerifyUserExistance()
        }
    }

    private fun VerifyUserExistance() {
        val currentUerID = mAuth!!.currentUser!!.uid

        RootRef!!.child("Users").child(currentUerID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child("name").exists()) {
                    Toast.makeText(this@MainActivity, "Welcome" + "  " + dataSnapshot.child("name").value!!.toString(), Toast.LENGTH_SHORT).show()
                } else {
                    SendUserToSettingsActivity()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        if (item.itemId == R.id.main_find_logout_option) {
            mAuth!!.signOut()
            SendUserToLoginActivity()
        }
        if (item.itemId == R.id.main_settings_option) {
            SendUserToSettingsActivity()
        }
        if (item.itemId == R.id.main_find_friends_option) {
            SendUsertoFindFriendsActivity()
        }
        if (item.itemId == R.id.main_create_group_option) {
            RequestNewGroup()
        }
        return true
    }


    private fun RequestNewGroup() {
        val builder = AlertDialog.Builder(this@MainActivity, R.style.AlertDialog)
        builder.setTitle("Enter Group Name :")
        val groupNameField = EditText(this@MainActivity)
        groupNameField.hint = "e.g Coding cafe"
        builder.setView(groupNameField)

        builder.setPositiveButton("Create") { dialog, which ->
            val groupName = groupNameField.text.toString()
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(this@MainActivity, "Please write Group Name", Toast.LENGTH_SHORT).show()

            } else {
                CreateNewGroup(groupName)
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
        builder.show()
    }

    private fun CreateNewGroup(groupName: String) {
        RootRef!!.child("Group").child(groupName).setValue("").addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@MainActivity, groupName + "is Created Successfully...", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun SendUserToLoginActivity() {
        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
        finish()
    }

    private fun SendUserToSettingsActivity() {
        val settingIntent = Intent(this@MainActivity, SettingActivity::class.java)
        //        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingIntent)
    }

    private fun SendUsertoFindFriendsActivity() {
        val findfriendsIntent = Intent(this@MainActivity, FindFriendsActivity::class.java)
        startActivity(findfriendsIntent)
    }

}
