package com.example.projeto.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.projeto.models.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TaskStorage {
    private static final String PREFS = "tasks_prefs";
    private static final String KEY = "tasks_json";
    private final SharedPreferences sp;
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<ArrayList<Task>>(){}.getType();

    public TaskStorage(Context ctx) {
        sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public ArrayList<Task> getAll() {
        String json = sp.getString(KEY, "[]");
        return gson.fromJson(json, listType);
    }

    public void add(Task t) {
        ArrayList<Task> list = getAll();
        list.add(t);
        sp.edit().putString(KEY, gson.toJson(list, listType)).apply();
    }
}