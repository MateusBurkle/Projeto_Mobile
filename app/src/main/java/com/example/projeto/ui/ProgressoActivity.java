package com.example.projeto.ui;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.projeto.R;
import com.example.projeto.models.HistoricoAgua;
import com.example.projeto.storage.AppDatabase;
import com.example.projeto.storage.HistoricoAguaDao;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressoActivity extends AppCompatActivity {

    private TextView tvResumoMeta;
    private TextView tvProgressoPorcentagem;
    private TextView tvProgressoCopos;
    private CircularProgressIndicator progressAgua;

    // --- MUDANÇA: Variáveis do Gráfico e DB ---
    private BarChart barChart;
    private HistoricoAguaDao historicoAguaDao;
    // --- FIM DA MUDANÇA ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_recursos);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- MUDANÇA: Pega a instância do DAO ---
        historicoAguaDao = AppDatabase.getInstance(this).historicoAguaDao();
        // --- FIM DA MUDANÇA ---

        // Referências da Nova UI
        tvResumoMeta = findViewById(R.id.txtMeta);
        tvProgressoPorcentagem = findViewById(R.id.txtProgressoPorcentagem);
        tvProgressoCopos = findViewById(R.id.txtProgressoCopos);
        progressAgua = findViewById(R.id.progressAgua);

        // --- MUDANÇA: Referência do Gráfico ---
        barChart = findViewById(R.id.barChartHistorico);
        // --- FIM DA MUDANÇA ---
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Carrega tanto o progresso de HOJE quanto o gráfico de 7 dias
        carregarProgressoDia();
        carregarHistoricoGrafico();
    }

    // --- MUDANÇA: Função Renomeada (só cuida do dia atual) ---
    private void carregarProgressoDia() {
        // Pega a meta com base no peso (ainda salvo no SharedPreferences)
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int weight = sp.getInt("weight_kg", 0);
        int goal = (weight > 0) ? weight * 40 : 2000;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 1. Pega os dados de HOJE do banco
        ListenableFuture<HistoricoAgua> future = historicoAguaDao.getByDate(today);
        future.addListener(() -> {
            try {
                HistoricoAgua historicoHoje = future.get();

                int currentWater = 0;
                int currentCups = 0;
                if (historicoHoje != null) {
                    currentWater = historicoHoje.getTotalMl();
                    currentCups = historicoHoje.getTotalCopos();
                }

                // 2. Calcula a porcentagem
                int percentage = 0;
                if (goal > 0) {
                    percentage = (int) (((float) currentWater / goal) * 100);
                }

                // 3. Atualiza a UI (Círculo e Textos)
                String resumo = getString(R.string.water_summary_format, currentWater, goal);
                tvResumoMeta.setText(resumo);

                String textoPorcentagem = getString(R.string.water_percentage_format, percentage);
                tvProgressoPorcentagem.setText(textoPorcentagem);

                if (currentCups > 0) {
                    tvProgressoCopos.setText("(Total de " + currentCups + " copos)");
                    tvProgressoCopos.setVisibility(View.VISIBLE);
                } else {
                    tvProgressoCopos.setVisibility(View.GONE);
                }

                progressAgua.setProgress(Math.min(percentage, 100), true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // --- MUDANÇA: Nova função para carregar o gráfico de barras ---
    private void carregarHistoricoGrafico() {
        ListenableFuture<List<HistoricoAgua>> future = historicoAguaDao.getUltimosSeteDias();

        future.addListener(() -> {
            try {
                List<HistoricoAgua> historico = future.get();
                if (historico == null || historico.isEmpty()) {
                    barChart.setVisibility(View.GONE); // Esconde o gráfico se não houver dados
                    return;
                }

                barChart.setVisibility(View.VISIBLE);
                // O Room retorna os dados de Z-A (hoje primeiro), invertemos para A-Z
                Collections.reverse(historico);

                setupBarChart(historico);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void setupBarChart(List<HistoricoAgua> historico) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // Formato para mostrar o dia (ex: 29/10)
        SimpleDateFormat labelFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 1. Converte os dados do DB para 'BarEntry'
        for (int i = 0; i < historico.size(); i++) {
            HistoricoAgua dia = historico.get(i);
            // (float x, float y) -> (posição no eixo X, altura da barra)
            entries.add(new BarEntry(i, dia.getTotalMl()));

            // Adiciona o label (data)
            try {
                Date date = parseFormat.parse(dia.getData());
                labels.add(labelFormat.format(date));
            } catch (Exception e) {
                labels.add(dia.getData()); // fallback
            }
        }

        // 2. Configura o DataSet (a aparência das barras)
        BarDataSet dataSet = new BarDataSet(entries, "Consumo de Água (mL)");
        dataSet.setColor(ContextCompat.getColor(this, R.color.md_theme_light_primary)); // Cor das barras
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f); // Largura da barra

        // 3. Configura o Gráfico (a aparência geral)
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false); // Remove a descrição
        barChart.getLegend().setEnabled(false); // Remove a legenda
        barChart.setFitBars(true); // Encaixa as barras
        barChart.setDrawGridBackground(false);
        barChart.getAxisRight().setEnabled(false); // Remove eixo Y da direita
        barChart.getAxisLeft().setAxisMinimum(0f); // Eixo Y começa em 0
        barChart.getAxisLeft().setTextColor(Color.DKGRAY);
        barChart.getAxisLeft().setDrawGridLines(false);

        // 4. Configura o Eixo X (os labels de data)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setGranularity(1f); // Garante 1 label por barra
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });

        // 5. Anima e atualiza o gráfico
        barChart.animateY(1000);
        barChart.invalidate();
    }
}