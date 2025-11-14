package com.example.projeto.storage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.projeto.ui.LoginActivity;
import com.example.projeto.ui.PrincipalActivity;

import java.util.HashMap;

public class SessionManager {

    // --- MUDANÇAS AQUI ---
    public SharedPreferences pref; // Tornar público
    public SharedPreferences.Editor editor; // Tornar público
    // --- FIM DA MUDANÇA ---

    Context _context;

    int PRIVATE_MODE = 0;

    // Nome do arquivo de SharedPreferences para a SESSÃO
    private static final String PREF_NAME = "ProjetoLoginSession";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    public static final String KEY_EMAIL = "email";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Cria a sessão de login
     */
    public void createLoginSession(String email) {
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);
        editor.commit(); // <-- Importante: 'commit()' aqui salva as mudanças acima
    }

    /**
     * Checa o status de login
     * Se for false, redireciona para a tela de Login
     */
    public void checkLogin() {
        if (!this.isLoggedIn()) {
            Intent i = new Intent(_context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
    }

    /**
     * Pega os dados do usuário logado
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        return user;
    }

    /**
     * Limpa os dados da sessão (Logout)
     */
    public void logoutUser() {
        editor.clear();
        editor.commit();

        // Após o logout, redireciona para a LoginActivity
        Intent i = new Intent(_context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        _context.startActivity(i);
    }

    /**
     * Checagem rápida de login
     **/
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGGED_IN, false);
    }
}