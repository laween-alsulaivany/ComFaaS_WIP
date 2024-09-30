import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FPOSquareRootBenchmark {
    private static final int NUM_ITERATIONS = 3600000;
    private static final int MIN_VALUE = 20000;
    private static final int MAX_VALUE = 40000;

    public static void main(String[] args) {
        LocalDateTime currentDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String dateTimeString = currentDateTime.format(formatter);

        try (PrintWriter writer = new PrintWriter(new FileWriter("Output/sqrtResult_" + dateTimeString + ".txt"))) {
            Random random = new Random();

            for (int i = 0; i < NUM_ITERATIONS; i++) {
                double randomNumber = getRandomNumber(random);
                double squareRoot = Math.sqrt(randomNumber);
                writer.println(squareRoot);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(1) ;
    }

    private static double getRandomNumber(Random random) {
        return MIN_VALUE + (MAX_VALUE - MIN_VALUE) * random.nextDouble();
    }
}
