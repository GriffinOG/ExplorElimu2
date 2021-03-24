package com.example.render;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class GetCategoryIndex {
    static HashMap<String, String> codeHash = new HashMap<String, String>();

    public static void init(ArrayList<Integer> indexArray, ArrayList<String> stringArray) {
        for(int i=0; i<indexArray.size(); i++) {
            codeHash.put(stringArray.get(i), indexArray.get(i).toString());
        }
    }

    public static int getId(String param) {
        Log.d("GetCategoryIndex getId", codeHash.get(param));
        return Integer.parseInt(codeHash.get(param));
    }
}
