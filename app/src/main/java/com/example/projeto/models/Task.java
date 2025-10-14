package com.example.projeto.models;

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
public class Task {
    private String id;
    private String titulo;
    private String notas;
    private long criadoData;
    private boolean concluida;
    private List<SubTask> subtarefas;

    public Task() {

    }
    public Task(String titulo, String notas){
        this.id = UUID.randomUUID().toString();
        this.titulo = titulo;
        this.notas = notas;
        this.criadoData = System.currentTimeMillis();
        this.concluida =  false;
        this.subtarefas = new ArrayList<>();
    }
    public String getId(){
        return this.id;
    }
    public String getTitulo(){
        return this.titulo;
    }
    public String getNotas(){
        return this.notas;
    }
    public long getCriadoData(){
        return this.criadoData;
    }
    public boolean isConcluida() {
        return this.concluida;
    }
    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }
    public List<SubTask> getSubtarefas(){
        return this.subtarefas;
    }
    public void setSubtarefas(List<SubTask> subtarefas){
        this.subtarefas = subtarefas;
    }
}
