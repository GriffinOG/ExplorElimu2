package com.example.explorelimu.ui.models

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.explorelimu.R
import com.example.explorelimu.data.category.CategoryAdapter
import com.example.explorelimu.data.model.ModelsRepository

class CategoriesFragment : Fragment() {

    private lateinit var modelsRepository: ModelsRepository
    private lateinit var modelsViewModel: ModelsViewModel
    private lateinit var modelsRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var myContainer: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_models, container, false)
        modelsRepository = ModelsRepository(requireContext()).getInstance()!!
        modelsViewModel =
                ViewModelProvider(this, ModelsViewModel.FACTORY(modelsRepository)).get(ModelsViewModel::class.java)

        myContainer = root.findViewById(R.id.models_parent_cl)
        modelsRecyclerView = root.findViewById(R.id.models_rv)
        categoryAdapter = CategoryAdapter(requireContext(), this)
        modelsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        modelsRecyclerView.adapter = categoryAdapter
        modelsViewModel._categories.observe(requireActivity()){ value->
            value.let {
                Log.d(javaClass.name, it.size.toString())
                categoryAdapter.setData(it)
            }
        }
        return root
    }
}