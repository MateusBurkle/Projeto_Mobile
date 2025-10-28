package com.example.projeto.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projeto.R;
import com.example.projeto.adapters.SubTarefasAdapter;
import com.example.projeto.models.SubTask;
import com.example.projeto.models.Task;
import com.example.projeto.storage.TaskStorage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class TarefasActivity extends AppCompatActivity {

    // Storage
    private TaskStorage taskStorage;

    // Views
    private MaterialToolbar toolbar;
    private TextInputLayout tilTitulo;
    private TextInputEditText etTitulo;
    private TextInputEditText etNotas;
    private RecyclerView rvSubtarefas;
    private TextInputEditText etNovaSubtarefa;
    private ImageButton btnAddSubtarefa;

    // Lista de Subtarefas
    private SubTarefasAdapter subTarefasAdapter;
    private List<SubTask> subtasksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarefas);

        // 1. Encontrar todas as Views
        toolbar = findViewById(R.id.toolbar);
        tilTitulo = findViewById(R.id.tilTitulo);
        etTitulo = findViewById(R.id.etTitulo);
        etNotas = findViewById(R.id.etNotas);
        rvSubtarefas = findViewById(R.id.rvSubtarefas);
        etNovaSubtarefa = findViewById(R.id.etNovaSubtarefa);
        btnAddSubtarefa = findViewById(R.id.btnAddSubtarefa);

        // 2. Configurar a Toolbar
        setupToolbar();

        // 3. Configurar Storage, Adapter e RecyclerView
        setupStorageAndAdapter();

        // 4. Configurar o clique do botão "Adicionar Subtarefa"
        setupClickListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Lidar com o clique no botão "voltar"
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupStorageAndAdapter() {
        taskStorage = new TaskStorage(this);
        subtasksList = new ArrayList<>(); // Inicializa a lista de subtarefas

        subTarefasAdapter = new SubTarefasAdapter(subtasksList);

        // Configura o que acontece quando o usuário clica em "X" (deletar) em uma subtarefa
        subTarefasAdapter.setOnDeleteClick(position -> {
            subtasksList.remove(position);
            subTarefasAdapter.notifyItemRemoved(position);
            // Notifica o adapter para re-calcular as posições
            subTarefasAdapter.notifyItemRangeChanged(position, subtasksList.size());
        });

        // Configura o que acontece quando o usuário marca/desmarca a checkbox da subtarefa
        subTarefasAdapter.setOnToggleClick((subTask, isChecked) -> {
            subTask.setConcluida(isChecked);
            // A lista já tem a referência, então o objeto é atualizado
        });

        rvSubtarefas.setLayoutManager(new LinearLayoutManager(this));
        rvSubtarefas.setAdapter(subTarefasAdapter);
    }

    private void setupClickListeners() {
        btnAddSubtarefa.setOnClickListener(v -> {
            String subtaskTitle = etNovaSubtarefa.getText() != null ? etNovaSubtarefa.getText().toString().trim() : "";
            if (!subtaskTitle.isEmpty()) {
                // Cria a nova subtarefa
                SubTask newSubTask = new SubTask(subtaskTitle);

                // Adiciona na lista
                subtasksList.add(newSubTask);

                // Notifica o adapter que um novo item foi inserido no final
                subTarefasAdapter.notifyItemInserted(subtasksList.size() - 1);

                // Limpa o campo de texto
                etNovaSubtarefa.setText("");
            }
        });
    }

    // --- Lógica do Menu (Salvar) ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla o menu 'menu_tarefa.xml' (com o botão de salvar)
        getMenuInflater().inflate(R.menu.menu_tarefa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Verifica se o item clicado é o 'action_salvar'
        if (item.getItemId() == R.id.action_salvar) {
            salvarTarefa();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void salvarTarefa() {
        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim() : "";
        String notas = etNotas.getText() != null ? etNotas.getText().toString().trim() : "";

        // 1. Validação: Verifica se o título está vazio
        if (titulo.isEmpty()) {
            tilTitulo.setError("O título é obrigatório");
            return;
        } else {
            tilTitulo.setError(null); // Limpa o erro se estiver preenchido
        }

        // 2. Criar o objeto Task
        Task novaTarefa = new Task(titulo, notas);

        // 3. Adicionar a lista de subtarefas que criamos
        novaTarefa.setSubtarefas(subtasksList);

        // 4. Salvar a tarefa no Storage
        // O TaskStorage.java salva a lista inteira, então precisamos
        // buscar a lista atual, adicionar a nova tarefa e salvar tudo.
        List<Task> allTasks = taskStorage.getAll();
        if (allTasks == null) {
            allTasks = new ArrayList<>();
        }
        allTasks.add(novaTarefa);
        taskStorage.saveAll(allTasks);

        // 5. Mostrar feedback e fechar a tela
        Toast.makeText(this, "Tarefa salva com sucesso!", Toast.LENGTH_SHORT).show();
        finish(); // Fecha a TarefasActivity e volta para a tela anterior
    }
}