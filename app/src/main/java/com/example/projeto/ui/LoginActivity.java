package com.example.projeto.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projeto.R;
import com.example.projeto.storage.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editSenha;
    private MaterialButton btnLogin, btnGoToRegister;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        // Botão de Login
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();

            if (validarLogin(email, senha)) {
                // Se o login for válido, crie a sessão
                session.createLoginSession(email);

                // Vá para a PrincipalActivity
                Intent i = new Intent(LoginActivity.this, PrincipalActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "E-mail ou senha inválidos", Toast.LENGTH_LONG).show();
            }
        });

        // Botão para ir para a tela de Cadastro (que você já tem)
        btnGoToRegister.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(i);
        });
    }

    private boolean validarLogin(String email, String senha) {
        if (email.isEmpty() || senha.isEmpty()) {
            return false;
        }

        // *** LÓGICA DE VALIDAÇÃO ***
        // Vamos checar o "user_prefs" que a CadastroActivity salva
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String savedEmail = userPrefs.getString("user_email", null);
        // Vamos ler a senha que a CadastroActivity vai salvar
        String savedPass = userPrefs.getString("user_pass", null);

        return email.equals(savedEmail) && senha.equals(savedPass);
    }
}