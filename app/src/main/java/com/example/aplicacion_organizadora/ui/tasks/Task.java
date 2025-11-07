package com.example.aplicacion_organizadora.ui.tasks;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "priority")
    public String priority;

    @ColumnInfo(name = "is_done")
    public boolean isDone;

    @ColumnInfo(name = "fechaCreacion")
    public long fechaCreacion;
    public Task(String title, String description, String priority, long fechaCreacion) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.isDone = false;
        this.fechaCreacion = fechaCreacion;
    }
}
