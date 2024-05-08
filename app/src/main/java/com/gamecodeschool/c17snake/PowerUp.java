package com.gamecodeschool.c17snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.Random;

public class PowerUp implements GameObject, Drawable {
    private static final int OFF_SCREEN_X = -10;

    private Point location = new Point();
    private Point mSpawnRange;
    private int mSize;
    private Bitmap mBitmapPowerUp;
    private Rect mHitbox;

    public enum PowerUpType {
        SPEED_BOOST,
        INVULNERABILITY,
        EAT_ROCKS
    }

    private PowerUpType type;

    PowerUp(Context context, Point sr, PowerUpType type, int s) {
        mSpawnRange = sr;
        mSize = s;
        location.x = OFF_SCREEN_X;
        this.type = type;
        mBitmapPowerUp = BitmapFactory.decodeResource(context.getResources(), R.drawable.bluepotion1);
        mBitmapPowerUp = Bitmap.createScaledBitmap(mBitmapPowerUp, s, s, false);

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
        if (mBitmapPowerUp != null && location != null) {
            canvas.drawBitmap(mBitmapPowerUp, location.x * mSize, location.y * mSize, null);
        }
    }

    @Override
    public void update(Point point) {
        updateHitbox();
    }

    public boolean checkCollisionWithSnake(Snake snake) {
        return Rect.intersects(snake.getBounds(), getHitbox());
    }

    public void applyEffect(Snake snake) {
        switch (type) {
            case SPEED_BOOST:
                snake.increaseSpeed(); // Increment the snake speed by 1
                break;
            case INVULNERABILITY:
                snake.setInvulnerable(true);
                break;
            case EAT_ROCKS:
                snake.setEatRocks(true);
                break;
        }
    }

    public static PowerUpType generateRandomType() {
        Random random = new Random();
        PowerUpType[] types = PowerUpType.values();
        return types[random.nextInt(types.length)];
    }
}
