package edu.utap.virtualsleepover

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.utap.virtualsleepover.api.TruthOrDareApi
import edu.utap.virtualsleepover.api.TruthOrDareRepository
import edu.utap.virtualsleepover.dbhelpers.GamesDBHelper
import edu.utap.virtualsleepover.dbhelpers.ScrapbookDBHelper
import edu.utap.virtualsleepover.dbhelpers.UsersDBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import edu.utap.virtualsleepover.models.Game
import edu.utap.virtualsleepover.models.Scrapbook

class MainViewModel : ViewModel() {

    // Track current authenticated user
    private var currentAuthUser = invalidUser
    private var uidLiveData = MutableLiveData<String>()
    var currentUID = ""
    var inBetweenRounds = false
    
    private val currentGame = MutableLiveData<Game>()

    private val userDisplayName = MutableLiveData<String>()
    private val currentPartnerName = MutableLiveData<String>()
    private val currentPartnerID = MutableLiveData<String>()

    private val gameID = MutableLiveData<String>()
    private val currentRoundLive = MutableLiveData<Int>()
    var playerNumber = 0
    var isUserWriting = true
    var timeToAnswer = false
    var currentRoundStatic = 1
    private val isUserWritingLive = MutableLiveData<Boolean>()
    private val player1ID = MutableLiveData<String>()
    private val player2ID = MutableLiveData<String>()
    private val player1Name = MutableLiveData<String>()
    private val player2Name = MutableLiveData<String>()
    private val currentQuestionLive = MutableLiveData<String>()
    var currentQuestion = ""
    private val writingUser = MutableLiveData<Int>()

    private var scrapbookList = MutableLiveData<List<Scrapbook>>()
    private var scrapbookEmpty = MediatorLiveData<Boolean>().apply {
        addSource(scrapbookList) {
            this.value = it.isNullOrEmpty()
        }
    }

    //database collections
    private val gamesDbHelp = GamesDBHelper()
    private val usersDbHelp = UsersDBHelper()
    private val scrapbookDbHelp = ScrapbookDBHelper()

    //////////////////////////////////////////////////////////////////////
    //Firestore database snapshots
    private val _partnerSnapshot = MutableLiveData<String>()
    val partnerSnapshot: LiveData<String>
        get() = _partnerSnapshot

    private val _questionSnapshot = MutableLiveData<String>()
    val questionSnapshot: LiveData<String>
        get() = _questionSnapshot

    private val _response1Snapshot = MutableLiveData<String>()
    val response1Snapshot: LiveData<String>
        get() = _response1Snapshot

    private val _response2Snapshot = MutableLiveData<String>()
    val response2Snapshot: LiveData<String>
        get() = _response2Snapshot

    private val _player1ReadySnapshot = MutableLiveData<Boolean>()
    val player1ReadySnapshot: LiveData<Boolean>
        get() = _player1ReadySnapshot

    private val _player2ReadySnapshot = MutableLiveData<Boolean>()
    val player2ReadySnapshot: LiveData<Boolean>
        get() = _player2ReadySnapshot

    //////////////////////////////////////////////////////////////////////
    //API
    private val truthApi = TruthOrDareApi.create()
    private val genQuestion = MutableLiveData<String>()
    private val truthRepository = TruthOrDareRepository(truthApi)

    //API FUNCTIONS
    fun netTruthQuestion() = viewModelScope.launch(
        context = viewModelScope.coroutineContext
                + Dispatchers.IO) {
        genQuestion.postValue(truthRepository.getTruthQuestion())
    }

    fun observeGenQuestion(): LiveData<String> {
        return genQuestion
    }

