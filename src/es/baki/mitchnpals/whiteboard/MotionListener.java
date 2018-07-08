package es.baki.mitchnpals.whiteboard;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MotionListener extends Thread {
    private ServerSocket socket;
    private Whiteboard w;

    // Socket Testing
    public static void main(String... args) throws IOException, InterruptedException {
        System.out.println("Running socket test");
        Scanner scan = new Scanner(new File("playback.txt"));
        Socket socket = new Socket("localhost", 15273);
        PrintWriter socketOut = new PrintWriter(socket.getOutputStream());
        while (scan.hasNext()) {
            String s = scan.nextLine();
            System.out.print(s);
            socketOut.write(s + "\n");
        }
        socketOut.flush();
        Thread.sleep(1000);
        socket.close();

    }
    public MotionListener(Whiteboard w) {
        this.w = w;
    }

    @Override
    public void run() {
        try {
            socket = new ServerSocket(15273);
            System.out.println("Server Socket Initialized");
            while (true) {
                MotionControlSocket mcs = new MotionControlSocket(socket.accept());
                System.out.printf("New socket connection from %s%n", socket.getInetAddress());
                mcs.start();
            }
        } catch (IOException e) {
            System.err.println("Could not make server socket, already running?");
        }
    }

    public void appClosed() {
        try {
            socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    private class MotionControlSocket extends Thread {
        private Socket s;
        private MotionControlSocket(Socket s){
            this.s = s;
        }
        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                while (true){
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String x = in.readLine();
                    if (x == null)
                        continue;
                    System.out.printf("Received %s%n", x);
                    String[] xs = x.split(",");
                    if (xs[0].equals("down")) {
                        double xcoord = Double.parseDouble(xs[1]);
                        double ycoord = Double.parseDouble(xs[2]);
                        System.out.println(w.getHeight() + " " + w.getWidth());
                        xcoord = (xcoord / 100.0) * w.getWidth();
                        ycoord = (ycoord / 100.0) * w.getHeight();
                        w.onMousePressed(xcoord, ycoord);
                    } else if (xs[0].equals("up")) {
                        w.onMouseReleased();
                    } else  {
                        try {
                            double xcoord = Double.parseDouble(xs[0]);
                            double ycoord = Double.parseDouble(xs[1]);
                            xcoord = (xcoord / 100.0) * w.getWidth();
                            ycoord = (ycoord / 100.0) * w.getHeight();
                            w.onMouseDragged(xcoord, ycoord);
                        } catch (NumberFormatException e ) {
                            System.err.printf("Could not parse packet stating with %s%n", xs[0]);
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

