package com.example.moneytrans;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "conversion_history")
public class ConversionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String fromCurrency;
    public String toCurrency;
    public double rateUsed;
    public double inputAmount;
    public double resultAmount;
    public long timestamp;

    public ConversionEntity(String fromCurrency, String toCurrency, double rateUsed, double inputAmount, double resultAmount, long timestamp) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rateUsed = rateUsed;
        this.inputAmount = inputAmount;
        this.resultAmount = resultAmount;
        this.timestamp = timestamp;
    }
}
