package com.example.movetracker.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.movetracker.database.entity.User;

@Dao
public interface UserDao {
    @Insert
    long insertUser(User user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(int id);
}