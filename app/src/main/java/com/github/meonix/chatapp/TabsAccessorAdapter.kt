package com.github.meonix.chatapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TabsAccessorAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(i: Int): Fragment? {
        when (i) {
            0 -> {
                return ChatsFragment()
            }
            1 -> {
                return GroupsFragment()
            }
            2 -> {
                return ContactsFragment()
            }
            3 -> {
                return RequestsFragment()
            }
            else -> return null
        }
    }

    override fun getCount(): Int {
        return 4
    }

    //    @Nullable
    //    @Override
    //    public CharSequence getPageTitle(int position) {
    //        switch(position)
    //        {
    //            case 0 :
    //                return "Friends Chat";
    //            case 1 :
    //                return "Groups Chat";
    //            case 2:
    //                return  "Contacts";
    //            case 3:
    //                return  "Requests";
    //            default:
    //                return null;
    //        }
    //    }
}
