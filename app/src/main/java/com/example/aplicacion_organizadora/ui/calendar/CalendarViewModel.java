package com.example.aplicacion_organizadora.ui.calendar;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.aplicacion_organizadora.ui.recordatorios.Recordatorio;
import com.example.aplicacion_organizadora.ui.recordatorios.RecordatorioRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CalendarViewModel extends AndroidViewModel {

    private final RecordatorioRepository repositorioDeRecordatorios;
    private final DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
    private final DateTimeFormatter formateadorMes = DateTimeFormatter.ofPattern("yyyy-MM", Locale.getDefault());

    private final MutableLiveData<LocalDate> fechaSeleccionada = new MutableLiveData<>(LocalDate.now());
    private final MutableLiveData<YearMonth> mesMostrado = new MutableLiveData<>(YearMonth.now());

    private final LiveData<List<Recordatorio>> recordatoriosDeFechaSeleccionada;
    private final LiveData<HashSet<LocalDate>> fechasConRecordatorios;

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        repositorioDeRecordatorios = new RecordatorioRepository(application);

        recordatoriosDeFechaSeleccionada = Transformations.switchMap(fechaSeleccionada, fecha -> {
            String fechaFormateada = fecha.format(formateador);
            return repositorioDeRecordatorios.getRecordatoriosPorFecha(fechaFormateada);
        });

        fechasConRecordatorios = Transformations.switchMap(mesMostrado, mes ->
                Transformations.map(
                        repositorioDeRecordatorios.getFechasConRecordatoriosDelMes(mes.format(formateadorMes)),
                        listaDeStrings -> {
                            if (listaDeStrings == null) {
                                return new HashSet<>(); // Devolver un set vacío si la lista es nula
                            }
                            // Usar el formateador explícitamente para asegurar la conversión correcta
                            return new HashSet<>(listaDeStrings.stream()
                                    .map(s -> LocalDate.parse(s, formateador))
                                    .collect(Collectors.toList()));
                        }
                )
        );
    }

    public void setFecha(LocalDate fecha) {
        fechaSeleccionada.setValue(fecha);
    }

    public void setMesMostrado(YearMonth mes) {
        mesMostrado.setValue(mes);
    }

    public LiveData<List<Recordatorio>> getRecordatoriosDeFechaSeleccionada() {
        return recordatoriosDeFechaSeleccionada;
    }

    public LiveData<LocalDate> getFechaSeleccionada() {
        return fechaSeleccionada;
    }

    public LiveData<HashSet<LocalDate>> getFechasConRecordatorios() {
        return fechasConRecordatorios;
    }
}
