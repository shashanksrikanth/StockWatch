package com.shashanksrikanth.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StockDownloader implements Runnable{
    private static final String TAG = "StockDownloader";
    private static final String sourceUrlFirstHalf = "https://cloud.iexapis.com/stable/stock/";
    private static final String sourceUrlSecondHalf = "/quote?token=pk_175d61ca0c6c4a4ba4a72bfc0933aef7";
    private MainActivity mainActivity;
    private String query;

    public StockDownloader(MainActivity mainActivity, String query) {
        this.mainActivity = mainActivity;
        this.query = query;
    }

    @Override
    public void run() {
        Uri.Builder builder = Uri.parse(sourceUrlFirstHalf + query + sourceUrlSecondHalf).buildUpon();
        String urlToUse = builder.toString();
        Log.d(TAG, "run: " + urlToUse);
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HttpURLConnection status: "+ conn.getResponseCode());
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
        try {
            JSONObject stock = new JSONObject(string);
            String stockSymbol = stock.getString("symbol");
            String companyName = stock.getString("companyName");
            String price = stock.getString("latestPrice");
            if(price.equals("null")) price = "0.0";
            String priceChange = stock.getString("change");
            if(priceChange.equals("null")) priceChange = "0.0";
            String changePercentage = stock.getString("changePercent");
            if(changePercentage.equals("null")) changePercentage = "0.0";
            final Stock newStock = new Stock(stockSymbol, companyName, Double.parseDouble(price),
                    Double.parseDouble(priceChange), Double.parseDouble(changePercentage));
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.addStock(newStock, query);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
