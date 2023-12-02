import mpi.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// /home/bloodyanger31/OpenMPI/bin/mpijavac mpiJava_sineBenchmark.java
// /home/bloodyanger31/OpenMPI/bin/mpirun -np 8 java mpiJava_sineBenchmark

public class mpiJava_sineBenchmark {

    // private static final int SUB_ITERATIONS = 10000;

    public static void main(String[] args) throws MPIException {
        int total_iterations = 10000 * 7;
        
        

        MPI.Init(args);

        int rank = MPI.COMM_WORLD.getRank();
        int size = MPI.COMM_WORLD.getSize();

        int NUM_ITERATIONS = total_iterations / size ;

        for (int j = 0; j < 2; j++) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String dateTimeString = currentDateTime.format(formatter);

            try (PrintWriter writer = new PrintWriter(new FileWriter("Output/sineResult_" + dateTimeString + "_Rank" + rank + ".txt"))) {
                int start = (rank * NUM_ITERATIONS) / size;
                int end = ((rank + 1) * NUM_ITERATIONS) / size;

                for (int i = start; i < end; i++) {
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

        MPI.Finalize();
        System.exit(0);
    }
}
