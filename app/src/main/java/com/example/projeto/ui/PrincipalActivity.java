package com.example.projeto.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Import necessário
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projeto.R;
import com.example.projeto.models.HistoricoAgua; // Import do novo Model
import com.example.projeto.models.Task;
import com.example.projeto.storage.AppDatabase; // Import do novo DB
import com.example.projeto.storage.HistoricoAguaDao; // Import do novo DAO
import com.example.projeto.storage.TaskStorage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.util.concurrent.ListenableFuture; // Import necessário

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.concurrent.ExecutorService; // Import necessário
import java.util.concurrent.Executors; // Import necessário

// NOVO: Imports corretos para Edge-to-Edge
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets; // <--- Esta é a correção

public class PrincipalActivity extends AppCompatActivity {

    private TextView tvResumoAgua;
    private TextView tvResumoTarefas;
    private TaskStorage taskStorage;

    // --- MUDANÇA: Variáveis do Banco de Dados ---
    private HistoricoAguaDao historicoAguaDao;
    private ExecutorService databaseExecutor;
    private int metaDiariaGlobal = 2000; // Valor padrão
    // --- FIM DA MUDANÇA ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        // --- MUDANÇA: Configura o Executor para o DB ---
        // Cria um pool de threads para rodar as queries do banco
        databaseExecutor = Executors.newSingleThreadExecutor();
        // Pega a instância do DAO
        historicoAguaDao = AppDatabase.getInstance(this).historicoAguaDao();
        // --- FIM DA MUDANÇA ---

        // --- CORREÇÃO DO CÓDIGO EDGE-TO-EDGE ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.telaPrincipal), (v, windowInsets) -> {
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
        // Removemos o 'checkAndResetDailyProgress' - o DB cuida disso.
        atualizarResumos();
    }

    // --- MÉTODOS DE LÓGICA DO DASHBOARD ---

    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    // --- MUDANÇA: A FUNÇÃO 'checkAndResetDailyProgress' FOI REMOVIDA ---
    // (A lógica de reset agora é implícita ao salvar por data)

    private void atualizarResumos() {
        atualizarResumoAgua();
        atualizarResumoTarefas();
    }

    // --- MUDANÇA GRANDE: ATUALIZAR RESUMO DE ÁGUA LENDO DO DB ---
    private void atualizarResumoAgua() {
        // Pega a meta com base no peso (ainda salvo no SharedPreferences)
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int weight = sp.getInt("weight_kg", 0);
        metaDiariaGlobal = (weight > 0) ? weight * 40 : 2000;

        String today = getTodayDateString();

        // 1. Pega os dados de HOJE do banco (em uma thread de background)
        ListenableFuture<HistoricoAgua> future = historicoAguaDao.getByDate(today);

        // 2. Define o que fazer quando o dado chegar (listener)
        future.addListener(() -> {
            try {
                HistoricoAgua historicoHoje = future.get();
                int currentWater = 0;

                if (historicoHoje != null) {
                    // Se já existe um registro para hoje, pega o total salvo
                    currentWater = historicoHoje.getTotalMl();
                }

                // 3. Atualiza a UI (na thread principal)
                int finalCurrentWater = currentWater;
                runOnUiThread(() -> {
                    String resumoAgua = getString(R.string.water_summary_dashboard, finalCurrentWater, metaDiariaGlobal);
                    tvResumoAgua.setText(resumoAgua);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this)); // Executa o listener na thread principal
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


    // --- MUDANÇA GRANDE: SALVAR ÁGUA NO DB ---
    private void abrirBottomSheetAgua() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_add_agua, null);
        dialog.setContentView(view);

        TextView tvMeta = view.findViewById(R.id.tvMetaDiaria);
        tvMeta.setText("Meta diária: " + metaDiariaGlobal + " ml");

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

            int coposAdicionados = 0;
            int mlAdicionados;

            if (rbMl.isChecked()) {
                mlAdicionados = Integer.parseInt(qtdStr);
            } else {
                String tamStr = etTamanhoCopo.getText() != null ? etTamanhoCopo.getText().toString().trim() : "250";
                if (tamStr.isEmpty()) tamStr = "250";
                int copos = Integer.parseInt(qtdStr);
                int tamCopoMl = Integer.parseInt(tamStr);
                mlAdicionados = copos * tamCopoMl;
                coposAdicionados = copos;
            }

            // --- INÍCIO DA LÓGICA DE SALVAR NO DB ---
            salvarConsumoNoBanco(mlAdicionados, coposAdicionados);
            // --- FIM DA LÓGICA DE SALVAR NO DB ---

            Toast.makeText(this, "Adicionado: " + mlAdicionados + " mL", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void salvarConsumoNoBanco(int mlAdicionados, int coposAdicionados) {
        String today = getTodayDateString();

        // Roda a lógica de salvar/atualizar em uma thread de background
        databaseExecutor.execute(() -> {
            // 1. Tenta buscar o registro de hoje
            ListenableFuture<HistoricoAgua> future = historicoAguaDao.getByDate(today);
            try {
                HistoricoAgua historicoHoje = future.get(); // Espera síncrona (OK, estamos na thread de BG)

                if (historicoHoje == null) {
                    // 2. Se não existe, cria um NOVO registro para hoje
                    HistoricoAgua novoHistorico = new HistoricoAgua(
                            today,
                            mlAdicionados,
                            coposAdicionados,
                            metaDiariaGlobal
                    );
                    historicoAguaDao.insert(novoHistorico);
                } else {
                    // 3. Se já existe, atualiza o registro
                    historicoHoje.setTotalMl(historicoHoje.getTotalMl() + mlAdicionados);
                    historicoHoje.setTotalCopos(historicoHoje.getTotalCopos() + coposAdicionados);
                    historicoHoje.setMetaMl(metaDiariaGlobal); // Garante que a meta está atualizada
                    historicoAguaDao.update(historicoHoje);
                }

                // 4. Após salvar, atualiza a UI (na thread principal)
                runOnUiThread(() -> {
                    atualizarResumoAgua();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}