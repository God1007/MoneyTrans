package com.example.moneytrans;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ConversionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertConversion(ConversionEntity entity);

    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC LIMIT 20")
    List<ConversionEntity> recentConversions();
}
