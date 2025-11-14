package com.example.projeto.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.projeto.storage.SubTaskListConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "tasks")
@TypeConverters(SubTaskListConverter.class)
public class Task implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String nome;
    private List<SubTask> subtasks;
    private boolean concluida;

    // Construtor vazio (necessário para o Room)
    public Task() {
        this.subtasks = new ArrayList<>();
    }

    public Task(String nome) {
        this.nome = nome;
        this.subtasks = new ArrayList<>();
        this.concluida = false;
    }

    // --- Getters e Setters ---

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<SubTask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<SubTask> subtasks) {
        this.subtasks = subtasks;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }

    // --- Métodos de ajuda (existentes) ---

    public void addSubtask(SubTask subtask) {
        if (this.subtasks == null) {
            this.subtasks = new ArrayList<>();
        }
        this.subtasks.add(subtask);
    }

    public void removeSubtask(int index) {
        if (this.subtasks != null && index >= 0 && index < this.subtasks.size()) {
            this.subtasks.remove(index);
        }
    }

    public void updateSubtask(int index, SubTask subtask) {
        if (this.subtasks != null && index >= 0 && index < this.subtasks.size()) {
            this.subtasks.set(index, subtask);
        }
    }
}