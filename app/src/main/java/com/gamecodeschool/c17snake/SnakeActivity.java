package com.gamecodeschool.c17snake;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class SnakeActivity extends Activity {
    private SnakeGame mSnakeGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeGame();
    }

    private void initializeGame() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int level = PreferencesManager.getInstance(getApplicationContext()).getSavedDifficulty();
        mSnakeGame = new SnakeGame(this, size);
        setContentView(mSnakeGame);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSnakeGame.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSnakeGame.pause();
    }

    private int getDifficultyLevel() {
        SharedPreferences prefs = getSharedPreferences("game_settings", Context.MODE_PRIVATE);
        return prefs.getInt("difficulty_level", 0);
    }


}

