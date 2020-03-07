public class Packet {
    double packetLength;
    private double arrivalTime;

    Packet(double packetLength, double arrivalTime) {
        this.packetLength = packetLength;
        this.arrivalTime = arrivalTime;
    }

    public void incrementArrivalTime(double waitingTime) { arrivalTime += waitingTime; }

    public void setArrivalTime(double newTime) { arrivalTime = newTime; }

    public double getArrivalTime() { return arrivalTime; }
}
