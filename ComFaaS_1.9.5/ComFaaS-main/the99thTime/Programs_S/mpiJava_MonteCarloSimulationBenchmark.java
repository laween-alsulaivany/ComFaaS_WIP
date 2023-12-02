import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import mpi.*;


// /home/jlee/OpenMPI/bin/mpijavac mpiJava_MonteCarloSimulationBenchmark.java
// /home/jlee/OpenMPI/bin/mpirun -np 4 java mpiJava_MonteCarloSimulationBenchmark

// make -j 10 all 2>&1 | tee make.out

// make install 2>&1 | tee install.out



public class mpiJava_MonteCarloSimulationBenchmark {
    private static final int NUM_POINTS = 360000000;

    public static void main(String[] args) throws MPIException {

        MPI.Init(args);

        int rank = MPI.COMM_WORLD.getRank();
        int size = MPI.COMM_WORLD.getSize();

        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String dateTimeString = currentDateTime.format(formatter);

        int localPointsInsideCircle = simulateMonteCarlo(NUM_POINTS / size);

        int[] globalPointsInsideCircle = new int[]{0};
        MPI.COMM_WORLD.reduce(new int[]{localPointsInsideCircle}, globalPointsInsideCircle, 1, MPI.INT, MPI.SUM, 0);

        if (rank == 0) {
            double piApproximation = calculatePiApproximation(globalPointsInsideCircle[0], NUM_POINTS);

            try (PrintWriter writer = new PrintWriter(new FileWriter("Output/montecarloResult_" + dateTimeString + ".txt"))) {
                writer.println(piApproximation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        MPI.Finalize();
    }

    private static int simulateMonteCarlo(int numPoints) {
        Random random = new Random();
        int pointsInsideCircle = 0;

        for (int i = 0; i < numPoints; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();

            if (isInsideCircle(x, y)) {
                pointsInsideCircle++;
            }
        }

        return pointsInsideCircle;
    }

    private static boolean isInsideCircle(double x, double y) {
        double distance = Math.sqrt(x * x + y * y);
        return distance <= 1.0;
    }

    private static double calculatePiApproximation(int pointsInsideCircle, int numPoints) {
        return 4.0 * pointsInsideCircle / numPoints;
    }
}
