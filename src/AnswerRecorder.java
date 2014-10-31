import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/***
 * Cette classe est utilisé pour enregistrer une réponse
 * dans le fichier texte en provenance d'un Server DNS autre.
 * @author Max
 *
 */

public class AnswerRecorder {
	private FileWriter writerFichierSource = null;
	private String adresseIP = null;
	private String hostname = null;
	private String filename = null;
	private String uneligne = null;
	
	/**
	 * Construteur par default
	 *
	 */
	public AnswerRecorder(){
		
	}
	
	/**
	 * Constructeur
	 * @param filename
	 * @param hostname
	 * @param adresseIP
	 */
	public AnswerRecorder(String filename,String hostname,String adresseIP){
		this.adresseIP = adresseIP;
		this.hostname = hostname;
		this.filename = filename;
	}
	
	/**
	 * Construteur
	 * @param filename
	 */
	public AnswerRecorder(String filename){
		this.filename = filename;
	}
	
	public void StartRecord(String hostname,String adresseIP){

		try {
			writerFichierSource = new FileWriter(filename,true);
			writerFichierSource.write(hostname + " " + adresseIP);
			writerFichierSource.write("\r\n");
			writerFichierSource.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