    //////////////////////////////////////////////////////////////////////
    //USER BASED FUNCTIONS
    fun setCurrentAuthUser(user: User) {
        currentAuthUser = user
        uidLiveData.value = currentAuthUser.uid
        currentUID = currentAuthUser.uid
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    fun setUserInfo() {
        Log.d(javaClass.simpleName, "Sending UID ${uidLiveData.value.toString()} to userDbhelper")

        usersDbHelp.getUserInfo(uidLiveData.value.toString()) { user ->
            currentPartnerName.postValue(user!!.connectedUserName)
            currentPartnerID.postValue(user.connectedUser)
            userDisplayName.postValue(user.displayName)
        }
    }

    fun observeUID(): LiveData<String>{
        return uidLiveData
    }

    fun observeDisplayName(): LiveData<String>{
        return userDisplayName
    }

    fun observePlayer1ID(): LiveData<String>{
        return player1ID
    }

    fun updatePartner(partner: String) {
        _partnerSnapshot.value = partner
    }

    fun partnerListener(){
        usersDbHelp.partnerListener(currentUID){
            updatePartner(it)
        }
    }

    fun stopPartnerListener(){
        usersDbHelp.stopPartnerListener()
    }

    fun observePartnerName(): LiveData<String>{
        return currentPartnerName
    }

    //the only way to call this is when player 2 types in the game ID
    //this is not very flexible but makes it relatively controlled and safe to code
    fun connectToUser(displayName: String){
        val uid = uidLiveData.value.toString()
        val partnerUid = player1ID.value.toString()

        Log.d(javaClass.simpleName, "Starting connectToUser, UID: $uid")
        Log.d(javaClass.simpleName, "Partner UID: $partnerUid")

        val updatePlayer1 = hashMapOf(
            "connectedUser" to uid,
            "connectedUserName" to displayName
        )
        val updatePlayer2 = hashMapOf(
            "connectedUser" to partnerUid
        )

        usersDbHelp.updateUser(uid, updatePlayer2){
            setUserInfo()
        }
        usersDbHelp.updateUser(partnerUid, updatePlayer1) {
            setUserInfo()
        }
    }

    fun disconnectUser(){
        val connectedUser = currentPartnerID.value.toString()

        val user = hashMapOf(
            "connectedUser" to "",
            "connectedUserName" to "",
            "currentGame" to ""
        )

        usersDbHelp.updateUser(uidLiveData.value.toString(), user){
            setUserInfo()
        }
        usersDbHelp.updateUser(connectedUser, user){
            setUserInfo()
        }

    }

    //////////////////////////////////////////////////////////////////////
    //GAME BASED FUNCTIONS
    fun fetchGameInProgress() {
        Log.d(javaClass.simpleName,"Sending UID $uidLiveData to gameDbhelper")
        gamesDbHelp.getGameInProgress(uidLiveData.value.toString()) { game ->
            currentGame.postValue(game!!)
            gameID.postValue(game.gameID)
            player1ID.postValue(game.player1)
            player2ID.postValue(game.player2)
            player1Name.postValue(game.player1Name)
            player2Name.postValue(game.player2Name)
            currentQuestionLive.postValue(game.currentQuestion)
            currentRoundLive.postValue(game.round)
            writingUser.postValue(game.writingUser)

            var partner = ""

            if(game.player1 == uidLiveData.value.toString()){
                playerNumber = 1
                partner = game.player2Name
            }
            else{
                playerNumber = 2
                partner = game.player1Name
            }
            val user = hashMapOf(
                "connectedUserName" to partner
            )

            usersDbHelp.updateUser(uidLiveData.value.toString(), user){
                setUserInfo()
            }

            Log.d(javaClass.simpleName, "Updating isUserWriting. DB writingUser =" +
                    " ${writingUser.value}, viewModel playerNumber = $playerNumber")

            if(playerNumber == writingUser.value){
                if(isUserWritingLive.value != true) isUserWritingLive.postValue(true)
                isUserWriting = true
            }
            else if (playerNumber != writingUser.value){
                if(isUserWritingLive.value != false) isUserWritingLive.postValue(false)
                isUserWriting = false
            }
        }
    }

    fun observeGameID(): LiveData<String>{
        return gameID
    }

    fun getCurrentRound(): Int{
        return currentGame.value?.round ?: 0
    }

    fun observeIsUserWriting(): LiveData<Boolean>{
        return isUserWritingLive
    }

    fun setDbQuestion(question: String){
        val game = hashMapOf(
            "currentQuestion" to question
        )
        gamesDbHelp.updateGame(currentGame.value!!.gameID, game){
            fetchGameInProgress()
        }
    }

    fun setPlayer1Response(response: String){
        val game = hashMapOf(
            "player1Response" to response
        )
        gamesDbHelp.updateGame(currentGame.value!!.gameID, game){
            fetchGameInProgress()
        }
    }

    fun setPlayer2Response(response: String){
        val game = hashMapOf(
            "player2Response" to response
        )
        gamesDbHelp.updateGame(currentGame.value!!.gameID, game){
            fetchGameInProgress()
        }
    }

    fun observeCurrentQuestion(): LiveData<String>{
        return currentQuestionLive
    }

    fun updateQuestion(question: String) {
        Log.d(javaClass.simpleName, "Question snapshot updated in viewModel to $question")
        _questionSnapshot.value = question
    }

    fun questionListener(){
        Log.d(javaClass.simpleName, "Question listener start. Game ID = ${gameID.value.toString()}")
        gamesDbHelp.questionListener(gameID.value.toString()){
            updateQuestion(it)
        }
    }

    fun stopQuestionListener(){
        Log.d(javaClass.simpleName, "Question listener stopped in view model")
        gamesDbHelp.stopQuestionListener()
    }

    fun updateResponse1(response: String) {
        _response1Snapshot.value = response
    }

    fun response1Listener(){
        gamesDbHelp.response1Listener(gameID.value.toString()){
            updateResponse1(it)
        }
    }

    fun stopResponse1Listener(){
        gamesDbHelp.stopResponse1Listener()
    }

    fun updateResponse2(response: String) {
        _response2Snapshot.value = response
    }

    fun response2Listener(){
        gamesDbHelp.response2Listener(gameID.value.toString()){
            updateResponse2(it)
        }
    }

    fun stopResponse2Listener(){
        gamesDbHelp.stopResponse2Listener()
    }

    fun updatePlayer1Ready(ready: Boolean) {
        _player1ReadySnapshot.value = ready
    }

    fun player1ReadyListener(){
        gamesDbHelp.player1ReadyListener(gameID.value.toString()){
            updatePlayer1Ready(it)
        }
    }

    fun stopPlayer1ReadyListener(){
        gamesDbHelp.stopPlayer1ReadyListener()
    }

    fun updatePlayer2Ready(ready: Boolean) {
        _player2ReadySnapshot.value = ready
    }

    fun player2ReadyListener(){
        gamesDbHelp.player2ReadyListener(gameID.value.toString()){
            updatePlayer2Ready(it)
        }
    }

    fun stopPlayer2ReadyListener(){
        gamesDbHelp.stopPlayer2ReadyListener()
    }

    fun readyForNextRound(){
        var game: HashMap<String, Any>
        if(playerNumber == 1){
            game = hashMapOf(
                "player1Ready" to true,
            )
        }
        else{
            game = hashMapOf(
                "player2Ready" to true,
            )
        }
        gamesDbHelp.updateGame(gameID.value.toString(), game){
            fetchGameInProgress()
        }
    }

    fun nextRound(finishGame: () -> Unit){
        if((currentRoundLive.value ?: 2) < 2) {
            currentRoundLive.value = currentRoundLive.value!! + 1
            if (writingUser.value == 1) writingUser.value = 2
            else writingUser.value = 1

            val game = hashMapOf(
                "writingUser" to writingUser.value as Any,
                "round" to currentRoundLive.value as Any,
                "currentQuestion" to "",
                "player1Response" to "",
                "player2Response" to "",
                "player1Ready" to false,
                "player2Ready" to false
            )
            gamesDbHelp.updateGame(gameID.value.toString(), game){
                fetchGameInProgress()
            }
        }
        else finishGame()
    }

    fun createGame(gameType: Int){
        //Create a game ID
        playerNumber = 1
        val gameID = (100..999999).random().toString()
        var firstWriter = 1

        val playerTwo = currentPartnerID.value.toString()
        if(playerTwo.isEmpty()) firstWriter = 2
        val playerTwoDisplayName = currentPartnerName.value.toString()

        //Add player to game
        val game = hashMapOf(
            "player1" to uidLiveData.value.toString(),
            "gameType" to gameType,
            "writingUser" to firstWriter,
            "round" to 1,
            "player2" to playerTwo,
            "player2Name" to playerTwoDisplayName
        )
        gamesDbHelp.createGame(gameID, game)
        fetchGameInProgress()

        // This method will add the game to the current user in the users collection.
        // It will also add the user to the collection if it's the first time they
        // create or join a game.
        val user = hashMapOf(
            "currentGame" to gameID
        )
        usersDbHelp.updateUser(uidLiveData.value.toString(), user){
            setUserInfo()
        }
    }

    fun updateDisplayName(gameID: String, displayName: String){
        var game: HashMap<String, Any>
        var partnerUid = ""
        if(playerNumber == 1){
            game = hashMapOf("player1Name" to displayName)
            partnerUid = player2ID.value.toString()
        }
        else{
            game = hashMapOf("player2Name" to displayName)
            partnerUid = player1ID.value.toString()
        }
        gamesDbHelp.updateGame(gameID, game){
            fetchGameInProgress()
        }

        val user = hashMapOf(
            "displayName" to displayName
        )
        usersDbHelp.updateUser(uidLiveData.value.toString(), user){
            setUserInfo()
        }
        if (!partnerUid.isNullOrEmpty()) {
            val partner = hashMapOf(
                "connectedUserName" to displayName
            )
            usersDbHelp.updateUser(partnerUid, partner) {}
        }
    }

    fun addAsPlayerTwo(gameID: String, displayName: String){
        playerNumber = 2
        val game = hashMapOf(
            "player2" to uidLiveData.value.toString(),
            "player2Name" to displayName
        )
        gamesDbHelp.updateGame(gameID, game){
            fetchGameInProgress()
        }

        val user = hashMapOf(
            "currentGame" to gameID
        )
        usersDbHelp.updateUser(uidLiveData.value.toString(), user){
            setUserInfo()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    //SCRAPBOOK BASED FUNCTIONS
    fun observeScrapbookList(): LiveData<List<Scrapbook>> {
        return scrapbookList
    }
    fun observeScrapbookEmpty(): LiveData<Boolean> {
        return scrapbookEmpty
    }

    fun addToScrapbook(savePlayerNumber: Int,
                       callback: () -> Unit = {}){
        var byUser = ""
        var response = ""

        if(savePlayerNumber == playerNumber){
            byUser = "self"
        }
        else{
            byUser = currentPartnerName.value.toString()
        }

        val scrapbook = hashMapOf(
            "question" to currentGame.value!!.currentQuestion,
            "byUser" to byUser,
            "savedByUser" to currentUID
        )

        if (savePlayerNumber == 1){
            scrapbook["textResponse"] = currentGame.value!!.player1Response
        }
        else{
            scrapbook["textResponse"] = currentGame.value!!.player2Response
        }

        scrapbookDbHelp.addToScrapbook(currentUID, scrapbook, scrapbookList, callback)
    }

    fun getScrapbookItem(position: Int): Scrapbook{
        val scrapbook = scrapbookList.value?.get(position)
        Log.d(javaClass.simpleName, "scrapbookList.value ${scrapbookList.value}")
        Log.d(javaClass.simpleName, "getScrapbookItem $position list len ${scrapbookList.value?.size}")
        return scrapbook!!
    }

    fun removeScrapbook(position: Int) {
        val scrapbook = getScrapbookItem(position)

        Log.d(javaClass.simpleName, "remove note at pos: $position id: ${scrapbook.firestoreID}")
        scrapbookDbHelp.removeScrapbook(currentUID, scrapbook, scrapbookList)
    }
}