package com.example.aplicacion_organizadora.ui.recordatorios;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_organizadora.R;
import com.example.aplicacion_organizadora.notifications.AlarmReceiver;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.Future;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class RecordatoriosFragment extends Fragment {

    private RecordatoriosViewModel recordatoriosViewModel;
    private final Calendar calendarioSeleccionado = Calendar.getInstance();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(getContext(), "Permiso de notificaciones concedido.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No se podrán mostrar notificaciones sin el permiso.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recordatorios, container, false);

        pedirPermisoDeNotificaciones();

        RecyclerView recyclerView = root.findViewById(R.id.recycler_recordatorios);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        final RecordatorioAdapter adapter = new RecordatorioAdapter();
        recyclerView.setAdapter(adapter);

        recordatoriosViewModel = new ViewModelProvider(this).get(RecordatoriosViewModel.class);
        recordatoriosViewModel.getAllRecordatorios().observe(getViewLifecycleOwner(), adapter::submitList);

        Button fab = root.findViewById(R.id.btn_nuevo_recordatorio);
        fab.setOnClickListener(v -> showRecordatorioDialog(null));

        adapter.setOnItemClickListener(new RecordatorioAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Recordatorio recordatorio) {
                showRecordatorioDialog(recordatorio);
            }

            @Override
            public void onDeleteClick(Recordatorio recordatorio) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Confirmar Eliminación")
                        .setMessage("¿Estás seguro?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            recordatoriosViewModel.delete(recordatorio);
                            Toast.makeText(getContext(), "Recordatorio eliminado", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });



        return root;
    }

    private void showRecordatorioDialog(final Recordatorio recordatorio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_recordatorio, null);

        final EditText etTitulo = dialogView.findViewById(R.id.et_titulo);
        final EditText etDescripcion = dialogView.findViewById(R.id.et_descripcion);
        final Button btnFecha = dialogView.findViewById(R.id.btn_seleccionar_fecha);
        final Button btnHora = dialogView.findViewById(R.id.btn_seleccionar_hora);
        final TextView tvFechaHora = dialogView.findViewById(R.id.tv_fecha_hora_seleccionada);

        if (recordatorio != null) {
            etTitulo.setText(recordatorio.titulo);
            etDescripcion.setText(recordatorio.descripcion);
            if (recordatorio.fechaHoraAlarma > 0) {
                calendarioSeleccionado.setTimeInMillis(recordatorio.fechaHoraAlarma);
            } else {
                calendarioSeleccionado.setTimeInMillis(System.currentTimeMillis());
                calendarioSeleccionado.add(Calendar.HOUR_OF_DAY, 1);
            }
            actualizarTextViewFechaHora(tvFechaHora);
        } else {
            calendarioSeleccionado.setTimeInMillis(System.currentTimeMillis());
            calendarioSeleccionado.add(Calendar.HOUR_OF_DAY, 1);
            actualizarTextViewFechaHora(tvFechaHora);
        }

        btnFecha.setOnClickListener(v -> mostrarDatePicker(tvFechaHora));
        btnHora.setOnClickListener(v -> mostrarTimePicker(tvFechaHora));

        builder.setView(dialogView)
                .setTitle(recordatorio == null ? "Nuevo Recordatorio" : "Editar Recordatorio")
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String titulo = etTitulo.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();

                    if (titulo.isEmpty()) {
                        Toast.makeText(getContext(), "El título no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long fechaHoraAlarmaMillis = calendarioSeleccionado.getTimeInMillis();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
                    LocalDate localDate = LocalDate.of(
                            calendarioSeleccionado.get(Calendar.YEAR),
                            calendarioSeleccionado.get(Calendar.MONTH) + 1,
                            calendarioSeleccionado.get(Calendar.DAY_OF_MONTH)
                    );
                    String fechaFormateada = localDate.format(formatter);

                    if (recordatorio == null) {
                        Recordatorio nuevo = new Recordatorio();
                        nuevo.titulo = titulo;
                        nuevo.descripcion = descripcion;
                        nuevo.fechaHoraAlarma = fechaHoraAlarmaMillis;
                        nuevo.fechaRecordatorio = fechaFormateada;

                        try {
                            Future<Long> futureId = recordatoriosViewModel.insert(nuevo);
                            long nuevoId = futureId.get();
                            nuevo.id = (int) nuevoId;
                            programarAlarma(nuevo);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error al guardar el recordatorio", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        recordatorio.titulo = titulo;
                        recordatorio.descripcion = descripcion;
                        recordatorio.fechaHoraAlarma = fechaHoraAlarmaMillis;
                        recordatorio.fechaRecordatorio = fechaFormateada;
                        recordatoriosViewModel.update(recordatorio);
                        programarAlarma(recordatorio);
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void pedirPermisoDeNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void mostrarDatePicker(TextView tv) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendarioSeleccionado.set(Calendar.YEAR, year);
                    calendarioSeleccionado.set(Calendar.MONTH, month);
                    calendarioSeleccionado.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    actualizarTextViewFechaHora(tv);
                },
                calendarioSeleccionado.get(Calendar.YEAR),
                calendarioSeleccionado.get(Calendar.MONTH),
                calendarioSeleccionado.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void mostrarTimePicker(TextView tv) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    calendarioSeleccionado.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendarioSeleccionado.set(Calendar.MINUTE, minute);
                    calendarioSeleccionado.set(Calendar.SECOND, 0);
                    actualizarTextViewFechaHora(tv);
                },
                calendarioSeleccionado.get(Calendar.HOUR_OF_DAY),
                calendarioSeleccionado.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void actualizarTextViewFechaHora(TextView tv) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        tv.setText(df.format(calendarioSeleccionado.getTime()));
    }

    private void programarAlarma(Recordatorio recordatorio) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        intent.putExtra("TITULO_RECORDATORIO", recordatorio.titulo);
        intent.putExtra("DESCRIPCION_RECORDATORIO", recordatorio.descripcion);
        intent.putExtra("NOTIFICATION_ID", recordatorio.id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                recordatorio.id,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, recordatorio.fechaHoraAlarma, pendingIntent);
                Toast.makeText(getContext(), "Recordatorio programado", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Permiso Necesario")
                        .setMessage("Para programar recordatorios exactos, la aplicación necesita un permiso especial. ¿Deseas ir a los ajustes para habilitarlo?")
                        .setPositiveButton("Ir a Ajustes", (dialog, which) -> {
                            Intent intentAjustes = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intentAjustes);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, recordatorio.fechaHoraAlarma, pendingIntent);
            Toast.makeText(getContext(), "Recordatorio programado", Toast.LENGTH_SHORT).show();
        }
    }
}