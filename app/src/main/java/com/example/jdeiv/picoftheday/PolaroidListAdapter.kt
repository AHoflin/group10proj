package com.example.jdeiv.picoftheday

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup


class PolaroidListAdapter(private val retry: () -> Unit)
    : PagedListAdapter<Polaroid, RecyclerView.ViewHolder>(PolaroidDiffCallback) {
//    private val DATA_VIEW_TYPE = 1
//    private val FOOTER_VIEW_TYPE = 2

    private var state = State.LOADING
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PolaroidViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PolaroidViewHolder).bind(getItem(position))
//        if (getItemViewType(position) == DATA_VIEW_TYPE)
//            (holder as PolaroidViewHolder).bind(getItem(position))
//        else (holder as ListFooterViewHolder).bind(state)
    }

//    override fun getItemViewType(position: Int): Int {
//        return if (position < super.getItemCount()) DATA_VIEW_TYPE else FOOTER_VIEW_TYPE
//    }

    companion object {
        val PolaroidDiffCallback = object : DiffUtil.ItemCallback<Polaroid>() {
            override fun areItemsTheSame(oldItem: Polaroid, newItem: Polaroid): Boolean {
                Log.d("TimeAgo", "PolaroidDiffCallback itemsTheSame: " + (oldItem.key == newItem.key).toString())
                return oldItem.key == newItem.key
            }

            override fun areContentsTheSame(oldItem: Polaroid, newItem: Polaroid): Boolean {
                Log.d("TimeAgo", "PolaroidDiffCallback contentsTheSame: " + (oldItem == newItem).toString())

                return oldItem == newItem
            }
        }
    }

    override fun getItem(position: Int): Polaroid? {
        return super.getItem(position)
    }
    override fun getItemCount(): Int {
//        Log.d("TimeAgo", "ItemCount: " + super.getItemCount().toString())
        return super.getItemCount()
    }
//
//    private fun hasFooter(): Boolean {
//        return super.getItemCount() != 0 && (state == State.LOADING || state == State.ERROR)
//    }

    fun setState(state: State) {
        this.state = state
        notifyItemChanged(super.getItemCount())
    }
}