package com.example.explorelimu.ui.members

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorelimu.data.member.MembersRepository
import com.example.explorelimu.util.singleArgViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class MembersViewModel(private val membersRepository: MembersRepository): ViewModel(), CoroutineScope {


    companion object {
        /**
         * Factory for creating [MainViewModel]
         *
         * @param arg the repository to pass to [MainViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::MembersViewModel)
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    var _teachers = membersRepository.teachersList

    var _learners = membersRepository.learnersList

    val _exception = MutableLiveData<String?>()
    val exception: LiveData<String?>
        get() = _exception

    val _spinner = MutableLiveData(false)
    val spinner: LiveData<Boolean?>
        get() = _spinner

    fun refreshTeachersList() = launchDataLoad { membersRepository.getTeachers() }

    fun refreshLearnersList() = launchDataLoad { membersRepository.getLearners() }

    private fun launchDataLoad(block: suspend () -> Unit): Unit {
        viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: Exception) {
                _exception.value = error.message
            } finally {
                _spinner.value = false
            }
        }
    }
}