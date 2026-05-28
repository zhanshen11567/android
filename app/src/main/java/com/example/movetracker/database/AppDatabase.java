package com.example.movetracker.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.movetracker.database.dao.UserDao;
import com.example.movetracker.database.dao.SportPlanDao;
import com.example.movetracker.database.dao.SportRecordDao;
import com.example.movetracker.database.entity.User;
import com.example.movetracker.database.entity.SportPlan;
import com.example.movetracker.database.entity.SportRecord;

@Database(entities = {User.class, SportPlan.class, SportRecord.class}, version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract SportPlanDao sportPlanDao();
    public abstract SportRecordDao sportRecordDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "sport_tracker.db").build();
                }
            }
        }
        return INSTANCE;
    }
}