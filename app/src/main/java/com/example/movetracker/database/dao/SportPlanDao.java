package com.example.movetracker.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.movetracker.database.entity.SportPlan;
import java.util.List;

@Dao
public interface SportPlanDao {
    @Insert
    long insertPlan(SportPlan plan);

    @Query("SELECT * FROM sport_plans WHERE isActive = 1 LIMIT 1")
    SportPlan getActivePlan();

    @Query("SELECT * FROM sport_plans")
    List<SportPlan> getAllPlans();

    @Update
    void updatePlan(SportPlan plan);
}