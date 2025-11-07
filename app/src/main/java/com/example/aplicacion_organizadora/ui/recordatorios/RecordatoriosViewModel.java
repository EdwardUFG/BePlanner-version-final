package com.example.aplicacion_organizadora.ui.recordatorios;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.Future;

public class RecordatoriosViewModel extends AndroidViewModel {

    private final RecordatorioRepository repository;
    private final LiveData<List<Recordatorio>> allRecordatorios;

    public RecordatoriosViewModel(@NonNull Application application) {
        super(application);
        repository = new RecordatorioRepository(application);
        allRecordatorios = repository.getAllRecordatorios();
    }

    public LiveData<List<Recordatorio>> getAllRecordatorios() {
        return allRecordatorios;
    }

    public Future<Long> insert(Recordatorio recordatorio) {
        return repository.insert(recordatorio);
    }

    public void update(Recordatorio recordatorio) {
        repository.update(recordatorio);
    }

    public void delete(Recordatorio recordatorio) {
        repository.delete(recordatorio);
    }
}
