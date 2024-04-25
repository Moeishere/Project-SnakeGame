package com.gamecodeschool.c17snake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class StartScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen_layout);
    }

    // Called when the "Play Game" text is clicked
    public void onPlayGameClick(View view) {
        // Intent to start SnakeActivity (or your main game activity)
        Intent intent = new Intent(this, SnakeActivity.class);
        startActivity(intent);
        // Optional: finish StartScreenActivity if you don't want it to appear when the user presses back from the game
        finish();
    }

    // Called when the "Leaderboard" text is clicked
    public void onLeaderboardClick(View view) {
        // Code to show leaderboard
    }

    // Called when the "Credits" text is clicked
    public void onCreditsClick(View view) {
        // Code to show credits
    }
}

