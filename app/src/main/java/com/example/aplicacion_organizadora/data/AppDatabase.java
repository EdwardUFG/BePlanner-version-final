package com.example.aplicacion_organizadora.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.aplicacion_organizadora.perfil.Usuario;
import com.example.aplicacion_organizadora.perfil.UsuarioDao;
import com.example.aplicacion_organizadora.ui.recordatorios.Recordatorio;
import com.example.aplicacion_organizadora.ui.recordatorios.RecordatorioDao;
import com.example.aplicacion_organizadora.ui.tasks.Task;
import com.example.aplicacion_organizadora.ui.tasks.TaskDao;
import com.example.aplicacion_organizadora.ui.notes.Note;
import com.example.aplicacion_organizadora.ui.notes.NoteDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Recordatorio.class, Task.class, Note.class, Usuario.class}, version = 10, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {

    public abstract RecordatorioDao recordatorioDao();
    public abstract TaskDao taskDao();
    public abstract NoteDao noteDao();
    public abstract UsuarioDao usuarioDao();


    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "organizador_app.db";

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
