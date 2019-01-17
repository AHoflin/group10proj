package com.example.jdeiv.picoftheday

import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.polaroid.view.*

class PicOfTheDayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private var lastClickTime: Long = 0// For double taps on the polaroid image
    private var DOUBLE_CLICK_INTERVAL: Long = 200 //ms
    // Function that checks the database if the user liked the post already and then sets
    // the correct imageresource on the itemView.favorite image
    fun checkAndHandleUserLikesPost(polaroidKey: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val userLikesRef = FirebaseDatabase.getInstance().getReference("POTD/postLikes/$userId")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(polaroidKey) && dataSnapshot.child(polaroidKey).value == true){
                    itemView.favorite.setImageResource(R.drawable.ic_favorite_clicked)
                    itemView.favorite.setTag(LikeStatus.LIKED)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("checkAndHandleUserLikesPost", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        userLikesRef.addValueEventListener(postListener)
    }

    fun bind(polaroid: Polaroid?) {
        if (polaroid != null) {
            itemView.card_text.text = polaroid.captionText
            Picasso.get().load(polaroid.imgSrc).into(itemView.card_image)

            itemView.favorite.setImageResource(R.drawable.ic_potd_winner)
            itemView.favorite.clearColorFilter()
            itemView.favorite.setColorFilter(R.color.colorGolden, PorterDuff.Mode.SRC_ATOP)
            itemView.moreButton.visibility = View.INVISIBLE
            itemView.timestamp.text = polaroid.uploadedDate
        }
    }
    companion object {
        fun create(parent: ViewGroup): PicOfTheDayViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.polaroid, parent, false)
            return PicOfTheDayViewHolder(view)
        }
    }
}