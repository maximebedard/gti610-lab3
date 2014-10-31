import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSender  {

	private final static int BUF_SIZE = 1024;
	private String SERVER_DNS = null;
	private int port = 0;  // port de r�ception
	private DatagramPacket packet;
	private DatagramSocket SendSocket;
	private InetAddress addr = null;
	
	public String getSERVER_DNS(){
		return SERVER_DNS;
	}
	
	public void setSocket(DatagramSocket SendSocket){
		this.SendSocket = SendSocket; 
	}
	
	public void setPort(int port){
		this.port = port;
	}
	
	public void setPacket(DatagramPacket packet){
		this.packet = packet;
	}
	
	public void setSERVER_DNS(String server_dns){
		this.SERVER_DNS = server_dns;
	}
	
	public UDPSender(String server_dns,int Port) {
		this.SERVER_DNS = server_dns;
		this.port = Port;
	}
	
	public UDPSender(DatagramPacket packet,String ServerDNS,int port){
		this.packet = packet;
		this.SERVER_DNS = ServerDNS;
		this.port = port;
	}
	
	public UDPSender(int port,DatagramSocket SendSocket){
		this.port = port;
		this.SendSocket = SendSocket;
	}
	
	public UDPSender(){
		
	}
	
	public void SendPacketNow(){
		//Envoi du packet à un serveur dns pour interrogation
		try {
			
			//cree l'adresse de destination
			this.addr = InetAddress.getByName(SERVER_DNS);
			
			//Crée le packet
			packet.setAddress(addr);
			packet.setPort(port);

			//Envoi le packet
			SendSocket.send(packet);

			
		} catch (Exception e) {
			System.err.println("Problème à l'exécution :");
			e.printStackTrace(System.err);
		}
	}
}
