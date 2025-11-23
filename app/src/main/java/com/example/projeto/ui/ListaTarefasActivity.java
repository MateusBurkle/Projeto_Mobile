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
import android.widget.LinearLayout; // Import necessário para o layout vazio

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
    private LinearLayout layoutVazio; // Referência para a tela de "Vazio"
    private TaskListAdapter adapter;
    private final List<Task> tasks = new ArrayList<>();

    private TaskDao taskDao;
    private ExecutorService databaseExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tarefas);

        // Configuração segura da Toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar); // ID correto do XML atualizado
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lista de Tarefas");
        }

        // Inicializa Banco de Dados
        databaseExecutor = Executors.newSingleThreadExecutor();
        taskDao = AppDatabase.getInstance(this).taskDao();

        rvTarefas = findViewById(R.id.rvTarefas);
        layoutVazio = findViewById(R.id.layoutVazio);

        // Configura o botão de "Criar primeira tarefa"
        findViewById(R.id.btnCriarPrimeiraTarefa).setOnClickListener(v -> {
            startActivity(new Intent(ListaTarefasActivity.this, TarefasActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        databaseExecutor.execute(() -> {
            List<Task> loadedTasks = taskDao.getAllTasks();

            runOnUiThread(() -> {
                tasks.clear();
                tasks.addAll(loadedTasks);

                // Lógica para mostrar/esconder a tela de "vazio"
                if (tasks.isEmpty()) {
                    rvTarefas.setVisibility(View.GONE);
                    layoutVazio.setVisibility(View.VISIBLE);
                } else {
                    rvTarefas.setVisibility(View.VISIBLE);
                    layoutVazio.setVisibility(View.GONE);
                }

                if (adapter == null) {
                    setupRecyclerView();
                } else {
                    adapter.notifyDataSetChanged();
                }
            });
        });
    }

    private void setupRecyclerView() {
        adapter = new TaskListAdapter(tasks,
                taskId -> {
                    Intent intent = new Intent(this, TarefasActivity.class);
                    intent.putExtra("TASK_ID", taskId);
                    startActivity(intent);
                },
                position -> {
                    Task task = tasks.get(position);
                    task.setConcluida(!task.isConcluida());
                    databaseExecutor.execute(() -> taskDao.update(task));
                    adapter.notifyItemChanged(position);
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
                Task taskToDelete = tasks.get(position);

                databaseExecutor.execute(() -> taskDao.delete(taskToDelete));
                tasks.remove(position);
                adapter.notifyItemRemoved(position);

                // Se ficou vazio após deletar, mostra a tela de vazio
                if (tasks.isEmpty()) {
                    rvTarefas.setVisibility(View.GONE);
                    layoutVazio.setVisibility(View.VISIBLE);
                }

                Snackbar.make(rvTarefas, "Tarefa excluída", Snackbar.LENGTH_LONG)
                        .setAction("DESFAZER", v -> {
                            databaseExecutor.execute(() -> taskDao.insert(taskToDelete));
                            tasks.add(position, taskToDelete);
                            adapter.notifyItemInserted(position);
                            rvTarefas.setVisibility(View.VISIBLE);
                            layoutVazio.setVisibility(View.GONE);
                        }).show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                // Tenta carregar o ícone de forma segura
                Drawable icon = ContextCompat.getDrawable(ListaTarefasActivity.this, R.drawable.icon_delete_24);
                if (icon == null) return; // Evita crash se a imagem não existir

                ColorDrawable background = new ColorDrawable(Color.RED);
                View itemView = viewHolder.itemView;
                int backgroundCornerOffset = 20;

                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + icon.getIntrinsicHeight();

                if (dX > 0) { // Direita
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    background.setBounds(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
                } else if (dX < 0) { // Esquerda
                    int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                            itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else {
                    background.setBounds(0, 0, 0, 0);
                }
                background.draw(c);
                icon.draw(c);
            }

        }).attachToRecyclerView(rvTarefas);
    }

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
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteCompletedTasks() {
        databaseExecutor.execute(() -> {
            taskDao.deleteCompletedTasks();
            runOnUiThread(this::loadTasks);
        });
        Snackbar.make(rvTarefas, "Tarefas concluídas foram excluídas", Snackbar.LENGTH_SHORT).show();
    }
}