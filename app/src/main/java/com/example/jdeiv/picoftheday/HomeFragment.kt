package com.example.jdeiv.picoftheday

import android.app.AlertDialog
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

enum class State {
    DONE, LOADING, ERROR
}

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
                return oldItem.imgSrc == newItem.imgSrc
            }

            override fun areContentsTheSame(oldItem: Polaroid, newItem: Polaroid): Boolean {
                return oldItem == newItem
            }
        }
    }

//    override fun getItemCount(): Int {
//        return super.getItemCount() + if (hasFooter()) 1 else 0
//    }
//
//    private fun hasFooter(): Boolean {
//        return super.getItemCount() != 0 && (state == State.LOADING || state == State.ERROR)
//    }

    fun setState(state: State) {
        this.state = state
        notifyItemChanged(super.getItemCount())
    }
}

enum class LikeStatus {
    NOT_LIKED,
    LIKED
}

class PolaroidViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private var lastClickTime: Long = 0// For double taps on the polaroid image
    private var DOUBLE_CLICK_INTERVAL: Long = 200 //ms
    private var context = view.context

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
            val uploadedTime = polaroid.uploaded
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



            itemView.moreButton.setOnClickListener{
                val pictureDialog = AlertDialog.Builder(context)
                pictureDialog.setTitle("Report!")
                pictureDialog.setMessage("Do you want to report this picture?")

                pictureDialog.setNegativeButton("Yes"){ dialog, which ->
                    val polaroidKey = polaroid.key
                    val reportRef = FirebaseDatabase.getInstance().getReference("POTD/reportedPost")
                    val reportedPost = reportRef.child("$polaroidKey")
                    val reportedPostsListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // The user haven't liked the picture so we add the like to the database

                            // Increment the reports on the post
                            reportedPost.runTransaction(object : Transaction.Handler{
                                override fun doTransaction(p0: MutableData): Transaction.Result {
                                    // CUZ FIREBASE TRANSACTIONS ARE STRANGE... https://stackoverflow.com/questions/35818946/firebase-runtransaction-not-working-mutabledata-is-null
                                    if(p0.value == null) {
                                        // Set value to 1 if the assumed value of null was correct, else
                                        // firebase will redo the transaction with the new value fetched from database.
                                        p0.value = 1
                                    } else {
                                        var reports = p0.value as Long
                                        p0.value = ++reports
                                    }

                                    return Transaction.success(p0)
                                }
                                override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                                    Log.d("reportPic", "New reports amount = " + p2?.value.toString())
                                }
                            })

                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            println("loadPost:onCancelled ${databaseError.toException()}")
                        }
                    }
                    reportedPost.addListenerForSingleValueEvent(reportedPostsListener)

                }
                pictureDialog.setPositiveButton("No"){ dialog, which ->

                }

                pictureDialog.show()
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

class PolaroidListViewModel : ViewModel() {
    private lateinit var location: FetchedLocation
    lateinit var polaroidList: LiveData<PagedList<Polaroid>>
    private val pageSize = 5
    private val compositeDisposable = CompositeDisposable()
    private lateinit var polaroidDataSourceFactory: PolaroidDataSourceFactory

    init {
    }

    fun sketchyLateinitConstructorPlsDontJudgeMe(location: FetchedLocation) {
        polaroidDataSourceFactory = PolaroidDataSourceFactory(compositeDisposable, location)
        val config = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(pageSize * 2)
            .setEnablePlaceholders(true)
            .build()
        polaroidList = LivePagedListBuilder<String, Polaroid>(polaroidDataSourceFactory, config).build()
    }

    fun getState(): LiveData<State> = Transformations.switchMap<PolaroidDataSource,
            State>(polaroidDataSourceFactory.polaroidDataSourceLiveData, PolaroidDataSource::state)

    fun retry() {
        polaroidDataSourceFactory.polaroidDataSourceLiveData.value?.retry()
    }

