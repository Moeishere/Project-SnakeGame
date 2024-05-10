package com.gamecodeschool.c17snake;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

@SuppressLint("ViewConstructor")
public class SnakeGame extends SurfaceView implements Runnable, GameObject {
    private Difficulty difficulty;
    private static final int NUM_BLOCKS_WIDE = 40;
    private static final long MILLIS_PER_SECOND = 1000;

    private final Context mContext;
    private Bitmap backgroundBitmap;
    private Bitmap mPauseButtonBitmap;
    private Bitmap munPauseButtonBitmap;

    private Thread mThread = null;
    private long mNextFrameTime;
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;
    private int mNumBlocksHigh;
    private int mScore;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;
    private Snake mSnake;
    private Apple mApple;
    private Obstacle mObstacle;
    private Potion mPotion;
    private boolean mGameStarted = false;
    private volatile boolean mGameJustStarted = false;
    private boolean mGameOver = false;

    // testing vars
    private final ArrayList<Integer> scores = new ArrayList<>();

    private final GameSoundManager soundManager;

    public SnakeGame(Context context, Point size) {
        super(context);
        mContext = context;

        int difficultyLevel = PreferencesManager.getInstance(context).getSavedDifficulty();

        initializeGame(context, size, difficultyLevel);
        soundManager = new GameSoundManager(context);

    }

    private void initializeGame(Context context, Point size, int difficultyLevel) {
        this.difficulty = new Difficulty(difficultyLevel);
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        mNumBlocksHigh = size.y / blockSize;
        mSurfaceHolder = getHolder();
        mPaint = new Paint();
        mApple = new Apple(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mObstacle = new Obstacle(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mPotion = new Potion(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mSnake = new Snake(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mPauseButtonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause_button);
        mPauseButtonBitmap = Bitmap.createScaledBitmap(mPauseButtonBitmap, 100, 100, false);
        munPauseButtonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.unpause_button);
        munPauseButtonBitmap = Bitmap.createScaledBitmap(munPauseButtonBitmap, 100, 100, false);
        Bitmap mLeaderboardButtonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leaderboard_button);
        mLeaderboardButtonBitmap = Bitmap.createScaledBitmap(mLeaderboardButtonBitmap, 100, 100, false);
    }

    public void newGame() {
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);
        mApple.spawn(difficulty.getAppleFrequency());
        mObstacle.spawn(difficulty.getObstacleFrequency());
        mPotion.spawn(difficulty.getPotionFrequency());
        mScore = 0;
        mNextFrameTime = System.currentTimeMillis();
        mGameJustStarted = true;
        mGameOver = false;
        soundManager.playGameMusic();
    }

    @Override
    public void run() {
        while (mPlaying) {
            if (!mPaused && !mGameOver) {
                if (updateRequired()) {
                    update();
                }
            }
            draw();
        }
    }

    public boolean updateRequired() {
        if (mNextFrameTime <= System.currentTimeMillis()) {
            int TARGET_FPS = difficulty.getSpeed();
            mNextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / TARGET_FPS;
            return true;
        }
        return false;
    }

    public void update() {
        mSnake.move();

        // Update the snake to handle transparency timing
        mSnake.update(new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh));

        if (mSnake.checkDinner(mApple.getLocations())) {
            soundManager.playEatSound();
            mApple.spawn();
            mScore += 1;
        }

        if (mSnake.checkRock(mObstacle.getLocations())) {
            soundManager.playRockSound();
            mObstacle.spawn();
            mScore -= 1;
        }
        if (mSnake.checkPotion(mPotion.getLocations())) {
            soundManager.playEatSound();
            mPotion.spawn();
            mScore += mSnake.getRandomPotionScore(); // Add the random score
        }

