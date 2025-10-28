package com.example.projeto.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projeto.R;
import com.example.projeto.models.Task;
import com.example.projeto.storage.TaskStorage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// NOVO: Imports necessários para verificar a data
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;

public class PrincipalActivity extends AppCompatActivity {

    private TextView tvResumoAgua;
    private TextView tvResumoTarefas;
    private TaskStorage taskStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        taskStorage = new TaskStorage(this);
        tvResumoAgua = findViewById(R.id.tvResumoAgua);
        tvResumoTarefas = findViewById(R.id.tvResumoTarefas);

        // --- Botões (lógica existente) ---
        View btnAdicionarTarefa = findViewById(R.id.btnAdicionarTarefa);
        if (btnAdicionarTarefa != null) {
            btnAdicionarTarefa.setOnClickListener(v ->
                    startActivity(new Intent(this, TarefasActivity.class))
            );
        }

        View btnVerListaTarefas = findViewById(R.id.btnVerListaTarefas);
        if (btnVerListaTarefas != null) {
            btnVerListaTarefas.setOnClickListener(v -> {
                Intent intent = new Intent(PrincipalActivity.this, ListaTarefasActivity.class);
                startActivity(intent);
            });
        }

        View btnProgresso = findViewById(R.id.btnProgresso);
        if (btnProgresso != null) {
            btnProgresso.setOnClickListener(v ->
                    startActivity(new Intent(this, ProgressoActivity.class))
            );
        }

        ExtendedFloatingActionButton fabAddAgua = findViewById(R.id.btnAguaFAB);
        if (fabAddAgua != null) {
            fabAddAgua.setOnClickListener(v -> abrirBottomSheetAgua());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // NOVO: Verifica se o dia mudou ANTES de atualizar os resumos
        checkAndResetDailyProgress();
        atualizarResumos();
    }

    // --- MÉTODOS DE LÓGICA DO DASHBOARD ---

    /**
     * NOVO: Pega a data de hoje formatada (ex: "2025-10-28")
     */
    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * NOVO: Verifica se a data salva é diferente da data de hoje.
     * Se for, zera o progresso da água.
     */
    private void checkAndResetDailyProgress() {
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String today = getTodayDateString();
        String lastSaveDate = sp.getString("last_water_save_date", ""); // Pega a última data salva

        if (!today.equals(lastSaveDate)) {
            // É UM NOVO DIA!
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("daily_water_ml", 0); // Zera a contagem de água
            editor.putString("last_water_save_date", today); // Salva a data de hoje
            // (Futuramente, você pode zerar outras coisas aqui)
            editor.apply();
        }
        // Se as datas forem iguais, não faz nada.
    }

    private void atualizarResumos() {
        atualizarResumoAgua();
        atualizarResumoTarefas();
    }

    private void atualizarResumoAgua() {
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        // Agora, ele vai ler 0 se for um novo dia
        int currentWater = sp.getInt("daily_water_ml", 0);
        int goal = sp.getInt("daily_goal_ml", 2000);

        String resumoAgua = getString(R.string.water_summary_dashboard, currentWater, goal);
        tvResumoAgua.setText(resumoAgua);
    }

    private void atualizarResumoTarefas() {
        List<Task> tasks = taskStorage.getAll();
        int tarefasPendentes = 0;
        if (tasks != null) {
            for (Task task : tasks) {
                if (!task.isConcluida()) {
                    tarefasPendentes++;
                }
            }
        }
        String resumoTarefas = getString(R.string.task_summary_dashboard, tarefasPendentes);
        tvResumoTarefas.setText(resumoTarefas);
    }


    /**
     * Método para abrir o BottomSheet de Água (com lógica de data adicionada)
     */
    private void abrirBottomSheetAgua() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_add_agua, null);
        dialog.setContentView(view);

        TextView tvMeta = view.findViewById(R.id.tvMetaDiaria);

        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int weight = sp.getInt("weight_kg", 0);
        int goal = (weight > 0) ? weight * 40 : 2000;
        sp.edit().putInt("daily_goal_ml", goal).apply();
        tvMeta.setText("Meta diária: " + goal + " ml");


        RadioGroup rgUnidade = view.findViewById(R.id.rgUnidade);
        MaterialRadioButton rbMl = view.findViewById(R.id.rbMl);
        MaterialRadioButton rbCopos = view.findViewById(R.id.rbCopos);

        TextInputLayout tilQtd = view.findViewById(R.id.tilQuantidade);
        TextInputEditText etQtd = view.findViewById(R.id.etQuantidade);

        TextInputLayout tilTamanhoCopo = view.findViewById(R.id.tilTamanhoCopo);
        TextInputEditText etTamanhoCopo = view.findViewById(R.id.etTamanhoCopo);

        MaterialButton btnCancelar = view.findViewById(R.id.btnCancelar);
        MaterialButton btnAdicionar = view.findViewById(R.id.btnAdicionar);

        rgUnidade.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMl) {
                tilQtd.setHint("Quantidade (mL)");
                tilTamanhoCopo.setVisibility(View.GONE);
            } else {
                tilQtd.setHint("Quantidade (copos)");
                tilTamanhoCopo.setVisibility(View.VISIBLE);
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAdicionar.setOnClickListener(v -> {
            String qtdStr = etQtd.getText() != null ? etQtd.getText().toString().trim() : "";
            if (qtdStr.isEmpty()) {
                etQtd.setError("Informe a quantidade");
                return;
            }

            int mlAdicionados;
            if (rbMl.isChecked()) {
                mlAdicionados = Integer.parseInt(qtdStr);
            } else {
                String tamStr = etTamanhoCopo.getText() != null ? etTamanhoCopo.getText().toString().trim() : "250";
                if (tamStr.isEmpty()) tamStr = "250";
                int copos = Integer.parseInt(qtdStr);
                int tamCopoMl = Integer.parseInt(tamStr);
                mlAdicionados = copos * tamCopoMl;
            }

            int currentWater = sp.getInt("daily_water_ml", 0);
            int newTotalWater = currentWater + mlAdicionados;

            // --- MUDANÇA PRINCIPAL ---
            // Agora, salvamos o total E a data de hoje.
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("daily_water_ml", newTotalWater);
            editor.putString("last_water_save_date", getTodayDateString()); // Salva a data!
            editor.apply();
            // --- FIM DA MUDANÇA ---

            Toast.makeText(this, "Adicionado: " + mlAdicionados + " mL", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            atualizarResumoAgua();
        });

        dialog.show();
    }
}