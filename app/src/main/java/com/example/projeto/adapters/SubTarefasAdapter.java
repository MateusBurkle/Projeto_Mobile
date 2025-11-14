package com.example.projeto.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projeto.R;
import com.example.projeto.models.SubTask;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

public class SubTarefasAdapter extends RecyclerView.Adapter<SubTarefasAdapter.VH> {

    private final List<SubTask> subtasks;
    private final OnToggleClick onToggleClick;
    private final OnDeleteClick onDeleteClick;

    // Construtor que recebe os dados e os listeners
    public SubTarefasAdapter(List<SubTask> subtasks, OnToggleClick onToggleClick, OnDeleteClick onDeleteClick) {
        this.subtasks = subtasks;
        this.onToggleClick = onToggleClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout que você me enviou (item_subtarefa.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtarefa, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SubTask subtask = subtasks.get(position);

        // Define o texto e o estado do checkbox
        holder.chkSubtarefa.setText(subtask.getName());
        holder.chkSubtarefa.setChecked(subtask.getDone());

        // Aplica o efeito de "riscado" se a subtarefa estiver concluída
        if (subtask.getDone()) {
            holder.chkSubtarefa.setPaintFlags(holder.chkSubtarefa.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.chkSubtarefa.setPaintFlags(holder.chkSubtarefa.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return subtasks.size();
    }

    // Interfaces para os cliques (vistas no TarefasActivity)
    public interface OnToggleClick {
        void onToggle(int position);
    }

    public interface OnDeleteClick {
        void onDelete(int position);
    }

    // ViewHolder que mapeia os componentes do XML (item_subtarefa.xml)
    class VH extends RecyclerView.ViewHolder {
        MaterialCheckBox chkSubtarefa;
        ImageButton btnDeletarSubtarefa;

        public VH(@NonNull View itemView) {
            super(itemView);
            chkSubtarefa = itemView.findViewById(R.id.chkSubtarefa);
            btnDeletarSubtarefa = itemView.findViewById(R.id.btnDeletarSubtarefa);

            // Configura os cliques para notificar a Activity
            chkSubtarefa.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onToggleClick.onToggle(getAdapterPosition());
                }
            });

            btnDeletarSubtarefa.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onDeleteClick.onDelete(getAdapterPosition());
                }
            });
        }
    }
}