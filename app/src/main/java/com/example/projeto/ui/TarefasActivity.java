package com.example.projeto.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projeto.R;
import com.example.projeto.adapters.SubTarefasAdapter;
import com.example.projeto.models.Task;
import com.example.projeto.storage.TaskStorage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TarefasActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private TextInputLayout tilTitulo, tilNotas;
    private TextInputEditText etTitulo, etNotas;
    private RecyclerView rvSubtarefas;

    private SubTarefasAdapter subtAdapter;
    private TaskStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarefas);

        storage = new TaskStorage(this);
        topAppBar = findViewById(R.id.topAppBar);
        tilTitulo = findViewById(R.id.tilTitulo);
        tilNotas = findViewById(R.id.tilNotas);
        etTitulo = findViewById(R.id.etTitulo);
        etNotas = findViewById(R.id.etNotas);
        rvSubtarefas = findViewById(R.id.rvSubtarefas);

        // Toolbar: voltar e menu
        topAppBar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed()
        );
        topAppBar.setOnMenuItemClickListener(this::onTopBarMenuClick);

        // RecyclerView de Subtarefas
        subtAdapter = new SubTarefasAdapter();
        rvSubtarefas.setLayoutManager(new LinearLayoutManager(this));
        rvSubtarefas.setAdapter(subtAdapter);

        // Botão "+ SUBTAREFA"
        findViewById(R.id.btnAdicionarSubtarefa).setOnClickListener(v ->
                subtAdapter.addNova()
        );
    }

    private boolean onTopBarMenuClick(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_salvar) {
            salvarTarefa();
            return true;
        }
        return false;
    }

    private void salvarTarefa() {
        String titulo = etTitulo.getText() == null ? "" : etTitulo.getText().toString().trim();
        String notas  = etNotas.getText()  == null ? "" : etNotas.getText().toString().trim();

        boolean hasError = false;
        if (titulo.isEmpty()) {
            tilTitulo.setError("Título obrigatório");
            hasError = true;
        } else {
            tilTitulo.setError(null);
        }
        // notas é opcional
        tilNotas.setError(null);

        if (hasError) return;

        Task t = new Task(titulo, notas);
        t.setSubtarefas(subtAdapter.getSubtarefasLimpa());

        storage.add(t);

        Toast.makeText(this, "Tarefa salva!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
