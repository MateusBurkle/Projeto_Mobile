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
import androidx.core.graphics.Insets; // <--- NOVO: Import da classe correta

import com.example.projeto.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CadastroActivity extends AppCompatActivity {

    private TextInputLayout layoutNome, layoutEmail, layoutSenha, layoutPeso;
    private TextInputEditText editNome, editEmail, editSenha, editPeso;
    private MaterialButton btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        // --- CORREÇÃO DO CÓDIGO EDGE-TO-EDGE ---
        // Aplicamos o listener ao 'telaCadastro' (o LinearLayout dentro do ScrollView)
        // para que ele adicione padding interno e empurre o conteúdo para baixo.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.telaCadastro), (v, windowInsets) -> {
            // MUDANÇA: Usando 'androidx.core.graphics.Insets' para compatibilidade
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Aplicamos o padding no topo e na base do LinearLayout
            // Mantemos o padding lateral original (32dp) definido no XML
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);

            // Retornamos os insets originais para que outros componentes (como a IME)
            // ainda possam reagir a eles.
            return windowInsets;
        });
        // --- FIM DA CORREÇÃO ---


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
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                // Navega para a tela principal
                Intent intent = new Intent(CadastroActivity.this, PrincipalActivity.class);
                // Limpa as telas anteriores para que o usuário não possa "voltar" para o cadastro
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private boolean validarCampos() {
        boolean valido = true;

        if (TextUtils.isEmpty(editNome.getText())) {
            layoutNome.setError(getString(R.string.error_nome_obrigatorio));
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

        // Salva o peso como um inteiro (ou float, mas int é mais simples para o cálculo)
        try {
            int peso = Integer.parseInt(editPeso.getText().toString().trim());
            editor.putInt("weight_kg", peso);
        } catch (NumberFormatException e) {
            editor.putInt("weight_kg", 0); // Salva 0 se o valor for inválido
        }

        editor.apply();
    }
}