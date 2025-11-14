package com.example.projeto.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns; // Import para validar e-mail
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.projeto.R;
import com.example.projeto.models.User; // Importar User
import com.example.projeto.storage.AppDatabase; // Importar AppDatabase
import com.example.projeto.storage.SessionManager;
import com.example.projeto.utils.SecurityUtils; // Importar o Hashing
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executors; // Importar Executors

public class CadastroActivity extends AppCompatActivity {

    private TextInputLayout layoutNome, layoutEmail, layoutSenha, layoutPeso;
    private TextInputEditText editNome, editEmail, editSenha, editPeso;
    private MaterialButton btnCadastrar;

    private SessionManager session;
    private AppDatabase db; // Variável do Banco

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.telaCadastro), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return windowInsets;
        });

        session = new SessionManager(getApplicationContext());
        db = AppDatabase.getInstance(getApplicationContext()); // Iniciar o DB

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
                // O método salvarDados agora vai rodar em background
                salvarDadosEFinalizar();
            }
        });
    }

    private boolean validarCampos() {
        boolean valido = true;
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();
        String peso = editPeso.getText().toString().trim();

        if (TextUtils.isEmpty(nome)) {
            layoutNome.setError(getString(R.string.error_nome_obrigatorio));
            valido = false;
        } else {
            layoutNome.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError(getString(R.string.error_email_obrigatorio));
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // *** CORREÇÃO AQUI ***
            layoutEmail.setError(getString(R.string.error_email_invalido));
            valido = false;
        } else {
            layoutEmail.setError(null);
        }

        if (TextUtils.isEmpty(senha)) {
            // *** LINHA DO SEU ERRO ***
            layoutSenha.setError(getString(R.string.error_senha_obrigatoria));
            valido = false;
        } else if (senha.length() < 6) {
            // *** CORREÇÃO AQUI ***
            layoutSenha.setError(getString(R.string.error_senha_curta));
            valido = false;
        } else {
            layoutSenha.setError(null);
        }

        if (TextUtils.isEmpty(peso)) {
            layoutPeso.setError(getString(R.string.error_peso_obrigatorio));
            valido = false;
        } else {
            try {
                Integer.parseInt(peso);
                layoutPeso.setError(null);
            } catch (NumberFormatException e) {
                // *** CORREÇÃO AQUI ***
                layoutPeso.setError(getString(R.string.error_peso_invalido));
                valido = false;
            }
        }
        return valido;
    }

    /**
     * Pega os dados da UI, salva no banco (em background) e
     * finaliza o cadastro (na UI thread).
     */
    private void salvarDadosEFinalizar() {
        // (Opcional) Mostrar um ProgressBar "Carregando..."
        btnCadastrar.setEnabled(false); // Desabilita o botão para evitar clique duplo

        // Pegar os dados da UI (ainda na thread principal)
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();
        int peso = Integer.parseInt(editPeso.getText().toString().trim());

        // Criar o objeto User
        User user = new User();
        user.setNome(nome);
        user.setEmail(email);
        user.setSenha(SecurityUtils.sha256(senha)); // Salva o HASH da senha
        user.setPeso(peso);

        // --- Executar a inserção em uma thread de Background ---
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Tenta inserir o usuário
                db.userDao().insert(user);

                // Se chegou aqui, a inserção deu certo!
                // Agora, voltamos para a Thread Principal para atualizar a UI
                runOnUiThread(() -> {
                    // Loga o usuário automaticamente
                    session.createLoginSession(user.getEmail());

                    // Salva dados úteis na sessão para acesso rápido
                    session.editor.putInt("weight_kg", user.getPeso());
                    session.editor.putString("user_name", user.getNome());
                    session.editor.commit();

                    Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                    // Navega para a tela principal
                    Intent intent = new Intent(CadastroActivity.this, PrincipalActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Fecha a CadastroActivity
                });

            } catch (Exception e) {
                // Se deu erro (provavelmente e-mail duplicado)
                runOnUiThread(() -> {
                    // (Opcional) Esconder o ProgressBar
                    btnCadastrar.setEnabled(true); // Reabilita o botão
                    layoutEmail.setError("Este e-mail já está cadastrado.");
                    Toast.makeText(CadastroActivity.this, "Este e-mail já está cadastrado.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}