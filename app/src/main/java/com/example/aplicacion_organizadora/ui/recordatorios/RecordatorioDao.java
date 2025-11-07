package com.example.aplicacion_organizadora.ui.recordatorios;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RecordatorioDao {
    @Insert
    long insert(Recordatorio recordatorio);

    @Update
    void update(Recordatorio recordatorio);

    @Delete
    void delete(Recordatorio recordatorio);

    @Query("SELECT * FROM recordatorios ORDER BY id DESC")
    LiveData<List<Recordatorio>> getAll();

    @Query("SELECT * FROM recordatorios ORDER BY fechaHoraAlarma ASC LIMIT 1")
    LiveData<Recordatorio> getNextReminder();

    @Query("SELECT * FROM recordatorios " +
            "WHERE fechaHoraAlarma > :ahora " +
            "ORDER BY fechaHoraAlarma ASC " +
            "LIMIT 1")
    LiveData<Recordatorio> getProximo(long ahora);

    @Query("SELECT * FROM recordatorios WHERE fecha_recordatorio = :fecha")
    LiveData<List<Recordatorio>> getRecordatoriosPorFecha(String fecha);

    @Query("SELECT DISTINCT fecha_recordatorio FROM recordatorios WHERE fecha_recordatorio LIKE :yearMonth || '%'")
    LiveData<List<String>> getFechasConRecordatoriosDelMes(String yearMonth);

}