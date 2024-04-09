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
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

@SuppressLint("ViewConstructor")
public class SnakeGame extends SurfaceView implements Runnable, GameObject {
    private static final int NUM_BLOCKS_WIDE = 40;
    private static final int TARGET_FPS = 10;
    private static final long MILLIS_PER_SECOND = 1000;

    private Context mContext;
    private Bitmap backgroundBitmap;
    private Bitmap mPauseButtonBitmap;
    private Bitmap munPauseButtonBitmap;

    private Thread mThread = null;
    private long mNextFrameTime;
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrashID = -1;
    private int mNumBlocksHigh;
    private int mScore;
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;
    private Snake mSnake;
    private Apple mApple;
    private boolean mGameStarted = false;
    private volatile boolean mGameJustStarted = false;


    public SnakeGame(Context context, Point size) {
        super(context);
        initializeGame(context, size);
    }

    private void initializeGame(Context context, Point size) {
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        mNumBlocksHigh = size.y / blockSize;
        initializeSoundPool(context);
        mSurfaceHolder = getHolder();
        mPaint = new Paint();
        mApple = new Apple(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mSnake = new Snake(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mPauseButtonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause_button);
        mPauseButtonBitmap = Bitmap.createScaledBitmap(mPauseButtonBitmap, 100, 100, false);
        munPauseButtonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.unpause_button);
        munPauseButtonBitmap = Bitmap.createScaledBitmap(munPauseButtonBitmap, 100, 100, false);

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

    private void loadSoundFiles(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("get_apple.ogg");
            mEat_ID = mSP.load(descriptor, 0);
            descriptor = assetManager.openFd("snake_death.ogg");
            mCrashID = mSP.load(descriptor, 0);

        } catch (IOException e) {
            // Log the exception
        }
    }

    public void newGame() {
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);
        mApple.spawn();
        mScore = 0;
        mNextFrameTime = System.currentTimeMillis();
        mGameJustStarted = true;

    }

    @Override
    public void run() {
        while (mPlaying) {
            if (!mPaused) {
                if (updateRequired()) {
                    update();
                }
            }
            draw();
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
        mSnake.move();
        if (mSnake.checkDinner(mApple.getLocation())) {
            mApple.spawn();
            mScore = mScore + 1;
            mSP.play(mEat_ID, 1, 1, 0, 0, 1);
        }

        if (mSnake.detectDeath()) {
            mSP.play(mCrashID, 1, 1, 0, 0, 1);
            mPaused = true;
            mGameStarted = false;
        }
    }

    public void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            mCanvas = mSurfaceHolder.lockCanvas();
            // background adding code
            if (backgroundBitmap == null) {
                backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_image1);
            }
            int canvasWidth = mCanvas.getWidth();
            int canvasHeight = mCanvas.getHeight();
            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_image1);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, canvasWidth, canvasHeight, true);
            mCanvas.drawBitmap(scaledBitmap, 0, 0, null);

            int x = mCanvas.getWidth() - mPauseButtonBitmap.getWidth() - 20;
            int y = 20;
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
            mCanvas.drawText("Final Project:Group 34", 1800, 990, mPaint);
            mPaint.setFakeBoldText(false);
            mApple.draw(mCanvas, mPaint);
            mSnake.draw(mCanvas, mPaint);
            if (mPaused && !mGameStarted) {
                mPaint.setColor(Color.argb(255, 0, 0, 0));
                mPaint.setTextSize(200);
                mCanvas.drawText(getResources().getString(R.string.tap_to_play), 650, 650, mPaint);
            }
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (mPaused && !mGameStarted) {
            mGameStarted = true;

            if (mGameJustStarted) {
                mGameJustStarted = false;
            }
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
                } else if (!mGameJustStarted || !mPlaying) {
                    mPaused = false;
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
        mSnake.draw(canvas, paint);
    }

    @Override
    public void update(Point size) {
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        mNumBlocksHigh = size.y / blockSize;
        mApple.update(size);
        mSnake.update(size);
    }
}
