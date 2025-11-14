package com.example.projeto.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projeto.R;
import com.example.projeto.models.Task;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.VH> {

    private final List<Task> tasks;
    private final OnItemClick onItemClick;
    private final OnToggleDone onToggleDone;

    public TaskListAdapter(List<Task> tasks, OnItemClick onItemClick, OnToggleDone onToggleDone) {
        this.tasks = tasks;
        this.onItemClick = onItemClick;
        this.onToggleDone = onToggleDone;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarefa, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Task task = tasks.get(position);
        holder.tvNomeTarefa.setText(task.getNome());
        holder.cbConcluida.setChecked(task.isConcluida());

        // --- MUDANÇA: O modelo Task não tem "Notas", então escondemos esse campo ---
        holder.tvNotas.setVisibility(View.GONE);
        // --- FIM DA MUDANÇA ---

        if (task.isConcluida()) {
            holder.tvNomeTarefa.setPaintFlags(holder.tvNomeTarefa.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvNomeTarefa.setPaintFlags(holder.tvNomeTarefa.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        int subtasksConcluidas = 0;
        if (task.getSubtasks() != null) {
            for (int i = 0; i < task.getSubtasks().size(); i++) {
                if (task.getSubtasks().get(i).getDone()) {
                    subtasksConcluidas++;
                }
            }
            holder.tvContadorSubtasks.setText(subtasksConcluidas + " de " + task.getSubtasks().size() + " concluídas");
        } else {
            holder.tvContadorSubtasks.setText("0 subtasks");
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public interface OnItemClick {
        void onItemClick(long taskId);
    }

    public interface OnToggleDone {
        void onToggleDone(int position);
    }

    class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvNomeTarefa;
        TextView tvContadorSubtasks;
        MaterialCheckBox cbConcluida;
        TextView tvNotas; // <-- MUDANÇA: Campo adicionado do novo XML

        public VH(@NonNull View itemView) {
            super(itemView);
            // --- MUDANÇA: IDs atualizados para bater com item_tarefa.xml ---
            tvNomeTarefa = itemView.findViewById(R.id.tvTitulo);
            tvContadorSubtasks = itemView.findViewById(R.id.tvMeta);
            cbConcluida = itemView.findViewById(R.id.chkConcluida);
            tvNotas = itemView.findViewById(R.id.tvNotas);
            // --- FIM DA MUDANÇA ---

            itemView.setOnClickListener(this);

            cbConcluida.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onToggleDone.onToggleDone(getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                long taskId = tasks.get(getAdapterPosition()).getId();
                onItemClick.onItemClick(taskId);
            }
        }
    }
}