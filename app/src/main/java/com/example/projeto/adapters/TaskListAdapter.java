package com.example.projeto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projeto.R;
import com.example.projeto.models.SubTask;
import com.example.projeto.models.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.VH> {

    public interface OnItemClick {
        void onClick(Task t);
    }

    private final List<Task> data = new ArrayList<>();
    private OnItemClick listener;

    public void setOnItemClick(OnItemClick l) { this.listener = l; }

    public void submit(List<Task> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarefa, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Task t = data.get(position);
        h.titulo.setText(t.getTitulo());

        String notas = t.getNotas() == null ? "" : t.getNotas().trim();
        h.notas.setVisibility(notas.isEmpty() ? View.GONE : View.VISIBLE);
        h.notas.setText(notas);

        List<SubTask> subs = t.getSubtarefas();
        int qtd = subs == null ? 0 : subs.size();
        h.meta.setText(qtd == 0 ? "Sem subtarefas" : (qtd + " subtarefa(s)"));

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(t);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView titulo, notas, meta;
        VH(@NonNull View v) {
            super(v);
            titulo = v.findViewById(R.id.tvTitulo);
            notas  = v.findViewById(R.id.tvNotas);
            meta   = v.findViewById(R.id.tvMeta);
        }
    }
}
