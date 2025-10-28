package com.example.projeto.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projeto.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class ProgressoActivity extends AppCompatActivity {

    private TextView tvResumoMeta;
    private TextView tvProgressoPorcentagem;
    private CircularProgressIndicator progressAgua;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_recursos);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Referências da Nova UI
        tvResumoMeta = findViewById(R.id.txtMeta);
        tvProgressoPorcentagem = findViewById(R.id.txtProgressoPorcentagem);
        progressAgua = findViewById(R.id.progressAgua);

        // Carrega os dados ao criar a tela
        carregarProgressoAgua();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega os dados caso o usuário volte de outra tela
        // (ex: acabou de adicionar água e voltou para o progresso)
        carregarProgressoAgua();
    }

    private void carregarProgressoAgua() {
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // 1. Pega a meta diária (calculada no login ou na tela de água)
        int goal = sp.getInt("daily_goal_ml", 2000); // Padrão de 2000ml

        // 2. Pega o consumo atual salvo
        int currentWater = sp.getInt("daily_water_ml", 0);

        // 3. Calcula a porcentagem
        int percentage = 0;
        if (goal > 0) {
            // Usamos float para garantir a precisão da divisão antes de converter para int
            percentage = (int) (((float) currentWater / goal) * 100);
        }

        // 4. Atualiza a UI
        String resumo = getString(R.string.water_summary_format, currentWater, goal);
        tvResumoMeta.setText(resumo);

        String textoPorcentagem = getString(R.string.water_percentage_format, percentage);
        tvProgressoPorcentagem.setText(textoPorcentagem);

        // Define o progresso no indicador circular
        // (limitamos a 100% para o gráfico não "virar")
        progressAgua.setProgress(Math.min(percentage, 100), true);
    }
}