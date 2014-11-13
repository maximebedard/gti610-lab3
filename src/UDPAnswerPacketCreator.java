import java.util.Arrays;
import java.util.List;

public class UDPAnswerPacketCreator {

	private byte[] buffer;
	private final List<String> addresses;
	private byte[] packet;

	public UDPAnswerPacketCreator(byte[] buffer, List<String> addresses){
		this.buffer = buffer;
		this.addresses = addresses;
		this.packet = Arrays.copyOf(buffer, 1024);
	}

	private void createHeader(){

		short answerCount = (short) addresses.size();

		// ID
		packet[0] = (byte)buffer[0];
		packet[1] = (byte)buffer[1];

		// flags
		packet[2] = (byte) 0x81;
		packet[3] = (byte) 0x80;

		// QCOUNT
		packet[4] = (byte) 0x00;
		packet[5] = (byte) 0x01;

		// ANSCOUNT
		packet[6] = (byte) 0x00;
		packet[7] = (byte) 0x00;

		// NSCOUNT
		packet[8] = (byte) 0x00;
		packet[9] = (byte) 0x00;

		// ARCOUNT
		packet[10] = (byte) 0x00;
		packet[11] = (byte) 0x00;

	}

	private int createQuestion(){

		int nbchar = packet[12];
		String hostName = "";
		int index = 13;

		while(nbchar != 0) {

			while (nbchar > 0) {
				hostName += String.valueOf(Character.toChars(packet[index]));
				index++;
				nbchar--;
			}
			hostName = hostName + ".";
			nbchar = packet[index];
			index++;
		}
		index = index - 1;
		// QTYPE
		packet[index + 1] = (byte) (0x00);
		packet[index + 2] = (byte) (0x01);

		// QCLASS
		packet[index + 3] = (byte) (0x00);
		packet[index + 4] = (byte) (0x01);

		return index + 5;
	}

	private void createAnswer(String address, int offset){

		String[] exploded = address.replace(".", " ").split(" ");

		byte part1 = (byte)(Integer.parseInt(exploded[0]) & 0xff);
		byte part2 = (byte)(Integer.parseInt(exploded[1]) & 0xff);
		byte part3 = (byte)(Integer.parseInt(exploded[2]) & 0xff);
		byte part4 = (byte)(Integer.parseInt(exploded[3]) & 0xff);

		packet[offset + 1] = (byte) (0xC0);
		packet[offset + 2] = (byte) (0x0C);

		// TYPE
		packet[offset + 3] = (byte) (0x00);
		packet[offset + 4] = (byte) (0x01);

		// CLASS
		packet[offset + 5] = (byte) (0x00);
		packet[offset + 6] = (byte) (0x01);

		// TTL
		packet[offset + 7] = (byte) (0x00);
		packet[offset + 8] = (byte) (0x01);
		packet[offset + 9] = (byte) (0x1a);
		packet[offset + 10] = (byte) (0x6c);

		packet[offset + 11] = (byte) (0x00);
		packet[offset + 12] = (byte) (0x04);
		//Grace a l'offset de position, nous somme en mesure
		//de faire l'injection de l'adresse IP dans le packet
		//et ce au endroit

		packet[offset + 13] = (byte) (part1 & 0xff);
		packet[offset + 14] = (byte) (part2 & 0xff);
		packet[offset + 15] = (byte) (part3 & 0xff);
		packet[offset + 16] = (byte) (part4 & 0xff);

	}

	public byte[] createPacket(){


		createHeader();

		int offset = createQuestion();

        System.out.println(" OFFSET : " + offset);

		for(String addr:addresses){
			createAnswer(addr, offset);
			offset += 17;
		}

        byte[] bytes = Arrays.copyOf(packet, offset);

        for(byte b:bytes) {
            System.out.print((char)b + "_");
        }
        System.out.print('\n');

        return bytes;

    }
}