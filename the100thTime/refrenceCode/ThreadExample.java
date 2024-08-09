import java.util.Scanner ;

public class ThreadExample {
    private static int sharedValue = 0 ;
    private static boolean killValue = false ;

    public static void main(String[] args) {
        Thread ClientServer = new Thread(() -> { ClientServerLoop() ;}) ;
        Thread UserControl = new Thread(() -> { UserControlLoop();}) ;

        ClientServer.start() ;
        UserControl.start() ;

        try {
            ClientServer.join() ;
            UserControl.join() ;
        } catch (InterruptedException e) {
            System.out.println("I'm not sure what happened") ;
            System.out.println(e) ;
        }
        

        System.out.println("Program Over");
    }

    private static void ClientServerLoop() {
        while (true) {
            try {
                Thread.sleep(1000) ; // Sleep for 1 second (1000 milliseconds)
            } catch (InterruptedException e) {
                System.out.println("I'm not sure what happened") ;
                System.out.println(e) ;
            }
            
            incrementSharedValue() ;
            if (getkillValue()) {
                break ;
            }
        }
    }

    private static void UserControlLoop() {
        Scanner scanner = new Scanner(System.in) ;

        String command = scanner.nextLine() ;

        setkillValue(true) ;
        System.out.println(command) ;

        scanner.close() ;

    }

    private static boolean getkillValue() {
        return killValue ;
    }

    private static synchronized void setkillValue(boolean arg) {
        killValue = arg ;
    }

    private static synchronized void incrementSharedValue() {
        sharedValue++;
        System.out.println("Shared value: " + sharedValue);
    }
}

