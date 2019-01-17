package com.example.jdeiv.picoftheday

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import java.io.File

class PicofthedayFragment : Fragment() {
    private lateinit var viewModel: PicOfTheDayViewModel
    private lateinit var picOfTheDayListAdapter: PicOfTheDayListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        viewModel = ViewModelProviders.of(this).get(PicOfTheDayViewModel::class.java)
        recyclerView = view.findViewById(R.id.feedRecyclerViewHome)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)

        initAdapter()
        initState()

        return view
    }

    private fun getLocationFromFile() : FetchedLocation {
        val fileName = "/location.txt"
        val file = File(context?.dataDir.toString() + fileName)
        val coor = file.bufferedReader().readLines()

        return FetchedLocation(coor[0].toDouble(), coor[1].toDouble())
    }

    private fun initAdapter() {
        viewModel.sketchyLateinitConstructorPlsDontJudgeMe(getLocationFromFile())
        picOfTheDayListAdapter = PicOfTheDayListAdapter { viewModel.retry() }
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        recyclerView.adapter = picOfTheDayListAdapter
        viewModel.picOfTheDayList.observe(this, Observer {
            picOfTheDayListAdapter.submitList(it)
        })
        swipeRefreshLayout.setOnRefreshListener {
            initAdapter()
            initState()
            swipeRefreshLayout.setRefreshing(false)
        }
    }
    private fun initState() {
//        txt_error.setOnClickListener { viewModel.retry() }
        viewModel.getState().observe(this, Observer { state ->
            View.VISIBLE
//            progress_bar.visibility = if (viewModel.listIsEmpty() && state == State.LOADING) View.VISIBLE else View.GONE
//            txt_error.visibility = if (viewModel.listIsEmpty() && state == State.ERROR) View.VISIBLE else View.GONE
//            if (!viewModel.listIsEmpty()) {
//                newsListAdapter.setState(state ?: State.DONE)
//            }
        })
    }
    companion object {
        fun newInstance(): PicofthedayFragment = PicofthedayFragment()
    }

}