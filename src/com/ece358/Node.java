package com.ece358;

import java.util.LinkedList;

public class Node {

    int nodeNumber;
    double linkCapacity;
    boolean isPersistent;
    ExponentialBackoff sensingBackoff;
    ExponentialBackoff collisionBackoff;
    LinkedList<Packet> packets;

    Node(LinkedList<Packet> packets, double linkCapacity, int nodeNumber, boolean isPersistent){
        this.nodeNumber = nodeNumber;
        this.linkCapacity = linkCapacity;
        this.isPersistent = isPersistent;
        this.sensingBackoff = new ExponentialBackoff(linkCapacity);
        this.collisionBackoff = new ExponentialBackoff(linkCapacity);
        this.packets = packets;
    }

    public void collision(){
        double waitTime = collisionBackoff.getWaitTime();
        if (waitTime < 0){
            packets.remove(0);
            return;
        }
        packets.get(0).incrementArrivalTime(waitTime);
        updatePacketTimes();
    }

    public void senderCollision( double maxPropagationDelay){
        double waitTime = collisionBackoff.getWaitTime();
        if (waitTime < 0){
            packets.remove(0);
            return;
        }
        waitTime +=  maxPropagationDelay + packets.get(0).packetLength/linkCapacity;
        packets.get(0).incrementArrivalTime(waitTime );
        updatePacketTimes();
    }

    private void updatePacketTimes() {
        for (int i = 1; i < packets.size(); i++){
            if (packets.get(i).getArrivalTime() < packets.get(i-1).getArrivalTime()){
                packets.get(i).setArrivalTime(
                        getArrivalTime(i-1) + getTransmissionTime(i-1));
            } else {
                break;
            }
        }
    }

    public void senseMedium(double senderSentTime, double senderTransmissionTime, double senderPropagationTime){
        double earliestPacketTime = getArrivalTime(0);
//        if ( earliestPacketTime > senderSentTime + senderTransmissionTime
//                && earliestPacketTime < senderSentTime + senderPropagationTime + senderTransmissionTime ){
//            packets.get(0).setArrivalTime(senderSentTime + senderPropagationTime + senderTransmissionTime);
//        }
        if ( earliestPacketTime > 0
                && earliestPacketTime - senderSentTime - senderPropagationTime < senderTransmissionTime ){
            packets.get(0).setArrivalTime(senderSentTime + senderPropagationTime + senderTransmissionTime);
        }
        if (!this.isPersistent){
            double waitTime = sensingBackoff.getWaitTime();
            if (waitTime > 0){
                packets.get(0).incrementArrivalTime(waitTime);
            }
        }
    }

    public double getTransmissionTime(int index){
        return packets.get(index).packetLength/linkCapacity;
    }

    public double getArrivalTime(int index){
        return packets.get(index).getArrivalTime();
    }

    public boolean isEmpty(){
        return this.packets.isEmpty();
    }

    public void transmit(){
        collisionBackoff.resetCounter();
        sensingBackoff.resetCounter();
        packets.remove(0);
    }
}
