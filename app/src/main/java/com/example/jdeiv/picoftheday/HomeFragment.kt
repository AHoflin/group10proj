package com.example.jdeiv.picoftheday

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.paging.*
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.fragment_home.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


class HomeFragment : Fragment() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutManager = LinearLayoutManager(activity)
        feedRecyclerViewHome.layoutManager = layoutManager

        adapter = RecyclerViewAdapter()
        feedRecyclerViewHome.adapter = adapter
    }

    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }

    var polaroidList: MutableList<Polaroid> = mutableListOf(
        Polaroid("imgSrcUrl", "This is the caption", "Eskilstuna", "13:37"),
        Polaroid("imgSrcUrl", "This is the caption2", "Eskilstuna", "13:38"),
        Polaroid("imgSrcUrl", "This is the caption3", "Eskilstuna", "13:39"),
        Polaroid("imgSrcUrl", "This is the caption4", "Eskilstuna", "13:310"),
        Polaroid("imgSrcUrl", "This is the caption5", "Eskilstuna", "13:311")
        )


}






class PolaroidDataSource : ItemKeyedDataSource<String, Polaroid>() {
    init {
        FirebaseManager.getPolaroidChangeSubject()?.observeOn(Schedulers.io())?.subscribeOn(Schedulers.computation())?.subscribe {
            invalidate()
        }
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Polaroid>) {
        FirebaseManager.getPolaroids(params.requestedLoadSize).subscribe({
            callback.onResult(it)
        }, {})
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Polaroid>) {
        FirebaseManager.getPolaroidsAfter(params.key, params.requestedLoadSize).subscribe({
            callback.onResult(it)
        }, {})
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Polaroid>) {
        FirebaseManager.getPolaroidsBefore(params.key, params.requestedLoadSize).subscribe({
            callback.onResult(it)
        }, {})
    }

    override fun getKey(item: Polaroid): String {
        return item.objectKey ?: ""
    }
}

object FirebaseManager {
    private val POLAROID_ROUTE = "POTD"

    private val polaroidAdapterInvalidation = PublishSubject.create<Any>()
    val database = FirebaseDatabase.getInstance()
    val databaseRef = database.reference

    init {
        databaseRef.child(POLAROID_ROUTE).addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                polaroidAdapterInvalidation.onNext(true)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                polaroidAdapterInvalidation.onNext(true)
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                polaroidAdapterInvalidation.onNext(true)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                polaroidAdapterInvalidation.onNext(true)
            }
        })
    }

    fun getPolaroidChangeSubject(): PublishSubject<Any>? {...}
    fun getPolaroids(count: Int): Single<List<Polaroid>> {...}
    fun getPolaroidsAfter(key: String, count: Int): Single<List<Polaroid>> {...}
    fun getPolaroidsBefore(key: String, count: Int): Single<List<Polaroid>> {...}
}

class PolaroidDataFactory : DataSource.Factory<String, Polaroid>() {

    private var datasourceLiveData = MutableLiveData<PolaroidDataSource>()

    override fun create(): PolaroidDataSource {
        val dataSource = PolaroidDataSource()
        datasourceLiveData.postValue(dataSource)
        return dataSource
    }
}

class PolaroidDataProvider {

    var polaroidDataFactory: PolaroidDataFactory = PolaroidDataFactory()
    private val PAGE_SIZE = 4

    fun getPolaroids(): LiveData<PagedList<Polaroid>>? {
        val config = PagedList.Config.Builder()
            .setInitialLoadSizeHint(PAGE_SIZE)
            .setPageSize(PAGE_SIZE)
            .build()

        return LivePagedListBuilder(polaroidDataFactory, config)
            .setInitialLoadKey("")
            .build()
    }
}

class PolaroidViewModel : ViewModel() {
    private val provider: PolaroidDataProvider? = PolaroidDataProvider()

    fun getPolaroids(): LiveData<PagedList<Polaroid>>? {
        return provider?.getPolaroids()
    }
}

class PolaroidAdapter constructor(context: Context) : PagedListAdapter<Polaroid, PolaroidAdapter.PolaroidViewHolder>(

    object : DiffUtil.ItemCallback<Polaroid>() {
        override fun areItemsTheSame(oldItem: Polaroid?, newItem: Polaroid?): Boolean = oldItem == newItem

        override fun areContentsTheSame(oldItem: Polaroid?, newItem: Polaroid?): Boolean = oldItem?.imgSrc == newItem?.imgSrc
    }) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PolaroidViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.polaroid, viewGroup, false)
        return PolaroidViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: PolaroidViewHolder, i: Int) {
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


    inner class PolaroidViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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