package com.example.movetracker.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sport_plans")
public class SportPlan {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String planName;
    private double targetDistance; // 单位：米
    private boolean isActive;

    public SportPlan(String planName, double targetDistance, boolean isActive) {
        this.planName = planName;
        this.targetDistance = targetDistance;
        this.isActive = isActive;
    }

    // Getter 和 Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public double getTargetDistance() { return targetDistance; }
    public void setTargetDistance(double targetDistance) { this.targetDistance = targetDistance; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}