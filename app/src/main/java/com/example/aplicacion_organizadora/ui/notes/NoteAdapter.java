package com.example.aplicacion_organizadora.ui.notes;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_organizadora.R;

import java.util.Arrays;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> noteList;
    private final NoteActionListener listener;

    public interface NoteActionListener {
        void onEdit(Note note);
        void onDelete(Note note);
    }

    public NoteAdapter(NoteActionListener listener) {
        this.listener = listener;
    }

    public void setNoteList(List<Note> notes) {
        this.noteList = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.textTitle.setText(limitWords(note.title, 8));
        holder.textBody.setText(limitWords(note.body, 25));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(note));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(note));
    }

    @Override
    public int getItemCount() {
        return noteList != null ? noteList.size() : 0;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textBody;
        ImageButton btnEdit, btnDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textNoteTitle);
            textBody = itemView.findViewById(R.id.textNoteBody);
            btnEdit = itemView.findViewById(R.id.btnEditNote);
            btnDelete = itemView.findViewById(R.id.btnDeleteNote);
        }
    }

    private String limitWords(String text, int maxWords) {
        if (text == null || text.trim().isEmpty()) return "";
        String[] words = text.trim().split("\\s+");
        if (words.length <= maxWords) return text;
        return TextUtils.join(" ", Arrays.copyOfRange(words, 0, maxWords)) + "...";
    }
}
