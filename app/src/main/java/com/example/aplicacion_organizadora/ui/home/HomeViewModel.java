package com.example.aplicacion_organizadora.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.aplicacion_organizadora.data.AppDatabase;
import com.example.aplicacion_organizadora.ui.notes.Note;
import com.example.aplicacion_organizadora.ui.tasks.Task;
import com.example.aplicacion_organizadora.ui.recordatorios.Recordatorio;

public class HomeViewModel extends AndroidViewModel {

    private final AppDatabase db;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
    }

    public LiveData<Note> getLatestNote() {
        return db.noteDao().getLastCreated();
    }

    public LiveData<Task> getTopTask() {
        return db.taskDao().getTopByPriorityAndDate();
    }

    public LiveData<Recordatorio> getNextRecordatorio() {
        long ahora = System.currentTimeMillis();

        return Transformations.map(db.recordatorioDao().getProximo(ahora), recordatorio -> {
            if (recordatorio != null && recordatorio.fechaHoraAlarma > ahora) {
                return recordatorio;
            } else {
                return null;
            }
        });
    }
}
