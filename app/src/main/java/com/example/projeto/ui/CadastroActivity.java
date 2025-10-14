// ui/CadastroActivity.java
package com.example.projeto.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projeto.R;

public class CadastroActivity extends AppCompatActivity {

    private EditText editNome, editEmail, editSenha, editPeso;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editNome  = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        editPeso  = findViewById(R.id.editPeso);

        Button btnCadastrar = findViewById(R.id.btnCadastrar);
        btnCadastrar.setOnClickListener(v -> onCadastrar()); // alteração
    }

    private void onCadastrar() {                            // alteração
        String pesoStr = editPeso.getText().toString().trim();
        if (pesoStr.isEmpty()) {
            editPeso.setError("Informe seu peso");
            editPeso.requestFocus();
            return;
        }

        try {
            // aceita “70” ou “70.5/70,5”
            pesoStr = pesoStr.replace(',', '.');
            double kgDouble = Double.parseDouble(pesoStr);
            int kg = (int) Math.round(kgDouble);
            if (kg <= 0 || kg > 400) {
                editPeso.setError("Peso inválido");
                editPeso.requestFocus();
                return;
            }

            int metaML = kg * 40;
            getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .edit()
                    .putInt("weight_kg", kg)
                    .putInt("daily_goal_ml", metaML)
                    .commit();

            Intent i = new Intent(this, PrincipalActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();

        } catch (NumberFormatException e) {
            editPeso.setError("Use apenas números");
            editPeso.requestFocus();
        }
    }
}
