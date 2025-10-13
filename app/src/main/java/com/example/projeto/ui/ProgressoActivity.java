package com.example.projeto.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projeto.R;
import com.google.android.material.appbar.MaterialToolbar;


public class ProgressoActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recursos);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }
}
