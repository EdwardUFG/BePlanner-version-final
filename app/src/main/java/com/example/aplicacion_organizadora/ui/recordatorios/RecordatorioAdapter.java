package com.example.aplicacion_organizadora.ui.recordatorios;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_organizadora.R;

public class RecordatorioAdapter extends ListAdapter<Recordatorio, RecordatorioAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onEditClick(Recordatorio recordatorio);
        void onDeleteClick(Recordatorio recordatorio);
    }

    private OnItemClickListener listener;
    private boolean showButtons = true;

    public RecordatorioAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setShowButtons(boolean show) {
        this.showButtons = show;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recordatorio, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recordatorio currentRecordatorio = getItem(position);

        holder.textViewTitulo.setText(currentRecordatorio.titulo);
        holder.textViewDescripcion.setText(currentRecordatorio.descripcion);

        long ahora = System.currentTimeMillis();
        boolean finalizado = currentRecordatorio.fechaHoraAlarma < ahora;

        if (finalizado) {
            holder.textViewStatus.setVisibility(View.VISIBLE);
            holder.textViewStatus.setText("Finalizado");

            holder.textViewTitulo.setPaintFlags(
                    holder.textViewTitulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
        } else {
            holder.textViewStatus.setVisibility(View.GONE);

            holder.textViewTitulo.setPaintFlags(
                    holder.textViewTitulo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
            );
        }

        if (showButtons) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitulo;
        private final TextView textViewDescripcion;
        private final TextView textViewStatus;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitulo = itemView.findViewById(R.id.item_titulo);
            textViewDescripcion = itemView.findViewById(R.id.item_descripcion);
            textViewStatus = itemView.findViewById(R.id.textStatus);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(getItem(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getItem(position));
                }
            });
        }
    }

    private static final DiffUtil.ItemCallback<Recordatorio> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Recordatorio>() {
                @Override
                public boolean areItemsTheSame(@NonNull Recordatorio oldItem, @NonNull Recordatorio newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Recordatorio oldItem, @NonNull Recordatorio newItem) {
                    return oldItem.titulo.equals(newItem.titulo) &&
                            oldItem.descripcion.equals(newItem.descripcion) &&
                            oldItem.fechaHoraAlarma == newItem.fechaHoraAlarma &&
                            oldItem.idTareaAsociada == newItem.idTareaAsociada &&
                            oldItem.idNotaAsociada == newItem.idNotaAsociada;
                }
            };
}
