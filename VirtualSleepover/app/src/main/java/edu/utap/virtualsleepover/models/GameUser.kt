package edu.utap.virtualsleepover.models

data class GameUser(
    //Not set by Firestore because we want it to match the user auth ID.
    var uid: String = "",

    var displayName: String = "",
    var currentGame: String = "",
    var connectedUser: String = "",
    var connectedUserName: String =""
    )