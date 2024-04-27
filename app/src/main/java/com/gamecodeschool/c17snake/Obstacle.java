package com.gamecodeschool.c17snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.Random;

public class Obstacle implements GameObject, Drawable {
    private static final int OFF_SCREEN_X = -10;

    private Point location = new Point();
    private Point mSpawnRange;
    private int mSize;
    private Bitmap mBitmapObstacle;

    Obstacle(Context context, Point sr, int s){
        mSpawnRange = sr;
        mSize = s;
        location.x = OFF_SCREEN_X;
        mBitmapObstacle = BitmapFactory.decodeResource(context.getResources(), R.drawable.rock);
        mBitmapObstacle = Bitmap.createScaledBitmap(mBitmapObstacle, s, s, false);
    }

    public void spawn(){
        Random random = new Random();
        location.x = random.nextInt(mSpawnRange.x) + 1;
        location.y = random.nextInt(mSpawnRange.y - 1) + 1;
    }

    public Point getLocation(){
        return location;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (mBitmapObstacle != null && location != null) {
            canvas.drawBitmap(mBitmapObstacle, location.x * mSize, location.y * mSize, null);
        }
    }

    @Override
    public void update(Point point){
    }
}
