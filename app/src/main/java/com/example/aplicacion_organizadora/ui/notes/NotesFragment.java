package com.example.aplicacion_organizadora.ui.notes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_organizadora.R;

import java.util.Arrays;

public class NotesFragment extends Fragment {

    private NoteViewModel viewModel;
    private NoteAdapter adapter;
    private RecyclerView recyclerView;
    private Button btnAddNote;

    public NotesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerView = view.findViewById(R.id.recyclerNotes);
        btnAddNote = view.findViewById(R.id.btnAddNote);

        adapter = new NoteAdapter(new NoteAdapter.NoteActionListener() {
            @Override
            public void onEdit(Note note) {
                showNoteDialog(note);
            }

            @Override
            public void onDelete(Note note) {
                viewModel.delete(note);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAddNote.setOnClickListener(v -> showNoteDialog(null));

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        viewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            adapter.setNoteList(notes);
        });
    }

    private void showNoteDialog(@Nullable Note noteToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_note, null);
        builder.setView(dialogView);

        EditText editTitle = dialogView.findViewById(R.id.editNoteTitle);
        EditText editBody = dialogView.findViewById(R.id.editNoteBody);

        if (noteToEdit != null) {
            editTitle.setText(noteToEdit.title);
            editBody.setText(noteToEdit.body);
        }

        editTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String texto = s.toString().trim();
                int cantidadPalabras = texto.isEmpty() ? 0 : texto.split("\\s+").length;

                if (cantidadPalabras > 2) {
                    Toast.makeText(requireContext(),
                            "Máximo 20 palabras en el título", Toast.LENGTH_SHORT).show();

                    editTitle.removeTextChangedListener(this);
                    String[] palabras = texto.split("\\s+");
                    String recortado = String.join(" ", Arrays.copyOf(palabras, 2));
                    editTitle.setText(recortado);
                    editTitle.setSelection(recortado.length());
                    editTitle.addTextChangedListener(this);
                }
            }
        });

        builder.setPositiveButton(noteToEdit == null ? "Crear" : "Actualizar", (dialog, which) -> {
            String title = editTitle.getText().toString().trim();
            String body = editBody.getText().toString().trim();

            if (!title.isEmpty()) {
                if (noteToEdit == null) {
                    long ahora = System.currentTimeMillis();
                    Note newNote = new Note(title, body, ahora);
                    viewModel.insert(newNote);
                } else {
                    noteToEdit.title = title;
                    noteToEdit.body = body;
                    viewModel.update(noteToEdit);
                }
            }

        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}
