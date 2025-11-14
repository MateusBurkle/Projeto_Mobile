package com.example.projeto.models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// @Entity informa ao Room que esta classe é uma tabela no banco.
// O "indices = @Index(value = {"email"}, unique = true)" garante que não
// teremos dois usuários com o mesmo e-mail.
@Entity(tableName = "users", indices = @Index(value = {"email"}, unique = true))
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nome;
    private String email;
    private String senha; // Vai armazenar o Hash SHA-256 da senha
    private int peso;

    // Construtor, Getters e Setters
    // O Room usará os getters e setters para acessar os campos

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public int getPeso() { return peso; }
    public void setPeso(int peso) { this.peso = peso; }
}