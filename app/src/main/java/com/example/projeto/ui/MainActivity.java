package com.example.projeto.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projeto.storage.SessionManager; // <-- IMPORT ADICIONADO

public class MainActivity extends AppCompatActivity {

    private SessionManager session; // <-- VARIÁVEL ADICIONADA

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- LÓGICA DO ONCREATE COMPLETAMENTE MODIFICADA ---

        session = new SessionManager(getApplicationContext());

        Intent i;
        if (session.isLoggedIn()) {
            // Se já está logado, vai para a tela principal
            i = new Intent(this, PrincipalActivity.class);
        } else {
            // Se não está logado, vai para a nova tela de Login
            i = new Intent(this, LoginActivity.class);
        }

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();

        // --- FIM DA MODIFICAÇÃO ---
    }
}