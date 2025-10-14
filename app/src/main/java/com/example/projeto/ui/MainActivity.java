package com.example.projeto.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int weight = sp.getInt("weight_kg", 0);

        Intent i;
        if (weight > 0) {
            i = new Intent(this, PrincipalActivity.class);
        } else {
            i = new Intent(this, CadastroActivity.class);
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
