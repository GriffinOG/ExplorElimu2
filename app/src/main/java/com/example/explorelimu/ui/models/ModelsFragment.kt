package com.example.explorelimu.ui.models

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.explorelimu.R
import com.example.explorelimu.data.model.ModelsAdapter
import com.example.explorelimu.data.model.ModelsRepository

private const val CATEGORY_ID = "categoryId"

class ModelsFragment : Fragment() {

    private var categoryId: Int? = null
    private lateinit var modelsRepository: ModelsRepository
    private lateinit var modelsViewModel: ModelsViewModel
    private lateinit var modelsRecyclerView: RecyclerView
    private lateinit var modelsAdapter: ModelsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryId = it.getInt(CATEGORY_ID)
        }
//        val onBackPressedCallback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                findNavController()
//            }
//        }
//        requireActivity().onBackPressedDispatcher.addCallback(
//            this, onBackPressedCallback
//        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_models, container, false)

        modelsRepository = ModelsRepository(requireContext()).getInstance()!!

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return  ModelsViewModel(modelsRepository,
                    categoryId!!) as T
            }
        }

        modelsViewModel =
                ViewModelProvider(this, factory).get(ModelsViewModel::class.java)

        modelsRecyclerView = root.findViewById(R.id.models_rv)
        modelsAdapter = ModelsAdapter(requireContext())
        modelsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        modelsRecyclerView.adapter = modelsAdapter
        modelsViewModel._models.observe(requireActivity()){ value->
            value.let {
                Log.d(javaClass.name, it.size.toString())
                modelsAdapter.setData(it)
            }
        }
        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(categoryId: Int) =
                ModelsFragment().apply {
                    arguments = Bundle().apply {
                        putInt(CATEGORY_ID, categoryId)
                    }
                }
    }
}