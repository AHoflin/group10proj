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
import android.widget.Toast
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.google.firebase.database.DataSnapshot
import java.util.*


class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    init {
        initPolaroidList()

    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.polaroid, viewGroup, false)
        return ViewHolder(v)
    }
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.polaroidCaption.text = polaroids[i].captionText
        Picasso.get().load(polaroids[i].imgSrc).into(viewHolder.polaroidImage)
//        viewHolder.polaroidImage.setImageResource(images[i])
    }

    override fun getItemCount(): Int {
        return polaroids.size
    }


    private val polaroids: MutableList<Polaroid> = mutableListOf()

    private fun initPolaroidList() {
        val firebaseRef = FirebaseDatabase.getInstance().getReference("/POTD").orderByChild("uploadDate/time").limitToFirst(5)
//        firebaseRef.orderByChild("uploadDate/time").limitToFirst(5)

        val polaroidsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val post = dataSnapshot.getValue(Polaroid::class.java)
//                Log.d("DEBUGGGG", post.toString())
                polaroids.clear()
//                "user" to user,
//                "caption" to captionText,
//                "hearts" to likes,
//                "uploadDate" to uploaded,
//                "position" to location,
//                "filename" to imgSrc
                for (messageSnapshot in dataSnapshot.children) {
                    val captionText = messageSnapshot.child("caption").value as String?
                    val user = messageSnapshot.child("user").value as String?
                    val likes = messageSnapshot.child("hearts").value as Long?
//                    val uploaded = messageSnapshot.child("uploadDate").value as Date?
//                    val location = messageSnapshot.child("position").value as FetchedLocation?
                    val imgSrc = messageSnapshot.child("filename").value as String?

                    val polaroid = Polaroid("temp",imgSrc, captionText,likes,FetchedLocation(1.0,1.0),user, 1)
                    polaroids.add(polaroid)
                    Log.d("Snapshot", messageSnapshot.child("uploadDate/time").toString())
                }


//                dataSnapshot.children.mapTo(polaroids) { it.getValue<Polaroid>(Polaroid::class.java) }
//                Log.d("DEBUGGGG", "Caption:" + polaroids[0].captionText.toString())
//                Log.d("DEBUGGGG", polaroids[0].imgSrc.toString())
//                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }
        firebaseRef.addListenerForSingleValueEvent(polaroidsListener)
    }

//    private val captions = arrayOf("Chapter One",
//        "Chapter Two", "Chapter Three", "Chapter Four",
//        "Chapter Five", "Chapter Six", "Chapter Seven",
//        "Chapter Eight")
//
//
//    private val images = intArrayOf(R.drawable.ic_picoftheday,
//        R.drawable.ic_picoftheday, R.drawable.ic_picoftheday,
//        R.drawable.ic_picoftheday, R.drawable.ic_picoftheday,
//        R.drawable.ic_picoftheday, R.drawable.ic_picoftheday,
//        R.drawable.ic_picoftheday)


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
