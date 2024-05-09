package com.gamecodeschool.c17snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Apple implements GameObject, Drawable {
    private static final int OFF_SCREEN_X = -10;
    private List<Point> locations;

    private Point location = new Point();
    private Point mSpawnRange;
    private int mSize;
    private Bitmap mBitmapApple;

    Apple(Context context, Point sr, int s){
        mSpawnRange = sr;
        mSize = s;
        //location.x = OFF_SCREEN_X;
        this.locations = new ArrayList<>();
        mBitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);
        mBitmapApple = Bitmap.createScaledBitmap(mBitmapApple, s, s, false);
    }

    /*public void spawn(){
        Random random = new Random();
        location.x = random.nextInt(mSpawnRange.x) + 1;
        location.y = random.nextInt(mSpawnRange.y - 1) + 1;
    }*/
    public void spawn(){
        Random random = new Random();

        int x = random.nextInt(mSpawnRange.x) + 1;
        int y = random.nextInt(mSpawnRange.y - 1) + 1;
        locations.add(new Point(x, y));



    }
    public void spawn(int appleFrequency){
        Random random = new Random();
        for (int i = 0; i < appleFrequency; i++) {
            int x = random.nextInt(mSpawnRange.x) + 1;
            int y = random.nextInt(mSpawnRange.y - 1) + 1;
            locations.add(new Point(x, y));

        }

    }

    public Point getLocation(){
        return location;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (mBitmapApple != null && location != null) {
            for (Point location : locations){
                canvas.drawBitmap(mBitmapApple, location.x * mSize, location.y * mSize, null);
            }
        }
    }

    @Override
    public void update(Point point){
    }
    public List<Point> getLocations(){
        return locations;
    }
}
