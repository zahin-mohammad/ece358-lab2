package com.ece358;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
	// write your code here
        double simulationTime = 1000;
        int nodeLength = 2;
        double averagePacketArrivalRate = 7;
        double linkCapacity = Math.pow(10,6);
        double packetSize = 1500;
        double nodeDistance = 10;
        double propogationSpeed = Math.pow(10, 8)*2;

        ArrayList<Node> nodes = new ArrayList<>();
        for (int i = 0; i < nodeLength; i++){
            LinkedList<Packet> packets = generatePackets(packetSize, averagePacketArrivalRate, simulationTime);
            nodes.add(new Node(packets, linkCapacity, true));
        }

        for (Node node: nodes) {

        }
    }

    public static LinkedList<Packet> generatePackets(
            double packetSize, double averagePacketArrivalRate, double simulationTime
    ){
        PoissonDistribution packetArrivals = new PoissonDistribution(averagePacketArrivalRate);
        double currentTime = 0;

        LinkedList<Packet> packets = new LinkedList<>();
        while (currentTime < simulationTime) {
            currentTime += packetArrivals.generateTimeInterval();
            Packet packet = new Packet(packetSize, currentTime);
            packets.add(packet);
        }
        return packets;
    }
}
