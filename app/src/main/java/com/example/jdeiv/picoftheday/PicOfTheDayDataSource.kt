package com.example.jdeiv.picoftheday

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.ItemKeyedDataSource
import android.location.Location
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*


class PicOfTheDayDataSource(private val compositeDisposable: CompositeDisposable, private val location: FetchedLocation)
    : ItemKeyedDataSource<String, Polaroid>() {
    override fun getKey(item: Polaroid): String {
        return item.key!!
    }
    var state: MutableLiveData<State> = MutableLiveData()
    val polaroids: MutableList<Polaroid> = mutableListOf()
    val firebaseRef = FirebaseDatabase.getInstance().getReference("POTD/posts")

    private var retryCompletable: Completable? = null

    private fun updateState(state: State) {
        this.state.postValue(state)
    }

    fun retry() {
        if (retryCompletable != null) {
            compositeDisposable.add(retryCompletable!!
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe())
        }
    }

    private fun setRetry(action: Action?) {
        retryCompletable = if (action == null) null else Completable.fromAction(action)
    }

    private fun checkLocationDistance(userLocation: FetchedLocation, polaroidLon: Double, polaroidLat: Double): Boolean{
        val userLon = userLocation.longitude
        val userLat = userLocation.latitude
        var distance = 0
        var userLoc = Location("")
        var picLoc = Location("")

        if (userLat != null && userLon != null) {
            userLoc.latitude = userLat
            userLoc.longitude = userLon
            picLoc.latitude = polaroidLat
            picLoc.longitude = polaroidLon

            distance = userLoc.distanceTo(picLoc).toInt()
        }
        if(distance < 50000){
            return true
        }
        return false
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Polaroid>) {
        Log.d("POTDDatasource", "In LoadInitial. Load size: " + params.requestedLoadSize.toString()
                + " requested intial key: " + params.requestedInitialKey.toString())

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        val todaysDate = dateFormat.format(Date())

        // Amount of days back we load a winner
        val numOfPics = 30
        // How many listeners we send to war (same as num of pics)
        val numOfValueEventListeners = numOfPics
        // Counter to track how many of the listeners that have returned from their long
        // tiresome journey across the digital sea called internet
        var numOfValueEventListenersReturned = 0
        for (i in 0..numOfPics) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val dayBefore = dateFormat.format(calendar.time)
            Log.d("POTDDatasource", "Finding POTD for date: " + dayBefore)
            val query = firebaseRef.orderByChild("uploadDate").equalTo(dayBefore)

            val polaroidsListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    if (i == 0) polaroids.clear()
                    numOfValueEventListenersReturned++
                    var picOfTheDay: Polaroid? = null // Polaroid("Placeholder","https://cdn.shopify.com/s/files/1/0748/6277/products/dicktrophy1.jpg?v=1491262695",
//                        "Placeholder pic of the day", 0, FetchedLocation(0.0,0.0), "Admin", "1337-13-37", 1337)
                    Log.d("POTDDatasource", "Children found for $dayBefore: " + dataSnapshot.childrenCount.toString())
                    for (messageSnapshot in dataSnapshot.children.reversed()) {
                        val lon = messageSnapshot.child("postition/longitude").value as Double
                        val lat = messageSnapshot.child("postition/latitude").value as Double
                        val polaroidKey = messageSnapshot.key
                        val captionText = messageSnapshot.child("caption").value as String?
                        val user = messageSnapshot.child("user").value as String?
                        val likes = messageSnapshot.child("hearts").value as Long?
                        val uploadedDate = messageSnapshot.child("uploadDate").value as String?
                        val uploadedTime = messageSnapshot.child("uploadTime").value as Long?
                        val imgSrc = messageSnapshot.child("filename").value as String?

                        val polaroid = Polaroid(polaroidKey, imgSrc, captionText,likes,FetchedLocation(lon,lat),user, uploadedDate, uploadedTime)
                        if (picOfTheDay == null)
                            picOfTheDay = polaroid
                        else if (polaroid.likes!! > picOfTheDay.likes!!){
                            picOfTheDay = polaroid
                        }
                    }
                    if (picOfTheDay != null) {
                        polaroids.add(picOfTheDay!!)
                        Log.d("POTDDatasource", "In LoadInitial, added ${picOfTheDay.captionText} with ${picOfTheDay.likes} likes")

                    }


                    // If this listener is the last one to return from battle
                    if (numOfValueEventListenersReturned == numOfValueEventListeners) {
                        // Sort the data and notify the callback.onResult
                        polaroids.sortByDescending { it.uploadedDate }
                        polaroids.forEach {
                            Log.d("POTDDatasource", "${it.uploadedDate} ${it.captionText} Likes: ${it.likes}")
                        }
                        callback.onResult(polaroids)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    updateState(State.ERROR)
                    setRetry(Action { loadInitial(params, callback) })
                    println("loadPost:onCancelled ${databaseError.toException()}")
                }
            }
            query.addListenerForSingleValueEvent(polaroidsListener)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Polaroid>) {
//        Log.d("TimeAgoLocationDistanceFunc", "In LoadAFter. Load size: " + params.requestedLoadSize.toString()
//                + " params key: " + params.key.toString() + " lastKnownKey: " + lastKnownKey)
//        val query = firebaseRef.orderByKey()//.endAt(lastKnownKey).limitToLast(params.requestedLoadSize+1)
//        updateState(State.LOADING)
//        val polaroidsListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                polaroids.clear()
//                val numNodes = dataSnapshot.childrenCount.toInt()
//                if (!dataSnapshot.hasChildren()) polaroids.add(Polaroid("Empty", "http://trappedmagazine.com/wp-content/uploads/2018/01/End-LOGO.jpg",
//                    "The end", 0, null,null,null))
//                Log.d("TimeAgo", "Number of nodes: " + numNodes)
//
//                for (messageSnapshot in dataSnapshot.children.reversed()) {
//                    val polaroidKey = messageSnapshot.key
//                    // This skips the first post, since it is a duplicate
//                    if( lastKnownKey == polaroidKey) {
//                        continue
//                    }
//                    val lon = messageSnapshot.child("postition/longitude").value as Double?
//                    val lat = messageSnapshot.child("postition/latitude").value as Double?
//
//                    // NOT WORKING
////                    // If this is the first polaroid that is shown, we save its key so we know where we started
////                    if (firstKnownKey == null) {
////                        firstKnownKey = polaroidKey
////                        Log.d("TimeAgoLocationDistanceFunc", "In LoadAFter, saving firstKnownKey: " + messageSnapshot.child("caption").value as String?)
////                    }
////                    // Else we check if the firstKnownKey is the same key as this polaroid's key,
////                    // which means we looped through all data and need to stop adding more polaroids to the list
////                    else if (firstKnownKey == polaroidKey) {
////                        Log.d("TimeAgoLocationDistanceFunc", "In LoadAFter, firstknownkey is current polaroid: " + messageSnapshot.child("caption").value as String?)
////                        lastKnownKey = polaroidKey
////                        break
////                    }
//                    // Check if the location of the polaroid is in the accepted distance
////                    if(checkLocationDistance(location, lon!!, lat!!)) {
//                    val captionText = messageSnapshot.child("caption").value as String?
//                    val user = messageSnapshot.child("user").value as String?
//                    val likes = messageSnapshot.child("hearts").value as Long?
//                    val uploadedDate = messageSnapshot.child("uploadDate").value as String?
//                    val uploadedTime = messageSnapshot.child("uploadTime").value as Long?
//                    val imgSrc = messageSnapshot.child("filename").value as String?
////                        Log.d("TimeAgo", Date(uploaded!!).toString())
//
//                    val polaroid = Polaroid(polaroidKey, imgSrc, captionText,likes,FetchedLocation(lon,lat),user, uploadedDate, uploadedTime)
//                    polaroids.add(polaroid)
//                    Log.d("TimeAgoLocationDistanceFunc", "In LoadAFter, added " + messageSnapshot.child("caption").value as String?)
//
////                    }
//                    lastKnownKey = polaroidKey
//
//                }
//                updateState(State.DONE)
//                callback.onResult(polaroids)
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                updateState(State.ERROR)
//                setRetry(Action { loadAfter(params, callback) })
//                println("loadPost:onCancelled ${databaseError.toException()}")
//            }
//        }
//        query.addListenerForSingleValueEvent(polaroidsListener)
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Polaroid>) {
    }
}