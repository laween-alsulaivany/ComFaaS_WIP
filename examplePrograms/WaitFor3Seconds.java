public class WaitFor3Seconds {

    public static void main(String[] args) {
        try {
            // Wait for 3 seconds
            System.out.println("Waiting for 3 seconds...");
            Thread.sleep(3000);  // Sleep for 3000 milliseconds (3 seconds)
            
            // Print the message
            System.out.println("All good to go!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
