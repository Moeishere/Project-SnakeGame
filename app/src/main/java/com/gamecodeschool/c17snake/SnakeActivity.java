package com.gamecodeschool.c17snake;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class SnakeActivity extends Activity {
    private SnakeGame mSnakeGame;  // Changed visibility to private for better encapsulation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeGame();
    }

    private void initializeGame() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mSnakeGame = new SnakeGame(this, size);
        setContentView(mSnakeGame);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSnakeGame.resume();  // Delegate game-specific logic to the SnakeGame class
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSnakeGame.pause();  // Delegate game-specific logic to the SnakeGame class
    }
}
