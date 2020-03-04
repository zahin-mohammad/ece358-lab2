package com.ece358;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        double simulationTime = 1000;
        double linkCapacity = Math.pow(10,6);
        double packetSize = 1500;
        double nodeDistance = 10;
        double propagationSpeed = Math.pow(10, 8)*2;

        ArrayList<SimulationResult> simulationResultList = new ArrayList<>();
        ArrayList<SimulationParams> simulationParamsList = new ArrayList<>();
        for (int n = 20; n <= 100; n+=20 ) {
            for (double averageArrivalRate: new double[]{ 7.0, 10.0,20.0 }) {
                SimulationParams params = new SimulationParams(
                        n,
                        true,
                        simulationTime,
                        averageArrivalRate,
                        linkCapacity,
                        packetSize,
                        nodeDistance,
                        propagationSpeed
                );
                SimulationResult result = new Simulation(params).simulate();
                simulationParamsList.add(params);
                simulationResultList.add(result);
            }
        }
        for (int n = 20; n <= 100; n+=20 ) {
            for (double averageArrivalRate: new double[]{ 7.0, 10.0,20.0 }) {
                SimulationParams params = new SimulationParams(
                        n,
                        false,
                        simulationTime,
                        averageArrivalRate,
                        linkCapacity,
                        packetSize,
                        nodeDistance,
                        propagationSpeed
                );
                SimulationResult result = new Simulation(params).simulate();
                simulationParamsList.add(params);
                simulationResultList.add(result);
            }
        }
        createCSV(simulationResultList, simulationParamsList, "berny.csv");
    }

    private static void createCSV(
            List<SimulationResult> simulationResultList,
            List<SimulationParams> simulationParamsList,
            String csvName
    ) throws IOException {

        PrintWriter printWriter = new PrintWriter(new FileWriter(csvName));
        printWriter.printf("Persistent,NodeCount,AveragePacketArrivalRate,Efficiency,Throughput\n");

        for (int i = 0; i < simulationResultList.size(); i++) {
            SimulationResult simulationResult = simulationResultList.get(i);
            SimulationParams simulationParams = simulationParamsList.get(i);

            printWriter.printf(
                    "%s,%d,%f,%f,%f\n",
                    simulationParams.persistent,
                    simulationParams.nodeCount,
                    simulationParams.averagePacketArrivalRate,
                    simulationResult.efficiency,
                    simulationResult.throughput
            );
        }
        printWriter.close();
    }
}