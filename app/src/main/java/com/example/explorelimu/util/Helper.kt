package com.example.explorelimu.util

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.explorelimu.MainActivity
import com.example.explorelimu.render.ModelActivity
import com.example.render.Global
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.net.URI
import java.net.URISyntaxException
import java.util.HashMap

val USER_TYPE = "user_type"
val STUDENT = "student"
val TEACHER = "educator"

fun launchModelRendererActivity(context: Context, uri: Uri, modelId: Int) {
    Log.i("Menu", "Launching renderer for '$uri'")
    val intent = Intent(context, ModelActivity::class.java)
    try {
        URI.create(uri.toString())
        intent.putExtra("uri", uri.toString())
    } catch (e: Exception) {
        // info: filesystem url may contain spaces, therefore we re-encode URI
        try {
            intent.putExtra("uri", URI(uri.scheme, uri.authority, uri.path, uri.query, uri.fragment).toString())
        } catch (ex: URISyntaxException) {
            Toast.makeText(context, "Error: $uri", Toast.LENGTH_LONG).show()
            return
        }
    }
    intent.putExtra("model_id", modelId)
    intent.putExtra("immersiveMode", "false")
    context.startActivity(intent)
}

fun getFirebaseFileRef(fileName: String): StorageReference{
    val storage = FirebaseStorage.getInstance()

    // Create a storage reference from our app
    val storageRef = storage.reference

    return storageRef.child("models/$fileName")
}

fun downloadModel(context: Context, fileName: String, modelId: Int){
    val storage = FirebaseStorage.getInstance()

    // Create a storage reference from our app
    val storageRef = storage.reference

    val fileRef = storageRef.child("models/$fileName")

    val localFile: File = File.createTempFile(fileName.substringBefore('.'), "." + fileName.substringAfter('.'), (context as MainActivity).outputDirectory)

    val dialog: ProgressDialog = ProgressDialog(context)
    dialog.setMessage("Fetching model. Please wait.")
    dialog.show()
    val addOnFailureListener = fileRef.getFile(localFile).addOnSuccessListener(object :
        OnSuccessListener<FileDownloadTask.TaskSnapshot?> {

        override fun onSuccess(p0: FileDownloadTask.TaskSnapshot?) {
            Log.d(javaClass.name + " uri", localFile.absolutePath)
//                ContentUtils.setCurrentDir(localFile.parentFile)
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            launchModelRendererActivity(context, Uri.parse("file://" + localFile.absolutePath), modelId)
            incrementViews(context, fileName)
        }
    }).addOnFailureListener(OnFailureListener {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
        Toast.makeText(context, "Something went wrong. Please try again later.", Toast.LENGTH_LONG).show()
        // Handle any errors
    })

//        launchModelRendererActivity(context, Uri.parse("file:///storage/emulated/0/Documents/3D/piper_pa18/piper_pa18.dae"))
}

fun incrementViews(context: Context, fileName: String) {
    val stringRequest: StringRequest = object : StringRequest(
        Method.POST, Global.ADD_VIEW_URL,
        Response.Listener { serverResponse -> // Hiding the progress dialog after all task complete.

            if (serverResponse.isNullOrEmpty()){
//                        finish()
            } else{
                // Showing response message coming from server.
                Toast.makeText(context, serverResponse, Toast.LENGTH_LONG).show()
            }
        },
        Response.ErrorListener { volleyError -> // Hiding the progress dialog after all task complete.
//                    progressDialog.dismiss()

            // Showing error message if something goes wrong.
            Toast.makeText(context, volleyError.toString(), Toast.LENGTH_LONG).show()
        }) {
        override fun getParams(): Map<String, String> {

            // Creating Map String Params.
            val params: MutableMap<String, String> = HashMap()

            // Adding All values to Params.
            params["file_name"] = fileName

            return params
        }
    }

    // Creating RequestQueue.

    // Creating RequestQueue.
    val requestQueue = Volley.newRequestQueue(context)

    // Adding the StringRequest object into requestQueue.

    // Adding the StringRequest object into requestQueue.
    requestQueue.add(stringRequest)
}