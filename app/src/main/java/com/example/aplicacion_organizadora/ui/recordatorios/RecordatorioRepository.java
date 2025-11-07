package com.example.aplicacion_organizadora.ui.recordatorios;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.aplicacion_organizadora.data.AppDatabase;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RecordatorioRepository {
    private final RecordatorioDao recordatorioDao;
    private final LiveData<List<Recordatorio>> allRecordatorios;
    private final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public RecordatorioRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        recordatorioDao = db.recordatorioDao();
        allRecordatorios = recordatorioDao.getAll();
    }

    public LiveData<List<Recordatorio>> getAllRecordatorios() {
        return allRecordatorios;
    }

    public Future<Long> insert(Recordatorio recordatorio) {
        Callable<Long> insertCallable = () -> recordatorioDao.insert(recordatorio);
        return databaseWriteExecutor.submit(insertCallable);
    }

    public void update(Recordatorio recordatorio) {
        databaseWriteExecutor.execute(() -> recordatorioDao.update(recordatorio));
    }

    public void delete(Recordatorio recordatorio) {
        databaseWriteExecutor.execute(() -> recordatorioDao.delete(recordatorio));
    }

    public LiveData<List<Recordatorio>> getRecordatoriosPorFecha(String fecha) {
        return recordatorioDao.getRecordatoriosPorFecha(fecha);
    }

    public LiveData<List<String>> getFechasConRecordatoriosDelMes(String yearMonth) {
        return recordatorioDao.getFechasConRecordatoriosDelMes(yearMonth);
    }
}
