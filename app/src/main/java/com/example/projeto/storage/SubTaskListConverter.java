package com.example.projeto.storage;

import androidx.room.TypeConverter;

import com.example.projeto.models.SubTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class SubTaskListConverter {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<SubTask> toSubTaskList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<SubTask>>() {
        }.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String fromSubTaskList(List<SubTask> subTasks) {
        return gson.toJson(subTasks);
    }
}