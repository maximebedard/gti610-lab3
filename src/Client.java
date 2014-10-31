import java.io.*;
import java.net.Socket;


public class Client {


    public static void main(String[] args) {


        if(args.length != 2) {
            System.err.println("Incorrect number of input parameters correct usage:");
            System.err.println("java Client <hostname>  <port>");
        }


        Socket socket = null;
        try {

            String hostname = args[0];
            int port = Integer.parseInt(args[1]);

            System.out.println(String.format("Connexion au serveur %s:%d", hostname, port));
            socket = new Socket(hostname, port);

            System.out.println("Connexion établie. Veuillez saisir du texte.");

            PrintWriter writer = null;
            BufferedReader reader = null;
            BufferedReader socketReader = null;
            String line;
            try {
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(System.in));
                socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while((line = reader.readLine()) != null) {
                    if(!socket.isConnected()) {
                        break;
                    }

                    if(line.toLowerCase().equals("exit")) {
                        break;
                    }

                    writer.println(line);
                    System.out.println(socketReader.readLine());
                }

            }
            catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
            finally {
                if (writer != null) {
                    writer.close();
                }

                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    System.err.println(e.getLocalizedMessage());
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println(e.getLocalizedMessage());
                }
            }

            System.out.print("Connexion au serveur terminé.");

        } catch (IOException e) {
            System.err.print(e.getLocalizedMessage());
        }
        catch (NumberFormatException e) {
            System.err.println(e);
        }
        finally {

            try {
                if(socket!= null){
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
        }


    }

}
