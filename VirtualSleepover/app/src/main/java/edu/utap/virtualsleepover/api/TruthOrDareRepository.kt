package edu.utap.virtualsleepover.api

class TruthOrDareRepository(private val api: TruthOrDareApi) {
    suspend fun getTruthQuestion(): String{
        return api.getTruthQuestion().question
    }
    suspend fun getDare(): String{
        return api.getDare().question
    }
    suspend fun getWyrQuestion(): String{
        return api.getWyrQuestion().question
    }
}