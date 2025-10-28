package com.example.projeto.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projeto.R;
import com.example.projeto.models.SubTask;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

public class SubTarefasAdapter extends RecyclerView.Adapter<SubTarefasAdapter.VH> {

    private final List<SubTask> list;

    // --- NOVO: Interfaces para os cliques ---
    public interface OnDeleteClick {
        void accept(int position);
    }
    public interface OnToggleClick {
        void accept(SubTask subTask, boolean isChecked);
    }

    private OnDeleteClick onDeleteClick;
    private OnToggleClick onToggleClick;

    public void setOnDeleteClick(OnDeleteClick listener) {
        this.onDeleteClick = listener;
    }
    public void setOnToggleClick(OnToggleClick listener) {
        this.onToggleClick = listener;
    }
    // --- FIM DO NOVO ---

    public SubTarefasAdapter(List<SubTask> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // MUDANÇA: Infla o novo layout 'item_subtarefa.xml'
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtarefa, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SubTask subTask = list.get(position);
        holder.bind(subTask);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    class VH extends RecyclerView.ViewHolder {

        // --- NOVO: Referências para as views do novo layout ---
        MaterialCheckBox chkSubtarefa;
        ImageButton btnDeletar;

        public VH(@NonNull View itemView) {
            super(itemView);
            // --- NOVO: Encontra as views ---
            chkSubtarefa = itemView.findViewById(R.id.chkSubtarefa);
            btnDeletar = itemView.findViewById(R.id.btnDeletarSubtarefa);
        }

        public void bind(SubTask subTask) {
            // --- NOVO: Lógica de binding ---
            chkSubtarefa.setText(subTask.getTitulo());
            chkSubtarefa.setChecked(subTask.isConcluida());

            // Atualiza o visual (riscado ou não)
            toggleStrikeThrough(chkSubtarefa, subTask.isConcluida());

            // --- NOVO: Listeners ---

            // Remove listener antigo para evitar chamadas duplicadas
            chkSubtarefa.setOnCheckedChangeListener(null);

            // Define o novo listener
            chkSubtarefa.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (onToggleClick != null) {
                    onToggleClick.accept(subTask, isChecked);
                }
                toggleStrikeThrough(chkSubtarefa, isChecked);
            });

            btnDeletar.setOnClickListener(v -> {
                if (onDeleteClick != null) {
                    // Pega a posição atual do adapter
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        onDeleteClick.accept(pos);
                    }
                }
            });
        }

        // --- NOVO: Método para riscar o texto ---
        private void toggleStrikeThrough(TextView textView, boolean isChecked) {
            if (isChecked) {
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textView.setAlpha(0.5f);
            } else {
                textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                textView.setAlpha(1.0f);
            }
        }
    }
}