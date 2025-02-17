
// StressBoth.java
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class StressBoth {
    public static void main(String[] args) {
        int targetSize = 20 * 1024 * 1024; // Target file size: 20 MB
        int chunkSize = 1024 * 1024; // 1 MB per chunk
        byte[] data = new byte[chunkSize];
        Random random = new Random();

        try (FileOutputStream fos = new FileOutputStream("stress_output.dat")) {
            int totalWritten = 0;
            while (totalWritten < targetSize) {
                // CPU intensive task: perform dummy math computations
                double dummy = 0;
                for (int i = 0; i < 1000000; i++) {
                    dummy += Math.sqrt(random.nextDouble());
                }
                // Memory stress: fill the byte array with random data
                random.nextBytes(data);
                fos.write(data);
                totalWritten += chunkSize;
                System.out.println("Written " + (totalWritten / (1024 * 1024)) + " MB");
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
