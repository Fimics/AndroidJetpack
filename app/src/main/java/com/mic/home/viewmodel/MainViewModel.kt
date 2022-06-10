package com.mic.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(){

    fun fetchData(){
        viewModelScope.launch(Dispatchers.IO) {
            //
        }
    }
}