package com.example.render

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class AddModelActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var categoriesSpinner: Spinner
    private var categoryId = 0
    private var vertexCount: Int? = null
    private var normalsCount: Int? = null
    private lateinit var descriptionEditText: EditText
    private val categoryIds = ArrayList<Int>()
    private val categoryStrings = ArrayList<String>()
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var fileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_model)

        loadCategories()

        fileName = intent.getStringExtra("fileName")!!
        vertexCount = intent.getIntExtra("vertexCount", 0)
        normalsCount = intent.getIntExtra("normalsCount", 0)
        nameEditText = findViewById(R.id.edit_name)
        categoriesSpinner = findViewById(R.id.category_select)
        descriptionEditText = findViewById(R.id.edit_description)
        cancelButton = findViewById(R.id.cancel_button)
        saveButton = findViewById(R.id.save_button)

        cancelButton.setOnClickListener { finish() }
        saveButton.setOnClickListener { secondSend() }
    }

    private fun loadCategories() {
        Log.d("loadCategories", "function called")
        val stringRequest = StringRequest(
            Request.Method.GET, Global.GET_CATEGORIES_URL,
            { response ->
                try {
                    //converting the string to json array object
                    val array = JSONArray(response)

                    Log.d("loadCats arrlength", array.length().toString())

                    //traversing through all the object
                    for (i in 0 until array.length()) {

                        //getting product object from json array
                        val product = array.getJSONObject(i)

                        //adding the product to product list
                        categoryIds.add(
                            product.getInt("category_id")
                        )
                        categoryStrings.add(
                            product.getString("name")
                        )
//                        Log.d("category name", product.getInt("name").toString())
                    }
                    GetCategoryIndex.init(categoryIds, categoryStrings)

                    val adapter = ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item, categoryStrings
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categoriesSpinner.adapter = adapter
//        adapter.notifyDataSetChanged()
                    categoriesSpinner.setSelection(1)
                    categoriesSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                categoriesSpinner.setSelection(position)
                                categoryId = GetCategoryIndex.getId(
                                    parent!!.getItemAtPosition(position).toString()
                                )
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                TODO("Not yet implemented")
                            }

                        }
                } catch (e: JSONException) {
                    Log.e("loadCategories JSON ", e.message!!)
                }
            }
        ) { error: VolleyError ->
            Log.e(
                "loadCategories volley ",
                error.message!!
            )
        }

        //adding our stringrequest to queue
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun sendToDb() {
        val dialog: ProgressDialog = ProgressDialog(this)
        dialog.setMessage("Posting to database. Please wait.")
        dialog.show()

        val requestQueue = Volley.newRequestQueue(this)

        val postData = JSONObject()
        try {
            postData.put("name", nameEditText.text.toString().trim())
            Log.d("sendToDb name", nameEditText.text.toString())
            postData.put("filename", fileName.trim())
            Log.d("sendToDb filename", fileName)
            postData.put("category_id", categoryId.toString().trim())
            Log.d("sendToDb category_id", categoryId.toString())
            postData.put("description", descriptionEditText.text.toString().trim())
            Log.d("sendToDb description", descriptionEditText.text.toString())
        } catch (e: JSONException) {
            Log.e("sendToDb", e.message!!)
            e.printStackTrace()
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, Global.ADD_MODEL_URL, postData,
            { response ->
                Log.e("sendToDb", response.toString())
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                finish()
                Toast.makeText(
                    this,
                    "Model data posted to database",
                    Toast.LENGTH_LONG
                ).show()
            },
            { error ->
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                Log.e("sendToDb", error.message!!)
                Toast.makeText(
                    this,
                    "Error posting to database. Try again later",
                    Toast.LENGTH_LONG
                ).show()
            })
        Log.d("sendToDb", jsonObjectRequest.url)

        requestQueue.add(jsonObjectRequest)
    }

    private fun secondSend(){
        val progressDialog: ProgressDialog = ProgressDialog(this)

        // Showing progress dialog at user registration time.
        progressDialog.setMessage("Please Wait, We are Inserting Your Data on Server");
        progressDialog.show();

        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, Global.ADD_MODEL_URL,
            Response.Listener { serverResponse -> // Hiding the progress dialog after all task complete.
                progressDialog.dismiss()

                if (serverResponse.isNullOrEmpty()){
                    finish()
                } else{
                    // Showing response message coming from server.
                    Toast.makeText(this, serverResponse, Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { volleyError -> // Hiding the progress dialog after all task complete.
                progressDialog.dismiss()

                // Showing error message if something goes wrong.
                Toast.makeText(this, volleyError.toString(), Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): Map<String, String> {

                // Creating Map String Params.
                val params: MutableMap<String, String> = HashMap()

                // Adding All values to Params.
                params["name"] = nameEditText.text.toString().trim()
                params["filename"] = fileName
                params["category_id"] = categoryId.toString()
                params["normals"] = normalsCount.toString()
                params["vertices"] = vertexCount.toString()
                params["description"] = descriptionEditText.text.toString().trim()
                return params
            }
        }

        // Creating RequestQueue.

        // Creating RequestQueue.
        val requestQueue = Volley.newRequestQueue(this)

        // Adding the StringRequest object into requestQueue.

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest)
    }
}