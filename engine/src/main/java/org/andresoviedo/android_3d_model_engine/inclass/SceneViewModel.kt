package org.andresoviedo.android_3d_model_engine.inclass

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.andresoviedo.android_3d_model_engine.services.SceneLoader
import org.andresoviedo.util.singleArgViewModelFactory
import kotlin.coroutines.CoroutineContext

class SceneViewModel(val sceneRepository: SceneRepository) : ViewModel(), CoroutineScope {

    companion object {
        /**
         * Factory for creating [MainViewModel]
         *
         * @param arg the repository to pass to [MainViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::SceneViewModel)
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    val _cameraPos = sceneRepository.cameraPos
    val _cameraZoom = sceneRepository.cameraZoom
    val _selectionMode = sceneRepository.selectionMode
    val _objId = sceneRepository.objIndex

    fun updateCameraPos(x: Float, y: Float){
        sceneRepository.updateCameraPos(x, y)
    }

    fun updateCameraZoom(zoom: Float){
        sceneRepository.updateCameraZoom(zoom)
    }

    fun updateSelectionMode(mode: SceneLoader.Mode){
        sceneRepository.updateSelectionMode(mode)
    }

    fun updateObjId(id: Int){
        sceneRepository.updateObjIndex(id)
    }
}