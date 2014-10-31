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
            socket = new DatagramSocket(port, InetAddress.getByName("localhost"));

            //*Boucle infinie de recpetion
			while(true){
				
				//*Reception d'un paquet UDP via le socket
                byte[] buffer = new byte[BUF_SIZE];
                socket.receive(new DatagramPacket(buffer, buffer.length));
                //System.out.print(buffer);

				//*Creation d'un DataInputStream ou ByteArrayInputStream pour manipuler les bytes du paquet
                DataInputStream stream = new DataInputStream(new ByteArrayInputStream(buffer));

				//*Lecture et sauvegarde des deux premier bytes, qui specifie l'identifiant
                short id = stream.readShort();
                System.out.println("ID : " + id);

				//* lecture du bit QR qui indique si le paquet est une requête ou ne réponse.
				//* vous pouvez aussi vous servir du huitieme byte (ANCount), qui specifie le nombre de reponses
				//*  dans le message (si  ANCount = 0 alors c'est une requête)

                byte flags1 = stream.readByte();
                byte flags2 = stream.readByte();

                boolean qr = flags1 < 0;

                short qdcount = stream.readShort();
                short ancount = stream.readShort();
                short nscount = stream.readShort();
                short arcount = stream.readShort();

                //*Lecture du Query Domain name, a partir du 13 byte
                String qname = "";
                byte cb;
                while((cb = stream.readByte()) != 0x00)
                    qname += (char)cb;

                qname = qname.trim().replace('\u0003', '.');
                System.out.println("Qname : " + qname);

                short qtype = stream.readShort();
                short qclass = stream.readShort();

                //******  Dans le cas d'un paquet requête *****
                if (!qr) {


                    //*Sauvegarde du Query Domain name

                    //*Sauvegarde de l'adresse, du port et de l'identifiant de la requete

                    String name = "";
                    while ((cb = stream.readByte()) != 0x00)
                        name += (char)cb;

                    name = name.trim().replace('\u0003', '.');
                    System.out.println("Name : " + name);

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

                        System.out.println(String.format("Recherche de l'hôte %s dans la cache local", qname));

                        if(dnsEntries.containsKey(qname)) {

                            //*Creer le paquet de reponse a l'aide du UDPAnswerPaquetCreator
                            UDPAnswerPacketCreator creator = new UDPAnswerPacketCreator();
                            byte[] newPacket = creator.CreateAnswerPacket(buffer, dnsEntries.get(qname));

                            //*Placer ce paquet dans le socket
                            socket.send(new DatagramPacket(newPacket, newPacket.length));

                            //*Envoyer le paquet

                        }
                        else {
                            //*Rediriger le paquet vers le serveur DNS
                            redirectPacket(socket, buffer);
                        }

                    }

                }
                //******  Dans le cas d'un paquet reponse *****

                else {
                    System.out.println("REPONSE");
                    //*Lecture du Query Domain name, a partir du 13 byte

					//*Sauvegarde du Query Domain name
					
					//*Passe par dessus Query Type et Query Class

                    stream.skipBytes(18);


                    String ip = String.format("%d.%d.%d.%d",
                            stream.readByte(),
                            stream.readByte(),
                            stream.readByte(),
                            stream.readByte());

                    System.out.println("IP: " + ip);


				    //*Passe par dessus les premiers champs du ressource record pour arriver au ressource data
			        //*qui contient l'adresse IP associe au hostname (dans le fond saut de 16 bytes)
					
					//*Capture de ou des  adresse(s) IP (ANCOUNT est le nombre de réponses retournées)
					
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

    private HashMap<String, String> getDnsEntries() {
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
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dnsEntries;
    }


    private void redirectPacket(DatagramSocket socket, byte[] packet) throws IOException {
        System.out.println(String.format("Transfert de la requête vers : %s", SERVER_DNS));
        UDPSender sender = new UDPSender(new DatagramPacket(packet, packet.length), SERVER_DNS, port);
        sender.setSocket(socket);
        sender.SendPacketNow();
    }






}

