package com.example.explorelimu.data.model

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.explorelimu.data.category.Category
import com.example.explorelimu.util.getFirebaseFileRef
import com.example.render.Global
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.StorageMetadata
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.set


class ModelsRepository(private val context: Context) {

    private var modelsRepository: ModelsRepository? = null

    @Synchronized
    fun getInstance(): ModelsRepository? {
        if (modelsRepository == null) modelsRepository = ModelsRepository(context)
        return modelsRepository
    }

    val _modelsList: MutableLiveData<List<Model>> = MutableLiveData()
    val modelList: LiveData<List<Model>>
        get() = _modelsList

    fun getCategories(): LiveData<List<Category>>{
        Log.d("loadCategories", "function called")

        val categories = ArrayList<Category>()
        val categoryList:MutableLiveData<List<Category>> = MutableLiveData()

        val stringRequest = StringRequest(
                Request.Method.GET, Global.GET_CATEGORIES_URL,
                { response ->
                    try {
                        //converting the string to json array object
                        val array = JSONArray(response)

                        Log.d(javaClass.name + " loadCats arrlength", array.length().toString())

                        //traversing through all the object
                        for (i in 0 until array.length()) {

                            //getting product object from json array
                            val product = array.getJSONObject(i)

                            if (product.getInt("mcount") > 0)
                                categories.add(Category(product.getInt("category_id"), product.getString("name"), product.getInt("mcount")))

//                        Log.d("category name", product.getInt("name").toString())
                        }

                        categoryList.postValue(categories)

                    } catch (e: JSONException) {
                        Log.e("loadCategories JSON ", e.message!!)
                    }
                }
        ) { error: VolleyError ->
            Log.e(
                    "loadCategories volley ",
                    error.toString()
            )
            Toast.makeText(context, "Error loading categories", Toast.LENGTH_LONG).show()
        }

        //adding our stringrequest to queue
        Volley.newRequestQueue(context).add(stringRequest)
        return categoryList
    }

    fun getModelsAndReturn(categoryId: Int): LiveData<List<Model>>{
        Log.d("loadModels", "function called")

        val modelList:MutableLiveData<List<Model>> = MutableLiveData()

        return createModelLoadRequest(categoryId, modelList)
    }

    fun refreshModelsList(categoryId: Int){
        Log.d("loadModels", "function called")

        createModelLoadRequest(categoryId, _modelsList)
    }

    private fun createModelLoadRequest(categoryId: Int, modelList:MutableLiveData<List<Model>>): MutableLiveData<List<Model>>{

        val models = ArrayList<Model>()

        val stringRequest: StringRequest = object : StringRequest(
                Method.POST, Global.GET_MODELS_URL,
                Response.Listener { serverResponse ->
                    try {
                        //converting the string to json array object
                        val array = JSONArray(serverResponse)

                        Log.d("loadModels arrlength", array.length().toString())

                        //traversing through all the object
                        for (i in 0 until array.length()) {

                            //getting product object from json array
                            val product = array.getJSONObject(i)

                            models.add(Model(product.getInt("model_id"), product.getString("model_name"), product.getString("file_name"), getFileSize(product.getString("file_name")),
                                    product.getString("description")))

//                        Log.d("category name", product.getInt("name").toString())
                        }

                        modelList.postValue(models)

                    } catch (e: JSONException) {
                        Log.e("loadCategories JSON ", e.message!!)
                    }
                },
                Response.ErrorListener { volleyError ->

                    // Showing error message if something goes wrong.
                    Toast.makeText(context, volleyError.toString(), Toast.LENGTH_LONG).show()
                }) {
            override fun getParams(): Map<String, String> {

                // Creating Map String Params.
                val params: MutableMap<String, String> = HashMap()

                // Adding All values to Params.
                params["category_id"] = categoryId.toString()
                return params
            }
        }

        // Creating RequestQueue.

        // Creating RequestQueue.
        val requestQueue = Volley.newRequestQueue(context)

        // Adding the StringRequest object into requestQueue.

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest)

        return modelList
    }

    private fun getFileSize(fileName: String): Long{
        val fileRef = getFirebaseFileRef(fileName)
        var size: Long = 0

        fileRef.metadata.addOnSuccessListener(OnSuccessListener<StorageMetadata?> {
            // Metadata now contains the metadata for 'images/forest.jpg'
            size = it.sizeBytes
        }).addOnFailureListener(OnFailureListener {
            // Uh-oh, an error occurred!
        })

        return size
    }

}