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

public class Potion implements GameObject, Drawable {
    private static final int OFF_SCREEN_X = -10;

    private Point location = new Point();
    private List<Point> locations;
    private Point mSpawnRange;
    private int mSize;
    private Bitmap mBitmapPotion;

    Potion(Context context, Point sr, int s){
        mSpawnRange = sr;
        mSize = s;
        //location.x = OFF_SCREEN_X;
        this.locations = new ArrayList<>();
        mBitmapPotion = BitmapFactory.decodeResource(context.getResources(), R.drawable.bluepotion1);
        mBitmapPotion = Bitmap.createScaledBitmap(mBitmapPotion, s, s, false);
    }

    public void spawn(){
        Random random = new Random();
       int x = random.nextInt(mSpawnRange.x) + 1;
        int y = random.nextInt(mSpawnRange.y - 1) + 1;
        locations.add(new Point(x, y));
    }



    public void spawn(int PotionFrequency){
        Random random = new Random();
        for (int i = 0; i < PotionFrequency; i++) {
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
        if (mBitmapPotion != null && location != null) {
            for (Point location : locations){
                canvas.drawBitmap(mBitmapPotion, location.x * mSize, location.y * mSize, null);

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

