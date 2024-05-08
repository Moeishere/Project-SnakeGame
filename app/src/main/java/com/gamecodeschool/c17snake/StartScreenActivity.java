package com.gamecodeschool.c17snake;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class StartScreenActivity extends Activity {
    private GameSoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen_layout);
        soundManager = new GameSoundManager(this);
        soundManager.playMenuMusic();
    }

    // Called when the "Play Game" text is clicked
    public void onPlayGameClick(View view) {
        // Intent to start SnakeActivity (or your main game activity)
        Intent intent = new Intent(this, SnakeActivity.class);
        startActivity(intent);
        finish();
    }

    // Called when the "Leaderboard" text is clicked
    public void onLeaderboardClick(View view) {
        Intent intent = new Intent(this, LeaderboardActivity.class);
        startActivity(intent);
    }

    // Called when the "Credits" text is clicked
    public void onCreditsClick(View view) {
        AlertDialog.Builder creditsDialog = new AlertDialog.Builder(this);
        creditsDialog.setTitle("Credits");
        String message = "This is a basic 2D snake game that has been greatly improved with the principles of OOP. " +
                "We have added numerous features and enhanced the appearance to bring you a better gaming experience. " +
                "\n\nFinal Project Group 34:\n" +
                "- Muhammad Hassan\n" +
                "- Brandon Symansiv\n" +
                "- Huy Tran\n" +
                "- Kevin Esquivel\n" +
                "- Steven Graham";

        creditsDialog.setMessage(message);
        creditsDialog.setPositiveButton("OK", (dialog, id) -> {
        });
        AlertDialog dialog = creditsDialog.create();
        dialog.show();
    }
}

