package edu.utap.virtualsleepover.dbhelpers

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.Query
import edu.utap.virtualsleepover.models.Scrapbook

class ScrapbookDBHelper() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionRoot = "scrapbook"

    fun addToScrapbook(uid: String, scrapbook: HashMap<String, out Any>, scrapbookList: MutableLiveData<List<Scrapbook>>,
                       callback: () -> Unit = {}) {
        db.collection(collectionRoot)
            .add(scrapbook)
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "Item added to scrapbook")
                dbFetchScrapbook(uid, scrapbookList){}
                callback()
            }
            .addOnFailureListener { e ->
                Log.d(javaClass.simpleName, "Note create FAILED")
                Log.w(javaClass.simpleName, "Error ", e)
            }
    }

    fun dbFetchScrapbook(
        uid: String,
        scrapbook: MutableLiveData<List<Scrapbook>>,
        callback: () -> Unit = {}
    ) {
        db.collection(collectionRoot)
            //.orderBy("timeStamp", Query.Direction.DESCENDING)
            .whereEqualTo("savedByUser", uid)
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "Scrapbook fetch ${result!!.documents.size}")
                // NB: This is done on a background thread
                scrapbook.postValue(result.documents.mapNotNull {
                    it.toObject(Scrapbook::class.java)
                })
                callback()
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "Scrapbook fetch FAILED ", it)
                callback()
            }
    }

    fun removeScrapbook(
        uid: String,
        scrapbook: Scrapbook,
        scrapbookList: MutableLiveData<List<Scrapbook>>
    ) {
        db.collection(collectionRoot)
            .document(scrapbook.firestoreID)
            .delete()
            .addOnSuccessListener {
                Log.d(
                    javaClass.simpleName,
                    "Note delete, id: ${scrapbook.firestoreID}"
                )
                dbFetchScrapbook(uid, scrapbookList){}
            }
            .addOnFailureListener { e ->
                Log.d(javaClass.simpleName, "Note deleting FAILED, id: ${scrapbook.firestoreID}")
                Log.w(javaClass.simpleName, "Error adding document", e)
            }
    }
}