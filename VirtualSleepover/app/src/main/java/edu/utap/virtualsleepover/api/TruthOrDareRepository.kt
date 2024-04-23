package edu.utap.virtualsleepover.api

class TruthOrDareRepository(private val api: TruthOrDareApi) {
    suspend fun getTruthQuestion(): String{
        return api.getTruthQuestion().question
    }
}