package com.ece358;

import java.util.ArrayList;
import java.util.Comparator;
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
        System.out.println(params);
        while (true) {
            Node earliestNode;
            double maxCollidedPropagationTime = 0;
            earliestNode = nodes.stream()
                    .filter(node -> !node.packets.isEmpty())
                    .min(Comparator.comparingDouble((Node n) -> n.getArrivalTime(0)))
                    .orElse(null);

            if (earliestNode == null) {
                break;
            }

            // Sense the medium
            for (Node node : nodes) {
                node.senseMedium(
                        earliestNode.getArrivalTime(0),
                        earliestNode.getTransmissionTime(0),
                        propagationTime(node, earliestNode)
                );
            }

            totalCounter++;

            // Collisions with sender
            for (Node node: nodes) {
                if (earliestNode == node || node.isEmpty()) { continue; }

                double propagationTime = propagationTime(node, earliestNode);
                if (node.getArrivalTime(0) < (earliestNode.getArrivalTime(0) + propagationTime)) {
                    maxCollidedPropagationTime = Math.max(maxCollidedPropagationTime, propagationTime);
                    node.collision();
                }
            }

            if (maxCollidedPropagationTime == 0) {
                earliestNode.transmitOrDropPacket();
                successCounter++;
            } else {
                earliestNode.senderCollision(maxCollidedPropagationTime);
            }
        }
        SimulationResult result = new SimulationResult(successCounter, totalCounter, params);
        System.out.println(result);
        return result;
    }

    public LinkedList<Packet> generatePackets() {
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

    public ArrayList<Node> generateNodes() {
        ArrayList<Node> nodes = new ArrayList<>();

        for (int i = 0; i < params.nodeCount; i++) {
            LinkedList<Packet> packets = generatePackets();
            nodes.add(new Node(packets, params.linkCapacity, i, params.persistent));
        }
        return nodes;
    }

    public double propagationTime(Node node1, Node node2) {
        return params.nodeDistance * (double) (Math.abs(node1.nodeNumber - node2.nodeNumber)) / params.propagationSpeed;
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
    @Override
    public String toString(){
        return String.format(
                "%d,%s,%f,%f,%f,%f,%f,%f",
                nodeCount,
                persistent,
                simulationTime,
                averagePacketArrivalRate,
                linkCapacity,
                packetSize,
                nodeDistance,
                propagationSpeed
        );
    }
}

class SimulationResult{
    double efficiency;
    double throughput;
    SimulationResult(double successCounter, double totalCounter, SimulationParams params){
        this.efficiency = successCounter/totalCounter;
        this.throughput = successCounter*params.packetSize/params.simulationTime;
    }

    @Override
    public String toString(){
        return String.format(
                "%f,%f",
                efficiency,
                throughput
        );
    }
}





