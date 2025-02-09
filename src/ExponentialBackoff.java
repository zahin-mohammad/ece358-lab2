import java.util.Random;

public class ExponentialBackoff {
    private Random r;
    private int counter;
    private int counterSaturation;
    private double bitTime;

    ExponentialBackoff(double linkCapacity) {
        this(0, 10, 512.0, linkCapacity);
    }

    private ExponentialBackoff(int counter, int counterSaturation, double bitTimeSize, double linkCapacity) {
        this.r = new Random();
        this.bitTime = bitTimeSize / linkCapacity;
        this.counter = counter;
        this.counterSaturation = counterSaturation;
    }

    public double getWaitTime() {
        counter++;
        if (counter > counterSaturation) {
            resetCounter();
            return -1;
        }
        // End is not inclusive
        return (double) r.nextInt( (int) Math.pow(2, counter)) * bitTime;
    }

    public void resetCounter() { counter = 0; }
}
