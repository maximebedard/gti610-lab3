import java.io.File;
import java.io.IOException;

 /**
 * Application principale qui lance les autres processus
 * @author Maxime Bouchard
 *
 */

public class ServeurDNS {
	
	public static void main(String[] args) {
		
		System.out.println("--------------------------------------");
		System.out.println("Ecole de Technologie Superieures (ETS)");
		System.out.println("GTI610 - Réseau de télécommunication");
		System.out.println("      Serveur DNS simplifié");
		System.out.println("--------------------------------------");
		
		if (args.length == 0) {
			System.out.println("Usage: "
					+"[addresse DNS] <Fichier DNS> <TrueFalse/Redirection seulement>");
			System.out.println("Pour lister la table: "
					+"showtable <Fichier DNS>");
			System.out.println("Pour lancer par defaut, tapper : default");
			System.exit(1);
		}
		
		UDPReceiver udpReceiver = new UDPReceiver();
		File f = null;	
		udpReceiver.setReceivingPort(53);
		
		/* cas où l'argument = default
		 Le serveur DNS de redirection est celui de l'école "10.162.8.51"
		         ====> attention, si vous travaillez ailleurs, pensez à le mettre à jour
		 Le cache dns est le fichier: "DNSFILE.TXT"
		 et la redirection est par defaut à "false"
		*/
		if(args[0].equals("default")){
			if (args.length <= 1) {
				udpReceiver.setDNSServerAddress("8.8.8.8");
				f = new File("DNSFILE.TXT");
				if(f.exists()){
					udpReceiver.setDnsFile("DNSFILE.TXT");
				}
				else{
					try {
						f.createNewFile();
						udpReceiver.setDnsFile("DNSFILE.TXT");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				udpReceiver.setRedirectionSeulement(false);
				
				// et on lance le thread
				udpReceiver.start();
			}
			else{
				System.out.print("L'éxécution par défaut n'a pas d'autres arguments");
			}
		}
		else{
			if(args[0].equals("showtable")){ // cas où l'argument = showtable cacheDNS
				if (args.length == 2) {
					f = new File(args[1]);
					if(f.exists()){
						udpReceiver.setDnsFile(args[1]);
					}
					else{
						try {
							f.createNewFile();
							udpReceiver.setDnsFile(args[1]);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					QueryFinder QF = new QueryFinder(args[1]);
					QF.listCorrespondingTable();	
				}
				else{
					System.out.println("vous n'avez pas indique le nom du fichier");
				}
			}
			else{
				if (args.length == 3) { // cas où les arguments sont: [IPserveurDNS] [cacheDNS] [redirectionOuNon]
					udpReceiver.setDNSServerAddress(args[0]);
					f = new File(args[1]);
					if(f.exists()){
						udpReceiver.setDnsFile(args[1]);
					}	
					else{
						try {
							f.createNewFile();
							udpReceiver.setDnsFile(args[1]);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					udpReceiver.setRedirectionSeulement(Boolean.parseBoolean(args[2]));

					// et on lance le thread
					udpReceiver.start();
				}
				else
					System.out.println("Un argument est manquant!");
			}
		}
	}	
}

