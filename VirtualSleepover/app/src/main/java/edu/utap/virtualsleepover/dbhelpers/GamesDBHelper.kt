package edu.utap.virtualsleepover.dbhelpers

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import edu.utap.virtualsleepover.models.Game

class GamesDBHelper() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionRoot = "games"
    private var questionListener: ListenerRegistration? = null
    private var response1Listener: ListenerRegistration? = null
    private var response2Listener: ListenerRegistration? = null
    private var player1ReadyListener: ListenerRegistration? = null
    private var player2ReadyListener: ListenerRegistration? = null

    fun createGame(gameID: String, game: HashMap<String, out Any>) {
        db.collection(collectionRoot).document(gameID)
            .set(game)
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "Game successfully created")
            }
            .addOnFailureListener { e ->
                Log.w(javaClass.simpleName, "Error creating game", e)
            }
    }

    fun getGameInProgress(uid: String, setGameVM: (Game?) -> Unit){
        Log.d(javaClass.simpleName, "Get game in progress start")

        val collectionRef = db.collection(collectionRoot)
        var returnGame = Game()

        val query = collectionRef.where(
            Filter.or(
                Filter.equalTo("player1", uid),
                Filter.equalTo("player2", uid)
            ))
            .limit(1)

        query
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty){
                    returnGame = documents.documents[0].toObject(Game::class.java)!!
                    returnGame.gameID = documents.documents[0].id
                    Log.d(javaClass.simpleName, "Game found, game ID: ${returnGame.gameID}")
                    setGameVM(returnGame)
                }
                else{
                    Log.d(javaClass.simpleName, "Game not found for UID ${uid}")
                    setGameVM(returnGame)
                }

            }
            .addOnFailureListener { exception ->
                Log.w(javaClass.simpleName, "Error getting documents: ", exception)
                setGameVM(returnGame)
            }
        Log.d(javaClass.simpleName, "Get game in progress end")
    }

    fun updateGame(gameID: String, game: HashMap<String, out Any>, fetchGame: () -> Unit){
        db.collection(collectionRoot).document(gameID)
            .set(game, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "Game successfully updated")
                fetchGame()
            }
            .addOnFailureListener { e ->
                Log.w(javaClass.simpleName, "Error updating game", e)
            }
    }

    fun questionListener(gameID: String, listener: (String) -> Unit) {
        var question = ""
        questionListener = db.collection(collectionRoot).document(gameID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(javaClass.simpleName, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                question = snapshot.getString("currentQuestion") ?: ""
            }
            listener(question)
        }
    }

    fun response1Listener(gameID: String, listener: (String) -> Unit) {
        var response = ""
        response1Listener = db.collection(collectionRoot).document(gameID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(javaClass.simpleName, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                response = snapshot.getString("player1Response") ?: ""
            }
            listener(response)
        }
    }

    fun response2Listener(gameID: String, listener: (String) -> Unit) {
        var response = ""
        response2Listener = db.collection(collectionRoot).document(gameID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(javaClass.simpleName, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                response = snapshot.getString("player2Response") ?: ""
            }
            listener(response)
        }
    }

    fun player1ReadyListener(gameID: String, listener: (Boolean) -> Unit) {
        var ready = false
        player1ReadyListener = db.collection(collectionRoot).document(gameID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(javaClass.simpleName, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                ready = snapshot.getBoolean("player1Ready") ?: false
            }
            listener(ready)
        }
    }

    fun player2ReadyListener(gameID: String, listener: (Boolean) -> Unit) {
        var ready = false
        player2ReadyListener = db.collection(collectionRoot).document(gameID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(javaClass.simpleName, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                ready = snapshot.getBoolean("player2Ready") ?: false
            }
            listener(ready)
        }
    }

    fun stopQuestionListener() {
        questionListener?.remove()
        questionListener = null
        Log.d(javaClass.simpleName, "StopQuestionListener removed")
    }

    fun stopResponse1Listener() {
        response1Listener?.remove()
        response1Listener = null
    }

    fun stopResponse2Listener() {
        response2Listener?.remove()
        response2Listener = null
    }

    fun stopPlayer1ReadyListener() {
        response1Listener?.remove()
        response1Listener = null
    }

    fun stopPlayer2ReadyListener() {
        response2Listener?.remove()
        response2Listener = null
    }
}