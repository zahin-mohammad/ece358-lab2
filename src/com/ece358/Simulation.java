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
        int successfulTransmissions = 0;
        int transmissionAttempts = 0;

        ArrayList<Node> nodes = generateNodes();
//        System.out.println(params);

        while (true) {
            double maxCollidedPropagationTime = -1;

            // Select which node should transmit next
            Node earliestNode = nodes.stream()
                    .filter(node -> !node.packets.isEmpty())
                    .min(Comparator.comparingDouble((Node n) -> n.getArrivalTime(0)))
                    .orElse(null);

            // If no nodes are able to transmit, the simulation is finished
            if (earliestNode == null || earliestNode.getArrivalTime(0) > params.simulationTime) { break; }
            transmissionAttempts++;

            // Detect and handle collisions
            // Go through all the nodes
            for (Node node : nodes) {
                // (skipping the sending node)
                if (earliestNode.nodeNumber == node.nodeNumber || node.isEmpty()) { continue; }
                // For each one, determine the propagation delay from the sending node to this node
                double propagationTime = propagationTime(node, earliestNode);

                // If this node’s scheduled sending time is less than the sending node’s transmission time plus
                // the propagation delay, we have a collision (because this node did not know it would collide)
                if (node.getArrivalTime(0) <= (earliestNode.getArrivalTime(0) + propagationTime)) {
                    transmissionAttempts++;
                    maxCollidedPropagationTime = Math.max(maxCollidedPropagationTime, propagationTime);
                    node.collision(earliestNode.getArrivalTime(0), propagationTime);
                } else {
                    node.senseMedium(
                            earliestNode.getArrivalTime(0),
                            propagationTime,
                            earliestNode.getTransmissionDelay(0)
                    );
                }
            }

            if (maxCollidedPropagationTime == -1) {
                // If no collisions, reset the collision counter and remove the frame from queue
                earliestNode.transmitPacket();
                successfulTransmissions++;
            } else {
                // On the sending node, if there were collisions with any of the nodes
                earliestNode.senderCollision(maxCollidedPropagationTime);
            }
        }

        SimulationResult result = new SimulationResult(successfulTransmissions, transmissionAttempts, params);
//        System.out.println(result);
        System.out.println(String.format("Persistence: %s Eff: %f Throughput: %f nodes: %d arrivalRate %f", params.persistent, result.efficiency, result.throughput, params.nodeCount, params.averagePacketArrivalRate));
//        System.out.println(String.format("Success: %d Total: %d", successfulTransmissions, transmissionAttempts));
        return result;
    }

    public LinkedList<Packet> generatePackets() {
        PoissonDistribution packetArrivals = new PoissonDistribution(params.averagePacketArrivalRate);
        double currentTime = packetArrivals.generateTimeInterval();
        LinkedList<Packet> packets = new LinkedList<>();

        while (currentTime < params.simulationTime) {
            Packet packet = new Packet(params.packetSize, currentTime);
            packets.add(packet);
            currentTime += packetArrivals.generateTimeInterval();
        }
        return packets;
    }

    public ArrayList<Node> generateNodes() {
        ArrayList<Node> nodes = new ArrayList<>();
        for (int i = 0; i < params.nodeCount; i++) {
            nodes.add(new Node(generatePackets(), params.linkCapacity, i, params.persistent));
        }
        return nodes;
    }

    public double propagationTime(Node node1, Node node2) {
        return (params.nodeDistance * (double) (Math.abs(node1.nodeNumber - node2.nodeNumber))) / params.propagationSpeed;
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
        this.efficiency = successCounter / totalCounter;
        this.throughput = successCounter * params.packetSize / (params.simulationTime * Math.pow(10,6));
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





