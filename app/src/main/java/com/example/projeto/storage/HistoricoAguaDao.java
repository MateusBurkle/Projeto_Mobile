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

    // --- SEUS MÉTODOS ORIGINAIS (MANTIDOS) ---

    // Insere ou Substitui (Upsert)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoricoAgua historico);

    // Atualiza
    @Update
    void update(HistoricoAgua historico);

    // Pega o registro (Assíncrono para a UI/Gráficos)
    @Query("SELECT * FROM historico_agua WHERE data = :data LIMIT 1")
    ListenableFuture<HistoricoAgua> getByDate(String data);

    // Pega os últimos 7 dias (Assíncrono para o Gráfico)
    @Query("SELECT * FROM historico_agua ORDER BY data DESC LIMIT 7")
    ListenableFuture<List<HistoricoAgua>> getUltimosSeteDias();


    // --- NOVO MÉTODO (ADICIONADO PARA A NOTIFICAÇÃO) ---

    // Este método é síncrono (sem Future) para o Worker conseguir ler rapidinho em segundo plano.
    // Ele pega direto o 'totalMl' do dia.
    @Query("SELECT totalMl FROM historico_agua WHERE data = :data")
    int getTotalAguaDoDia(String data);
}