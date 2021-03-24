package com.example.render;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddModelDialog extends AppCompatDialogFragment {
    private EditText nameEditText;
    private Spinner categoriesSpinner;
    private int categoryId;
    private EditText descriptionEditText;
    private AddCategoriesDialogListener listener;
    private final ArrayList<Integer> categoryIds = new ArrayList<>();
    private final ArrayList<String> categoryStrings = new ArrayList<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_upload_dialog, null);
        builder.setView(view)
                .setTitle("Login")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = nameEditText.getText().toString();
                        int categoryId = Integer.parseInt(categoriesSpinner.getSelectedItem().toString());
                        String description = descriptionEditText.getText().toString();
                        listener.uploadModel(name, categoryId, description);
                    }
                });
        nameEditText = view.findViewById(R.id.edit_name);
        categoriesSpinner = view.findViewById(R.id.category_select);
        descriptionEditText = view.findViewById(R.id.edit_description);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_spinner_item, categoryStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriesSpinner.setAdapter(adapter);
        categoriesSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                categoryId = GetCategoryIndex.getId(parent.getItemAtPosition(position).toString());
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddCategoriesDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ExampleDialogListener");
        }
    }

    public interface AddCategoriesDialogListener {
        void uploadModel(String name, int categoryId, String description);
    }

    private void loadProducts() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Global.GET_CATEGORIES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject product = array.getJSONObject(i);

                                //adding the product to product list
                                categoryIds.add(
                                        product.getInt("category_id"));
                                categoryStrings.add(
                                        product.getString("name"));
                            }

                            GetCategoryIndex.init(categoryIds, categoryStrings);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> Log.e("loadCategories volley ", error.getMessage()));

        //adding our stringrequest to queue
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

}
