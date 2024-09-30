import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MonteCarloSimulationBenchmark {
    private static final int NUM_POINTS = 36000000;

    public static void main(String[] args) {

        LocalDateTime currentDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String dateTimeString = currentDateTime.format(formatter);

        int pointsInsideCircle = simulateMonteCarlo(NUM_POINTS);

        double piApproximation = calculatePiApproximation(pointsInsideCircle, NUM_POINTS);
  
        try (PrintWriter writer = new PrintWriter(new FileWriter("Output/montecarloResult_" + dateTimeString + ".txt"))) {
            writer.println(piApproximation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(1) ;
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
