package com.example.projeto.ui;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.example.projeto.storage.TaskStorage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ListaTarefasActivity extends AppCompatActivity {

    private TaskStorage storage;
    private TaskListAdapter adapter;
    private RecyclerView rv;
    private android.widget.TextView tvVazio;

    private List<Task> tasks;

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

        // Swipe: direita = concluir/desconcluir, esquerda = excluir (com desfazer)
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) pos = vh.getAdapterPosition();

                final Task task = adapter.getItem(pos);
                if (task == null) return;

                if (direction == ItemTouchHelper.RIGHT) {

                    boolean novoEstado = !task.isConcluida();
                    task.setConcluida(novoEstado);
                    if (tasks != null && pos >= 0 && pos < tasks.size()) {
                        tasks.set(pos, task);
                    }
                    storage.saveAll(tasks);
                    adapter.notifyItemChanged(pos);
                } else if (direction == ItemTouchHelper.LEFT) {
                    // Excluir com desfazer
                    final int deletedPos = pos;
                    final Task removedTask = task;

                    if (tasks != null && deletedPos >= 0 && deletedPos < tasks.size()) {
                        tasks.remove(deletedPos);
                    }
                    adapter.removeAt(deletedPos);
                    storage.deleteById(removedTask.getId());
                    atualizarVazio();

                    Snackbar.make(rv, "Tarefa excluída", Snackbar.LENGTH_LONG)
                            .setAction("DESFAZER", v -> {
                                if (tasks == null) tasks = new java.util.ArrayList<>();
                                int insertPos = Math.max(0, Math.min(deletedPos, tasks.size()));
                                tasks.add(insertPos, removedTask);
                                storage.saveAll(tasks);
                                adapter.insertAt(insertPos, removedTask);
                                atualizarVazio();
                            })
                            .show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder vh,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                super.onChildDraw(c, recyclerView, vh, dX, dY, actionState, isCurrentlyActive);

                View item = vh.itemView;
                Paint paint = new Paint();
                float density = recyclerView.getResources().getDisplayMetrics().density;
                int margin = (int) (16f * density);
                int iconSize = (int) (24f * density);

                if (dX > 0) { // direita = concluir/desconcluir
                    int green = 0xFF4CAF50; // Material Green 500
                    paint.setColor(green);
                    c.drawRect(item.getLeft(), item.getTop(), item.getLeft() + dX, item.getBottom(), paint);

                    Drawable icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_check_24);
                    if (icon != null) {
                        int left = item.getLeft() + margin;
                        int right = left + iconSize;
                        int top = item.getTop() + (item.getHeight() - iconSize) / 2;
                        int bottom = top + iconSize;
                        icon.setBounds(left, top, right, bottom);
                        icon.draw(c);
                    }
                } else if (dX < 0) { // esquerda = excluir
                    int red = ContextCompat.getColor(recyclerView.getContext(), android.R.color.holo_red_light);
                    paint.setColor(red);
                    c.drawRect(item.getRight() + dX, item.getTop(), item.getRight(), item.getBottom(), paint);

                    Drawable icon = ContextCompat.getDrawable(recyclerView.getContext(),
                            R.drawable.icon_delete_24 /* ou ic_delete_24 */);
                    if (icon != null) {
                        int right = item.getRight() - margin;
                        int left = right - iconSize;
                        int top = item.getTop() + (item.getHeight() - iconSize) / 2;
                        int bottom = top + iconSize;
                        icon.setBounds(left, top, right, bottom);
                        icon.draw(c);
                    }
                }
            }
        });
        helper.attachToRecyclerView(rv);

        adapter.setOnItemClick(task -> {
            // futuro: abrir detalhes/edição
        });

        adapter.setOnToggleDone((task, done, pos) -> {
            task.setConcluida(done);
            storage.saveAll(tasks);
            adapter.notifyItemChanged(pos);
            atualizarVazio();
        });

        carregar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregar();
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
        tasks = storage.getAll();
        adapter.submit(tasks);
        atualizarVazio();
    }

    private void atualizarVazio() {
        tvVazio.setVisibility(tasks == null || tasks.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
