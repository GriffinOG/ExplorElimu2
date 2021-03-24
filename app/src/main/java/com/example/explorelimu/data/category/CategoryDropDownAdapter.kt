package com.example.explorelimu.data.category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.explorelimu.R

class CategoryDropDownAdapter(val context: Context) : BaseAdapter() {

    var categoryList = emptyList<Category>()
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getCount(): Int {
        return categoryList.size
    }

    override fun getItem(position: Int): Any {
        return categoryList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: CategoryViewHolder

        if (convertView == null){
            view = inflater.inflate(R.layout.spinner_dropdown_item, parent, false)
            viewHolder = CategoryViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as CategoryViewHolder
        }

        viewHolder.bindCategory(categoryList[position])

        return view
    }

    fun setData(categoryList: List<Category>){
        this.categoryList = categoryList
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View){
        val categoryNameTextView: TextView = itemView.findViewById(R.id.category_name_tv)

        fun bindCategory(category: Category){
            categoryNameTextView.text = category.name
        }
    }
}