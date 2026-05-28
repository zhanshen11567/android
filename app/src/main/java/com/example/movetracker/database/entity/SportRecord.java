package com.example.movetracker.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sport_records")
public class SportRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private String date; // yyyy-MM-dd
    private double distance; // 米
    private long duration; // 秒
    private double calories;
    private long startTime;
    private long endTime;
    private String trackData; // JSON 轨迹坐标

    public SportRecord(int userId, String date) {
        this.userId = userId;
        this.date = date;
    }

    // Getter 和 Setter（所有属性都需要）
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public String getTrackData() { return trackData; }
    public void setTrackData(String trackData) { this.trackData = trackData; }
}