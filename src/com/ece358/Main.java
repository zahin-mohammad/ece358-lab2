package com.ece358;

import java.util.ArrayList;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
        int successCounter = 0;
        int totalCounter = 0;
        double simulationTime = 1000;
        int nodeCount = 20;
        double averagePacketArrivalRate = 10;
        double linkCapacity = Math.pow(10,6);
        double packetSize = 1500;
        double nodeDistance = 10;
        double propagationSpeed = Math.pow(10, 8)*2;

        ArrayList<Node> nodes = generateNodes(
                nodeCount, averagePacketArrivalRate, simulationTime, packetSize, linkCapacity);

        while (true) {
            Node earliestNode = nodes.get(0);
            double maxCollidedPropagationTime = 0;

            for (Node node : nodes) {
                if (earliestNode == node || node.packets.isEmpty()){
                    continue;
                }
                if (earliestNode.isEmpty()){
                    earliestNode = node;
                } else if (!earliestNode.isEmpty() &&
                                earliestNode.getArrivalTime(0) > node.getArrivalTime(0)){
                    earliestNode = node;
                }
            }
            if (earliestNode.isEmpty()){
                break;
            }
            totalCounter++;
            for (Node node: nodes){
                if (earliestNode == node || node.isEmpty()){
                    continue;
                }
                double propagationTime = nodeDistance*((double) Math.abs(node.nodeNumber-earliestNode.nodeNumber))/propagationSpeed;
                if (node.getArrivalTime(0) < (earliestNode.getArrivalTime(0) + propagationTime)){
                    maxCollidedPropagationTime = Math.max(maxCollidedPropagationTime, propagationTime);
                    node.collision();
                }
            }
            if (maxCollidedPropagationTime == 0){
                for (Node node : nodes) {
                    if (earliestNode == node || node.isEmpty()){
                        continue;
                    }
                    double propagationTime = nodeDistance*(Math.abs(node.nodeNumber-earliestNode.nodeNumber))/propagationSpeed;
                    node.senseMedium(earliestNode.getArrivalTime(0),earliestNode.getTransmissionTime(0),propagationTime);
                }
                earliestNode.transmit();
                successCounter++;
            } else {
                earliestNode.senderCollision(maxCollidedPropagationTime);
            }
        }
        System.out.println(successCounter);
        System.out.println(totalCounter);
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

    public static ArrayList<Node> generateNodes(
            int nodeLength, double averagePacketArrivalRate, double simulationTime, double packetSize, double linkCapacity
    ){
        ArrayList<Node> nodes = new ArrayList<>();
        for (int i = 0; i < nodeLength; i++){
            LinkedList<Packet> packets = generatePackets(packetSize, averagePacketArrivalRate, simulationTime);
            nodes.add(new Node(packets, linkCapacity,i,true));
        }
        return  nodes;
    }
}
