package com.gamecodeschool.c17snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import java.util.Random;

public class Egg implements GameObject, Drawable {
    private static final int OFF_SCREEN_X = -10;

    private Point location = new Point();
    private Point mSpawnRange;
    private int mSize;
    private Bitmap mBitmapQuestionMark;
    private Rect mHitbox;  // Add a Rect for the hitbox

    Egg(Context context, Point sr, int s) {
        mSpawnRange = sr;
        mSize = s;
        location.x = OFF_SCREEN_X;
        mBitmapQuestionMark = BitmapFactory.decodeResource(context.getResources(), R.drawable.bluepotion1);
        mBitmapQuestionMark = Bitmap.createScaledBitmap(mBitmapQuestionMark, s, s, false);

        // Initialize the hitbox
        mHitbox = new Rect(location.x, location.y, location.x + mSize, location.y + mSize);
    }

    public void spawn() {
        Random random = new Random();
        location.x = random.nextInt(mSpawnRange.x) + 1;
        location.y = random.nextInt(mSpawnRange.y - 1) + 1;

        // Update the hitbox
        updateHitbox();
    }

    private void updateHitbox() {
        mHitbox.left = location.x * mSize;
        mHitbox.top = location.y * mSize;
        mHitbox.right = mHitbox.left + mSize;
        mHitbox.bottom = mHitbox.top + mSize;
    }

    public Rect getHitbox() {
        return mHitbox;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (mBitmapQuestionMark != null && location != null) {
            canvas.drawBitmap(mBitmapQuestionMark, location.x * mSize, location.y * mSize, null);
        }
    }

    @Override
    public void update(Point point) {
        updateHitbox();
    }
}