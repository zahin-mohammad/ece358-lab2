package com.ece358;

import java.util.Random;

public class ExponentialBackoff {
    private Random r;
    private int counter = 0;
    private int counterSaturation = 0;
    private double bitTime = 0;

    ExponentialBackoff(double linkCapacity){
        this(0, 10, 512, linkCapacity);
    }
    ExponentialBackoff(int counter, int counterSaturation, int bitTimeSize, double linkCapacity){
        this.r = new Random();
        this.bitTime = bitTimeSize/linkCapacity;
        this.counter = counter;
        this.counterSaturation = counterSaturation;
    }

    public double getWaitTime(){
        counter++;
        if (counter > counterSaturation){
            counter = 0;
            return -1;
        }
        return r.nextInt((int) Math.pow(2,counter))*bitTime;
    }
    public void resetCounter(){
        counter = 0;
    }

    public int getCounter(){
        return this.counter;
    }
}
