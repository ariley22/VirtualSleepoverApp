package edu.utap.virtualsleepover.ui.scrapbook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.utap.virtualsleepover.dbhelpers.ScrapbookDBHelper

class ScrapbookViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    fun getScrapbookItem(){

    }
}