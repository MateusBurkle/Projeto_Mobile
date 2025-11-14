package com.example.projeto.storage;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.projeto.models.HistoricoAgua;
import com.example.projeto.models.Task;
import com.example.projeto.models.User; // <-- 1. IMPORTAR USER

// 2. ADICIONAR USER E MUDAR VERSÃO PARA 3 (ou um número maior que a atual)
@Database(entities = {HistoricoAgua.class, Task.class, User.class}, version = 3)
@TypeConverters({SubTaskListConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract HistoricoAguaDao historicoAguaDao();
    public abstract TaskDao taskDao();
    public abstract UserDao userDao(); // <-- 3. ADICIONAR O MÉTODO DO USERDAO

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            // Isso vai apagar o banco antigo e criar o novo
                            // com a tabela 'users'. Perfeito para desenvolvimento.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}