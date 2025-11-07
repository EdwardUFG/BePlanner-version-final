package com.example.aplicacion_organizadora.ui.notes;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.aplicacion_organizadora.data.AppDatabase;
import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private final NoteDao noteDao;
    private final LiveData<List<Note>> allNotes;

    public NoteViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        noteDao = db.noteDao();
        allNotes = noteDao.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insert(Note note) {
        new Thread(() -> noteDao.insert(note)).start();
    }

    public void update(Note note) {
        new Thread(() -> noteDao.update(note)).start();
    }

    public void delete(Note note) {
        new Thread(() -> noteDao.delete(note)).start();
    }
}