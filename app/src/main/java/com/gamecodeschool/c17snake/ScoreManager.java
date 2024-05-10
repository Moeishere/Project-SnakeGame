package com.gamecodeschool.c17snake;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreManager {
    private static final String SCORES_KEY = "scores";
    private final SharedPreferences sharedPreferences;

    public ScoreManager(Context context) {
        sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
    }

    public void saveScore(int score) {
        List<Integer> scores = loadScores();
        if (score > 0) {
            scores.add(score);
        }
        // Sort and keep the top 10 scores
        scores.sort(Collections.reverseOrder());
        if (scores.size() > 10) {
            scores = scores.subList(0, 10);
        }
        // Save scores to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(scores);
        editor.putString(SCORES_KEY, json);
        editor.apply();
    }

    public List<Integer> loadScores() {
        String json = sharedPreferences.getString(SCORES_KEY, null);
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        List<Integer> scores = new Gson().fromJson(json, type);
        return scores != null ? scores : new ArrayList<>();
    }
}