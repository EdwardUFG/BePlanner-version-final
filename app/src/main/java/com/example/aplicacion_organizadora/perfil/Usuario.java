package com.example.aplicacion_organizadora.perfil;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios")
public class Usuario {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nombre;
    public String correo;
    public String carnet;
    public String carrera;
    public String telefono;
    public String password;
    public String fotoUri;
}

