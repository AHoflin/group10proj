package com.example.jdeiv.picoftheday

import android.arch.lifecycle.*
import android.arch.lifecycle.Observer
import android.arch.paging.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import android.arch.paging.ItemKeyedDataSource
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.content.res.TypedArrayUtils.getString
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.polaroid.view.*
import java.io.File


class HomeFragment : Fragment() {
    private lateinit var viewModel: PolaroidListViewModel
    private lateinit var polaroidListAdapter: PolaroidListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        viewModel = ViewModelProviders.of(this).get(PolaroidListViewModel::class.java)
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
        polaroidListAdapter = PolaroidListAdapter { viewModel.retry() }
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        recyclerView.adapter = polaroidListAdapter
        viewModel.polaroidList.observe(this, Observer {
            polaroidListAdapter.submitList(it)
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
        fun newInstance(): HomeFragment = HomeFragment()
    }
}







