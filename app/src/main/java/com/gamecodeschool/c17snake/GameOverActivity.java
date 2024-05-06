package com.gamecodeschool.c17snake;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class GameOverActivity extends Activity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over_layout);

        // Retrieve and display the score
        TextView scoreText = findViewById(R.id.score_text);
        int score = getIntent().getIntExtra("SCORE", 0);  // Default to 0 if no score was passed
        scoreText.setText("Score: " + score);

        // Setup the restart game text view
        TextView restartText = findViewById(R.id.restart_text);
        restartText.setOnClickListener(v -> {
            // Start the SnakeActivity to restart the game
            Intent intent = new Intent(GameOverActivity.this, SnakeActivity.class);
            startActivity(intent);
            finish();  // Finish this activity
        });

        // Setup the return to main menu text view
        TextView homeText = findViewById(R.id.home_text);
        homeText.setOnClickListener(v -> {
            // Start the StartScreenActivity to go back to the main menu
            Intent intent = new Intent(GameOverActivity.this, StartScreenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  // Clears all the other activities on top of StartScreenActivity
            startActivity(intent);
            finish();  // Finish this activity
        });
    }
}
