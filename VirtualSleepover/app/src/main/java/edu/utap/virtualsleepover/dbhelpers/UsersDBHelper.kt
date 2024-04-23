package edu.utap.virtualsleepover.dbhelpers

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import edu.utap.virtualsleepover.models.GameUser

class UsersDBHelper() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionRoot = "users"
    private var partnerListener: ListenerRegistration? = null

    fun getUserInfo(uid: String, getUser: (GameUser?) -> Unit){
        var returnUser = GameUser()
        val collectionRef = db.collection(collectionRoot)

        collectionRef.document(uid)
            .get()
            .addOnSuccessListener {
                if(it.exists()) returnUser = it.toObject(GameUser::class.java)!!
                getUser(returnUser)
            }
            .addOnFailureListener{
                Log.w(javaClass.simpleName, "Failed to get user info", it)
                getUser(returnUser)
            }
    }

    fun updateUser(uid: String, user: HashMap<String, out Any>, fetchUser: () -> Unit) {
        db.collection(collectionRoot).document(uid)
            .set(user, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "User updated")
                fetchUser()
            }
            .addOnFailureListener { e ->
                Log.w(javaClass.simpleName, "Error updating user", e)
            }
    }

    fun partnerListener(uid: String, listener: (String) -> Unit) {
        var partner = ""
        partnerListener = db.collection(collectionRoot).document(uid).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(javaClass.simpleName, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                partner = snapshot.getString("connectedUser") ?: ""
            }
            listener(partner)
        }
    }

    fun stopPartnerListener() {
        partnerListener?.remove()
        partnerListener = null
    }
}