    fun listIsEmpty(): Boolean {
        return polaroidList.value?.isEmpty() ?: true
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}

class PolaroidDataSourceFactory(private val compositeDisposable: CompositeDisposable, private val location: FetchedLocation)
    : DataSource.Factory<String, Polaroid>() {
    val polaroidDataSourceLiveData = MutableLiveData<PolaroidDataSource>()

    override fun create(): DataSource<String, Polaroid> {
        val polaroidDataSource = PolaroidDataSource(compositeDisposable, location)
        polaroidDataSourceLiveData.postValue(polaroidDataSource)
        return polaroidDataSource
    }
}

class PolaroidDataSource(private val compositeDisposable: CompositeDisposable, private val location: FetchedLocation)
    : ItemKeyedDataSource<String, Polaroid>() {
    override fun getKey(item: Polaroid): String {
        return item.key!!
    }
    var state: MutableLiveData<State> = MutableLiveData()
    val polaroids: MutableList<Polaroid> = mutableListOf()
    val firebaseRef = FirebaseDatabase.getInstance().getReference("POTD/posts")
    var lastKnownKey: String? = ""
    var firstKnownKey: String? = null

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
        Log.d("TimeAgoLocationDistanceFunc", "In LoadInitial. Load size: " + params.requestedLoadSize.toString() )
        firebaseRef.orderByKey().limitToFirst(params.requestedLoadSize)
        updateState(State.LOADING)
        val polaroidsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                polaroids.clear()
                for (messageSnapshot in dataSnapshot.children.reversed()) {
                    val lon = messageSnapshot.child("postition/longitude").value as Double
                    val lat = messageSnapshot.child("postition/latitude").value as Double
                    val polaroidKey = messageSnapshot.key

                    // NOT WORKING
//                    // If this is the first polaroid that is shown, we save its key so we know where we started
//                    if (firstKnownKey == null) {
//                        firstKnownKey = polaroidKey
//                        Log.d("TimeAgoLocationDistanceFunc", "In LoadInitial, saving firstKnownKey: " + messageSnapshot.child("caption").value as String?)
//                    }
//                    // Else we check if the firstKnownKey is the same key as this polaroid's key,
//                    // which means we looped through all data and need to stop adding more polaroids to the list
//                    else if (firstKnownKey == polaroidKey) {
//                        Log.d("TimeAgoLocationDistanceFunc", "In LoadInitial, firstknownkey is current polaroid: " + messageSnapshot.child("caption").value as String?)
//                        lastKnownKey = polaroidKey
//                        break
//                    }
                    // Check if the location of the polaroid is in the accepted distance
                    if(checkLocationDistance(location, lon!!, lat!!)) {
                        val captionText = messageSnapshot.child("caption").value as String?
                        val user = messageSnapshot.child("user").value as String?
                        val likes = messageSnapshot.child("hearts").value as Long?
                        val uploaded = messageSnapshot.child("uploadDate/time").value as Long?
                        val imgSrc = messageSnapshot.child("filename").value as String?
//                        Log.d("TimeAgo", Date(uploaded!!).toString())

                        val polaroid = Polaroid(polaroidKey, imgSrc, captionText,likes,FetchedLocation(lon,lat),user, uploaded)
                        polaroids.add(polaroid)
                        Log.d("TimeAgoLocationDistanceFunc", "In LoadInitial, added " + messageSnapshot.child("caption").value as String?)

                    }
                    lastKnownKey = polaroidKey
                }
                updateState(State.DONE)
                callback.onResult(polaroids)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                updateState(State.ERROR)
                setRetry(Action { loadInitial(params, callback) })

                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }

        firebaseRef.addListenerForSingleValueEvent(polaroidsListener)
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Polaroid>) {
        Log.d("TimeAgoLocationDistanceFunc", "In LoadAFter. Load size: " + params.requestedLoadSize.toString())
        firebaseRef.orderByKey().startAt(lastKnownKey).limitToLast(params.requestedLoadSize)
        updateState(State.LOADING)
        val polaroidsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                polaroids.clear()
                for (messageSnapshot in dataSnapshot.children.reversed()) {
                    val lon = messageSnapshot.child("postition/longitude").value as Double?
                    val lat = messageSnapshot.child("postition/latitude").value as Double?
                    val polaroidKey = messageSnapshot.key
                    // NOT WORKING
//                    // If this is the first polaroid that is shown, we save its key so we know where we started
//                    if (firstKnownKey == null) {
//                        firstKnownKey = polaroidKey
//                        Log.d("TimeAgoLocationDistanceFunc", "In LoadAFter, saving firstKnownKey: " + messageSnapshot.child("caption").value as String?)
//                    }
//                    // Else we check if the firstKnownKey is the same key as this polaroid's key,
//                    // which means we looped through all data and need to stop adding more polaroids to the list
//                    else if (firstKnownKey == polaroidKey) {
//                        Log.d("TimeAgoLocationDistanceFunc", "In LoadAFter, firstknownkey is current polaroid: " + messageSnapshot.child("caption").value as String?)
//                        lastKnownKey = polaroidKey
//                        break
//                    }
                    // Check if the location of the polaroid is in the accepted distance
                    if(checkLocationDistance(location, lon!!, lat!!)) {
                        val captionText = messageSnapshot.child("caption").value as String?
                        val user = messageSnapshot.child("user").value as String?
                        val likes = messageSnapshot.child("hearts").value as Long?
                        val uploaded = messageSnapshot.child("uploadDate/time").value as Long?
                        val imgSrc = messageSnapshot.child("filename").value as String?
//                        Log.d("TimeAgo", Date(uploaded!!).toString())

                        val polaroid = Polaroid(polaroidKey, imgSrc, captionText,likes,FetchedLocation(lon,lat),user, uploaded)
                        polaroids.add(polaroid)
                        Log.d("TimeAgoLocationDistanceFunc", "In LoadAFter, added " + messageSnapshot.child("caption").value as String?)

                    }
                    lastKnownKey = polaroidKey

                }
                updateState(State.DONE)
                callback.onResult(polaroids)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                updateState(State.ERROR)
                setRetry(Action { loadAfter(params, callback) })
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }
        firebaseRef.addListenerForSingleValueEvent(polaroidsListener)
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Polaroid>) {
//        firebaseRef.orderByChild("uploadDate/time").startAt(params.key.toDouble()-5).limitToFirst(5)
//        val polaroidsListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                polaroids.clear()
//                for (messageSnapshot in dataSnapshot.children) {
//                    val captionText = messageSnapshot.child("caption").value as String?
//                    val user = messageSnapshot.child("user").value as String?
//                    val likes = messageSnapshot.child("hearts").value as Long?
//                    val uploaded = messageSnapshot.child("uploadDate/time").value as Long?
//                    val lon = messageSnapshot.child("position/longitude").value as Double?
//                    val lat = messageSnapshot.child("position/latitude").value as Double?
//                    val imgSrc = messageSnapshot.child("filename").value as String?
//
//                    val polaroid = Polaroid(imgSrc, captionText,likes,FetchedLocation(lon,lat),user, Date(uploaded!!))
//                    polaroids.add(polaroid)
//                }
//                callback.onResult(polaroids)
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                println("loadPost:onCancelled ${databaseError.toException()}")
//            }
//        }
//        firebaseRef.addListenerForSingleValueEvent(polaroidsListener)
    }
}
