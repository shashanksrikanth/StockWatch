package com.shashanksrikanth.stockwatch;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {
    TextView rowStockSymbol;
    TextView rowCompanyName;
    TextView rowPrice;
    TextView rowPriceChange;
    TextView rowChangePercentage;
    ImageView rowChangeSymbol;

    public StockViewHolder(View view) {
        super(view);
        rowStockSymbol = view.findViewById(R.id.rowStockSymbol);
        rowCompanyName = view.findViewById(R.id.rowCompanyName);
        rowPrice = view.findViewById(R.id.rowPrice);
        rowPriceChange = view.findViewById(R.id.rowPriceChange);
        rowChangePercentage = view.findViewById(R.id.rowChangePercentage);
        rowChangeSymbol = view.findViewById(R.id.rowChangeSymbol);
    }
}
