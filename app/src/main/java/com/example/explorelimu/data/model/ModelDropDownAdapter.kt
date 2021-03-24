package com.example.explorelimu.data.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.explorelimu.R

class ModelDropDownAdapter(val context: Context) : BaseAdapter() {

    private var modelList = emptyList<Model>()
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return modelList.size
    }

    override fun getItem(position: Int): Any {
        return modelList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ModelViewHolder

        if (convertView == null){
            view = inflater.inflate(R.layout.spinner_dropdown_item, parent, false)
            viewHolder = ModelViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ModelViewHolder
        }

        viewHolder.bindModel(modelList[position])

        return view
    }

    fun setData(modelList: List<Model>){
        this.modelList = modelList
        notifyDataSetChanged()
    }

    inner class ModelViewHolder(itemView: View){
        val modelNameTextView: TextView = itemView.findViewById(R.id.category_name_tv)

        fun bindModel(model: Model){
            modelNameTextView.text = model.name
        }
    }
}