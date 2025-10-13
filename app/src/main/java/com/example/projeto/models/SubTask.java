package com.example.projeto.models;

import java.util.UUID;
public class SubTask {
    private String id;
    private String titulo;
    private boolean concluida;

    public SubTask() {
        this.id = UUID.randomUUID().toString();
        this.titulo = "";
        this.concluida = false;
    }

    public SubTask(String titulo) {
        this();
        this.titulo = titulo;
    }

    public String getId() { return id; }
    public String getTitulo(){
        return this.titulo;
    }
    public void setTitulo(String titulo){
        this.titulo = titulo;
    }
    public boolean isConcluida() {
        return this.concluida; }
    public void setConcluida(boolean concluida){
        this.concluida = concluida;
    }
}
