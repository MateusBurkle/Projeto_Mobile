package com.example.projeto;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.content.Intent;

public class PrincipalActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal);

        Button btnAdicionarTarefa = findViewById(R.id.btnAdicionarTarefa);

        btnAdicionarTarefa.setOnClickListener(v ->{
            Intent intent = new Intent(PrincipalActivity.this, TarefasActivity.class);
            startActivity(intent);
        });
    }
}
