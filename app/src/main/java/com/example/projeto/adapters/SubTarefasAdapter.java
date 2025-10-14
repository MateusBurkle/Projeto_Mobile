package com.example.projeto.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projeto.R;
import com.example.projeto.models.SubTask;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SubTarefasAdapter extends RecyclerView.Adapter<SubTarefasAdapter.VH> {

    private final List<SubTask> data = new ArrayList<>();

    // === API usada na Activity ===
    public void addNova() {
        data.add(new SubTask());
        notifyItemInserted(data.size() - 1);
    }

    public List<SubTask> getSubtarefasLimpa() {
        List<SubTask> out = new ArrayList<>();
        for (SubTask s : data) {
            if (s.getTitulo() != null && !s.getTitulo().trim().isEmpty()) {
                out.add(s);
            }
        }
        return out;
    }
    // =============================

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_subtarefas, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SubTask s = data.get(position);
        h.cb.setChecked(s.isConcluida());
        h.et.setText(s.getTitulo());

        h.cb.setOnCheckedChangeListener((buttonView, isChecked) -> s.setConcluida(isChecked));

        h.et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence c, int s1, int c2, int c3) {}
            @Override public void onTextChanged(CharSequence c, int s1, int b, int c2) {
                s.setTitulo(c.toString());
            }
            @Override public void afterTextChanged(Editable e) {}
        });

        h.btnRemover.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                data.remove(pos);
                notifyItemRemoved(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        MaterialCheckBox cb;
        TextInputEditText et;
        ImageButton btnRemover;

        VH(@NonNull View v) {
            super(v);
            cb = v.findViewById(R.id.cbConcluida);
            et = v.findViewById(R.id.etSubtitulo);
            btnRemover = v.findViewById(R.id.btnRemover);
        }
    }
}
