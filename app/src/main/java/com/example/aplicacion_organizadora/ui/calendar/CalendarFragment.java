package com.example.aplicacion_organizadora.ui.calendar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_organizadora.R;
import com.example.aplicacion_organizadora.ui.recordatorios.RecordatorioAdapter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    private CalendarViewModel calendarViewModel;
    private TextView monthYearText;
    private GridLayout calendarGrid;
    private RecyclerView recordatoriosRecyclerView;
    private TextView textoSinRecordatorios;
    private YearMonth mesMostrado;
    private final List<View> celdasDias = new ArrayList<>();
    private View celdaSeleccionadaAnterior = null;
    private RecordatorioAdapter recordatorioAdapter;
    private HashSet<LocalDate> fechasConRecordatorios = new HashSet<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        monthYearText = view.findViewById(R.id.month_year_text);
        calendarGrid = view.findViewById(R.id.calendar_grid);
        recordatoriosRecyclerView = view.findViewById(R.id.recycler_view_recordatorios_dia);
        textoSinRecordatorios = view.findViewById(R.id.texto_sin_recordatorios);
        Button prevButton = view.findViewById(R.id.prev_button);
        Button nextButton = view.findViewById(R.id.next_button);

        recordatorioAdapter = new RecordatorioAdapter();
        recordatorioAdapter.setShowButtons(false);
        recordatoriosRecyclerView.setAdapter(recordatorioAdapter);

        mesMostrado = YearMonth.now();
        calendarViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        prevButton.setOnClickListener(v -> mesAnterior());
        nextButton.setOnClickListener(v -> mesSiguiente());

        observarViewModel();
        calendarViewModel.setFecha(LocalDate.now());
        calendarViewModel.setMesMostrado(mesMostrado); // Carga inicial
    }

    private void observarViewModel() {
        calendarViewModel.getFechaSeleccionada().observe(getViewLifecycleOwner(), fecha -> {
            YearMonth newMonth = YearMonth.from(fecha);
            if (!newMonth.equals(mesMostrado)) {
                mesMostrado = newMonth;
                calendarViewModel.setMesMostrado(mesMostrado);
            } else {
                actualizarSeleccionVisual();
            }
        });

        calendarViewModel.getRecordatoriosDeFechaSeleccionada().observe(getViewLifecycleOwner(), recordatorios -> {
            if (recordatorios == null || recordatorios.isEmpty()) {
                recordatoriosRecyclerView.setVisibility(View.GONE);
                textoSinRecordatorios.setVisibility(View.VISIBLE);
            } else {
                recordatoriosRecyclerView.setVisibility(View.VISIBLE);
                textoSinRecordatorios.setVisibility(View.GONE);
                recordatorioAdapter.submitList(recordatorios);
            }
        });

        calendarViewModel.getFechasConRecordatorios().observe(getViewLifecycleOwner(), fechas -> {
            if (fechas != null) {
                fechasConRecordatorios = fechas;
                dibujarCalendario(); // Redibuja el calendario cuando las fechas con recordatorios cambian
            }
        });
    }

    private void dibujarCalendario() {
        calendarGrid.removeAllViews();
        celdasDias.clear();

        String textoMesAño = mesMostrado.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + mesMostrado.getYear();
        monthYearText.setText(textoMesAño.substring(0, 1).toUpperCase() + textoMesAño.substring(1));

        LocalDate primerDiaDelMes = mesMostrado.atDay(1);
        int diaDeLaSemana = primerDiaDelMes.getDayOfWeek().getValue();
        int diasVaciosAlPrincipio = (diaDeLaSemana == 7) ? 0 : diaDeLaSemana;

        for (int i = 0; i < diasVaciosAlPrincipio; i++) {
            calendarGrid.addView(crearCeldaVacia());
        }

        int diasEnMes = mesMostrado.lengthOfMonth();
        for (int i = 1; i <= diasEnMes; i++) {
            LocalDate fechaActual = mesMostrado.atDay(i);
            View celdaDia = crearCeldaDia(fechaActual);
            calendarGrid.addView(celdaDia);
            celdasDias.add(celdaDia);
        }

        actualizarSeleccionVisual();
    }

    private View crearCeldaDia(LocalDate fecha) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        FrameLayout celdaLayout = (FrameLayout) inflater.inflate(R.layout.calendar_day_cell, calendarGrid, false);
        TextView celdaTexto = celdaLayout.findViewById(R.id.day_text);
        View indicator = celdaLayout.findViewById(R.id.reminder_indicator);

        celdaTexto.setText(String.valueOf(fecha.getDayOfMonth()));
        celdaLayout.setTag(fecha);

        if (fechasConRecordatorios != null && fechasConRecordatorios.contains(fecha)) {
            indicator.setVisibility(View.VISIBLE);
        } else {
            indicator.setVisibility(View.GONE);
        }

        celdaLayout.setOnClickListener(v -> {
            LocalDate fechaClickeada = (LocalDate) v.getTag();
            calendarViewModel.setFecha(fechaClickeada);
        });

        return celdaLayout;
    }

    private void actualizarSeleccionVisual() {
        LocalDate fechaSeleccionada = calendarViewModel.getFechaSeleccionada().getValue();
        if (fechaSeleccionada == null) return;

        if (celdaSeleccionadaAnterior != null) {
            TextView textoAnterior = celdaSeleccionadaAnterior.findViewById(R.id.day_text);
            textoAnterior.setBackground(null);
            textoAnterior.setTextColor(Color.parseColor("#140101"));
        }

        for (View celda : celdasDias) {
            if (celda.getTag().equals(fechaSeleccionada)) {
                TextView textoCelda = celda.findViewById(R.id.day_text);
                textoCelda.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.calendar_selected_day_bg));
                textoCelda.setTextColor(Color.WHITE);
                celdaSeleccionadaAnterior = celda;
                break;
            }
        }
    }

    private View crearCeldaVacia() {
        View view = new View(getContext());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        view.setLayoutParams(params);
        return view;
    }

    private void mesSiguiente() {
        mesMostrado = mesMostrado.plusMonths(1);
        calendarViewModel.setMesMostrado(mesMostrado);
    }

    private void mesAnterior() {
        mesMostrado = mesMostrado.minusMonths(1);
        calendarViewModel.setMesMostrado(mesMostrado);
    }
}
