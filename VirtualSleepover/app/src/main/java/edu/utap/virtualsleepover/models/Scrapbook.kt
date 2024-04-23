package edu.utap.virtualsleepover.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Scrapbook(
    var byUser: String = "",
    var question: String = "",
    var textResponse: String = "",
    var photoResponse: String = "",
    var savedByUser: String = "",

    @ServerTimestamp val timeStamp: Timestamp = Timestamp.now(),
    @DocumentId var firestoreID: String = ""
)