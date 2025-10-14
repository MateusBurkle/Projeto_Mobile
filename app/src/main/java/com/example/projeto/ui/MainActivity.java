package com.example.projeto.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;

import com.example.projeto.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.telaCadastro), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextInputLayout layoutNome = findViewById(R.id.layoutNome);
        TextInputLayout layoutEmail = findViewById(R.id.layoutEmail);
        TextInputLayout layoutSenha = findViewById(R.id.layoutSenha);
        TextInputLayout layoutPeso = findViewById(R.id.layoutPeso );

        TextInputEditText editNome = findViewById(R.id.editNome);
        TextInputEditText editEmail = findViewById(R.id.editEmail);
        TextInputEditText editSenha = findViewById(R.id.editSenha);
        TextInputEditText editPeso = findViewById(R.id.editPeso);

        Button btnCadastrar = findViewById(R.id.btnCadastrar);
        btnCadastrar.setOnClickListener(v -> {
            boolean hasErrors = false;
            String nome = editNome.getText() != null ? editNome.getText().toString().trim() : "";
            if (TextUtils.isEmpty(nome)) {
                layoutNome.setError(getString(R.string.error_nome_obrigatorio));
                hasErrors = true;
            } else {
                layoutNome.setError(null);
            }

            String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";
            if (TextUtils.isEmpty(email)) {
                layoutEmail.setError(getString(R.string.error_email_obrigatorio));
                hasErrors = true;
            } else {
                layoutEmail.setError(null);
            }

            String senha = editSenha.getText() != null ? editSenha.getText().toString().trim() : "";
            if (TextUtils.isEmpty(senha)) {
                layoutSenha.setError(getString(R.string.error_senha_obrigatoria));
                hasErrors = true;
            } else {
                layoutSenha.setError(null);
            }

            String peso = editPeso.getText() != null ? editPeso.getText().toString().trim() : "";
            if (TextUtils.isEmpty(peso)) {
                layoutPeso.setError(getString(R.string.error_peso_obrigatorio));
                hasErrors = true;
            } else {
                layoutPeso.setError(null);
            }

            if (!hasErrors) {
                Intent intent = new Intent(MainActivity.this, PrincipalActivity.class);
                startActivity(intent);
            }
        });


    }
}