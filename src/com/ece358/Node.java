package com.ece358;

import java.util.LinkedList;

public class Node {
    int nodeNumber;
    double linkCapacity;
    boolean isPersistent;
    ExponentialBackoff sensingBackoff;
    ExponentialBackoff collisionBackoff;
    LinkedList<Packet> packets;

    Node(LinkedList<Packet> packets, double linkCapacity, int nodeNumber, boolean isPersistent) {
        this.nodeNumber = nodeNumber;
        this.linkCapacity = linkCapacity;
        this.isPersistent = isPersistent;
        this.sensingBackoff = new ExponentialBackoff(linkCapacity);
        this.collisionBackoff = new ExponentialBackoff(linkCapacity);
        this.packets = packets;
    }

    public void collision() {
        double waitTime = collisionBackoff.getWaitTime();
        if (waitTime < 0) {
            transmitOrDropPacket();
            return;
        }
        incrementArrivalTime(0, waitTime);
        updatePacketTimes();
    }

    public void senderCollision(double maxPropagationDelay) {
        double waitTime = collisionBackoff.getWaitTime();
        if (waitTime < 0) {
            transmitOrDropPacket();
            return;
        }
        waitTime += maxPropagationDelay + getTransmissionTime(0);
        incrementArrivalTime(0, waitTime);
        updatePacketTimes();
    }

    private void updatePacketTimes() {
        for (int i = 1; i < packets.size(); i++) {
            if (getArrivalTime(i) < getArrivalTime(i-1)) {
                setArrivalTime(i, getArrivalTime(i-1) + getTransmissionTime(i-1));
            } else { break; }
        }
    }

    public void senseMedium(double senderSentTime, double senderTransmissionTime, double senderPropagationTime) {
        if (packets.isEmpty()) { return; }

        double earliestPacketTime = getArrivalTime(0);
        if (
                earliestPacketTime > senderSentTime + senderPropagationTime &&
                earliestPacketTime < senderSentTime + senderPropagationTime + senderTransmissionTime
        ) {
            setArrivalTime(0, senderSentTime + senderPropagationTime + senderTransmissionTime);

            if (!isPersistent) {
                double waitTime = sensingBackoff.getWaitTime();
                if (waitTime > 0) {
                    incrementArrivalTime(0, waitTime);
                    updatePacketTimes();
                }
            }
        }


    }

    public void transmitOrDropPacket() {
        collisionBackoff.resetCounter();
        sensingBackoff.resetCounter();
        packets.remove(0);
    }

    private void setArrivalTime(int index, double waitTime) { packets.get(index).setArrivalTime(waitTime); }

    public double getTransmissionTime(int index) { return packets.get(index).packetLength/linkCapacity; }

    public double getArrivalTime(int index) { return packets.get(index).getArrivalTime(); }

    public void incrementArrivalTime(int index, double waitTime) { packets.get(index).incrementArrivalTime(waitTime); }

    public boolean isEmpty() { return packets.isEmpty(); }
}
