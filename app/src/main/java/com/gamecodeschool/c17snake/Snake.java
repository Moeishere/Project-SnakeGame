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

public class Snake implements GameObject, Movable, Drawable{
    private static final int OUT_OF_BOUNDS = -1;
    private static final int NEW_SEGMENT_POSITION = -10;
    private static final int HALF = 2;
    private final ArrayList<Point> segmentLocations;
    private final int mSegmentSize;
    private final Point mMoveRange;
    private final int halfWayPoint;
    private enum Heading {UP, RIGHT, DOWN, LEFT}
    private Heading heading = Heading.RIGHT;
    private Bitmap mBitmapHeadRight;
    private Bitmap mBitmapHeadLeft;
    private Bitmap mBitmapHeadUp;
    private Bitmap mBitmapHeadDown;
    private Bitmap mBitmapBody;

    Snake(Context context, Point mr, int ss) {
        segmentLocations = new ArrayList<>();
        mSegmentSize = ss;
        mMoveRange = mr;
        mBitmapHeadRight = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);
        mBitmapHeadLeft = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);
        mBitmapHeadUp = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);
        mBitmapHeadDown = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);
        mBitmapHeadRight = Bitmap.createScaledBitmap(mBitmapHeadRight, ss, ss, false);
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        mBitmapHeadLeft = Bitmap.createBitmap(mBitmapHeadRight, 0, 0, ss, ss, matrix, true);
        matrix.preRotate(-90);
        mBitmapHeadUp = Bitmap.createBitmap(mBitmapHeadRight, 0, 0, ss, ss, matrix, true);
        matrix.preRotate(180);
        mBitmapHeadDown = Bitmap.createBitmap(mBitmapHeadRight, 0, 0, ss, ss, matrix, true);
        mBitmapBody = BitmapFactory.decodeResource(context.getResources(), R.drawable.body);
        mBitmapBody = Bitmap.createScaledBitmap(mBitmapBody, ss, ss, false);
        halfWayPoint = mr.x * ss / 2;
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
            segmentLocations.get(i).x = segmentLocations.get(i - 1).x;
            segmentLocations.get(i).y = segmentLocations.get(i - 1).y;
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
        return isOutOfBounds(head) || isEatingItself(head);
    }

    private boolean isOutOfBounds(Point head) {
        return head.x == OUT_OF_BOUNDS || head.x > mMoveRange.x || head.y == OUT_OF_BOUNDS || head.y > mMoveRange.y;
    }

    private boolean isEatingItself(Point head) {
        for (int i = segmentLocations.size() - 1; i > 0; i--) {
            if (head.x == segmentLocations.get(i).x && head.y == segmentLocations.get(i).y) {
                return true;
            }
        }
        return false;
    }


    /*boolean checkDinner(Point l) {
        if (segmentLocations.get(0).x == l.x && segmentLocations.get(0).y == l.y) {
            segmentLocations.add(new Point(-10, -10));
            return true;
        }
        return false;
    }*/

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
            // If the snake has only one segment (the head), do not remove any segment
            return false;
        }
        Point head = segmentLocations.get(0);
        for (Point obstacle : obstacles) {
            if (head.equals(obstacle)) {
                segmentLocations.remove(segmentLocations.size() - 1);
                return true;
            }
        }


        /*if (segmentLocations.get(0).x == l.x && segmentLocations.get(0).y == l.y) {
            // Remove the last segment from the snake
            segmentLocations.remove(segmentLocations.size() - 1);
            return true;
        }*/
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
        Bitmap bitmapHead;
        switch (heading) {
            case RIGHT:
                bitmapHead = mBitmapHeadRight;
                break;
            case LEFT:
                bitmapHead = mBitmapHeadLeft;
                break;
            case UP:
                bitmapHead = mBitmapHeadUp;
                break;
            case DOWN:
                bitmapHead = mBitmapHeadDown;
                break;
            default:
                throw new IllegalStateException("Invalid heading: " + heading);
        }
        canvas.drawBitmap(bitmapHead, segmentLocations.get(0).x * mSegmentSize, segmentLocations.get(0).y * mSegmentSize, paint);
    }

    private void drawBody(Canvas canvas, Paint paint) {
        for (int i = 1; i < segmentLocations.size(); i++) {
            canvas.drawBitmap(mBitmapBody, segmentLocations.get(i).x * mSegmentSize, segmentLocations.get(i).y * mSegmentSize, paint);
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