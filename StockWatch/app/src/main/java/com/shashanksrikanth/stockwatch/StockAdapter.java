package com.shashanksrikanth.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private ArrayList<Stock> stocks;
    private MainActivity mainActivity;

    public StockAdapter(ArrayList<Stock> stocks, MainActivity mainActivity) {
        this.stocks = stocks;
        this.mainActivity = mainActivity;
    }

    @Override
    public StockViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View stockView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list_row, parent, false);
        stockView.setOnClickListener(mainActivity);
        stockView.setOnLongClickListener(mainActivity);
        StockViewHolder holder = new StockViewHolder(stockView);
        return holder;
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        Stock stock = stocks.get(position);
        String stockSymbol = stock.getStockSymbol();
        String companyName = stock.getCompanyName();
        String price = String.valueOf(stock.getPrice());
        String priceChange = String.valueOf(stock.getPriceChange());
        if(priceChange.length()>=4) priceChange = priceChange.substring(0,4);
        String changePercentage = String.valueOf(stock.getChangePercentage());
        if(changePercentage.length()>=4) changePercentage = changePercentage.substring(0,4);
        String formatChangePercentage = "(" + changePercentage + "%)";
        holder.rowStockSymbol.setText(stockSymbol);
        holder.rowCompanyName.setText(companyName);
        holder.rowPrice.setText(price);
        holder.rowPriceChange.setText(priceChange);
        holder.rowChangePercentage.setText(formatChangePercentage);
        if(stock.getPriceChange()>=0) {
            holder.rowStockSymbol.setTextColor(Color.parseColor("#b2ff59"));
            holder.rowCompanyName.setTextColor(Color.parseColor("#b2ff59"));
            holder.rowPrice.setTextColor(Color.parseColor("#b2ff59"));
            holder.rowPriceChange.setTextColor(Color.parseColor("#b2ff59"));
            holder.rowChangePercentage.setTextColor(Color.parseColor("#b2ff59"));
            holder.rowChangeSymbol.setImageResource(R.drawable.greenarrow);
        }
        else {
            holder.rowStockSymbol.setTextColor(Color.parseColor("#ff3d00"));
            holder.rowCompanyName.setTextColor(Color.parseColor("#ff3d00"));
            holder.rowPrice.setTextColor(Color.parseColor("#ff3d00"));
            holder.rowPriceChange.setTextColor(Color.parseColor("#ff3d00"));
            holder.rowChangePercentage.setTextColor(Color.parseColor("#ff3d00"));
            holder.rowChangeSymbol.setImageResource(R.drawable.redarrow);
        }
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }
}
