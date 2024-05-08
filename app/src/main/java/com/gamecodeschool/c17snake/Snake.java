package com.gamecodeschool.c17snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.LinkedList;

public class Snake implements GameObject, Movable, Drawable {
    private int mSpeed = 1; // Initial speed
    private int mOriginalSpeed = mSpeed;
    private final int SPEED_INCREMENT = 1; // Speed increment value
    private static final int MAX_SPEED = 10; // Maximum speed value
    private static final int OUT_OF_BOUNDS = -1;
    private static final int NEW_SEGMENT_POSITION = -10;
    private final ArrayList<Point> segmentLocations;
    private final ArrayList<Integer> segmentColors; // Store segment colors separately
    private int colorIndex = 0; // Index to track the current color
    private final int mSegmentSize;
    private final Point mMoveRange;
    private final int halfWayPoint;
    private Rect mHitbox; // Single hitbox for the snake head

    private enum Heading {UP, RIGHT, DOWN, LEFT}

    private Heading heading = Heading.RIGHT;
    private Bitmap mBitmapHeadRight;
    private Bitmap mBitmapHeadLeft;
    private Bitmap mBitmapHeadUp;
    private Bitmap mBitmapHeadDown;
    private Bitmap mBitmapBody;

    private boolean invulnerable = false;
    private boolean eatRocks = false;
    private boolean alive = true;
    private LinkedList<Point> segments = new LinkedList<>();

    Snake(Context context, Point mr, int ss) {
        segmentColors = new ArrayList<>();
        segmentColors.add(Color.GREEN); // Initial color for all segments
        segmentColors.add(Color.RED); // Second color
        segmentColors.add(Color.BLUE); // Third color

        segmentLocations = new ArrayList<>();
        mSegmentSize = ss;
        mMoveRange = mr;
        mBitmapHeadRight = BitmapFactory.decodeResource(context.getResources(), R.drawable.worm);
        mBitmapHeadLeft = BitmapFactory.decodeResource(context.getResources(), R.drawable.worm);
        mBitmapHeadUp = BitmapFactory.decodeResource(context.getResources(), R.drawable.worm);
        mBitmapHeadDown = BitmapFactory.decodeResource(context.getResources(), R.drawable.worm);
        mBitmapHeadRight = Bitmap.createScaledBitmap(mBitmapHeadRight, ss, ss, false);
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        mBitmapHeadLeft = Bitmap.createBitmap(mBitmapHeadRight, 0, 0, ss, ss, matrix, true);
        matrix.preRotate(-90);
        mBitmapHeadUp = Bitmap.createBitmap(mBitmapHeadRight, 0, 0, ss, ss, matrix, true);
        matrix.preRotate(180);
        mBitmapHeadDown = Bitmap.createBitmap(mBitmapHeadRight, 0, 0, ss, ss, matrix, true);
        mBitmapBody = BitmapFactory.decodeResource(context.getResources(), R.drawable.bodyworm);
        mBitmapBody = Bitmap.createScaledBitmap(mBitmapBody, ss, ss, false);
        halfWayPoint = mr.x * ss / 2;

        // Initialize the snake head's hitbox
        mHitbox = new Rect(0, 0, ss, ss);
    }

    // Reset the snake to its initial state
    void reset(int w, int h) {
        heading = Heading.RIGHT;
        segmentLocations.clear();
        segmentLocations.add(new Point(w / 2, h / 2));
        updateHitbox();
        mSpeed = mOriginalSpeed; // Reset speed to original
        invulnerable = false;
        eatRocks = false;
        alive = true;
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
        for (int i = 0; i < mSpeed; i++) { // Repeat movement according to speed
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
        updateHitbox();
    }

    // Check if the snake is dead (e.g., collided with a wall or itself)
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

    // Check if the snake has eaten a "dinner" (e.g., food)
    boolean checkDinner(Point l) {
        if (segmentLocations.get(0).x == l.x && segmentLocations.get(0).y == l.y) {
            segmentLocations.add(new Point(NEW_SEGMENT_POSITION, NEW_SEGMENT_POSITION));
            return true;
        }
        return false;
    }


    // Check if the snake has collided with a rock
    boolean checkRock(Point l) {
        if (segmentLocations.size() <= 1) {
            // If the snake has only one segment (the head), do not remove any segment
            return false;
        }
        if (segmentLocations.get(0).x == l.x && segmentLocations.get(0).y == l.y) {
            // Remove the last segment from the snake
            segmentLocations.remove(segmentLocations.size() - 1);
            return true;
        }
        return false;
    }

    // Draw the snake on the canvas
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
        if (!segmentLocations.isEmpty()) {
            for (int i = 1; i < segmentLocations.size(); i++) {
                canvas.drawBitmap(mBitmapBody, segmentLocations.get(i).x * mSegmentSize, segmentLocations.get(i).y * mSegmentSize, paint);
            }
        }
    }

    // Change the snake's heading based on touch input
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
        updateHitbox();
    }

    // Retrieve the bounding rectangle of the snake
    public Rect getBounds() {
        return mHitbox;
    }

    private void updateHitbox() {
        Point head = segmentLocations.get(0);
        mHitbox.left = head.x * mSegmentSize;
        mHitbox.top = head.y * mSegmentSize;
        mHitbox.right = mHitbox.left + mSegmentSize;
        mHitbox.bottom = mHitbox.top + mSegmentSize;
    }

    // Increase the speed of the snake
    public void increaseSpeed() {
        if (mSpeed < MAX_SPEED) {
            mSpeed += SPEED_INCREMENT;
        }
    }

    public void resetSpeed() {
        mSpeed = mOriginalSpeed;
    }
    public boolean isAlive() {
        return alive;
    }

    public void die() {
        alive = false;
        resetSpeed(); // Reset speed when the snake dies
    }

    // Retrieve the current speed of the snake
    public int getSpeed() {
        return mSpeed;
    }

    // Check if the snake is invulnerable
    public boolean isInvulnerable() {
        return invulnerable;
    }

    // Set the invulnerability state of the snake
    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    // Set whether the snake can eat rocks without losing points
    public void setEatRocks(boolean eatRocks) {
        this.eatRocks = eatRocks;
    }

    // Allow the snake to eat rocks without losing points
    public void eatRocks() {
        if (segmentLocations.size() > 1) {
            segmentLocations.remove(segmentLocations.size() - 1);
        }
    }

    public void changeColor() {
        // Ensure the snake has segments
        if (segmentLocations.isEmpty()) {
            return;
        }

        // Cycle through the predefined set of colors
        colorIndex = (colorIndex + 1) % segmentColors.size();
        int newColor = segmentColors.get(colorIndex);

        // Set the new color for all segments
        for (int i = 0; i < segmentLocations.size(); i++) {
            segmentColors.set(i, newColor);
        }
    }

    // Ensure reset method in Snake class does reset all properties

    public void update(Point size) {
        updateHitbox();
    }
}
