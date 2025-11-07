package com.example.aplicacion_organizadora.ui.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacion_organizadora.R;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private final Consumer<Task> onDelete;
    private final Consumer<Task> onEdit;
    private final Consumer<Task> onUpdate;

    public TaskAdapter(Consumer<Task> onDelete, Consumer<Task> onEdit, Consumer<Task> onUpdate) {
        this.onDelete = onDelete;
        this.onEdit = onEdit;
        this.onUpdate = onUpdate;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.textTitle.setText(task.title);
        holder.textDescription.setText(task.description);

        holder.priorityDot.setBackgroundResource(R.drawable.circle_priority);
        switch (task.priority) {
            case "Alta":
                holder.priorityDot.getBackground().setTint(
                        holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark)
                );
                break;
            case "Media":
                holder.priorityDot.getBackground().setTint(
                        holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_light)
                );
                break;
            case "Baja":
                holder.priorityDot.getBackground().setTint(
                        holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark)
                );
                break;
        }

        if (task.isDone) {
            holder.textStatus.setVisibility(View.VISIBLE);
            holder.textStatus.setText("Completada");
        } else {
            holder.textStatus.setVisibility(View.GONE);
        }

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isDone);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.isDone = isChecked;
            onUpdate.accept(task); // se guarda en BD y LiveData reordena
        });

        holder.btnDelete.setOnClickListener(v -> onDelete.accept(task));
        holder.btnEdit.setOnClickListener(v -> onEdit.accept(task));
    }


    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDescription, textStatus;
        View priorityDot;
        CheckBox checkBox;
        ImageButton btnDelete, btnEdit;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            textStatus = itemView.findViewById(R.id.textStatus);
            priorityDot = itemView.findViewById(R.id.priorityDot);
            checkBox = itemView.findViewById(R.id.checkBox);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
