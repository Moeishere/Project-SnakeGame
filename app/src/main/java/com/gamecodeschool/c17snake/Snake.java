package com.gamecodeschool.c17snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.List;

public class Snake implements GameObject, Movable, Drawable {
    private static final int OUT_OF_BOUNDS = -1;
    private static final int NEW_SEGMENT_POSITION = -10;
    private static final int HALF = 2;
    private final ArrayList<Point> segmentLocations;
    private final int segmentSize;
    private final Point moveRange;
    private final int halfWayPoint;
    private enum Heading {UP, RIGHT, DOWN, LEFT}
    private Heading heading = Heading.RIGHT;
    private Bitmap bitmapHeadRight, bitmapHeadLeft, bitmapHeadUp, bitmapHeadDown, bitmapBody;

    public Snake(Context context, Point moveRange, int segmentSize) {
        this.segmentLocations = new ArrayList<>();
        this.segmentSize = segmentSize;
        this.moveRange = moveRange;
        initializeBitmaps(context, segmentSize);
        this.halfWayPoint = moveRange.x * segmentSize / 2;
    }

    private void initializeBitmaps(Context context, int size) {
        bitmapHeadRight = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);
        bitmapHeadRight = Bitmap.createScaledBitmap(bitmapHeadRight, size, size, false);
        bitmapBody = BitmapFactory.decodeResource(context.getResources(), R.drawable.body);
        bitmapBody = Bitmap.createScaledBitmap(bitmapBody, size, size, false);

        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        bitmapHeadLeft = Bitmap.createBitmap(bitmapHeadRight, 0, 0, size, size, matrix, false);

        matrix.setRotate(-90);
        bitmapHeadUp = Bitmap.createBitmap(bitmapHeadRight, 0, 0, size, size, matrix, false);

        matrix.setRotate(180);
        bitmapHeadDown = Bitmap.createBitmap(bitmapHeadRight, 0, 0, size, size, matrix, false);
    }

    void reset(int w, int h) {
        heading = Heading.RIGHT;
        segmentLocations.clear();
        segmentLocations.add(new Point(w / 2, h / 2));
    }

    @Override
    public void move() {
        moveBody();
        moveHead();
    }

    private void moveBody() {
        for (int i = segmentLocations.size() - 1; i > 0; i--) {
            segmentLocations.get(i).set(segmentLocations.get(i - 1).x, segmentLocations.get(i - 1).y);
        }
    }

    private void moveHead() {
        Point p = segmentLocations.get(0);
        switch (heading) {
            case UP:
                p.y--;
                break;
            case RIGHT:
                p.x++;
                break;
            case DOWN:
                p.y++;
                break;
            case LEFT:
                p.x--;
                break;
        }
    }

    boolean detectDeath() {
        Point head = segmentLocations.get(0);
        return isOutOfBounds(head) || isEatingItself();
    }

    private boolean isOutOfBounds(Point head) {
        return head.x == OUT_OF_BOUNDS || head.x > moveRange.x || head.y == OUT_OF_BOUNDS || head.y > moveRange.y;
    }

    private boolean isEatingItself() {
        Point head = segmentLocations.get(0);
        for (int i = segmentLocations.size() - 1; i > 0; i--) {
            if (head.equals(segmentLocations.get(i))) {
                return true;
            }
        }
        return false;
    }


    boolean checkDinner(List<Point> apples) {
        Point head = segmentLocations.get(0);
        for (Point apple : apples) {
            if (head.equals(apple)) {
                segmentLocations.add(new Point(-10, -10));
                apples.remove(apple);
                return true;
            }
        }
        return false;
    }

    boolean checkRock(List<Point> obstacles) {
        if (segmentLocations.size() <= 1) {
            return false;
        }
        Point head = segmentLocations.get(0);
        for (Point obstacle : obstacles) {
            if (head.equals(obstacle)) {
                segmentLocations.remove(segmentLocations.size() - 1);
                return true;
            }
        }

        return false;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (!segmentLocations.isEmpty()) {
            drawHead(canvas, paint);
            drawBody(canvas, paint);
        }
    }

    private void drawHead(Canvas canvas, Paint paint) {
        Bitmap bitmapHead = null;
        switch (heading) {
            case RIGHT:
                bitmapHead = bitmapHeadRight;
                break;
            case LEFT:
                bitmapHead = bitmapHeadLeft;
                break;
            case UP:
                bitmapHead = bitmapHeadUp;
                break;
            case DOWN:
                bitmapHead = bitmapHeadDown;
                break;
            default:
                throw new IllegalStateException("Invalid heading: " + heading);
        }
        canvas.drawBitmap(bitmapHead, segmentLocations.get(0).x * segmentSize, segmentLocations.get(0).y * segmentSize, paint);
    }

    private void drawBody(Canvas canvas, Paint paint) {
        for (int i = 1; i < segmentLocations.size(); i++) {
            canvas.drawBitmap(bitmapBody, segmentLocations.get(i).x * segmentSize, segmentLocations.get(i).y * segmentSize, paint);
        }
    }

    void switchHeading(MotionEvent motionEvent) {
        if (motionEvent.getX() >= halfWayPoint) {
            switch (heading) {
                case UP:
                    heading = Heading.RIGHT;
                    break;
                case RIGHT:
                    heading = Heading.DOWN;
                    break;
                case DOWN:
                    heading = Heading.LEFT;
                    break;
                case LEFT:
                    heading = Heading.UP;
                    break;
            }
        } else {
            switch (heading) {
                case UP:
                    heading = Heading.LEFT;
                    break;
                case LEFT:
                    heading = Heading.DOWN;
                    break;
                case DOWN:
                    heading = Heading.RIGHT;
                    break;
                case RIGHT:
                    heading = Heading.UP;
                    break;
            }
        }
    }

    @Override
    public void update(Point size) {}
}
