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

    // Increment this node’s collision counter, and if it’s greater than 10 then drop the frame
    // from the queue and reset the counter
    public void collision(double senderSentTime, double senderPropagationTime) {
        double waitTime = collisionBackoff.getWaitTime();
        if (waitTime < 0) {
            dropPacket();
            return;
        }
        // Otherwise, back off this node to the sending node’s transmission time plus the
        // propagation delay plus an exponential backoff (using this node’s collision counter)
        waitTime += senderSentTime + senderPropagationTime;
        setArrivalTime(0, waitTime);
        updatePacketTimes();
    }

    // Back off the sending node by its transmission time plus the maximum propagation delay to any
    // colliding node plus an exponential backoff based on the sending node’s collision counter
    public void senderCollision(double maxPropagationDelay) {
        double waitTime = collisionBackoff.getWaitTime();
        if (waitTime < 0) {
            dropPacket();
            return;
        }
        waitTime += maxPropagationDelay;
        incrementArrivalTime(0, waitTime);
        updatePacketTimes();
    }

    private void updatePacketTimes() {
//        for ( int i = 1; i < packets.size(); i++) {
//            if (getArrivalTime(i) <= getArrivalTime(i-1)) {
//                setArrivalTime(i, getArrivalTime(i-1) + getTransmissionDelay(i-1));
//            } else { break;}
//        }
        if (packets.size() > 1) {
            if (getArrivalTime(1) <= getArrivalTime(0)) {
                setArrivalTime(1, getArrivalTime(0) + getTransmissionDelay(0));
            }
        }
    }

    public void senseMedium(double senderSentTime, double senderPropagationTime, double senderTransmissionTime) {
        if (packets.isEmpty()) { return; }
        // Any node that senses between (T sent + T prop) and (T sent + T prop + T trans) will find the bus busy
        if (
                getArrivalTime(0) > senderSentTime + senderPropagationTime &&
                getArrivalTime(0) < senderSentTime + senderPropagationTime + senderTransmissionTime
        ) {
            if (isPersistent) {
                // In the persistent case, we simply schedule our next bus-sense right at time
                // T sent + T prop + T trans (i.e. wait until the sending node’s packet has gone by and
                // then immediately try again)
                setArrivalTime(0, senderSentTime + senderPropagationTime + senderTransmissionTime);
            } else {
                // In the non-persistent case, we add an exponential backoff to the current sensing time
                while (getArrivalTime(0) < senderSentTime + senderPropagationTime + senderTransmissionTime) {
                    double waitTime = sensingBackoff.getWaitTime();
                    if (waitTime > 0) {
                        incrementArrivalTime(0, waitTime);
                    } else {
                        dropPacket();
                        break;
                    }
                }
            }
            updatePacketTimes();
        } else {
            sensingBackoff.resetCounter();
        }
    }

    // If no collisions, reset the collision counter and remove the frame from queue
    public void transmitPacket() {
        updatePacketTimes();
        dropPacket();
    }

    public void dropPacket() {
        packets.remove(0);
        collisionBackoff.resetCounter();
        sensingBackoff.resetCounter();
    }

    private void setArrivalTime(int index, double waitTime) { packets.get(index).setArrivalTime(waitTime); }

    public double getTransmissionDelay(int index) { return packets.get(index).packetLength / linkCapacity; }

    public double getArrivalTime(int index) { return packets.get(index).getArrivalTime(); }

    public void incrementArrivalTime(int index, double waitTime) { packets.get(index).incrementArrivalTime(waitTime); }

    public boolean isEmpty() { return packets.isEmpty(); }
}
