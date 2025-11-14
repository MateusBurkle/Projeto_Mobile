package com.example.projeto.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projeto.R;
import com.example.projeto.models.User; // Importar User
import com.example.projeto.storage.AppDatabase; // Importar AppDatabase
import com.example.projeto.storage.SessionManager;
import com.example.projeto.utils.SecurityUtils; // Importar o Hashing
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors; // Importar Executors

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editSenha;
    private MaterialButton btnLogin, btnGoToRegister;
    private SessionManager session;
    private AppDatabase db; // Variável do Banco

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(getApplicationContext());
        db = AppDatabase.getInstance(getApplicationContext()); // Iniciar o DB

        // --- CHECAGEM DE SESSÃO ---
        // Se o usuário já estiver logado, vá direto para a PrincipalActivity
        // Isso deve vir ANTES de setContentView() para economizar recursos
        if (session.isLoggedIn()) {
            Intent i = new Intent(LoginActivity.this, PrincipalActivity.class);
            startActivity(i);
            finish();
            return; // Impede o resto do onCreate de rodar
        }

        // Se não está logado, infla o layout
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        // Botão de Login
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
                return;
            }

            // Vamos validar o login no banco
            validarLoginNoBanco(email, senha);
        });

        // Botão para ir para a tela de Cadastro
        btnGoToRegister.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(i);
        });
    }

    /**
     * Valida o login no banco de dados (em background) e age
     * conforme o resultado (na UI thread).
     */
    private void validarLoginNoBanco(String email, String senha) {
        // (Opcional) Mostrar um ProgressBar "Carregando..."
        btnLogin.setEnabled(false); // Desabilita o botão

        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Tenta buscar o usuário no banco SOMENTE PELO E-MAIL
            User user = db.userDao().findByEmail(email);

            // 2. Gera o hash da senha digitada
            String senhaHash = SecurityUtils.sha256(senha);

            // 3. Volta para a Thread Principal para checar
            runOnUiThread(() -> {
                // (Opcional) Esconder o ProgressBar
                btnLogin.setEnabled(true); // Reabilita o botão

                // 4. Compara o usuário e o hash da senha
                if (user != null && user.getSenha().equals(senhaHash)) {
                    // --- SUCESSO! ---
                    // Crie a sessão
                    session.createLoginSession(user.getEmail());

                    // Salva dados úteis na sessão para acesso rápido
                    session.editor.putInt("weight_kg", user.getPeso());
                    session.editor.putString("user_name", user.getNome());
                    session.editor.commit();

                    // Vá para a PrincipalActivity
                    Intent i = new Intent(LoginActivity.this, PrincipalActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                } else {
                    // --- FALHA! ---
                    Toast.makeText(LoginActivity.this, "E-mail ou senha inválidos", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // O método validarLogin() antigo com SharedPreferences não é mais necessário
    // private boolean validarLogin(String email, String senha) { ... }
}