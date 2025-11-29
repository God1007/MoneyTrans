package com.example.moneytrans;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversionAdapter extends RecyclerView.Adapter<ConversionAdapter.ViewHolder> {

    private final List<ConversionEntity> data = new ArrayList<>();
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConversionEntity entity = data.get(position);
        String rate = decimalFormat.format(entity.rateUsed);
        String input = decimalFormat.format(entity.inputAmount);
        String result = decimalFormat.format(entity.resultAmount);
        String timestamp = dateFormat.format(new Date(entity.timestamp));
        String formatted = holder.itemView.getContext().getString(
                R.string.history_item_format,
                entity.fromCurrency,
                entity.toCurrency,
                rate + " (" + timestamp + ")",
                input,
                result
        );
        holder.historyText.setText(formatted);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void submitList(List<ConversionEntity> newData) {
        data.clear();
        if (newData != null) {
            data.addAll(newData);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView historyText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            historyText = itemView.findViewById(R.id.historyText);
        }
    }
}
