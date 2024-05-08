    package com.gamecodeschool.c17snake;

    import android.annotation.SuppressLint;
    import android.content.Context;
    import android.content.res.AssetFileDescriptor;
    import android.content.res.AssetManager;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.graphics.Canvas;
    import android.graphics.Color;
    import android.graphics.Paint;
    import android.graphics.Point;
    import android.graphics.Typeface;
    import android.media.AudioAttributes;
    import android.media.AudioManager;
    import android.media.MediaPlayer;
    import android.media.SoundPool;
    import android.os.Build;
    import android.os.Handler;
    import android.util.Log;
    import android.view.MotionEvent;
    import android.view.SurfaceHolder;
    import android.view.SurfaceView;
    import android.graphics.Rect;
    import com.google.gson.Gson;
    import com.google.gson.reflect.TypeToken;

    import java.io.IOException;
    import java.lang.reflect.Type;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.Iterator;
    import java.util.Random;
    import android.content.SharedPreferences;
    import com.gamecodeschool.c17snake.PowerUp.PowerUpType;

    @SuppressLint("ViewConstructor")
    public class SnakeGame extends SurfaceView implements Runnable, GameObject {
        private int width, height;
        private int blockSize;
        private static final int NUM_BLOCKS_WIDE = 40;
        private static final int TARGET_FPS = 10;
        private static final long MILLIS_PER_SECOND = 1000;
        private static final int BLOCK_SIZE = 40;

        private Context mContext;
        private Bitmap backgroundBitmap;
        private Bitmap mPauseButtonBitmap;
        private Bitmap munPauseButtonBitmap;
        private Bitmap mLeaderboardButtonBitmap;

        private Thread mThread = null;
        private long mNextFrameTime;
        private volatile boolean mPlaying = false;
        private volatile boolean mPaused = true;
        private SoundPool mSP;
        private MediaPlayer mMMP;
        private MediaPlayer mGMP;
        private int mEat_ID = -1;
        private int mCrashID = -1;
        private int mRockID = -1;
        private int mNumBlocksHigh;
        private int mScore;
        private Canvas mCanvas;
        private SurfaceHolder mSurfaceHolder;
        private Paint mPaint;
        private Snake mSnake;
        private int maxX, maxY;
        private Apple mApple;
        private Obstacle mObstacle;
        private Egg mEgg;
        private boolean mGameStarted = false;
        private volatile boolean mGameJustStarted = false;
        private Handler handler = new Handler();
        private Runnable mRelocatePowerUp = new Runnable() {
            @Override
            public void run() {
                if (!powerUps.isEmpty()) {
                    powerUps.get(0).spawn(); // Relocate the existing power-up
                } else {
                    // Re-add the power-up if not present
                    PowerUp.PowerUpType type = PowerUp.generateRandomType();
                    powerUp = new PowerUp(mContext, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), type, blockSize);
                    powerUps.add(powerUp);
                }
                handler.postDelayed(this, 10000); // Reschedule this handler
            }
        };


        // Power-ups
        private Random random = new Random();
        private ArrayList<PowerUp> powerUps = new ArrayList<>();
        private PowerUp powerUp; // Track the single power-up object

        // Leaderboard
        private ArrayList<Integer> scores = new ArrayList<>();
        private boolean mDisplayLeaderboard = false;

        public SnakeGame(Context context, Point size) {
            super(context);
            this.width = size.x;
            this.height = size.y;
            this.mContext = context;
            initializeGame(context, size);
        }

        private void initializeGame(Context context, Point size) {
            blockSize = size.x / NUM_BLOCKS_WIDE; // Initialize block size
            mNumBlocksHigh = size.y / blockSize;
            initializeSoundPool(context);
            initializeMenuMediaPlayer(context);
            initializeGameMediaPlayer(context);
            mSurfaceHolder = getHolder();
            mPaint = new Paint();
            mApple = new Apple(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
            mObstacle = new Obstacle(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
            mEgg = new Egg(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
            mSnake = new Snake(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
            mPauseButtonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause_button);
            mPauseButtonBitmap = Bitmap.createScaledBitmap(mPauseButtonBitmap, 100, 100, false);
            munPauseButtonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.unpause_button);
            munPauseButtonBitmap = Bitmap.createScaledBitmap(munPauseButtonBitmap, 100, 100, false);
            mLeaderboardButtonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leaderboard_button);
            mLeaderboardButtonBitmap = Bitmap.createScaledBitmap(mLeaderboardButtonBitmap, 100, 100, false);

            // Initialize a single power-up object
            powerUp = new PowerUp(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), PowerUp.PowerUpType.SPEED_BOOST, blockSize);
            powerUp.spawn();
           powerUps.clear(); // Clear any existing power-ups
              powerUps.add(powerUp); // Add only one power-up

            // Start periodic relocation of power-ups
            startRelocatingPowerUps();

            if (scores == null) {
                scores = new ArrayList<>();
            }
        }

        private void initializeSoundPool(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();

                mSP = new SoundPool.Builder()
                        .setMaxStreams(5)
                        .setAudioAttributes(audioAttributes)
                        .build();
            } else {
                mSP = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            }
            loadSoundFiles(context);
        }

        private void initializeMenuMediaPlayer(Context context) {
            mMMP = new MediaPlayer();
            try {
                AssetFileDescriptor descriptor = context.getAssets().openFd("menu_music.mp3");
                mMMP.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
                mMMP.setLooping(true); // Loop the menu music
                mMMP.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void initializeGameMediaPlayer(Context context) {
            mGMP = new MediaPlayer();
            try {
                AssetFileDescriptor descriptor = context.getAssets().openFd("ingame_music.mp3");
                mGMP.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
                mGMP.setLooping(true); // Loop the game music
                mGMP.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void loadSoundFiles(Context context) {
            try {
                AssetManager assetManager = context.getAssets();
                AssetFileDescriptor descriptor;

                descriptor = assetManager.openFd("snake_eat.mp3");
                mEat_ID = mSP.load(descriptor, 0);
                descriptor = assetManager.openFd("snake_death.mp3");
                mCrashID = mSP.load(descriptor, 0);
                descriptor = assetManager.openFd("rock_destroy.mp3");
                mRockID = mSP.load(descriptor, 0);

            } catch (IOException e) {
                Log.d("error", "failed to load sound files");
            }
        }

        private void playMenuMusic() {
            if (mMMP != null && !mMMP.isPlaying()) {
                mMMP.start();
            }
        }

        private void stopMenuMusic() {
            if (mMMP != null && mMMP.isPlaying()) {
                mMMP.pause();
            }
        }

        private void playGameMusic() {
            if (mGMP != null && !mGMP.isPlaying()) {
                mGMP.start();
            }
        }

        private void stopGameMusic() {
            if (mGMP != null && mGMP.isPlaying()) {
                mGMP.pause();
            }
        }

        public void newGame() {
            mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);
            mSnake.resetSpeed(); // Reset the speed of the snake
            mApple.spawn();
            mEgg.spawn(); // Spawn the Egg object
            mObstacle.spawn();
            powerUps.clear(); // Ensure no power-ups persist
            mScore = 0;
            mNextFrameTime = System.currentTimeMillis();
            mGameJustStarted = true;
            playGameMusic();
        }


        @Override
        public void run() {
            while (mPlaying) {
                if (!mPaused) {
                    if (updateRequired()) {
                        update();
                        collectPowerUps(); // Collect power-ups on each update
                    }
                }
                draw();  // Include power-ups in the draw method
            }
        }

        public boolean updateRequired() {
            if (mNextFrameTime <= System.currentTimeMillis()) {
                mNextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / TARGET_FPS;
                return true;
            }
            return false;
        }

        public void update() {
            if (mSnake.isAlive()) {
                mSnake.move();

                // Check if the snake has collided with the apple
                if (mSnake.checkDinner(mApple.getLocation())) {
                    mApple.spawn(); // Respawn the apple
                    int scoreIncrease = new Random().nextInt(5) + 1;
                    mScore += scoreIncrease;
                    mSP.play(mEat_ID, 1, 1, 0, 0, 1);
                }

                // Check if the snake has collided with the egg
                if (Rect.intersects(mSnake.getBounds(), mEgg.getHitbox())) {
                    mEgg.spawn(); // Respawn the Egg
                    mSP.play(mEat_ID, 1, 1, 0, 0, 1); // Play sound effect for eating the Egg
                   // PowerUp.PowerUpType type = PowerUp.generateRandomType();
                    // powerUp = new PowerUp(mContext, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), type, blockSize);
                    powerUps.clear(); // Ensure there's only one power-up at a time
                    powerUps.add(powerUp);
                }

                // Check if the snake has collided with a power-up
                collectPowerUps(); // Ensure the effect is applied

                // Check if the snake has collided with the rock
                if (mSnake.checkRock(mObstacle.getLocation())) {
                    mObstacle.spawn(); // Respawn the rock
                    int scoreDecrease = new Random().nextInt(5) + 1;
                    mScore = Math.max(0, mScore - scoreDecrease);
                    mSP.play(mRockID, 1, 1, 0, 0, 1);
                }

                // Check if the snake has collided with itself or the wall
                if (mSnake.detectDeath()) {
                    stopGameMusic();
                    mSP.play(mCrashID, 1, 1, 0, 0, 1);
                    mPaused = true;
                    mGameStarted = false;
                    saveScore(mScore);
                }

                // Toggle menu music based on game state
                if (!mGMP.isPlaying()) {
                    playMenuMusic();
                } else {
                    stopMenuMusic();
                }
            }
        }

        public void draw() {
            if (mSurfaceHolder.getSurface().isValid()) {
                mCanvas = mSurfaceHolder.lockCanvas();
                // Background adding code
                if (backgroundBitmap == null) {
                    backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gamebackground);
                }
                int canvasWidth = mCanvas.getWidth();
                int canvasHeight = mCanvas.getHeight();
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(backgroundBitmap, canvasWidth, canvasHeight, true);
                mCanvas.drawBitmap(scaledBitmap, 0, 0, null);

                int x = mCanvas.getWidth() - mPauseButtonBitmap.getWidth() - 20; // 20 pixels from the right
                int y = 20; // 20 pixels from the top
                if (mPaused) {
                    mCanvas.drawBitmap(munPauseButtonBitmap, x, y, null);
                } else {
                    mCanvas.drawBitmap(mPauseButtonBitmap, x, y, null);
                }

                int leaderboardX = mCanvas.getWidth() - mLeaderboardButtonBitmap.getWidth() - 200;

                if (!mGameStarted || !mPlaying) {
                    mCanvas.drawBitmap(mLeaderboardButtonBitmap, leaderboardX, y, null);
                }

                if (mDisplayLeaderboard) {
                    // Sort the scores in descending order
                    ArrayList<Integer> sortedScores = new ArrayList<>(scores);
                    Collections.sort(sortedScores, Collections.reverseOrder());

                    // Set the custom font
                    Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "Dune_Rise.ttf");
                    mPaint.setTypeface(typeface);
                    mPaint.setFakeBoldText(true);

                    // The top 10 scores
                    mPaint.setColor(Color.argb(255, 0, 0, 0));
                    mPaint.setTextSize(70); // Increase text size

                    mCanvas.drawText("Top 10 High Scores", mCanvas.getWidth() / 4f, 100, mPaint);

                    for (int i = 0; i < Math.min(10, sortedScores.size()); i++) {
                        String scoreText = "Score " + (i + 1) + ": " + sortedScores.get(i);
                        float x2 = mCanvas.getWidth() / 4f;
                        float y2 = mCanvas.getHeight() / 2f + i * 80;
                        mCanvas.drawText(scoreText, x2, y2, mPaint);
                    }

                    // "Tap to start new game" at the bottom of the screen
                    mCanvas.drawText("Tap to start new game", mCanvas.getWidth() / 4f, mCanvas.getHeight() - 100, mPaint);
                }

                Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "myfont.ttf");
                mPaint.setTypeface(typeface);

                mPaint.setColor(Color.argb(255, 220, 20, 60));
                mPaint.setFakeBoldText(true);
                mPaint.setTextSize(100);
                mCanvas.drawText("  " + mScore, 10, 100, mPaint);
                mPaint.setTextSize(40);
                mPaint.setColor(Color.argb(255, 0, 0, 0));
                mCanvas.drawText("Final Project:Group 34", 1800, 990, mPaint);
                mPaint.setFakeBoldText(false);
                mApple.draw(mCanvas, mPaint);
                mObstacle.draw(mCanvas, mPaint);
                mSnake.draw(mCanvas, mPaint);
                mEgg.draw(mCanvas, mPaint); // Draw the Egg object
                drawPowerUps(mCanvas, mPaint);

                if (mPaused && !mGameStarted) {
                    mPaint.setColor(Color.argb(255, 0, 0, 0));
                    mPaint.setTextSize(200);
                    mCanvas.drawText(getResources().getString(R.string.tap_to_play), 650, 650, mPaint);
                }
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }

        private void drawPowerUps(Canvas canvas, Paint paint) {
            for (PowerUp powerUp : powerUps) {
                if (powerUp != null) {
                    powerUp.draw(canvas, paint);
                }
            }
        }

        private void collectPowerUps() {
            Rect snakeBounds = mSnake.getBounds();
            Iterator<PowerUp> iterator = powerUps.iterator();
            while (iterator.hasNext()) {
                PowerUp powerUp = iterator.next();
                if (Rect.intersects(snakeBounds, powerUp.getHitbox())) {
                    powerUp.applyEffect(mSnake); // Apply the effect to the snake
                    iterator.remove(); // Remove the power-up after collecting
                }
            }
        }

        private void startRelocatingPowerUps() {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (PowerUp powerUp : powerUps) {
                        powerUp.spawn(); // Relocate each power-up
                    }
                    handler.postDelayed(this, 10000); // Schedule next relocation in 10 seconds
                }
            }, 10000);
        }

        private void saveScore(int mScore) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("scores", null);
            Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
            scores = gson.fromJson(json, type);

            if (scores == null) {
                scores = new ArrayList<>();
            }

            if (mScore > 0) {
                scores.add(mScore);
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            json = gson.toJson(scores);
            editor.putString("scores", json);
            editor.apply();
        }

        private void printLeaderboard() {
            Log.d("Leaderboard", "Scores: " + scores);
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (mPaused && !mGameStarted) {
                mGameStarted = true;
                resume();

                if (mGameJustStarted) {
                    mGameJustStarted = false;
                }
                collectPowerUps();

                return true;
            }

            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();

            int canvasWidth = getWidth();
            int canvasHeight = getHeight();
            int pauseButtonX = canvasWidth - mPauseButtonBitmap.getWidth() - 20;
            int pauseButtonY = 20;
            int pauseButtonWidth = mPauseButtonBitmap.getWidth();
            int pauseButtonHeight = mPauseButtonBitmap.getHeight();

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    if (x >= pauseButtonX && x <= (pauseButtonX + pauseButtonWidth) && y >= pauseButtonY && y <= (pauseButtonY + pauseButtonHeight)) {
                        mPaused = !mPaused;
                    } else if (x >= canvasWidth - mLeaderboardButtonBitmap.getWidth() - 200 && x <= (canvasWidth - 200) && y >= 20 && y <= 120) {
                        Log.d("Leaderboard", "Leaderboard button clicked");
                        printLeaderboard();
                        mDisplayLeaderboard = true;
                    } else if (!mGameJustStarted || !mPlaying) {
                        mPaused = false;
                        mDisplayLeaderboard = false;
                        newGame();
                    } else if (!mPaused){
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
                Log.d("Error", "Thread join interrupted");
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
            mEgg.draw(canvas, paint);
        }


        @Override
        public void update(Point size) {
            int blockSize = size.x / NUM_BLOCKS_WIDE;
            mNumBlocksHigh = size.y / blockSize;
            mApple.update(size);
            mObstacle.update(size);
            mEgg.update(size);
            mSnake.update(size);
        }
    }
