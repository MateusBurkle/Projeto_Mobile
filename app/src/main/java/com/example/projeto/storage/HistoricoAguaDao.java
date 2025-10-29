package com.example.projeto.storage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.projeto.models.HistoricoAgua;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface HistoricoAguaDao {

    // Insere um novo registro. Se a data já existir, ele substitui (bom para o upsert)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoricoAgua historico);

    // Atualiza um registro existente
    @Update
    void update(HistoricoAgua historico);

    // Pega o registro de UMA data específica (ex: "2025-10-29")
    // O ListenableFuture é para rodar isso fora da thread principal
    @Query("SELECT * FROM historico_agua WHERE data = :data LIMIT 1")
    ListenableFuture<HistoricoAgua> getByDate(String data);

    // Pega os últimos 7 dias para o gráfico
    // O ListenableFuture é para rodar isso fora da thread principal
    @Query("SELECT * FROM historico_agua ORDER BY data DESC LIMIT 7")
    ListenableFuture<List<HistoricoAgua>> getUltimosSeteDias();
}