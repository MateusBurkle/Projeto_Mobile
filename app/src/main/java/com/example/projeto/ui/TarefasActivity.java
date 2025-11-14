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
import com.example.projeto.storage.AppDatabase;
import com.example.projeto.storage.TaskDao;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TarefasActivity extends AppCompatActivity {

    private TextInputEditText editNomeTarefa;
    private TextInputEditText editNotas; // Adicionado para o novo XML
    private TextInputEditText editNovaSubtarefa; // Adicionado para o novo XML
    private ImageButton btnAddSubtarefa; // Adicionado para o novo XML
    private RecyclerView rvSubtarefas;
    private SubTarefasAdapter adapter;

    private TaskDao taskDao;
    private ExecutorService databaseExecutor;
    private Task currentTask;
    private boolean isEditMode = false;
    private long taskId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarefas);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // O título da toolbar é pego do XML agora

        databaseExecutor = Executors.newSingleThreadExecutor();
        taskDao = AppDatabase.getInstance(this).taskDao();

        // --- MUDANÇA DA CORREÇÃO AQUI ---
        editNomeTarefa = findViewById(R.id.etTitulo); // ID corrigido (era editNomeTarefa)
        editNotas = findViewById(R.id.etNotas); // ID do novo XML
        rvSubtarefas = findViewById(R.id.rvSubtarefas);
        editNovaSubtarefa = findViewById(R.id.etNovaSubtarefa); // ID do novo XML
        btnAddSubtarefa = findViewById(R.id.btnAddSubtarefa); // ID do novo XML
        // --- FIM DA CORREÇÃO ---

        // --- LÓGICA ANTIGA REMOVIDA (FAB e Dialog) ---
        // FloatingActionButton fabAddSubtarefa = findViewById(R.id.fabAddSubtarefa);
        // fabAddSubtarefa.setOnClickListener(v -> showAddSubtaskDialog());
        // --- FIM DA LÓGICA ANTIGA ---

        // --- NOVA LÓGICA DE ADICIONAR SUBTAREFA (Inline) ---
        btnAddSubtarefa.setOnClickListener(v -> {
            String nomeSubtarefa = editNovaSubtarefa.getText().toString().trim();
            if (!nomeSubtarefa.isEmpty()) {
                currentTask.addSubtask(new SubTask(nomeSubtarefa));
                adapter.notifyItemInserted(currentTask.getSubtasks().size() - 1);
                editNovaSubtarefa.setText(""); // Limpa o campo
            }
        });
        // --- FIM DA NOVA LÓGICA ---

        taskId = getIntent().getLongExtra("TASK_ID", -1L);
        isEditMode = (taskId != -1L);

        if (isEditMode) {
            getSupportActionBar().setTitle("Editar Tarefa");
            loadTaskData();
        } else {
            getSupportActionBar().setTitle("Criar Tarefa");
            currentTask = new Task("");
            setupRecyclerView();
        }
    }

    private void loadTaskData() {
        databaseExecutor.execute(() -> {
            currentTask = taskDao.getTaskById(taskId);

            runOnUiThread(() -> {
                if (currentTask == null) {
                    currentTask = new Task("");
                    isEditMode = false;
                    getSupportActionBar().setTitle("Criar Tarefa");
                }
                editNomeTarefa.setText(currentTask.getNome());
                // NOTA: O seu modelo Task.java não tem campo "notas"
                // Para carregar, você precisaria adicionar "String notas" no Task.java
                // editNotas.setText(currentTask.getNotas());
                setupRecyclerView();
            });
        });
    }

    private void setupRecyclerView() {
        if (currentTask.getSubtasks() == null) {
            currentTask.setSubtasks(new ArrayList<>());
        }
        adapter = new SubTarefasAdapter(
                currentTask.getSubtasks(),
                // OnToggleClick
                position -> {
                    SubTask subtask = currentTask.getSubtasks().get(position);
                    subtask.setDone(!subtask.getDone());
                    adapter.notifyItemChanged(position);
                },
                // OnDeleteClick
                position -> {
                    currentTask.removeSubtask(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, currentTask.getSubtasks().size());
                }
        );
        rvSubtarefas.setLayoutManager(new LinearLayoutManager(this));
        rvSubtarefas.setAdapter(adapter);
    }

    // --- MÉTODO ANTIGO REMOVIDO ---
    // O "showAddSubtaskDialog" não é mais usado, pois a adição
    // é feita direto na tela (inline)
    /*
    private void showAddSubtaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_subtarefa, null);
        EditText editNomeSubtarefa = view.findViewById(R.id.etNomeSubtarefa);

        builder.setView(view)
                .setTitle("Nova Subtarefa")
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String nome = editNomeSubtarefa.getText().toString().trim();
                    if (!nome.isEmpty()) {
                        currentTask.addSubtask(new SubTask(nome));
                        adapter.notifyItemInserted(currentTask.getSubtasks().size() - 1);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    */
    // --- FIM DO MÉTODO REMOVIDO ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tarefa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveTask();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveTask() {
        String nomeTarefa = editNomeTarefa.getText().toString().trim();
        if (nomeTarefa.isEmpty()) {
            Toast.makeText(this, "Por favor, dê um nome à tarefa.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentTask.setNome(nomeTarefa);
        // NOTA: O seu modelo Task.java não tem campo "notas"
        // Para salvar, você precisaria adicionar "String notas" no Task.java
        // currentTask.setNotas(editNotas.getText().toString().trim());

        databaseExecutor.execute(() -> {
            if (isEditMode) {
                taskDao.update(currentTask);
            } else {
                taskDao.insert(currentTask);
            }

            runOnUiThread(this::finish);
        });

        Toast.makeText(this, "Tarefa salva!", Toast.LENGTH_SHORT).show();
    }
}