package com.example.jdeiv.picoftheday

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import java.util.HashMap

class ViewPagerAdapter// Superclass constructor
    (fm: FragmentManager, private val fragmentHashMap: HashMap<Int, Fragment>) : FragmentPagerAdapter(fm) {

    override fun getItem(i: Int): Fragment? {
        return fragmentHashMap[i]
    }

    override fun getCount(): Int {
        return fragmentHashMap.size
    }
}