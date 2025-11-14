package com.example.projeto.models;

import java.io.Serializable;

public class SubTask implements Serializable {
    private String name;
    private boolean done;

    public SubTask(String name) {
        this.name = name;
        this.done = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --- MUDANÇA AQUI ---
    public boolean getDone() { // Renomeado de isDone para getDone
        return done;
    }
    // --- FIM DA MUDANÇA ---

    public void setDone(boolean done) {
        this.done = done;
    }
}