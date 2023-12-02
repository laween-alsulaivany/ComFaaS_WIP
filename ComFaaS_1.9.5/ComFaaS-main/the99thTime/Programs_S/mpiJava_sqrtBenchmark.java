import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import mpi.* ;

//To compile: /home/bloodyanger31/OpenMPI/bin/mpijavac mpiJava_sqrtBenchmark.java 
//To run: time /home/bloodyanger31/OpenMPI/bin/mpirun -n 4 java mpiJava_sqrtBenchmark
public class mpiJava_sqrtBenchmark {
    private static int NUM_ITERATIONS ;
    private static final int MIN_VALUE = 20000;
    private static final int MAX_VALUE = 40000;
    public static void main(String args[]) throws MPIException {

        MPI.Init(args) ;
        LocalDateTime currentDateTime = LocalDateTime.now();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss") ;
        String dateTimeString = currentDateTime.format(formatter) ;
        

        int rank = MPI.COMM_WORLD.getRank(),
            size = MPI.COMM_WORLD.getSize() ;
        
        NUM_ITERATIONS = (3600000 * 7)/(size) ;

        System.out.println("My rank: " + rank) ;
        if (rank == 0) {
            System.out.println("I am master");
            MPI.COMM_WORLD.barrier() ;
            
        } else {
            try {
                PrintWriter writer = new PrintWriter(new FileWriter("Output/sqrtResult_" + dateTimeString +"_"+rank+ ".txt")) ;
                Random random = new Random(System.currentTimeMillis()+ (10*rank)) ;
                for (int i = 0; i < NUM_ITERATIONS; i++) {
                    double randomNumber = getRandomNumber(random) ;
                    double squareRoot = Math.sqrt(randomNumber) ;
                    writer.println(squareRoot) ;
                }
                writer.close() ;
            } catch (IOException e) {
                e.printStackTrace() ;
                MPI.COMM_WORLD.abort(-1) ;
            }
            MPI.COMM_WORLD.barrier() ;

        }

        MPI.Finalize() ;
    }

    private static double getRandomNumber(Random random) {
        return MIN_VALUE + (MAX_VALUE - MIN_VALUE) * random.nextDouble() ;
    }
}
