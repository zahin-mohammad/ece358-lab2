package com.ece358;

public class Packet {
    double packetLength = 0;
    private double arrivalTime = 0;

    Packet (double packetLength){
        this(packetLength, 0);
    }
    Packet(double packetLength, double arrivalTime){
        this.packetLength = packetLength;
        this.arrivalTime = arrivalTime;
    }
    public void updateArrivalTime(double waitingTime){
        arrivalTime += waitingTime;
    }
    public double getArrivalTime(){
        return arrivalTime;
    }
}
