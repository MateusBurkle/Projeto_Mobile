package com.example.projeto.storage;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.projeto.models.HistoricoAgua;

// Lista todas as tabelas (Entities) que o banco de dados terá
@Database(entities = {HistoricoAgua.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Diz ao Room qual DAO (interface de queries) esta classe "conhece"
    public abstract HistoricoAguaDao historicoAguaDao();

    // --- Singleton Pattern (Garante que só exista UMA instância do banco de dados) ---
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "hidrameta_database")
                            // Permite queries na thread principal (NÃO RECOMENDADO, mas simplifica)
                            // Vamos trocar isso por threads na Activity
                            // .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}