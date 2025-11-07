package com.example.aplicacion_organizadora.ui.notes;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String body;

    public long fechaCreacion;

    public Note(String title, String body, long fechaCreacion) {
        this.title = title;
        this.body = body;
        this.fechaCreacion = fechaCreacion;
    }
}

