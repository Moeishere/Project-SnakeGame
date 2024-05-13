package com.gamecodeschool.c17snake;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboard_layout);
        RecyclerView leaderboardList = findViewById(R.id.leaderboard_list);

        // Set the RecyclerView to use a GridLayoutManager
        int numberOfColumns = 2;  // Set this number based on your layout needs
        leaderboardList.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        // Fetch the high scores
        List<Integer> highScores = getHighScores();
        Log.d("TESTER", "onCreate: highScores: " + highScores);

        // Ensure only the top 10 scores are displayed
        if (highScores.size() > 10) {
            highScores = highScores.subList(0, 10);
        }
        LeaderboardAdapter adapter = new LeaderboardAdapter(highScores);
        leaderboardList.setAdapter(adapter);
    }
    public void goBackToStartScreen(View view) {
        // Finishes this activity and returns to the previous one
        finish();
    }
    private List<Integer> getHighScores() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("scores", null);
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        List<Integer> scores = gson.fromJson(json, type);

        if (scores == null) {
            scores = new ArrayList<>();
        }
        // Log Scores: adding logging inside to verify that the scores are being stored.
        Log.d("SnakeGame", "Loaded scores: " + scores);
        // Sort scores in descending order
        scores.sort(Collections.reverseOrder());
        return scores;
    }
}

// An adapter for RecyclerView to display the leaderboard
class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private final List<Integer> highScores;

    public LeaderboardAdapter(List<Integer> highScores) {
        this.highScores = highScores;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Integer score = highScores.get(position); // Retrieve score from list
        String scoreText = "Score " + (position + 1) + ": " + score;
        Log.d("TESTER", "onBindViewHolder: " + scoreText); // Add this line
        //holder.rankTextView.setText(String.valueOf(position + 1));
        holder.scoreTextView.setText(scoreText);
    }
    @Override
    public int getItemCount() {
        return highScores.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankTextView, scoreTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rank_text);
            scoreTextView = itemView.findViewById(R.id.score_text);
        }
    }

}