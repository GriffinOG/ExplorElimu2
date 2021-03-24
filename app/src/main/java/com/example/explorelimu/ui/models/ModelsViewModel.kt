package com.example.explorelimu.ui.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorelimu.data.model.Model
import com.example.explorelimu.data.model.ModelsRepository
import com.example.explorelimu.util.singleArgViewModelFactory
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class ModelsViewModel(val modelsRepository: ModelsRepository) : ViewModel(), CoroutineScope {

    private var categoryId: Int? = null
    var _models: LiveData<List<Model>> = MutableLiveData()

    constructor(modelsRepository: ModelsRepository, categoryId: Int) : this(modelsRepository) {
        this.categoryId = categoryId
        _models = modelsRepository.getModelsAndReturn(categoryId)
    }

    val _instantModels = modelsRepository.modelList

    companion object {
        /**
         * Factory for creating [MainViewModel]
         *
         * @param arg the repository to pass to [MainViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::ModelsViewModel)
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    var _categories = modelsRepository.getCategories()

    fun refreshModelsList(categoryId: Int) = launchDataLoad { modelsRepository.refreshModelsList(categoryId) }

    private fun launchDataLoad(block: suspend () -> Unit): Unit {
        viewModelScope.launch {
            try {
                block()
            } catch (error: Exception) {
                Log.e(javaClass.name, error.message.toString())
            }
        }
    }

//    fun getAllCategories(): MutableLiveData<List<Category>> {
//        return _categories
//    }

//    fun fetchModels(categoryId: Int) {
//        launch(Dispatchers.Main) {
//            _models.value = withContext(Dispatchers.IO){
//                modelsRepository.getModels(categoryId)
//            }
//        }
//    }

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is models Fragment"
//    }
//    val text: LiveData<String> = _text
}