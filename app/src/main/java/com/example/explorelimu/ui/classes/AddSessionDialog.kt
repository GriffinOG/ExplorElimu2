package com.example.explorelimu.ui.classes

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.explorelimu.R
import com.example.explorelimu.data.category.CategoryDropDownAdapter
import com.example.explorelimu.data.model.Model
import com.example.explorelimu.data.model.ModelDropDownAdapter
import com.example.explorelimu.data.model.ModelsRepository
import com.example.explorelimu.ui.models.ModelsViewModel

class AddSessionDialog: DialogFragment() {
    private lateinit var sessionNameEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var modelSpinner: Spinner

    private lateinit var modelsRepository: ModelsRepository
    private lateinit var modelsViewModel: ModelsViewModel

    val SESSION_NAME = "session_name"
    val MODEL = "model"
    val MODEL_ID = "model_id"

    private var model: Model? = null
    private var modelId: Int? = null

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val root = inflater.inflate(R.layout.add_session_dialog, container, false)
//
//
//        return root
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val i = requireActivity().layoutInflater

        val root: View = i.inflate(R.layout.add_session_dialog, null)

        sessionNameEditText = root.findViewById(R.id.session_name_et)
        categorySpinner = root.findViewById(R.id.category_select)
        modelSpinner = root.findViewById(R.id.model_select)

        val categoryAdapter = CategoryDropDownAdapter(requireContext())
        categorySpinner.adapter = categoryAdapter

        val modelAdapter = ModelDropDownAdapter(requireContext())
        modelSpinner.adapter = modelAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                categorySpinner.setSelection(position)
                modelsViewModel.refreshModelsList(categoryAdapter.categoryList[position].categoryId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                model = parent!!.getItemAtPosition(position) as Model
                Log.d(javaClass.name + " model details", model!!.name)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        modelsRepository = ModelsRepository(requireContext()).getInstance()!!
        modelsViewModel =
                ViewModelProvider(this, ModelsViewModel.FACTORY(modelsRepository)).get(ModelsViewModel::class.java)


        modelsViewModel._categories.observe(requireActivity()){ value->
            value.let {
                categoryAdapter.setData(it)
                modelsViewModel.refreshModelsList(it[0].categoryId)
            }
        }

        modelsViewModel._instantModels.observe(requireActivity()){ value->
            value.let {
                modelAdapter.setData(it)
                Log.d(javaClass.name + " modellist", it[0].name)
            }
        }

        return AlertDialog.Builder(activity)
                .setTitle("Add Session")
                .setPositiveButton("Create"
                ) { dialog, _ ->
                    targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK,
                        requireActivity().intent.putExtra(SESSION_NAME, sessionNameEditText.text.toString())
                                .putExtra(MODEL, modelSpinner.selectedItem as Model))
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel"
                ) { dialog, _ ->
                    dialog.dismiss()
                }.setView(root)
                .create()
    }
}