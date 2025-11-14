package com.example.projeto.storage;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.projeto.models.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    List<Task> getAllTasks();

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getTaskById(long id);

    @Query("SELECT COUNT(*) FROM tasks WHERE concluida = 0")
    int getPendingTasksCount();

    @Query("DELETE FROM tasks WHERE concluida = 1")
    void deleteCompletedTasks();
}