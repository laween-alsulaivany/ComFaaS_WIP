import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastSender {
    public static void main(String[] args) throws IOException {
        String multicastAddress = "239.0.0.1";
        int multicastPort = 5000;

        MulticastSocket socket = new MulticastSocket(multicastPort);
        InetAddress group = InetAddress.getByName(multicastAddress);

        byte[] message = "Hello, Multicast!".getBytes();
        DatagramPacket packet = new DatagramPacket(message, message.length, group, multicastPort);

        socket.send(packet);
        socket.close();
    }
}
