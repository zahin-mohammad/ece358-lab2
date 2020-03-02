package com.ece358;

import java.util.Random;

public class ExponentialBackoff {
    private Random r;
    private int counter = 0;
    private int counterSaturation = 0;
    private int bitTime = 0;

    ExponentialBackoff(int linkCapacity){
        this(0, 10, 512, linkCapacity);
    }
    ExponentialBackoff(int counter, int counterSaturation, int bitTimeSize, int linkCapacity){
        this.r = new Random();
        this.bitTime = bitTimeSize/linkCapacity;
        this.counter = counter;
        this.counterSaturation = counterSaturation;
    }

    public double getWaitTime(){
        counter++;
        if (counter > counterSaturation){
            // TODO: Throw error?
            System.out.println("Counter greater than saturation");
        }
        return r.nextInt((int) Math.pow(2,counter))*bitTime;
    }
    public void resetCounter(){
        counter = 0;
    }
}
