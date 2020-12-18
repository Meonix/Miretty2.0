package com.github.meonix.chatapp

import android.content.Intent
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mytabsAccessorAdapter: TabsAccessorAdapter? = null

    private var currentUser: FirebaseUser? = null
    private var mAuth: FirebaseAuth? = null
    private var rootRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth?.currentUser

        rootRef = FirebaseDatabase.getInstance().reference

        setSupportActionBar(main_page_toolbar as Toolbar)
        supportActionBar?.title = getString(R.string.name_app)

        mytabsAccessorAdapter = TabsAccessorAdapter(supportFragmentManager)
        mainTabsPager.adapter = mytabsAccessorAdapter

        //when we go back the main activity  we will back old position
        mainTabsPager.currentItem = mainTabsPager.currentItem

        main_tabs.setupWithViewPager(mainTabsPager)

        val icons = intArrayOf(R.drawable.chatfragment, R.drawable.groupchatfragment, R.drawable.contactfragment, R.drawable.requestfragment)

        //Get reference to your Tablayout

        main_tabs.getTabAt(0)?.setIcon(icons[0])
        main_tabs.getTabAt(1)?.setIcon(icons[1])
        main_tabs.getTabAt(2)?.setIcon(icons[2])
        main_tabs.getTabAt(3)?.setIcon(icons[3])
    }

    override fun onStart() {
        super.onStart()
        if (currentUser == null) {
            sendUserToLoginActivity()
        } else {
            verifyUserExistence()
        }
    }

    private fun verifyUserExistence() {
        val currentUerID = mAuth?.currentUser?.uid

        rootRef?.child("Users")?.child(currentUerID)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child("name").exists()) {
                    Toast.makeText(this@MainActivity,
                            "Welcome" + "  " + dataSnapshot.child("name")
                                    .value.toString(), Toast.LENGTH_SHORT).show()
                } else {
                    sendUserToSettingsActivity()
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
            mAuth?.signOut()
            sendUserToLoginActivity()
        }
        if (item.itemId == R.id.main_settings_option) {
            sendUserToSettingsActivity()
        }
        if (item.itemId == R.id.main_find_friends_option) {
            sendUserToFindFriendsActivity()
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
                createNewGroup(groupName)
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
        builder.show()
    }

    private fun createNewGroup(groupName: String) {
        rootRef?.child("Group")?.child(groupName)?.setValue("")?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@MainActivity, groupName + "is Created Successfully...", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun sendUserToLoginActivity() {
        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
        finish()
    }

    private fun sendUserToSettingsActivity() {
        val settingIntent = Intent(this@MainActivity, SettingActivity::class.java)
        startActivity(settingIntent)
    }

    private fun sendUserToFindFriendsActivity() {
        val findFriendsIntent = Intent(this@MainActivity, FindFriendsActivity::class.java)
        startActivity(findFriendsIntent)
    }

}
