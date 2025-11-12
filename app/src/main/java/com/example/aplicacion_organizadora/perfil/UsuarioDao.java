package com.example.aplicacion_organizadora.perfil;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertarUsuario(Usuario usuario);

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE LOWER(correo) = LOWER(:correo))")
    boolean existeCorreo(String correo);

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE carnet = :carnet)")
    boolean existeCarnet(String carnet);

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE telefono = :telefono)")
    boolean existeTelefono(String telefono);

    @Query("SELECT * FROM usuarios WHERE carnet = :carnet LIMIT 1")
    Usuario obtenerPorCarnet(String carnet);

    @Query("SELECT * FROM usuarios WHERE LOWER(correo)=LOWER(:correo) LIMIT 1")
    Usuario obtenerPorCorreoSimple(String correo);

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE LOWER(correo)=LOWER(:correo) AND password=:password)")
    boolean existePorCorreoYPassword(String correo, String password);

    @Query("SELECT * FROM usuarios WHERE LOWER(correo)=LOWER(:correo) AND password=:password LIMIT 1")
    Usuario obtenerPorCorreoYPassword(String correo, String password);

    @Query("UPDATE usuarios SET fotoUri = :fotoUri WHERE LOWER(correo) = LOWER(:correo)")
    void actualizarFotoPorCorreo(String correo, String fotoUri);

    @Query("UPDATE usuarios SET nombre=:nombre, carrera=:carrera, telefono=:telefono, password=:password " +
            "WHERE LOWER(correo)=LOWER(:correo)")
    int actualizarPerfilPorCorreo(String correo, String nombre, String carrera, String telefono, String password);
}
