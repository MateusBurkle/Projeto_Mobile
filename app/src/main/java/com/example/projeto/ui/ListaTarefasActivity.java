package com.example.projeto.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projeto.R;
import com.example.projeto.models.Task;
import com.example.projeto.storage.TaskStorage;
import com.example.projeto.adapters.TaskListAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class ListaTarefasActivity extends AppCompatActivity {

    private TaskStorage storage;
    private TaskListAdapter adapter;
    private androidx.recyclerview.widget.RecyclerView rv;
    private android.widget.TextView tvVazio;

    private List<Task> tasks; // alteração

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tarefas);

        storage = new TaskStorage(this);

        MaterialToolbar top = findViewById(R.id.topAppBar);
        rv = findViewById(R.id.rvTarefas);
        tvVazio = findViewById(R.id.tvVazio);

        setSupportActionBar(top);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        top.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        top.setOnMenuItemClickListener(this::onMenuClick);

        adapter = new TaskListAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        adapter.setOnItemClick(task -> {
            // futuro: abrir detalhes/edição
        });

        // quando marcar/desmarcar no checkbox
        adapter.setOnToggleDone((task, done, pos) -> {                 // alteração
            task.setConcluida(done);                                   // alteração
            // persiste a lista inteira atualizada
            storage.saveAll(tasks); /* alteração
               Se seu TaskStorage usa outro nome (ex.: saveTasks, setAll),
               use-o aqui no lugar de saveAll. */
            adapter.notifyItemChanged(pos);                             // alteração
            atualizarVazio();                                           // alteração
        });

        carregar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregar(); // atualiza quando volta do "criar"
    }

    private boolean onMenuClick(MenuItem item) {
        if (item.getItemId() == R.id.action_adicionar) {
            Intent intent = new Intent(this, TarefasActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    private void carregar() {
        tasks = storage.getAll();                // alteração
        adapter.submit(tasks);
        atualizarVazio();                        // alteração
    }

    private void atualizarVazio() {              // alteração
        tvVazio.setVisibility(tasks == null || tasks.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
