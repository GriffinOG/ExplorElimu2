package com.example.explorelimu.data.category

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.explorelimu.R

class CategoryAdapter(private val context: Context,
                      private val fragment: Fragment): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var categoryList = emptyList<Category>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CategoryViewHolder(layoutInflater.inflate(R.layout.category_item, parent, false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bindCategory(categoryList[position])
        val bundle = bundleOf("categoryId" to categoryList[position].categoryId)
        holder.categoryLayout.setOnClickListener { fragment.findNavController().navigate(R.id.actionCategoryModels, bundle) }
    }

    override fun getItemCount(): Int {
        Log.d(javaClass.name, categoryList.size.toString())
        return categoryList.size
    }

    fun setData(categoryList: List<Category>){
        this.categoryList = categoryList
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val categoryLayout: ConstraintLayout
        private val categoryNameTextView: TextView
        private val modelCountTextView: TextView

        init {
            categoryLayout = itemView.findViewById(R.id.parent_cl)
            categoryNameTextView =itemView.findViewById(R.id.category_name_tv)
            modelCountTextView = itemView.findViewById(R.id.model_count_tv)
        }

        fun bindCategory(category: Category) {
            categoryNameTextView.text = category.name
            modelCountTextView.text = if (category.modelCount == 1){
                context.getString(R.string.single_model)
            } else {
                context.getString(R.string.model_count, category.modelCount)
            }
        }
    }
}