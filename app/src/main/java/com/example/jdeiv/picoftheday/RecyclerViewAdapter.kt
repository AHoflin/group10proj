package com.example.jdeiv.picoftheday

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.polaroid, viewGroup, false)
        return ViewHolder(v)
    }
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.polaroidCaption.text = captions[i]
        viewHolder.polaroidImage.setImageResource(images[i])
    }

    override fun getItemCount(): Int {
        return captions.size
    }

    private val captions = arrayOf("Chapter One",
        "Chapter Two", "Chapter Three", "Chapter Four",
        "Chapter Five", "Chapter Six", "Chapter Seven",
        "Chapter Eight")


    private val images = intArrayOf(R.drawable.ic_picoftheday,
        R.drawable.ic_picoftheday, R.drawable.ic_picoftheday,
        R.drawable.ic_picoftheday, R.drawable.ic_picoftheday,
        R.drawable.ic_picoftheday, R.drawable.ic_picoftheday,
        R.drawable.ic_picoftheday)


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var polaroidImage: ImageView
        var polaroidCaption: TextView
        var favoriteImage: ImageView

        init {
            polaroidImage = itemView.findViewById(R.id.card_image)
            polaroidCaption = itemView.findViewById(R.id.card_text)
            favoriteImage = itemView.findViewById(R.id.favorite)

            favoriteImage.setOnClickListener {
                Log.d("DEBUGFEED", polaroidCaption.text.toString())
                if (favoriteImage.tag == 0) {
                    // DO DATABASE STUFF HERE? YES PROBABLY

                    favoriteImage.setImageResource(R.drawable.ic_favorite_clicked)
                    favoriteImage.tag = 1
                    Log.d("DEBUGFEED", polaroidCaption.text.toString() + ": Changed picture to clicked")
                }
                else {
                    favoriteImage.setImageResource(R.drawable.ic_favorite)
                    favoriteImage.tag = 0
                    Log.d("DEBUGFEED", polaroidCaption.text.toString() + ": Changed picture to not clicked")

                }
            }
        }
    }
}
