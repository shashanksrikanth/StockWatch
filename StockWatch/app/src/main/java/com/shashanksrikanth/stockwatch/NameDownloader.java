package com.shashanksrikanth.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class NameDownloader implements Runnable {
    private static final String TAG = "NameDownloader";
    private static final String sourceUrl = "https://api.iextrading.com/1.0/ref-data/symbols";
    public static HashMap<String, String> symbolCompanyName = new HashMap<>();

    @Override
    public void run() {
        Uri uri = Uri.parse(sourceUrl);
        String urlToUse = uri.toString();
        Log.d(TAG, "run: " + urlToUse);
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HttpResponseCode is not ok");
                return;
            }
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append('\n');
            Log.d(TAG, "run: " + sb.toString());
        }
        catch (Exception e) {
            return;
        }
        processData(sb.toString());
    }

    public void processData(String string) {
        // Populate hashmap with stock symbols and corresponding company names
        try {
            JSONArray jsonArray = new JSONArray(string);
            for(int i = 0; i<jsonArray.length(); i++) {
                JSONObject stock = (JSONObject) jsonArray.get(i);
                String symbol = stock.getString("symbol");
                String name = stock.getString("name");
                symbolCompanyName.put(symbol, name);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> stockMatches(String string) {
        // Return list that matches user search
        String query = string.toLowerCase().trim();
        HashSet<String> matches = new HashSet<>();
        for(String key : symbolCompanyName.keySet()) {
            if(key.toLowerCase().trim().contains(query)) matches.add(key + "-" + symbolCompanyName.get(key));
            String name = symbolCompanyName.get(key);
            if(name!=null && name.toLowerCase().trim().contains(query)) matches.add(key + "-" + name);
        }
        ArrayList<String> stockMatchResults = new ArrayList<>(matches);
        Collections.sort(stockMatchResults);
        return stockMatchResults;
    }

}
