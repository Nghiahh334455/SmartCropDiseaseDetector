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

    @Query("SELECT * FROM diagnosis_history WHERE uid = :uid ORDER BY timestamp DESC")
    List<HistoryEntity> getHistoryByUid(String uid);

    @Query("SELECT * FROM diagnosis_history WHERE uid = :uid ORDER BY timestamp DESC LIMIT :limit")
    List<HistoryEntity> getRecentByUid(String uid, int limit);
}
