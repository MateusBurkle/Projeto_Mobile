package com.example.projeto.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.projeto.R;
import com.example.projeto.models.HistoricoAgua;
import com.example.projeto.storage.AppDatabase;
import com.example.projeto.storage.HistoricoAguaDao;
import com.example.projeto.storage.SessionManager;
import com.example.projeto.storage.TaskDao;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PrincipalActivity extends AppCompatActivity {

    private TextView tvResumoAgua;
    private TextView tvResumoTarefas;

    private HistoricoAguaDao historicoAguaDao;
    private TaskDao taskDao;
    private ExecutorService databaseExecutor;
    private int metaDiariaGlobal = 2000;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        databaseExecutor = Executors.newSingleThreadExecutor();
        historicoAguaDao = AppDatabase.getInstance(this).historicoAguaDao();
        taskDao = AppDatabase.getInstance(this).taskDao();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.telaPrincipal), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvResumoAgua = findViewById(R.id.tvResumoAgua);
        tvResumoTarefas = findViewById(R.id.tvResumoTarefas);

        // --- Botões ---
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

        // --- CÓDIGO NOVO: Iniciar o Agendamento das Notificações ---
        iniciarAgendamentoNotificacoes();
        pedirPermissaoNotificacao();
    }

    private void iniciarAgendamentoNotificacoes() {
        // Configura para rodar a cada 15 minutos (mínimo permitido pelo Android)
        PeriodicWorkRequest lembreteRequest =
                new PeriodicWorkRequest.Builder(LembreteAguaWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        // Enfileira o trabalho (KEEP garante que não vamos duplicar se já estiver agendado)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "LembreteAguaWork",
                ExistingPeriodicWorkPolicy.KEEP,
                lembreteRequest
        );
    }

    private void pedirPermissaoNotificacao() {
        // Pede permissão apenas se for Android 13 ou superior (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // --- Menu e Resumos (Mantidos iguais) ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            session.logoutUser();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarResumos();
    }

    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void atualizarResumos() {
        atualizarResumoAgua();
        atualizarResumoTarefas();
    }

    private void atualizarResumoAgua() {
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int weight = sp.getInt("weight_kg", 0);
        metaDiariaGlobal = (weight > 0) ? weight * 40 : 2000;

        String today = getTodayDateString();

        ListenableFuture<HistoricoAgua> future = historicoAguaDao.getByDate(today);

        future.addListener(() -> {
            try {
                HistoricoAgua historicoHoje = future.get();
                int currentWater = 0;

                if (historicoHoje != null) {
                    currentWater = historicoHoje.getTotalMl();
                }

                int finalCurrentWater = currentWater;
                runOnUiThread(() -> {
                    String resumoAgua = getString(R.string.water_summary_dashboard, finalCurrentWater, metaDiariaGlobal);
                    tvResumoAgua.setText(resumoAgua);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void atualizarResumoTarefas() {
        databaseExecutor.execute(() -> {
            int tarefasPendentes = taskDao.getPendingTasksCount();
            runOnUiThread(() -> {
                String resumoTarefas = getString(R.string.task_summary_dashboard, tarefasPendentes);
                tvResumoTarefas.setText(resumoTarefas);
            });
        });
    }

    // --- BottomSheet Água (Mantido igual) ---
    private void abrirBottomSheetAgua() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_add_agua, null);
        dialog.setContentView(view);

        TextView tvMeta = view.findViewById(R.id.tvMetaDiaria);
        tvMeta.setText("Meta diária: " + metaDiariaGlobal + " ml");

        RadioGroup rgUnidade = view.findViewById(R.id.rgUnidade);
        MaterialRadioButton rbMl = view.findViewById(R.id.rbMl);
        // MaterialRadioButton rbCopos = view.findViewById(R.id.rbCopos); // Não usado diretamente, mas presente no layout

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

            salvarConsumoNoBanco(mlAdicionados, coposAdicionados);

            Toast.makeText(this, "Adicionado: " + mlAdicionados + " mL", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void salvarConsumoNoBanco(int mlAdicionados, int coposAdicionados) {
        String today = getTodayDateString();

        databaseExecutor.execute(() -> {
            ListenableFuture<HistoricoAgua> future = historicoAguaDao.getByDate(today);
            try {
                HistoricoAgua historicoHoje = future.get();

                if (historicoHoje == null) {
                    HistoricoAgua novoHistorico = new HistoricoAgua(
                            today,
                            mlAdicionados,
                            coposAdicionados,
                            metaDiariaGlobal
                    );
                    historicoAguaDao.insert(novoHistorico);
                } else {
                    historicoHoje.setTotalMl(historicoHoje.getTotalMl() + mlAdicionados);
                    historicoHoje.setTotalCopos(historicoHoje.getTotalCopos() + coposAdicionados);
                    historicoHoje.setMetaMl(metaDiariaGlobal);
                    historicoAguaDao.update(historicoHoje);
                }

                runOnUiThread(() -> {
                    atualizarResumoAgua();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}