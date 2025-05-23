package com.example.medcare.map;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    public static List<Facility> loadFacilities(Context context, String filename) {
        List<Facility> list = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonStr = new String(buffer, "UTF-8");

            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                Facility f = new Facility();
                f.name = o.getString("name");
                f.type = o.getString("type");
                f.latitude = o.getDouble("latitude");
                f.longitude = o.getDouble("longitude");
                list.add(f);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
