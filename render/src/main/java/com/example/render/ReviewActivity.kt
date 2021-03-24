package com.example.render

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.*

class ReviewActivity : AppCompatActivity() {
    private var rating: Float? = null
    private lateinit var username: String
    private lateinit var reviewText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        val modelId = intent.getIntExtra("model_id", 0)
        val simpleRatingBar: RatingBar = findViewById(R.id.rating_bar)
        val reviewEditText: EditText = findViewById(R.id.review_et)
        val submitButton: Button = findViewById(R.id.submit_button)
        val cancelButton: Button = findViewById(R.id.cancel_button)
        username = getPreferences(MODE_PRIVATE).getString("xmpp_jid","Anonymous User")!!
        Log.d("$localClassName modelId", modelId.toString())

        submitButton.setOnClickListener {
            rating = simpleRatingBar.rating
            reviewText = reviewEditText.text.toString()

            addReview(username, modelId, rating!!, reviewText)
        }

        cancelButton.setOnClickListener { finish() }
    }

    fun addReview(username: String, modelId: Int, rating: Float, review: String){
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, Global.ADD_REVIEW_URL,
            Response.Listener { serverResponse -> // Hiding the progress dialog after all task complete.

                if (serverResponse.isNullOrEmpty()) {
                    Toast.makeText(this, "Review posted", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    // Showing response message coming from server.
                    Toast.makeText(this, serverResponse, Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { volleyError -> // Hiding the progress dialog after all task complete.

                // Showing error message if something goes wrong.
                Toast.makeText(this, volleyError.toString(), Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): Map<String, String> {

                // Creating Map String Params.
                val params: MutableMap<String, String> = HashMap()

                // Adding All values to Params.
                params["username"] = username
                params["model_id"] = modelId.toString()
                params["rating"] = rating.toString()
                params["review_text"] = reviewText

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