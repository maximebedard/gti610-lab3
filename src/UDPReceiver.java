import java.io.*;
import java.net.*;
import java.util.HashMap;

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
	
	/**
	 * Les champs Reponse, Autorite, Additionnel sont tous representes de la meme maniere :
	 *
	 * � Nom (16 bits) : Pour eviter de recopier la totalite du nom, on utilise des offsets. Par exemple si ce champ
	 *   vaut C0 0C, cela signifie qu�on a un offset (C0) de 12 (0C) octets. C�est-a-dire que le nom en clair se trouve
	 *   au 12eme octet du message.
	 * � Type (16 bits) : idem que pour le champ Question.
	 * � Class (16 bits) : idem que pour le champ Question.
	 * � TTL (32 bits) : dur�ee de vie de l�entr�ee.
	 * � RDLength (16 bits): nombre d�octets de la zone RDData.
	 * � RDData (RDLength octets) : reponse
	 */
	
	private DataInputStream d = null;
	protected final static int BUF_SIZE = 1024;
	protected String SERVER_DNS = null;
	protected int port = 53;  // port de réception
	private String DomainName = "none";
	private String DNSFile = null;
	private String adrIP = null;
	private boolean RedirectionSeulement = false;
	private String adresseIP = null;

	public void setport(int p) {
		this.port = p;
	}
	
	public void setRedirectionSeulement(boolean b){
		this.RedirectionSeulement = b;
	}
	
	public String gethostNameFromPacket(){
		return DomainName;
	}
	
	public String getAdrIP(){
		return adrIP;
	}
	
	private void setAdrIP(String ip){
		adrIP = ip;
	}
	
	public void sethostNameFromPacket(String hostname){
		this.DomainName = hostname;
	}
	
	public String getSERVER_DNS(){
		return SERVER_DNS;
	}
	
	public void setSERVER_DNS(String server_dns){
		this.SERVER_DNS = server_dns;
	}
	
	public void UDPReceiver(String SERVER_DNS,int Port) {
		this.SERVER_DNS = SERVER_DNS;
		this.port = Port;
	}
	
	public void setDNSFile(String filename){
		DNSFile = filename;
	}
	
	public void run(){

        DatagramSocket socket = null;
        try{

            //*Creation d'un socket UDP
            socket = new DatagramSocket(port);

            //*Boucle infinie de recpetion
			while(true){
				
				//*Reception d'un paquet UDP via le socket
                byte[] buffer = new byte[BUF_SIZE];
                socket.receive(new DatagramPacket(buffer, buffer.length));
                //System.out.print(buffer);

				//*Creation d'un DataInputStream ou ByteArrayInputStream pour manipuler les bytes du paquet
                DataInputStream stream = new DataInputStream(new ByteArrayInputStream(buffer));

                // Creation du paquet
                DnsMessage message = new DnsMessage(stream);

                //******  Dans le cas d'un paquet requête *****
                if (message.isRequest()) {

                    DnsMessage.Question question = message.getQuestions().get(0);

                    System.out.println(String.format("Request(%d) -> %s", message.getTransactionId(), question.getName()));

                    //*Sauvegarde du Query Domain name

                    //*Sauvegarde de l'adresse, du port et de l'identifiant de la requete

                    //*Si le mode est redirection seulement

                    //*Rediriger le paquet vers le serveur DNS
                    if(RedirectionSeulement){
                        redirectPacket(socket, buffer);
                    }
                    else {
                        //*Sinon

                        //*Rechercher l'adresse IP associe au Query Domain name dans le fichier de
                        //*correspondance de ce serveur

                        HashMap<String, String> dnsEntries = getDnsEntries();

                        if(dnsEntries.containsKey(question.getName())) {

                            System.out.println(String.format("  cache -> %s = %s",
                                    question.getName(),
                                    dnsEntries.get(question.getName())));

                            //*Creer le paquet de reponse a l'aide du UDPAnswerPaquetCreator
                            //UDPAnswerPacketCreator creator = new UDPAnswerPacketCreator();
                            //byte[] newPacket = creator.CreateAnswerPacket(buffer, dnsEntries.get(question.getQname()));

                            //*Placer ce paquet dans le socket
                            //System.out.println(String.format("  transfert to -> localhost"));
                            //UDPSender sender = new UDPSender(new DatagramPacket(newPacket, newPacket.length), "127.0.0.1", port);
                            //sender.setSocket(socket);
                            //sender.SendPacketNow();

                            //*Envoyer le paquet
                            sendAnswerPacket();
                        }
                        else {
                            //*Rediriger le paquet vers le serveur DNS
                            redirectPacket(socket, buffer);
                        }
                    }
                }
                //******  Dans le cas d'un paquet reponse *****
                else {


                    DnsMessage.Question question = message.getQuestions().get(0);

                    System.out.println(String.format("Response(%d) -> %s",
                            message.getTransactionId(), question.getName()));


                    for(DnsMessage.Answer answer : message.getAnswers()){
                        System.out.println(answer.getAddress());


                    }
                    //*Capture de ou des  adresse(s) IP (ANCOUNT est le nombre de réponses retournées)
                    //System.out.println("ANSWER COUNT : " + header.getAncount());
                    /*if(header.getAncount() > 0){

                        DnsAnswer answer = new DnsAnswer(stream);
                        addDnsEntry(answer.getIpAddress(), question.getQname());


                        HashMap<String, String> dnsEntries = getDnsEntries();

                        if(!dnsEntries.containsKey(question.getQname())){
                            addDnsEntry(answer.getIpAddress(), question.getQname());
                        }


                    } */

                    //*Ajouter la ou les correspondance(s) dans le fichier DNS si elles ne y sont pas déjà
					
					//*Faire parvenir le paquet reponse au demandeur original, ayant emis une requete 
					//*avec cet identifiant
                }
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

    private HashMap<String, String> getDnsEntries() throws IOException {
        HashMap<String, String> dnsEntries = new HashMap<String, String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(DNSFile));

            String line;
            while((line = reader.readLine()) != null) {
                String[] exploded = line.split(" ");
                if(exploded.length > 1) {
                    dnsEntries.put(exploded[1], exploded[0]);
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return dnsEntries;
    }

    private void sendAnswerPacket(){

    }


    private void redirectPacket(DatagramSocket socket, byte[] packet) throws IOException {
        redirectPacket(socket, packet, SERVER_DNS);
    }

    private void redirectPacket(DatagramSocket socket, byte[] packet, String address) throws IOException {
        System.out.println(String.format("  transfert to -> %s", address));
        UDPSender sender = new UDPSender(new DatagramPacket(packet, packet.length), address, port);
        sender.setSocket(socket);
        sender.SendPacketNow();
    }


    private void addDnsEntry(String ip, String hostname) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(DNSFile, true));
            writer.write(String.format("%s %s\n", ip, hostname));
                    }
        finally {
            if (writer != null) {
                writer.close();
            }
        }
    }




}



