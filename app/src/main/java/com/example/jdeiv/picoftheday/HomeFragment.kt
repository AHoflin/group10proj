package com.example.jdeiv.picoftheday

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutManager = LinearLayoutManager(activity)
        feedRecyclerViewHome.layoutManager = layoutManager

        adapter = RecyclerViewAdapter()
        feedRecyclerViewHome.adapter = adapter
    }

    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }

    var polaroidList: MutableList<Polaroid> = mutableListOf(
        Polaroid("imgSrcUrl", "This is the caption", "Eskilstuna", "13:37"),
        Polaroid("imgSrcUrl", "This is the caption2", "Eskilstuna", "13:38"),
        Polaroid("imgSrcUrl", "This is the caption3", "Eskilstuna", "13:39"),
        Polaroid("imgSrcUrl", "This is the caption4", "Eskilstuna", "13:310"),
        Polaroid("imgSrcUrl", "This is the caption5", "Eskilstuna", "13:311")
        )


}


class Polaroid(val imgSrc: String, val captionText: String, val location: String, val uploaded: String) {


}