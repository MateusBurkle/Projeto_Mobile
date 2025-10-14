package com.example.projeto.adapters;

import android.graphics.Paint; // alteração
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox; // alteração
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projeto.R;
import com.example.projeto.models.SubTask;
import com.example.projeto.models.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.VH> {


    public interface OnItemClick { void onClick(Task t); }

    public interface OnToggleDone { void onToggle(Task t, boolean done, int position); } // alteração

    private final List<Task> data = new ArrayList<>();
    private OnItemClick listener;
    private OnToggleDone toggleListener;

    public void setOnItemClick(OnItemClick l) { this.listener = l; }
    public void setOnToggleDone(OnToggleDone l) { this.toggleListener = l; } // alteração

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

        // Tarefa concluída: liga/desliga
        h.chkConcluida.setOnCheckedChangeListener(null);      // alteração
        h.chkConcluida.setChecked(t.isConcluida());           // alteração
        applyDoneStyle(h, t.isConcluida());                   // alteração

        h.chkConcluida.setOnCheckedChangeListener((btn, checked) -> { // alteração
            if (toggleListener != null) {
                toggleListener.onToggle(t, checked, h.getBindingAdapterPosition());
            }
            applyDoneStyle(h, checked);
        });

        // Tocar no item alterna o checkbox
        h.itemView.setOnClickListener(v -> { // mantém seu comportamento + toggle
            if (listener != null) listener.onClick(t);
        });
        h.itemView.setOnLongClickListener(v -> { // long-press: atalho para alternar
            h.chkConcluida.performClick();
            return true;
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView titulo, notas, meta;
        CheckBox chkConcluida; // alteração
        VH(@NonNull View v) {
            super(v);
            titulo = v.findViewById(R.id.tvTitulo);
            notas  = v.findViewById(R.id.tvNotas);
            meta   = v.findViewById(R.id.tvMeta);
            chkConcluida = v.findViewById(R.id.chkConcluida); // alteração
        }
    }

    // Visual quando concluída (risco e opacidade)
    private void applyDoneStyle(VH h, boolean done) { // alteração
        if (done) {
            h.titulo.setPaintFlags(h.titulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.notas.setPaintFlags(h.notas.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.itemView.setAlpha(0.6f);
        } else {
            h.titulo.setPaintFlags(h.titulo.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            h.notas.setPaintFlags(h.notas.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            h.itemView.setAlpha(1f);
        }
    }
}
