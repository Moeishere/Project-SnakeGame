package com.gamecodeschool.c17snake;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


public class StartScreenActivity extends Activity {
    private GameSoundManager soundManager;

    public static final String PREFS_NAME = "GamePrefs";
    public static final String PREF_DIFFICULTY_KEY = "difficulty";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen_layout);


        setupDifficultySpinner();

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

    private void setupDifficultySpinner() {
        Spinner spinner = findViewById(R.id.spinnerDifficulty);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_levels, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                handleDifficultySelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void handleDifficultySelection(int position) {
        // Get a reference to the SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save the selected difficulty position
        editor.putInt(PREF_DIFFICULTY_KEY, position);
        editor.apply();
        Toast.makeText(this, "Difficulty set to " + getResources().getStringArray(R.array.difficulty_levels)[position], Toast.LENGTH_SHORT).show();
    }
    private void savePreference(String key, int value) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void changeDifficulty(int newDifficulty) {
        PreferencesManager.getInstance(getApplicationContext()).setSavedDifficulty(newDifficulty);
    }




}

