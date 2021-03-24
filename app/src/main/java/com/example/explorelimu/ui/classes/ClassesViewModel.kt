package com.example.explorelimu.ui.classes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorelimu.data.model.Model
import com.example.explorelimu.data.session.SessionsRepository
import com.example.explorelimu.util.singleArgViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class ClassesViewModel(private val sessionsRepository: SessionsRepository) : ViewModel(), CoroutineScope {

    companion object {
        /**
         * Factory for creating [MainViewModel]
         *
         * @param arg the repository to pass to [MainViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::ClassesViewModel)
    }

    private val _text = MutableLiveData<String>().apply {
        value = "You have no classes"
    }
    val text: LiveData<String> = _text

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    val sessions = sessionsRepository.classesList

    val _exception = MutableLiveData<String?>()
    val exception: LiveData<String?>
        get() = _exception

//    val _noSessions = MutableLiveData(true)
//    val noSessions: LiveData<Boolean>
//        get() = _noSessions

    val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean>
        get() = _loading

    fun refreshSessionsList() = launchDataLoad { sessionsRepository.getClasses() }

    fun addSession(roomName: String, model: Model) = launchDataLoad {
        sessionsRepository.createSession(roomName, model, sessionsRepository.getRosterEntries())
    }

    private fun launchDataLoad(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _loading.value = true
                block()
            } catch (error: Exception) {
                _exception.value = error.message
            } finally {
                _loading.value = false
            }
        }
    }
}