import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: Maxime
 * Date: 29/10/14
 * Time: 18:35
 * To change this template use File | Settings | File Templates.
 */
public class SingleClientServer {


    public static void main(String[] args) {

        if(args.length != 1) {
            System.err.println("Incorrect number of input parameters correct usage:");
            System.err.println("java SingleClientServer <port>");
        }

        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {

            System.out.println("Initialisation du serveur.");
            serverSocket = new ServerSocket(Integer.parseInt(args[0]));

            System.out.println("Attente de connexion d'un client.");
            clientSocket = serverSocket.accept();

            System.out.println("Client présentement connecté.");

            InputStream stream = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(String.format("%s:%d > %s",clientSocket.getInetAddress().getHostAddress(),
                        clientSocket.getPort(), line));

                writer.println(line.toUpperCase());
            }

            System.out.println("Fermeture du serveur.");

        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }
        catch (NumberFormatException e) {
            System.err.println(e.getLocalizedMessage());
        }
        finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }

            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
        }



    }


}
