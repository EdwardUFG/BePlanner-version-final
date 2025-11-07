package com.example.aplicacion_organizadora.ui.tasks;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks " +
            "ORDER BY is_done ASC, " +
            "CASE WHEN priority = 'Alta' THEN 3 " +
            "     WHEN priority = 'Media' THEN 2 " +
            "     WHEN priority = 'Baja' THEN 1 " +
            "     ELSE 0 END DESC")
    LiveData<List<Task>> getTasksOrdenadasPorEstadoYPrioridad();

    @Query("SELECT * FROM tasks ORDER BY id DESC LIMIT 1")
    LiveData<Task> getLatestTask();

    @Query("SELECT * FROM tasks " +
            "ORDER BY CASE priority " +
            "WHEN 'Alta' THEN 1 " +
            "WHEN 'Media' THEN 2 " +
            "ELSE 3 END, " +
            "fechaCreacion DESC " +
            "LIMIT 1")
    LiveData<Task> getTopByPriorityAndDate();
}