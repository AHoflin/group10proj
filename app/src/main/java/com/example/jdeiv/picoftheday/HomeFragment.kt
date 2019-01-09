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
import android.content.res.Resources
import android.support.v4.content.res.TypedArrayUtils.getString
import android.util.Log
import android.widget.LinearLayout
import com.squareup.picasso.Picasso
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.polaroid.view.*


class HomeFragment : Fragment() {
    private lateinit var viewModel: PolaroidListViewModel
    private lateinit var polaroidListAdapter: PolaroidListAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        viewModel = ViewModelProviders.of(this).get(PolaroidListViewModel::class.java)
        recyclerView = view.findViewById(R.id.feedRecyclerViewHome)
        initAdapter()
        initState()

        return view
    }

    private fun initAdapter() {
        polaroidListAdapter = PolaroidListAdapter { viewModel.retry() }
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        recyclerView.adapter = polaroidListAdapter
        viewModel.polaroidList.observe(this, Observer {
            polaroidListAdapter.submitList(it)
        })
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

class PolaroidViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(polaroid: Polaroid?) {
        if (polaroid != null) {
            itemView.card_text.text = polaroid.captionText
            Picasso.get().load(polaroid.imgSrc).into(itemView.card_image)
            itemView.favorite.setImageResource(R.drawable.ic_favorite)

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


            Log.d("TimeAgo", timeAgoString)
            itemView.timestamp.text = timeAgoString
            itemView.favorite.setOnClickListener {
            if (it.favorite.tag == 0) {
                // DO DATABASE STUFF HERE? YES PROBABLY
                it.favorite.setImageResource(R.drawable.ic_favorite_clicked)
                it.favorite.tag = 1
            }
            else {
                it.favorite.setImageResource(R.drawable.ic_favorite)
                it.favorite.tag = 0

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


//    init {
//        polaroidImage = itemView.findViewById(R.id.card_image)
//        polaroidCaption = itemView.findViewById(R.id.card_text)
//        favoriteImage = itemView.findViewById(R.id.favorite)
//
//        favoriteImage.setOnClickListener {
//            Log.d("DEBUGFEED", polaroidCaption.text.toString())
//            if (favoriteImage.tag == 0) {
//                // DO DATABASE STUFF HERE? YES PROBABLY
//
//                favoriteImage.setImageResource(R.drawable.ic_favorite_clicked)
//                favoriteImage.tag = 1
//                Log.d("DEBUGFEED", polaroidCaption.text.toString() + ": Changed picture to clicked")
//            }
//            else {
//                favoriteImage.setImageResource(R.drawable.ic_favorite)
//                favoriteImage.tag = 0
//                Log.d("DEBUGFEED", polaroidCaption.text.toString() + ": Changed picture to not clicked")
//
//            }
//        }
    }

class PolaroidListViewModel : ViewModel() {

    var polaroidList: LiveData<PagedList<Polaroid>>
    private val pageSize = 5
    private val compositeDisposable = CompositeDisposable()
    private val polaroidDataSourceFactory: PolaroidDataSourceFactory

    init {
        polaroidDataSourceFactory = PolaroidDataSourceFactory(compositeDisposable)
        val config = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(pageSize * 2)
            .setEnablePlaceholders(false)
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

class PolaroidDataSourceFactory(private val compositeDisposable: CompositeDisposable)
    : DataSource.Factory<String, Polaroid>() {
    val polaroidDataSourceLiveData = MutableLiveData<PolaroidDataSource>()

    override fun create(): DataSource<String, Polaroid> {
        val polaroidDataSource = PolaroidDataSource(compositeDisposable)
        polaroidDataSourceLiveData.postValue(polaroidDataSource)
        return polaroidDataSource
    }
}

class PolaroidDataSource(private val compositeDisposable: CompositeDisposable)
    : ItemKeyedDataSource<String, Polaroid>() {
    override fun getKey(item: Polaroid): String {
        return item.key!!
    }
    var state: MutableLiveData<State> = MutableLiveData()
    val polaroids: MutableList<Polaroid> = mutableListOf()
    val firebaseRef = FirebaseDatabase.getInstance().getReference("/POTD")
    var lastKnownKey: String? = ""
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

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Polaroid>) {
        firebaseRef.orderByChild("uploadDate/time").limitToFirst(params.requestedLoadSize)
        updateState(State.LOADING)
        val polaroidsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                polaroids.clear()
                for (messageSnapshot in dataSnapshot.children) {

                    val polaroidKey = messageSnapshot.key
                    val captionText = messageSnapshot.child("caption").value as String?
                    val user = messageSnapshot.child("user").value as String?
                    val likes = messageSnapshot.child("hearts").value as Long?
                    val uploaded = messageSnapshot.child("uploadDate/time").value as Long?
                    val lon = messageSnapshot.child("position/longitude").value as Double?
                    val lat = messageSnapshot.child("position/latitude").value as Double?
                    val imgSrc = messageSnapshot.child("filename").value as String?
                    Log.d("TimeAgo", Date(uploaded!!).toString())

                    val polaroid = Polaroid(polaroidKey, imgSrc, captionText,likes,FetchedLocation(lon,lat),user, uploaded)
                    polaroids.add(polaroid)
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
        Log.d("Loaddd", params.key)
        firebaseRef.orderByKey().endAt(lastKnownKey).limitToFirst(params.requestedLoadSize)
        updateState(State.LOADING)
        val polaroidsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                polaroids.clear()
                for (messageSnapshot in dataSnapshot.children) {
                    val polaroidKey = messageSnapshot.key
                    val captionText = messageSnapshot.child("caption").value as String?
                    val user = messageSnapshot.child("user").value as String?
                    val likes = messageSnapshot.child("hearts").value as Long?
                    val uploaded = messageSnapshot.child("uploadDate/time").value as Long?
                    val lon = messageSnapshot.child("position/longitude").value as Double?
                    val lat = messageSnapshot.child("position/latitude").value as Double?
                    val imgSrc = messageSnapshot.child("filename").value as String?

                    val polaroid = Polaroid(polaroidKey, imgSrc, captionText,likes,FetchedLocation(lon,lat),user, uploaded)
                    polaroids.add(polaroid)
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


//class PolaroidDataSource : PageKeyedDataSource


//
//
//class PolaroidDataSource : ItemKeyedDataSource<String, Polaroid>() {
//    init {
//        FirebaseManager.getPolaroidChangeSubject()?.observeOn(Schedulers.io())?.subscribeOn(Schedulers.computation())?.subscribe {
//            invalidate()
//        }
//    }
//
//    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Polaroid>) {
//        FirebaseManager.getPolaroids(params.requestedLoadSize).subscribe({
//            callback.onResult(it)
//        }, {})
//    }
//
//    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Polaroid>) {
//        FirebaseManager.getPolaroidsAfter(params.key, params.requestedLoadSize).subscribe({
//            callback.onResult(it)
//        }, {})
//    }
//
//    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Polaroid>) {
//        FirebaseManager.getPolaroidsBefore(params.key, params.requestedLoadSize).subscribe({
//            callback.onResult(it)
//        }, {})
//    }
//
//    override fun getKey(item: Polaroid): String {
//        return item.objectKey ?: ""
//    }
//}
//
//object FirebaseManager {
//    private val POLAROID_ROUTE = "POTD"
//
//    private val polaroidAdapterInvalidation = PublishSubject.create<Any>()
//    val database = FirebaseDatabase.getInstance()
//    val databaseRef = database.reference
//
//    init {
//        databaseRef.child(POLAROID_ROUTE).addChildEventListener(object : ChildEventListener {
//            override fun onCancelled(p0: DatabaseError) {}
//
//            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
//                polaroidAdapterInvalidation.onNext(true)
//            }
//
//            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
//                polaroidAdapterInvalidation.onNext(true)
//            }
//
//            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//                polaroidAdapterInvalidation.onNext(true)
//            }
//
//            override fun onChildRemoved(p0: DataSnapshot) {
//                polaroidAdapterInvalidation.onNext(true)
//            }
//        })
//    }
//
//    fun getPolaroidChangeSubject(): PublishSubject<Any>? {...}
//    fun getPolaroids(count: Int): Single<List<Polaroid>> {...}
//    fun getPolaroidsAfter(key: String, count: Int): Single<List<Polaroid>> {...}
//    fun getPolaroidsBefore(key: String, count: Int): Single<List<Polaroid>> {...}
//}
//
//class PolaroidDataFactory : DataSource.Factory<String, Polaroid>() {
//
//    private var datasourceLiveData = MutableLiveData<PolaroidDataSource>()
//
//    override fun create(): PolaroidDataSource {
//        val dataSource = PolaroidDataSource()
//        datasourceLiveData.postValue(dataSource)
//        return dataSource
//    }
//}
//
//class PolaroidDataProvider {
//
//    var polaroidDataFactory: PolaroidDataFactory = PolaroidDataFactory()
//    private val PAGE_SIZE = 4
//
//    fun getPolaroids(): LiveData<PagedList<Polaroid>>? {
//        val config = PagedList.Config.Builder()
//            .setInitialLoadSizeHint(PAGE_SIZE)
//            .setPageSize(PAGE_SIZE)
//            .build()
//
//        return LivePagedListBuilder(polaroidDataFactory, config)
//            .setInitialLoadKey("")
//            .build()
//    }
//}
//
//class PolaroidViewModel : ViewModel() {
//    private val provider: PolaroidDataProvider? = PolaroidDataProvider()
//
//    fun getPolaroids(): LiveData<PagedList<Polaroid>>? {
//        return provider?.getPolaroids()
//    }
//}
//
//class PolaroidAdapter constructor(context: Context) : PagedListAdapter<Polaroid, PolaroidAdapter.PolaroidViewHolder>(
//
//    object : DiffUtil.ItemCallback<Polaroid>() {
//        override fun areItemsTheSame(oldItem: Polaroid?, newItem: Polaroid?): Boolean = oldItem == newItem
//
//        override fun areContentsTheSame(oldItem: Polaroid?, newItem: Polaroid?): Boolean = oldItem?.imgSrc == newItem?.imgSrc
//    }) {
//
//    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PolaroidViewHolder {
//        val v = LayoutInflater.from(viewGroup.context)
//            .inflate(R.layout.polaroid, viewGroup, false)
//        return PolaroidViewHolder(v)
//    }
//
//    override fun onBindViewHolder(viewHolder: PolaroidViewHolder, i: Int) {
//        viewHolder.polaroidCaption.text = captions[i]
//        viewHolder.polaroidImage.setImageResource(images[i])
//    }
//
//    override fun getItemCount(): Int {
//        return captions.size
//    }
//
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
//
//
//    inner class PolaroidViewHolder(itemView: View) : RecyclerView.PolaroidViewHolder(itemView) {
//        var polaroidImage: ImageView
//        var polaroidCaption: TextView
//        var favoriteImage: ImageView
//
//        init {
//            polaroidImage = itemView.findViewById(R.id.card_image)
//            polaroidCaption = itemView.findViewById(R.id.card_text)
//            favoriteImage = itemView.findViewById(R.id.favorite)
//
//            favoriteImage.setOnClickListener {
//                Log.d("DEBUGFEED", polaroidCaption.text.toString())
//                if (favoriteImage.tag == 0) {
//                    // DO DATABASE STUFF HERE? YES PROBABLY
//
//                    favoriteImage.setImageResource(R.drawable.ic_favorite_clicked)
//                    favoriteImage.tag = 1
//                    Log.d("DEBUGFEED", polaroidCaption.text.toString() + ": Changed picture to clicked")
//                }
//                else {
//                    favoriteImage.setImageResource(R.drawable.ic_favorite)
//                    favoriteImage.tag = 0
//                    Log.d("DEBUGFEED", polaroidCaption.text.toString() + ": Changed picture to not clicked")
//
//                }
//            }
//        }
//    }
//}