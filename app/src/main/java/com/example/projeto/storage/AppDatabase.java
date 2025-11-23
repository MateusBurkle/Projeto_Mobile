package com.example.projeto.storage;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.projeto.models.HistoricoAgua;
import com.example.projeto.models.Task;
import com.example.projeto.models.User;

// AQUI: Adicionei User.class e Task.class para criar as tabelas
@Database(entities = {HistoricoAgua.class, User.class, Task.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // DAOs existentes
    public abstract HistoricoAguaDao historicoAguaDao();

    // AQUI: Adicionei o método que estava faltando para o Cadastro funcionar
    public abstract UserDao userDao();

    // AQUI: Já deixei pronto para as Tarefas
    public abstract TaskDao taskDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "hidrameta_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}