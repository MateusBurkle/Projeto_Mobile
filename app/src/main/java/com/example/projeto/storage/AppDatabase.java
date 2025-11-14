package com.example.projeto.storage;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.projeto.models.HistoricoAgua;
import com.example.projeto.models.Task;

@Database(entities = {HistoricoAgua.class, Task.class}, version = 2) // <-- MUDANÇA AQUI
@TypeConverters({SubTaskListConverter.class}) // <-- MUDANÇA AQUI
public abstract class AppDatabase extends RoomDatabase {

    public abstract HistoricoAguaDao historicoAguaDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration() // <-- MUDANÇA AQUI
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static volatile AppDatabase INSTANCE;

    public abstract TaskDao taskDao(); // <-- MUDANÇA AQUI
}