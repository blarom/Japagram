package com.japagram.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import androidx.room.TypeConverter;

public class JapaneseToolboxDbTypeConverters {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<Word.Meaning> stringToWordMeaningsList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Word.Meaning>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String wordMeaningsListToString(List<Word.Meaning> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<Word.Meaning.Explanation> stringToWordMeaningExplanationsList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Word.Meaning.Explanation>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String wordMeaningExplanationsListToString(List<Word.Meaning.Explanation> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<Word.Meaning.Explanation.Example> stringToWordMeaningExplanationExamplesList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Word.Meaning.Explanation.Example>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String wordMeaningExplanationExamplesListToString(List<Word.Meaning.Explanation.Example> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<KanjiComponent.AssociatedComponent> stringToAssociatedComponentsList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<KanjiComponent.AssociatedComponent>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String associatedComponentsListToString(List<KanjiComponent.AssociatedComponent> list) {
        return gson.toJson(list);
    }
}