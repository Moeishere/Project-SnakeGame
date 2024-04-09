package com.gamecodeschool.c17snake;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
public interface GameObject {
    void update(Point size);
    void draw(Canvas canvas, Paint paint);
}