import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class MultipleClientsServer {


    public static void main(String[] args) {

        if(args.length != 1) {
            System.err.println("Incorrect number of input parameters correct usage:");
            System.err.println("java MultipleClientsServer <port>");
        }


        ServerSocket socket = null;
        try {
            System.out.println("Initialisation du serveur.");
            socket = new ServerSocket(Integer.parseInt(args[0]));

            System.out.println("Attente de connexion de clients.");

            while(true){
                new Thread(new Client(socket.accept())).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
        }

    }



    public static class Client implements Runnable {

        private final Socket socket;

        public Client(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                System.out.println(String.format("Client %s connecté.", getFormattedAddress()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String line = null;
                while ((line = reader.readLine()) != null) {
                    if(line.toLowerCase().equals("exit"))
                        break;

                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.println(line.toUpperCase());

                    System.out.println(String.format("%s > %s", getFormattedAddress(), line));
                }
                System.out.println(String.format("Client %s deconnecté.", getFormattedAddress()));
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
            finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println(e.getLocalizedMessage());
                }
            }
        }

        private String getFormattedAddress() {
            return String.format("%s:%d", socket.getInetAddress().getHostAddress(), socket.getPort());
        }
    }
}
