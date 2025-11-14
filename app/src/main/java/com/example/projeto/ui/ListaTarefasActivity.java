package com.example.projeto.ui;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projeto.R;
import com.example.projeto.adapters.TaskListAdapter;
import com.example.projeto.models.Task;
import com.example.projeto.storage.AppDatabase;
import com.example.projeto.storage.TaskDao;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListaTarefasActivity extends AppCompatActivity {

    private RecyclerView rvTarefas;
    private TaskListAdapter adapter;
    private final List<Task> tasks = new ArrayList<>();

    // --- MUDANÇA AQUI ---
    private TaskDao taskDao;
    private ExecutorService databaseExecutor;
    // --- FIM DA MUDANÇA ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tarefas);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Lista de Tarefas");

        // --- MUDANÇA AQUI ---
        // Inicializa o acesso ao banco de dados
        databaseExecutor = Executors.newSingleThreadExecutor();
        taskDao = AppDatabase.getInstance(this).taskDao();
        // --- FIM DA MUDANÇA ---

        rvTarefas = findViewById(R.id.rvTarefas);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks(); // Carrega as tarefas do banco toda vez que a tela é exibida
    }

    private void loadTasks() {
        // --- MUDANÇA AQUI ---
        // Busca as tarefas em uma thread separada
        databaseExecutor.execute(() -> {
            List<Task> loadedTasks = taskDao.getAllTasks();

            // Atualiza a UI na thread principal
            runOnUiThread(() -> {
                tasks.clear();
                tasks.addAll(loadedTasks);
                if (adapter == null) {
                    setupRecyclerView();
                } else {
                    adapter.notifyDataSetChanged();
                }
            });
        });
        // --- FIM DA MUDANÇA ---
    }

    private void setupRecyclerView() {
        adapter = new TaskListAdapter(tasks,
                // OnItemClick (agora passa taskId)
                taskId -> {
                    Intent intent = new Intent(this, TarefasActivity.class);
                    intent.putExtra("TASK_ID", taskId); // <-- MUDANÇA AQUI
                    startActivity(intent);
                },
                // OnToggleDone (agora atualiza no banco)
                position -> {
                    // --- MUDANÇA AQUI ---
                    Task task = tasks.get(position);
                    task.setConcluida(!task.isConcluida());
                    databaseExecutor.execute(() -> taskDao.update(task));
                    adapter.notifyItemChanged(position);
                    // --- FIM DA MUDANÇA ---
                }
        );
        rvTarefas.setLayoutManager(new LinearLayoutManager(this));
        rvTarefas.setAdapter(adapter);

        setupSwipeToDelete();
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // --- MUDANÇA AQUI ---
                Task taskToDelete = tasks.get(position);

                databaseExecutor.execute(() -> taskDao.delete(taskToDelete)); // Deleta do banco
                tasks.remove(position); // Remove da lista local
                adapter.notifyItemRemoved(position); // Atualiza o adapter

                Snackbar.make(rvTarefas, "Tarefa excluída", Snackbar.LENGTH_LONG)
                        .setAction("DESFAZER", v -> {
                            // Adiciona de volta no banco e na lista
                            databaseExecutor.execute(() -> taskDao.insert(taskToDelete));
                            tasks.add(position, taskToDelete);
                            adapter.notifyItemInserted(position);
                        }).show();
                // --- FIM DA MUDANÇA ---
            }

            // Lógica para desenhar o fundo vermelho (sem mudanças)
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                Drawable icon = ContextCompat.getDrawable(ListaTarefasActivity.this, R.drawable.icon_delete_24);
                ColorDrawable background = new ColorDrawable(Color.RED);
                View itemView = viewHolder.itemView;
                int backgroundCornerOffset = 20;

                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + icon.getIntrinsicHeight();

                if (dX > 0) { // Swiping to the right
                    // ... (código original)
                } else if (dX < 0) { // Swiping to the left
                    int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                            itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else { // view is unSwiped
                    background.setBounds(0, 0, 0, 0);
                }
                background.draw(c);
                icon.draw(c);
            }

        }).attachToRecyclerView(rvTarefas);
    }

    // --- MUDANÇA AQUI ---
    // Adiciona o menu para "Excluir Concluídas"
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_tarefas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete_completed) {
            deleteCompletedTasks();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish(); // Trata o clique no botão "voltar" da toolbar
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteCompletedTasks() {
        databaseExecutor.execute(() -> {
            taskDao.deleteCompletedTasks();
            // Recarrega as tarefas na UI thread
            runOnUiThread(this::loadTasks);
        });
        Snackbar.make(rvTarefas, "Tarefas concluídas foram excluídas", Snackbar.LENGTH_SHORT).show();
    }
    // --- FIM DA MUDANÇA ---
}