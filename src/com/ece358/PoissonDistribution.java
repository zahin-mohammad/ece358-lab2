package com.ece358;

import java.util.Random;

class PoissonDistribution {
    private double lambda;
    private Random r;
    PoissonDistribution(double lambda) {
        this.lambda = lambda;
        // seeded with time
        this.r = new Random();
    }

    double generateTimeInterval() {
        double U = r.nextDouble();
        return (-1.0/lambda) * Math.log(1.0-U);
    }
}
