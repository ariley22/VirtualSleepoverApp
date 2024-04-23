package edu.utap.virtualsleepover.models

data class Game(
    // Primary key not set by Firestore because users will use it to log into games
    // Because it's just a demo with very limited usage, I want to keep
    // the game IDs simple by using random 3- to 6-digit numbers. Firestore
    // IDs are way too long. If this was a production app I would need to
    // prevent duplication so that an ongoing game couldn't get overridden.
    var gameID: String = "",

    //0 = undefined, 1 = 10 Questions, 2 = Would You Rather, 3 = Truth or Dare
    var gameType: Int = 0,
    var player1: String = "",
    var player2: String = "",
    var player1Name: String = "",
    var player2Name: String = "",
    var writingUser: Int = 0,
    var round: Int = 1,

    var currentQuestion: String = "",
    var player1Response: String = "",
    var player2Response: String = "",
    var player1Ready: Boolean = false,
    var player2Ready: Boolean = false
)