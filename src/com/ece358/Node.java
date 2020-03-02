package com.ece358;

import java.util.ArrayList;
import java.util.LinkedList;

public class Node {

    int nodeNumber;
    double linkCapacity;
    ExponentialBackoff sensingBackoff;
    ExponentialBackoff collisionBackoff;
    LinkedList<Packet> packets;

    Node(LinkedList<Packet> packets, double linkCapacity, boolean persistent){
        this.linkCapacity = linkCapacity;
        if (!persistent){
            this.sensingBackoff = new ExponentialBackoff(linkCapacity);
        }
        this.collisionBackoff = new ExponentialBackoff(linkCapacity);
        this.packets = packets;
    }

    public void collision(){
        if (packets.peekFirst() == null){
            // TODO: Error
            return;
        }
        packets.get(0).updateArrivalTime(collisionBackoff.getWaitTime());

        for (int i = 1; i < packets.size(); i++){
            if (packets.get(i).getArrivalTime() < packets.get(i-1).getArrivalTime()){
                packets.get(i).updateArrivalTime(
                        packets.get(i-1).getArrivalTime() + packets.get(i-1).packetLength/linkCapacity);
            }
        }
    }
}
