import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;

public class UDPSender  {


	public static final int DEFAULT_PORT = 53;

	public static void send(DatagramSocket socket, byte[] bytes, String address, int port) {
		try {
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
			packet.setAddress(InetAddress.getByName(address));
			packet.setPort(port);

			socket.send(packet);
		}
		catch (IOException e){
			e.printStackTrace(System.err);
		}
	}

	public static void send(DatagramSocket socket, byte[] bytes, String address) {
		send(socket, bytes, address, DEFAULT_PORT);
	}


}
