import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FPOSineBenchmark {
    private static final int NUM_ITERATIONS = 10000;

    public static void main(String[] args) {
        for (int j = 0; j < 2; j++) {
            LocalDateTime currentDateTime = LocalDateTime.now();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String dateTimeString = currentDateTime.format(formatter);

            try (PrintWriter writer = new PrintWriter(new FileWriter("Output/sineResult_" + dateTimeString + ".txt"))) {
                for (int i = 0; i < NUM_ITERATIONS; i++) {
                    for (int degrees = 0; degrees < 360; degrees++) {
                        double radians = Math.toRadians(degrees);
                        double sineValue = Math.sin(radians);
                        writer.println(sineValue);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0) ;
    }
}
