package com.shashanksrikanth.stockwatch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";
    private final ArrayList<Stock> stocks = new ArrayList<>();
    private final ArrayList<String> tempStocks = new ArrayList<>();
    private RecyclerView recyclerView;
    private StockAdapter adapter;
    private SwipeRefreshLayout swiper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new StockAdapter(stocks, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(checkNetworkConnection()) {
            NameDownloader downloader = new NameDownloader();
            new Thread(downloader).start();
            readJSON(true);
            for(String stockSymbol: tempStocks) {
                StockDownloader stockDownloader = new StockDownloader(this, stockSymbol);
                new Thread(stockDownloader).start();
            }
            tempStocks.clear();
            Log.d(TAG, "onCreate: new stock data uploaded");
            Collections.sort(stocks);
            adapter.notifyDataSetChanged();
        }
        else {
            readJSON(false);
            showNoNetworkConnection("The device is not connected to the network");
            Log.d(TAG, "onCreate: old stock data uploaded");
        }

        // Set up SwipeRefresh listener
        swiper = findViewById(R.id.refreshLayout);
        swiper.setOnRefreshListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Logic that dictates what to do when the add button is pressed
        if(item.getItemId() == R.id.addStock) {
            chooseStock();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkNetworkConnection() {
        // Helper function that checks if device is connected to the network
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void showNoNetworkConnection(String message) {
        // Helper function that pops up a dialog if the device is not connected to the network
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle("No Network Connection");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showStockNotAdded(String query) {
        // Helper function that shows a stock cannot be found
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Data for stock symbol");
        builder.setTitle("Symbol not found: " + query);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        // Logic that dictates what happens when a stock is clicked
        final int index = recyclerView.getChildLayoutPosition(v);
        String marketWatchUrl = "http://www.marketwatch.com/investing/stock/" + stocks.get(index).getStockSymbol();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(marketWatchUrl));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        // Logic that dictates what happens when a stock is long-clicked
        final int index = recyclerView.getChildLayoutPosition(v);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_delete_black_36);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stocks.remove(index);
                writeJSON();
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        builder.setMessage("Delete " + stocks.get(index).getStockSymbol() + "?");
        builder.setTitle("Delete Stock");
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    private void chooseStock() {
        // Helper function that props up dialog for user to choose a stock
        if(!checkNetworkConnection()) {
            showNoNetworkConnection("Stocks cannot be added without a network connection");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setGravity(Gravity.CENTER_HORIZONTAL);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        builder.setView(editText);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String query = editText.getText().toString().trim();
                final ArrayList<String> stockMatches = NameDownloader.stockMatches(query);
                if(stockMatches.size()==0) {
                    showStockNotAdded(query);
                }
                else if (stockMatches.size()==1) {
                    getStockInformation(stockMatches.get(0));
                }
                else {
                    String[] array = stockMatches.toArray(new String[0]);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Make a selection");
                    builder.setItems(array, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getStockInformation(stockMatches.get(which));
                        }
                    });
                    builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    });
                    AlertDialog selectionDialog = builder.create();
                    selectionDialog.show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        builder.setMessage("Please enter a Stock Symbol: ");
        builder.setTitle("Stock Selection");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void addStock(Stock newStock, String query) {
        // Logic that adds a stock to the list
        if(newStock == null) {
            showStockNotAdded(query);
            return;
        }
        if(stocks.contains(newStock)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Stock symbol " + query + " is already displayed");
            builder.setTitle("Duplicate Stock");
            builder.setIcon(R.drawable.baseline_warning_black_36);
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        stocks.add(newStock);
        Collections.sort(stocks);
        writeJSON();
        adapter.notifyDataSetChanged();
    }

    private void getStockInformation(String string) {
        // Helper function that gets stock information
        String[] parts = string.split("-");
        StockDownloader downloader = new StockDownloader(this, parts[0].trim());
        new Thread(downloader).start();
    }

    public void onRefresh() {
        if(!checkNetworkConnection()) {
            showNoNetworkConnection("Stocks cannot be updated without a network connection");
            swiper.setRefreshing(false);
            return;
        }
        stocks.clear();
        readJSON(true);
        for(String stockSymbol: tempStocks) {
            StockDownloader stockDownloader = new StockDownloader(this, stockSymbol);
            new Thread(stockDownloader).start();
        }
        tempStocks.clear();
        Collections.sort(stocks);
        adapter.notifyDataSetChanged();
        swiper.setRefreshing(false);
    }

    private void readJSON(boolean isConnected) {
        // If there is network connectivity, we get only the stock symbol and give updated info
        // If there is no connectivity, we give already stored info
        if(isConnected) {
            try {
                FileInputStream fis = getApplicationContext().openFileInput("StockData.json");
                byte[] data = new byte[fis.available()];
                int loaded = fis.read(data);
                Log.d(TAG, "readJSONData: Loaded " + loaded + " bytes");
                fis.close();
                String json = new String(data);
                JSONArray stockArray = new JSONArray(json);
                for (int i = 0; i < stockArray.length(); i++) {
                    JSONObject stockObject = stockArray.getJSONObject(i);
                    String stockSymbol = stockObject.getString("stockSymbol");
                    tempStocks.add(stockSymbol);
                }
                Log.d(TAG, "readJSON: Adding to tempStocks");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                FileInputStream fis = getApplicationContext().openFileInput("StockData.json");
                byte[] data = new byte[fis.available()];
                int loaded = fis.read(data);
                Log.d(TAG, "readJSONData: Loaded " + loaded + " bytes");
                fis.close();
                String json = new String(data);
                JSONArray stockArray = new JSONArray(json);
                for (int i = 0; i < stockArray.length(); i++) {
                    JSONObject stockObject = stockArray.getJSONObject(i);
                    String stockSymbol = stockObject.getString("stockSymbol");
                    String companyName = stockObject.getString("companyName");
                    String price = stockObject.getString("price");
                    String priceChange = stockObject.getString("priceChange");
                    String changePercentage = stockObject.getString("changePercentage");
                    Stock stock = new Stock(stockSymbol, companyName, Double.parseDouble(price),
                            Double.parseDouble(priceChange), Double.parseDouble(changePercentage));
                    stocks.add(stock);
                    Log.d(TAG, "readJSON: Adding to stocks");
                }
                Collections.sort(stocks);
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void writeJSON() {
        // Writing data to JSON file
        try {
            FileOutputStream fos = getApplicationContext().openFileOutput("StockData.json", Context.MODE_PRIVATE);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            writer.setIndent("  ");
            writer.beginArray();
            for(Stock stock : stocks) {
                writer.beginObject();
                writer.name("stockSymbol").value(stock.getStockSymbol());
                writer.name("companyName").value(stock.getCompanyName());
                writer.name("price").value(stock.getPrice());
                writer.name("priceChange").value(stock.getPriceChange());
                writer.name("changePercentage").value(stock.getChangePercentage());
                writer.endObject();
            }
            writer.endArray();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}