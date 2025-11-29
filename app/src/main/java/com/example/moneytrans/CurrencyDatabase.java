package com.example.moneytrans;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ConversionEntity.class}, version = 1, exportSchema = false)
public abstract class CurrencyDatabase extends RoomDatabase {

    public abstract ConversionDao conversionDao();

    private static volatile CurrencyDatabase INSTANCE;

    public static CurrencyDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CurrencyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            CurrencyDatabase.class,
                            "conversion_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
