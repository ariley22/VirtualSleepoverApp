package edu.utap.virtualsleepover.dbhelpers

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import edu.utap.virtualsleepover.models.GameUser

class UsersDBHelper() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionRoot = "users"

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
}