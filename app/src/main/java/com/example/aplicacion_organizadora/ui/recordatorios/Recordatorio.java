package com.example.aplicacion_organizadora.ui.recordatorios;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recordatorios")
public class Recordatorio {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String titulo;
    public String descripcion;
    public long fechaHoraAlarma;

   @ColumnInfo(name = "fecha_recordatorio")
   public String fechaRecordatorio;

    public int idTareaAsociada = -1;
    public int idNotaAsociada = -1;
}
