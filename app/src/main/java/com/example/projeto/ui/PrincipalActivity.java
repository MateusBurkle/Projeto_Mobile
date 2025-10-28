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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;

// NOVO: Imports corretos para Edge-to-Edge
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets; // <--- Esta é a correção

public class PrincipalActivity extends AppCompatActivity {

    private TextView tvResumoAgua;
    private TextView tvResumoTarefas;
    private TaskStorage taskStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        // --- CORREÇÃO DO CÓDIGO EDGE-TO-EDGE ---
        // Este bloco lida com o padding das barras de sistema (status e navegação)
        // para que o layout não fique por baixo delas.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.telaPrincipal), (v, windowInsets) -> {
            // MUDANÇA: Usando 'androidx.core.graphics.Insets' para compatibilidade
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });
        // --- FIM DA CORREÇÃO ---


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
        checkAndResetDailyProgress();
        atualizarResumos();
    }

    // --- MÉTODOS DE LÓGICA DO DASHBOARD ---

    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void checkAndResetDailyProgress() {
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String today = getTodayDateString();
        String lastSaveDate = sp.getString("last_water_save_date", "");

        if (!today.equals(lastSaveDate)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("daily_water_ml", 0);
            editor.putString("last_water_save_date", today);
            editor.apply();
        }
    }

    private void atualizarResumos() {
        atualizarResumoAgua();
        atualizarResumoTarefas();
    }

    // 1. SUBSTITUA A SUA FUNÇÃO ANTIGA POR ESTA
    private void atualizarResumoAgua() {
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // --- INÍCIO DA CORREÇÃO ---
        // 1. Ler o peso salvo
        int weight = sp.getInt("weight_kg", 0);

        // 2. Calcular a meta (peso * 40)
        int goal = (weight > 0) ? weight * 40 : 2000;

        // 3. Salvar a nova meta (para que o resto do app veja)
        sp.edit().putInt("daily_goal_ml", goal).apply();
        // --- FIM DA CORREÇÃO ---

        // 4. Ler o consumo atual e mostrar na tela
        int currentWater = sp.getInt("daily_water_ml", 0);
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


    // 2. SUBSTITUA TAMBÉM ESTA FUNÇÃO
    private void abrirBottomSheetAgua() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_add_agua, null);
        dialog.setContentView(view);

        TextView tvMeta = view.findViewById(R.id.tvMetaDiaria);

        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // --- INÍCIO DA CORREÇÃO ---
        // Apenas lê a meta que já foi calculada e salva pela função "atualizarResumoAgua"
        int goal = sp.getInt("daily_goal_ml", 2000);
        tvMeta.setText("Meta diária: " + goal + " ml");
        // --- FIM DA CORREÇÃO ---


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

            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("daily_water_ml", newTotalWater);
            editor.putString("last_water_save_date", getTodayDateString());
            editor.apply();

            Toast.makeText(this, "Adicionado: " + mlAdicionados + " mL", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            atualizarResumoAgua();
        });

        dialog.show();
    }
}