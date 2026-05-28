package com.example.movetracker.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.movetracker.database.entity.SportRecord;

@Dao
public interface SportRecordDao {
    @Insert
    long insertRecord(SportRecord record);

    @Query("SELECT * FROM sport_records WHERE userId = :userId AND date = :date ORDER BY startTime DESC")
    java.util.List<SportRecord> getTodayRecords(int userId, String date);

    @Query("SELECT COALESCE(SUM(distance), 0) FROM sport_records WHERE userId = :userId AND date = :date")
    double getTodayTotalDistance(int userId, String date);

    @Query("SELECT COALESCE(SUM(duration), 0) FROM sport_records WHERE userId = :userId AND date = :date")
    long getTodayTotalDuration(int userId, String date);

    @Query("SELECT COALESCE(SUM(calories), 0) FROM sport_records WHERE userId = :userId AND date = :date")
    double getTodayTotalCalories(int userId, String date);

    @Query("SELECT * FROM sport_records WHERE userId = :userId AND date = :date ORDER BY startTime DESC LIMIT 1")
    SportRecord getTodayRecord(int userId, String date);

    @Query("UPDATE sport_records SET distance = :distance, duration = :duration, endTime = :endTime, trackData = :trackData WHERE userId = :userId AND date = :date")
    void updateRecord(int userId, String date, double distance, long duration, long endTime, String trackData);
}