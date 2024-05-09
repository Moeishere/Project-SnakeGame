package com.gamecodeschool.c17snake;

public class Difficulty {
    private int speed;
    private int obstacleFrequency;
    private int appleFrequency;
    private int potionFrequency;

    public Difficulty(int level) {
        switch (level) {
            case 0: // Easy
                this.speed = 5;
                this.appleFrequency = 3;
                this.obstacleFrequency = 1;
                this.potionFrequency = 3;
                break;
            case 1: // Medium
                this.speed = 10;
                this.appleFrequency = 2;
                this.obstacleFrequency = 2;
                this.potionFrequency = 2;
                break;
            case 2: // Hard
                this.speed = 15;
                this.appleFrequency = 1;
                this.obstacleFrequency = 3;
                this.potionFrequency = 1;
                break;
            }
    }

    public int getSpeed() {
        return speed;
    }

    public int getObstacleFrequency() {
        return obstacleFrequency;
    }

    public int getAppleFrequency() {
        return appleFrequency;
    }
    public int getPotionFrequency() {
        return potionFrequency;
    }

}


