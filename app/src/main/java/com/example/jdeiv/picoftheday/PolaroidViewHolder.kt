package com.example.jdeiv.picoftheday

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.polaroid.view.*

class PolaroidViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
            itemView.favorite.setImageResource(R.drawable.ic_favorite)
            itemView.favorite.setTag(LikeStatus.NOT_LIKED)
            checkAndHandleUserLikesPost(polaroid.key!!)
            itemView.card_image.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                // User Double Clicked
                if(currentTime - lastClickTime < DOUBLE_CLICK_INTERVAL) {
                    itemView.favorite.performClick()
                }
                // Not a double click
                else {
                    // (For future functionality, maybe display location
                    // when single click, like instagram shows tagged people)
                }
                lastClickTime = currentTime
            }

            // Calculate how many seconds ago the post was uploaded
            val currTime = System.currentTimeMillis()/1000
            val uploadedTime = polaroid.uploadedTime
            val timeAgoSeconds = (currTime - (uploadedTime!!/1000)).toInt()
            var timeAgoString = ""

            // If it was uploaded over a minute ago
            if (timeAgoSeconds > 60) {
                // If it was uploaded over an hour ago
                if (timeAgoSeconds > 3600){
                    // If it was uploaded over a day ago
                    if (timeAgoSeconds > 86400){
                        val timeAgoDays = (timeAgoSeconds / 86400)
                        timeAgoString = timeAgoDays.toString() + " " + itemView.context.getString(R.string.days) + " " + itemView.context.getString(R.string.ago)
                    } else {
                        val timeAgoHours = (timeAgoSeconds / 3600)
                        timeAgoString = timeAgoHours.toString() + " " + itemView.context.getString(R.string.hours) + " " + itemView.context.getString(R.string.ago)
                    }
                } else {
                    val timeAgoMinutes = (timeAgoSeconds / 60)
                    timeAgoString = timeAgoMinutes.toString() + " " + itemView.context.getString(R.string.minutes) + " " + itemView.context.getString(R.string.ago)
                }
            } else {
                timeAgoString = timeAgoSeconds.toString() + " " + itemView.context.getString(R.string.seconds) + " " + itemView.context.getString(R.string.ago)
            }

            Log.d("TimeAgo", timeAgoString)
            itemView.timestamp.text = timeAgoString
            itemView.favorite.setOnClickListener {
                val username = FirebaseAuth.getInstance().currentUser?.uid.toString()
                Log.d("LikePic", username)
                val polaroidKey = polaroid.key
                val userLikesRef = FirebaseDatabase.getInstance().getReference("POTD/postLikes/${username}")
                val postLikesRef = FirebaseDatabase.getInstance().getReference("POTD/posts/$polaroidKey/hearts")

                // Picture is not currently liked, so we need to perform a like.
                // This is a quick check based on the tag on the image resource, if the tag would be wrong somehow
                // the app still wont send a like because we do another check if its already liked in the database
                if (it.favorite.tag == LikeStatus.NOT_LIKED) {
                    // Like the picture
                    it.favorite.setImageResource(R.drawable.ic_favorite_clicked)
                    it.favorite.tag = LikeStatus.LIKED

                    val likedPostsListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Check if the user already liked the picture
                            if (dataSnapshot.hasChild("$polaroidKey") && (dataSnapshot.child("$polaroidKey").value == true)) {
                                // Do nothing for now
                            } else {
                                // The user haven't liked the picture so we add the like to the database
                                userLikesRef.child("$polaroidKey").setValue(true)
                                // ... and increment the hearts on the post
                                postLikesRef.runTransaction(object : Transaction.Handler{
                                    override fun doTransaction(p0: MutableData): Transaction.Result {
                                        // CUZ FIREBASE TRANSACTIONS ARE STRANGE... https://stackoverflow.com/questions/35818946/firebase-runtransaction-not-working-mutabledata-is-null
                                        if(p0.value == null) {
                                            // Set value to 1 if the assumed value of null was correct, else
                                            // firebase will redo the transaction with the new value fetched from database.
                                            p0.value = 1
                                        } else {
                                            var likes = p0.value as Long
                                            p0.value = ++likes
                                        }

                                        return Transaction.success(p0)
                                    }
                                    override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                                        Log.d("LikePic", "New likes amount = " + p2?.value.toString())
                                    }
                                })
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            println("loadPost:onCancelled ${databaseError.toException()}")
                        }
                    }
                    userLikesRef.addListenerForSingleValueEvent(likedPostsListener)
                }
                // Picture is liked already, so a click should remove the like
                else {
                    it.favorite.setImageResource(R.drawable.ic_favorite)
                    it.favorite.tag = LikeStatus.NOT_LIKED

                    val likedPostsListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Check if the user already liked the picture
                            if (dataSnapshot.hasChild("$polaroidKey") && (dataSnapshot.child("$polaroidKey").value == true)) {
                                // The user have liked the picture but pressed the like button again, so we need to remove the like.
                                // Remove it from the posts user likes table
                                userLikesRef.child("$polaroidKey").removeValue()
                                // ... and remove 1 heart from the post in the database
                                postLikesRef.runTransaction(object : Transaction.Handler{
                                    override fun doTransaction(p0: MutableData): Transaction.Result {
                                        // CUZ FIREBASE TRANSACTIONS ARE STRANGE... https://stackoverflow.com/questions/35818946/firebase-runtransaction-not-working-mutabledata-is-null
                                        if(p0.value == null) {
                                            // Set value to 0 if the assumed value of null was correct, else
                                            // firebase will redo the transaction with the new value fetched from database.
                                            p0.value = 0
                                        } else {
                                            var likes = p0.value as Long
                                            p0.value = --likes
                                        }
                                        return Transaction.success(p0)
                                    }
                                    override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                                        Log.d("LikePic", "New likes amount = " + p2?.value.toString())
                                    }
                                })
                            } else {
                                // The user haven't liked the picture so we add the like to the database and increment the hearts on the post
                                userLikesRef.child("$polaroidKey").setValue(true)

                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            println("loadPost:onCancelled ${databaseError.toException()}")
                        }
                    }
                    userLikesRef.addListenerForSingleValueEvent(likedPostsListener)
                }
            }
        }
    }
    companion object {
        fun create(parent: ViewGroup): PolaroidViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.polaroid, parent, false)
            return PolaroidViewHolder(view)
        }
    }
}