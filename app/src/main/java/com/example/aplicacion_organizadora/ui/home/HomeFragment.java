package com.example.aplicacion_organizadora.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aplicacion_organizadora.R;
import com.example.aplicacion_organizadora.ui.notes.Note;
import com.example.aplicacion_organizadora.ui.recordatorios.Recordatorio;
import com.example.aplicacion_organizadora.ui.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        TextView noteTitle = root.findViewById(R.id.textNotaTitulo);
        TextView noteBody = root.findViewById(R.id.textNotaCuerpo);
        TextView reminderTitle = root.findViewById(R.id.textRecordatorioTitulo);
        TextView reminderTime = root.findViewById(R.id.textRecordatorioFecha);
        TextView taskDescription = root.findViewById(R.id.textTareaTitulo);
        TextView taskStatus = root.findViewById(R.id.textTareaPrioridad);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.getLatestNote().observe(getViewLifecycleOwner(), note -> {
            if (note != null) {
                noteTitle.setText(note.title);
                noteBody.setText(getPrimerasPalabras(note.body, 10));
            } else {
                noteTitle.setText("Sin notas");
                noteBody.setText("");
            }
        });
        homeViewModel.getNextRecordatorio().observe(getViewLifecycleOwner(), recordatorio -> {
            if (recordatorio != null) {
                reminderTitle.setText(recordatorio.titulo);
                reminderTime.setText("Sonará: " + formatoFecha(recordatorio.fechaHoraAlarma));
            } else {
                reminderTitle.setText("Sin recordatorios");
                reminderTime.setText("");
            }
        });
        homeViewModel.getTopTask().observe(getViewLifecycleOwner(), task -> {
            if (task != null) {
                taskDescription.setText(getPrimerasPalabras(task.title, 10));
                taskStatus.setText("Prioridad: " + task.priority);
            } else {
                taskDescription.setText("Sin tareas");
                taskStatus.setText("");
            }
        });

        return root;
    }

    private String getPrimerasPalabras(String texto, int maxPalabras) {
        if (texto == null) return "";
        String[] palabras = texto.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(maxPalabras, palabras.length); i++) {
            sb.append(palabras[i]).append(" ");
        }
        return sb.toString().trim();
    }

    private String formatoFecha(long millis) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return df.format(new Date(millis));
    }
}