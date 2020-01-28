package com.github.meonix.chatapp


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.util.ArrayList
import java.util.HashSet


/**
 * A simple [Fragment] subclass.
 */
class GroupsFragment : Fragment() {

    private var groupFragmentView: View? = null
    private var list_view: ListView? = null
    private var arrayAdapter: ArrayAdapter<String>? = null
    private val list_of_groups = ArrayList<String>()

    private var GroupRef: DatabaseReference? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false)

        GroupRef = FirebaseDatabase.getInstance().reference.child("Group")

        IntializeFields()
        RetrieveAndDisplayGroups()


        list_view!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            val currentFroupName = adapterView.getItemAtPosition(position).toString()
            val groupIntent = Intent(context, GroupChatActivity::class.java)
            groupIntent.putExtra("groupName", currentFroupName)
            startActivity(groupIntent)
        }

        return groupFragmentView
    }

    private fun IntializeFields() {
        list_view = groupFragmentView!!.findViewById<View>(R.id.list_view) as ListView
        arrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, list_of_groups)
        list_view!!.adapter = arrayAdapter
    }


    private fun RetrieveAndDisplayGroups() {
        GroupRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val set = HashSet<String>()
                val iterator = dataSnapshot.children.iterator()
                while (iterator.hasNext()) {
                    set.add((iterator.next() as DataSnapshot).key)
                }

                list_of_groups.clear()
                list_of_groups.addAll(set)
                arrayAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

}// Required empty public constructor