        if (mSnake.detectDeath()) {
            soundManager.stopGameMusic();
            soundManager.stopMenuMusic();
            soundManager.playCrashSound();
            gameOver();
            mPaused = true;
            mGameStarted = false;
            saveScore(mScore);
        }

    }

    public void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            Canvas mCanvas = mSurfaceHolder.lockCanvas();
            if (mGameOver) {
                displayGameOver(mCanvas, mPaint);
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                return;
            }

            // Background drawing code
            if (backgroundBitmap == null) {
                backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gamebackground);
            }
            int canvasWidth = mCanvas.getWidth();
            int canvasHeight = mCanvas.getHeight();
            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gamebackground);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, canvasWidth, canvasHeight, true);
            mCanvas.drawBitmap(scaledBitmap, 0, 0, null);

            int x = mCanvas.getWidth() - mPauseButtonBitmap.getWidth() - 20; // 20 pixels from the right
            int y = 20; // 20 pixels from the top

            if (mPaused) {
                mCanvas.drawBitmap(munPauseButtonBitmap, x, y, null);
            } else {
                mCanvas.drawBitmap(mPauseButtonBitmap, x, y, null);
            }

            Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "myfont.ttf");
            mPaint.setTypeface(typeface);
            mPaint.setColor(Color.argb(255, 220, 20, 60));
            mPaint.setFakeBoldText(true);
            mPaint.setTextSize(100);
            mCanvas.drawText("  " + mScore, 10, 100, mPaint);
            mPaint.setTextSize(40);
            mPaint.setColor(Color.argb(255, 0, 0, 0));
            mCanvas.drawText("Final Project: Group 34", 1800, 990, mPaint);
            mPaint.setFakeBoldText(false);
            mApple.draw(mCanvas, mPaint);
            mObstacle.draw(mCanvas, mPaint);
            mPotion.draw(mCanvas, mPaint);
            mSnake.draw(mCanvas, mPaint);

            if (mPaused) {
                mPaint.setTextSize(150);
                mCanvas.drawText(getResources().getString(R.string.game_paused_text), 750, 600, mPaint);
            }
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    private void displayGameOver(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        paint.setTextSize(100);
        paint.setTextAlign(Paint.Align.CENTER);
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
    }

    public boolean isGameOver() {
        return mGameOver;
    }

    void gameOver() {
        mGameOver = true;
        mPlaying = false;
        mPaused = true;
        saveScore(mScore);  // Ensure scores are saved when game is over

        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            activity.runOnUiThread(() -> {
                // Create an Intent to transition to the GameOverActivity
                Intent intent = new Intent(activity, GameOverActivity.class);

                // Pass the score to the GameOverActivity
                intent.putExtra("SCORE", mScore);

                // Start the GameOverActivity
                activity.startActivity(intent);

                // Finish the current activity
                activity.finish();
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (isGameOver()) {
            return true;
        }

        if (mPaused && !mGameStarted) {
            mGameStarted = true;
            if (mGameJustStarted) {
                mGameJustStarted = false;
            }
        }

        // Get the x and y coordinates of the touch event
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();

        // Calculate the position and dimensions of the pause button on the screen
        int canvasWidth = getWidth();
        int canvasHeight = getHeight();
        int pauseButtonX = canvasWidth - mPauseButtonBitmap.getWidth() - 20;
        int pauseButtonY = 20;
        int pauseButtonWidth = mPauseButtonBitmap.getWidth();
        int pauseButtonHeight = mPauseButtonBitmap.getHeight();

        if ((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            if (x >= pauseButtonX && x <= (pauseButtonX + pauseButtonWidth) && y >= pauseButtonY && y <= (pauseButtonY + pauseButtonHeight)) {
                mPaused = !mPaused;
            }

            else if (!mGameJustStarted || !mPlaying) {
                mPaused = false;
                boolean mDisplayLeaderboard = false;
                newGame();
            }

            else if (!mPaused) {
                mSnake.switchHeading(motionEvent);
            }
            return true;
        }
        return true;
    }

    public void pause() {
        mPlaying = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // Log the exception
        }
    }

    public void resume() {
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        mApple.draw(canvas, paint);
        mObstacle.draw(canvas, paint);
        mSnake.draw(canvas, paint);
        mPotion.draw(canvas, paint);
    }

    @Override
    public void update(Point size) {
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        mNumBlocksHigh = size.y / blockSize;
        mApple.update(size);
        mObstacle.update(size);
        mPotion.update(size);
        mSnake.update(size);
        mSnake.move();
        if (mSnake.checkDinner(mApple.getLocations())) {
            mApple.spawn(difficulty.getAppleFrequency());
            mScore += 1;
        }
        if (mSnake.checkPotion(mPotion.getLocations())) {
            mPotion.spawn(difficulty.getPotionFrequency());
            mScore += 2;
        }

        if (mSnake.detectDeath()) {
            gameOver();
        }
    }

    private void saveScore(int mScore) {
        // Load scores from SharedPreferences
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("scores", null);
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        List<Integer> scores = gson.fromJson(json, type);
        // Initialize scores list if null
        if (scores == null) {
            scores = new ArrayList<>();
        }
        // Add the new score
        if (mScore > 0) {
            scores.add(mScore);
        }
        // Sort and keep the top 10 scores
        scores.sort(Collections.reverseOrder());
        if (scores.size() > 10) {
            scores = scores.subList(0, 10);
        }
        // Save scores to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        json = gson.toJson(scores);
        editor.putString("scores", json);
        editor.apply();
    }

}
