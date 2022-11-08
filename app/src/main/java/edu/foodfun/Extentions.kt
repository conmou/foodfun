package edu.foodfun

import androidx.lifecycle.MutableLiveData

class Extentions {
    companion object {
        fun <T> MutableLiveData<T>.notifyObserver() {
            this.postValue(this.value)
        }
    }
}