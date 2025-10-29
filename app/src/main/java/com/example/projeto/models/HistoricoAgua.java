package com.example.projeto.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Define o nome da tabela no banco de dados
@Entity(tableName = "historico_agua")
public class HistoricoAgua {

    // Define a 'data' (ex: "2025-10-29") como a chave primária.teste
    // Isso garante que só podemos ter um registro por dia.
    @PrimaryKey
    @NonNull
    private String data; // Formato: "yyyy-MM-dd"

    private int totalMl;
    private int totalCopos;
    private int metaMl;

    // Construtor
    public HistoricoAgua(@NonNull String data, int totalMl, int totalCopos, int metaMl) {
        this.data = data;
        this.totalMl = totalMl;
        this.totalCopos = totalCopos;
        this.metaMl = metaMl;
    }

    // Getters e Setters (necessários para o Room)
    @NonNull
    public String getData() { return data; }
    public void setData(@NonNull String data) { this.data = data; }

    public int getTotalMl() { return totalMl; }
    public void setTotalMl(int totalMl) { this.totalMl = totalMl; }

    public int getTotalCopos() { return totalCopos; }
    public void setTotalCopos(int totalCopos) { this.totalCopos = totalCopos; }

    public int getMetaMl() { return metaMl; }
    public void setMetaMl(int metaMl) { this.metaMl = metaMl; }
}