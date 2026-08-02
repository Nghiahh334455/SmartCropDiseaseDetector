package com.example.smartcrop.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insert(HistoryEntity history);

    @Query("SELECT * FROM diagnosis_history ORDER BY timestamp DESC")
    List<HistoryEntity> getAllHistory();
}
