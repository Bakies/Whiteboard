package es.baki.mitchnpals.whiteboard;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BroadcastListener extends Thread {
    private DatagramSocket socket;

    public static void main(String... args) throws IOException {
        new BroadcastListener().start();
    }

    public BroadcastListener() throws IOException{
        socket = new DatagramSocket(15272, InetAddress.getByName("0.0.0.0"));
        socket.setBroadcast(true);
    }

    public void run() {
        try {
            System.out.println("Starting broadcast listening service");
            while (true) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.printf("Received \"%s\" from %s%n", received, address.toString());
                if (received.equals("end")) {
                    break;
                }
                socket.send(packet);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
