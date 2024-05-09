package com.gamecodeschool.c17snake;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class StartScreenActivity extends Activity {

    public static final String PREFS_NAME = "GamePrefs";
    public static final String PREF_DIFFICULTY_KEY = "difficulty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen_layout);

        GameSoundManager soundManager = new GameSoundManager(this);
        soundManager.playMenuMusic();
    }

    public void onPlayGameClick(View view) {
        Intent intent = new Intent(this, SnakeActivity.class);
        startActivity(intent);
        finish();
    }

    public void onLeaderboardClick(View view) {
        Intent intent = new Intent(this, LeaderboardActivity.class);
        startActivity(intent);
    }

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
        creditsDialog.setPositiveButton("OK", (dialog, id) -> {});
        AlertDialog dialog = creditsDialog.create();
        dialog.show();
    }
    //
    public void showDifficultyDialog(View view) {
        // Create a new dialog instance.
        final Dialog difficultyDialog = new Dialog(this);
        // Set the layout for the dialog from XML.
        difficultyDialog.setContentView(R.layout.dialog_select_difficulty);
        // Find the spinner component within the dialog.
        Spinner spinner = difficultyDialog.findViewById(R.id.spinnerDifficultyDialog);
        // Create an ArrayAdapter using a custom item layout and the array of difficulty levels.
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, R.layout.
                custom_spinner_item, getResources().getStringArray(R.array.difficulty_levels));
        // Set the dropdown view resource for the adapter.
        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
        // Set the adapter to the spinner.
        spinner.setAdapter(adapter);
        // Set an item selected listener on the spinner to handle difficulty selection.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean userInteractionRequired = false;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!userInteractionRequired) {
                    userInteractionRequired = true;
                    return;  // Return early if this is the first automatic selection.
                }
                // Handle the difficulty selection.
                handleDifficultySelection(position);
                // Dismiss the dialog after the user has made a selection.
                difficultyDialog.dismiss();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // This method can be left empty unless there's a need to handle cases where nothing is selected.
            }
        });

        // Make the dialog cancelable by touching outside of it.
        difficultyDialog.setCancelable(true);
        // Display the dialog.
        difficultyDialog.show();
    }

    private void handleDifficultySelection(int position) {
        // Access the SharedPreferences file.
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Get an editor for SharedPreferences.
        SharedPreferences.Editor editor = prefs.edit();
        // Save the selected difficulty position.
        editor.putInt(PREF_DIFFICULTY_KEY, position);
        // Apply changes to SharedPreferences.
        editor.apply();
        // Display a toast message to inform the user of the selected difficulty.
        Toast.makeText(this, "Difficulty set to " + getResources().getStringArray(R.
                array.difficulty_levels)[position], Toast.LENGTH_SHORT).show();
    }
}
