import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Cette classe permet la r�ception d'un paquet UDP sur le port de r�ception
 * UDP/DNS. Elle analyse le paquet et extrait le hostname
 * 
 * Il s'agit d'un Thread qui �coute en permanance 
 * pour ne pas affecter le d�roulement du programme
 * @author Max
 *
 */


public class UDPReceiver extends Thread {
	/**
	 * Les champs d'un Packet UDP
	 * --------------------------
	 * En-t�te (12 octects)
	 * Question : l'adresse demand�
	 * R�ponse : l'adresse IP
	 * Autorit� : info sur le serveur d'autorit�
	 * Additionnel : information suppl�mentaire
	 */
	
	/**
	 * D�finition de l'En-t�te d'un Packet UDP
	 * ---------------------------------------
	 * Identifiant Param�tres
	 * QDcount Ancount
	 * NScount ARcount
	 * 
	 *� identifiant est un entier permettant d�identifier la requete.
	 *� parametres contient les champs suivant :
	 *	� QR (1 bit) : indique si le message est une question (0) ou une reponse (1).
	 *	� OPCODE (4 bits) : type de la requete (0000 pour une requete simple).
	 *	� AA (1 bit) : le serveur qui a fourni la reponse a-t�il autorite sur le domaine?
	 *	� TC (1 bit) : indique si le message est tronque.
	 *	� RD (1 bit) : demande d�une requete recursive.
	 *	� RA (1 bit) : indique que le serveur peut faire une demande recursive.
	 *	� UNUSED, AD, CD (1 bit chacun) : non utilises.
	 *	� RCODE (4 bits) : code de retour. 0 : OK, 1 : erreur sur le format de la requete, 2: probleme du serveur,
	 *    3 : nom de domaine non trouve (valide seulement si AA), 4 : requete non supportee, 5 : le serveur refuse
	 *    de repondre (raisons de s�ecurite ou autres).
	 * � QDCount : nombre de questions.
	 * � ANCount, NSCount, ARCount : nombre d�entrees dans les champs �Reponse�, �Autorite�, �Additionnel�.
	 */

    protected final static int BUF_SIZE = 1024;
	protected String dnsServer = null;
	protected int port = 53;  // port de réception
	private String domainName = "none";
	private String dnsFile = null;
    private boolean RedirectionSeulement = false;
    private DatagramSocket socket;
    private DnsCache cache;
    private InetAddress senderAddr;

    public void setPort(int p) {
		this.port = p;
	}
	
	public void setRedirectionSeulement(boolean b){
		this.RedirectionSeulement = b;
	}

    public void setDNSServerAddress(String dnsServer){
		this.dnsServer = dnsServer;
	}
	
	public void UDPReceiver(String dnsServer,int port) {
		this.dnsServer = dnsServer;
		this.port = port;
	}

	public void setDnsFile(String filename){
		dnsFile = filename;
	}

	public void run(){


        try {
            // On ouvre la cache
            cache = new DnsCache(dnsFile);
            // Creation d'un socket UDP
            socket = new DatagramSocket(port);

            // Boucle infinie de recpetion
			while(true){
				
				//*Reception d'un paquet UDP via le socket
                byte[] buffer = new byte[BUF_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                senderAddr = packet.getAddress();
                //senderAddr = packet.getSocketAddress();
                socket.receive(packet);
                //System.out.print(buffer);

				//*Creation d'un DataInputStream ou ByteArrayInputStream pour manipuler les bytes du paquet
                DataInputStream stream = new DataInputStream(new ByteArrayInputStream(buffer));

                // Creation du paquet
                DnsMessage message = new DnsMessage(stream);

                // TODO Check if exists?
                DnsMessage.Question question = message.getQuestions().get(0);

                //******  Dans le cas d'un paquet requête *****
                if (message.isRequest()) {
                    System.out.println(String.format("Request(%d) -> %s", message.getTransactionId(), question.getName()));
                    parseRequest(buffer, question);
                }
                //******  Dans le cas d'un paquet reponse *****
                else {
                    System.out.println(String.format("Response(%d) -> %s", message.getTransactionId(), question.getName()));
                    parseResponse(buffer, question, message.getAnswers());
                }
                System.out.print("\n");
			}
		}
        catch(Exception e){
			System.err.println("Problème à l'exécution :");
			e.printStackTrace(System.err);
		}
        finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    private void parseResponse(byte[] buffer, DnsMessage.Question question,
                               List<DnsMessage.Answer> answers) throws IOException {

        if(answers.size() == 0) {
            System.out.println("  -> No answers");
            return;
        }

        System.out.println(String.format(" -> %d answer(s)", answers.size()));
        for(DnsMessage.Answer answer : answers) {
            System.out.println("    -> " + answer.getAddress());
            cache.put(question.getName(), answer.getAddress());
        }
        System.out.println("    -> saving cache");
        cache.save();
    }

    private void parseRequest(byte[] buffer, DnsMessage.Question question) {

        if(RedirectionSeulement) {
            System.out.print(" redirect -> " + dnsServer);
            UDPSender.send(socket, buffer, dnsServer, port);
            return;
        }

        if(!cache.containsKey(question.getName())) {
            System.out.print(" not in cache, redirect -> " + dnsServer);
            UDPSender.send(socket, buffer, dnsServer, port);
            return;
        }

        List<String> addresses = cache.get(question.getName());

        for(String addr:addresses){
            System.out.println(String.format("  cache -> %s = %s", question.getName(), addr));

            UDPAnswerPacketCreator creator = new UDPAnswerPacketCreator();
            byte[] newPacket = creator.CreateAnswerPacket(buffer, addr);

            //UDPSender.send(socket, newPacket, );
        }

        //*Creer le paquet de reponse a l'aide du UDPAnswerPaquetCreator




        //*Placer ce paquet dans le socket
        //System.out.println(String.format("  transfert to -> localhost"));
        //UDPSender sender = new UDPSender(new DatagramPacket(newPacket, newPacket.length), "127.0.0.1", port);
        //sender.setSocket(socket);
        //sender.SendPacketNow();

        //*Envoyer le paquet

    }



}



