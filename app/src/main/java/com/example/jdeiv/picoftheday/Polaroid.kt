package com.example.jdeiv.picoftheday
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Polaroid(
    val key: String? = "",
    val imgSrc: String? = "",
    val captionText: String? = "",
    val likes: Long? = 0,
    val location: FetchedLocation? = null,
    val user: String? = "",
    val uploadedDate: String? = null,
    val uploadedTime: Long? = null) {



//    // This is not used atm.
//    @Exclude
//    fun toMap(): Map<String, Any?> {
//        return mapOf(
//            "user" to user,
//            "caption" to captionText,
//            "hearts" to likes,
//            "uploadDate" to uploaded,
//            "position" to location,
//            "filename" to imgSrc
//        )
//    }
}
