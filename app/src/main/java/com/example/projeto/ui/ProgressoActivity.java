package com.example.projeto.ui;

import android.os.Bundle;
import android.widget.TextView; // alteração
import androidx.appcompat.app.AppCompatActivity;
import com.example.projeto.R;
import com.google.android.material.appbar.MaterialToolbar;

public class ProgressoActivity extends AppCompatActivity {

    private TextView tvResumoMeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_recursos); // alteração (seu layout)

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvResumoMeta = findViewById(R.id.txtMeta);

        int goal = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("daily_goal_ml", -1);
        if (goal <= 0) {
            int weight = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("weight_kg", 0);
            goal = (weight > 0) ? weight * 40 : 2000;
        }


        tvResumoMeta.setText("Meta: " + goal + " ml/dia");
    }
}

