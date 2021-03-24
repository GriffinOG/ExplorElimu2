package com.example.explorelimu.data.model

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.explorelimu.MainActivity
import com.example.explorelimu.R
import com.example.explorelimu.util.downloadModel
import com.example.explorelimu.util.launchModelRendererActivity
import com.example.render.Global
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.HashMap


class ModelsAdapter(private val context: Context): RecyclerView.Adapter<ModelsAdapter.ModelViewHolder>() {

    private var modelList = emptyList<Model>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ModelViewHolder(layoutInflater.inflate(R.layout.model_item, parent, false))
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bindModel(modelList[position])
        holder.modelParent.setOnClickListener {
            downloadModel(context, modelList[position].fileName, modelList[position].id)
        }
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    fun setData(modelList: List<Model>){
        this.modelList = modelList
        notifyDataSetChanged()
    }

    inner class ModelViewHolder(modelView: View): RecyclerView.ViewHolder(modelView) {
        val modelParent: ConstraintLayout
        private val modelNameTextView: TextView
        private val modelFileNameTextView: TextView
        private val modelDescTextView: TextView

        init {
            modelParent = modelView.findViewById(R.id.model_parent_cl)
            modelNameTextView = modelView.findViewById(R.id.model_name_tv)
            modelFileNameTextView = modelView.findViewById(R.id.file_name_tv)
            modelDescTextView = modelView.findViewById(R.id.model_description_tv)
        }

        fun bindModel(model: Model) {
            modelNameTextView.text = model.name
            modelFileNameTextView.text = model.fileName
            modelDescTextView.text = model.description
        }
    }
}