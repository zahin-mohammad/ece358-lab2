package com.ece358;

import java.util.ArrayList;
import java.util.LinkedList;

public class Simulation {
    SimulationParams params;

    Simulation(SimulationParams params){
        this.params = params;
    }

    public  SimulationResult simulate() {
        int successCounter = 0;
        int totalCounter = 0;

        ArrayList<Node> nodes = generateNodes();

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
                if (earliestNode == node || node.isEmpty()){ continue; }

                double propagationTime = propagationSpeed(node, earliestNode);
                if (node.getArrivalTime(0) < (earliestNode.getArrivalTime(0) + propagationTime)){
                    maxCollidedPropagationTime = Math.max(maxCollidedPropagationTime, propagationTime);
                    node.collision();
                }
            }
            if (maxCollidedPropagationTime == 0){
                for (Node node : nodes) {
                    if (earliestNode == node || node.isEmpty()){ continue; }

                    double propagationTime = propagationSpeed(node, earliestNode);
                    node.senseMedium(
                            earliestNode.getArrivalTime(0),
                            earliestNode.getTransmissionTime(0),
                            propagationTime);
                }
                earliestNode.transmit();
                successCounter++;
            } else {
                earliestNode.senderCollision(maxCollidedPropagationTime);
            }
        }
        return new SimulationResult(successCounter, totalCounter, params);
    }

    public LinkedList<Packet> generatePackets(){
        PoissonDistribution packetArrivals = new PoissonDistribution(params.averagePacketArrivalRate);
        double currentTime = 0;
        LinkedList<Packet> packets = new LinkedList<>();

        while (currentTime < params.simulationTime) {
            currentTime += packetArrivals.generateTimeInterval();
            Packet packet = new Packet(params.packetSize, currentTime);
            packets.add(packet);
        }
        return packets;
    }

    public ArrayList<Node> generateNodes(){
        ArrayList<Node> nodes = new ArrayList<>();

        for (int i = 0; i < params.nodeCount; i++){
            LinkedList<Packet> packets = generatePackets();
            nodes.add(new Node(packets, params.linkCapacity,i,params.persistent));
        }
        return  nodes;
    }

    public double propagationSpeed(Node node1, Node node2){
        return params.nodeDistance*(Math.abs(node1.nodeNumber-node2.nodeNumber))/params.propagationSpeed;
    }

}

class SimulationParams{
    int nodeCount;
    boolean persistent;
    double simulationTime;
    double averagePacketArrivalRate;
    double linkCapacity;
    double packetSize;
    double nodeDistance;
    double propagationSpeed;
    SimulationParams(
            int nodeCount, boolean persistent, double simulationTime , double averagePacketArrivalRate, double linkCapacity,
            double packetSize, double nodeDistance, double propagationSpeed){
        this.nodeCount = nodeCount;
        this.persistent = persistent;
        this.simulationTime = simulationTime;
        this.averagePacketArrivalRate = averagePacketArrivalRate;
        this.linkCapacity = linkCapacity;
        this.packetSize = packetSize;
        this.nodeDistance = nodeDistance;
        this.propagationSpeed = propagationSpeed;
    }
}

class SimulationResult{
    double efficiency;
    double throughput;
    SimulationResult(double successCounter, double totalCounter, SimulationParams params){
        this.efficiency = successCounter/totalCounter;
        this.throughput = successCounter*params.packetSize/params.simulationTime;
    }
}





