package com.example.projeto.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.projeto.R;
import com.example.projeto.storage.SessionManager; // <-- IMPORT ADICIONADO
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CadastroActivity extends AppCompatActivity {

    private TextInputLayout layoutNome, layoutEmail, layoutSenha, layoutPeso;
    private TextInputEditText editNome, editEmail, editSenha, editPeso;
    private MaterialButton btnCadastrar;

    private SessionManager session; // <-- VARIÁVEL ADICIONADA

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.telaCadastro), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return windowInsets;
        });

        session = new SessionManager(getApplicationContext()); // <-- LINHA ADICIONADA

        layoutNome = findViewById(R.id.layoutNome);
        editNome = findViewById(R.id.editNome);

        layoutEmail = findViewById(R.id.layoutEmail);
        editEmail = findViewById(R.id.editEmail);

        layoutSenha = findViewById(R.id.layoutSenha);
        editSenha = findViewById(R.id.editSenha);

        layoutPeso = findViewById(R.id.layoutPeso);
        editPeso = findViewById(R.id.editPeso);

        btnCadastrar = findViewById(R.id.btnCadastrar);

        btnCadastrar.setOnClickListener(v -> {
            if (validarCampos()) {
                salvarDados();

                // --- MUDANÇA ADICIONADA ---
                // Loga o usuário automaticamente após o cadastro
                String email = editEmail.getText().toString().trim();
                session.createLoginSession(email);
                // --- FIM DA MUDANÇA ---

                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                // Navega para a tela principal
                Intent intent = new Intent(CadastroActivity.this, PrincipalActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private boolean validarCampos() {
        boolean valido = true;

        if (TextUtils.isEmpty(editNome.getText())) {
            // --- LINHA CORRIGIDA ---
            layoutNome.setError(getString(R.string.error_nome_obrigatorio));
            // --- FIM DA CORREÇÃO ---
            valido = false;
        } else {
            layoutNome.setError(null);
        }

        if (TextUtils.isEmpty(editEmail.getText())) {
            layoutEmail.setError(getString(R.string.error_email_obrigatorio));
            valido = false;
        } else {
            layoutEmail.setError(null);
        }

        if (TextUtils.isEmpty(editSenha.getText())) {
            layoutSenha.setError(getString(R.string.error_senha_obrigatoria));
            valido = false;
        } else {
            layoutSenha.setError(null);
        }

        if (TextUtils.isEmpty(editPeso.getText())) {
            layoutPeso.setError(getString(R.string.error_peso_obrigatorio));
            valido = false;
        } else {
            layoutPeso.setError(null);
        }

        return valido;
    }

    private void salvarDados() {
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("user_name", editNome.getText().toString().trim());
        editor.putString("user_email", editEmail.getText().toString().trim());

        // --- MUDANÇA ADICIONADA ---
        // ⚠️ AVISO: Salvar senhas assim é muito inseguro.
        // Para um projeto de faculdade/estudo está OK para fazer o login funcionar.
        // Em um app real, use um hash (SHA-256) e salve em banco.
        editor.putString("user_pass", editSenha.getText().toString().trim());
        // --- FIM DA MUDANÇA ---


        // Salva o peso como um inteiro
        try {
            int peso = Integer.parseInt(editPeso.getText().toString().trim());
            editor.putInt("weight_kg", peso);
        } catch (NumberFormatException e) {
            editor.putInt("weight_kg", 0);
        }

        editor.apply();
    }
}