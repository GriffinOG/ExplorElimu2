package org.andresoviedo.android_3d_model_engine.inclass.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.andresoviedo.android_3d_model_engine.services.SceneLoader

class SceneRepository(private val context: Context) {
    private var sceneRepository: SceneRepository? = null

    fun getInstance(): SceneRepository? {
        if (sceneRepository == null) sceneRepository = SceneRepository(context)
        return sceneRepository
    }

    val _cameraPos: MutableLiveData<Array<Float>> = MutableLiveData()
    val cameraPos: LiveData<Array<Float>>
        get() = _cameraPos

    val _cameraZoom: MutableLiveData<Float> = MutableLiveData()
    val cameraZoom: LiveData<Float>
        get() = _cameraZoom

    val _selectionMode: MutableLiveData<SceneLoader.Mode> = MutableLiveData()
    val selectionMode: LiveData<SceneLoader.Mode>
        get() = _selectionMode

    val _objIndex: MutableLiveData<Int> = MutableLiveData()
    val objIndex: LiveData<Int>
        get() = _objIndex

    fun updateCameraPos(x: Float, y: Float){
        val newPos = arrayOf(x,y)
        _cameraPos.postValue(newPos)
    }

    fun updateCameraZoom(zoom: Float){
        _cameraZoom.postValue(zoom)
    }

    fun updateSelectionMode(mode: SceneLoader.Mode){
        _selectionMode.postValue(mode)
    }

    fun updateObjIndex(id: Int){
        _objIndex.postValue(id)
    }
}