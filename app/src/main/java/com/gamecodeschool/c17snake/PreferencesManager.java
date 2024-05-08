package com.gamecodeschool.c17snake;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static PreferencesManager instance;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "GamePrefs";

    private PreferencesManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context);
        }
        return instance;
    }

    public int getSavedDifficulty() {
        return sharedPreferences.getInt("difficulty", 0); // Default to 'easy' if not set
    }

    public void setSavedDifficulty(int difficultyLevel) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("difficulty", difficultyLevel);
        editor.apply();
    }
}
