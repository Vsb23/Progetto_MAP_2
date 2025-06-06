package mining;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

import data.Data;
import exception.ClusteringRadiusException;
import exception.EmptyDatasetException;

/**
 * classe che implementa l'algoritmo di Quality Threshold.
 */
public class QTMiner {

	private ClusterSet C;
	private double radius;
	private static final String DIRECTORY_PATH = "./results/";

	/**
	 * Crea un nuovo ClusterSet e assegna il raggio.
	 * 
	 * @param radius raggio dei cluster
	 */
	public QTMiner(final double radius) {
		C = new ClusterSet();
		this.radius = radius;
	}

	/**
	 * carica da file un ClusterSet salvato.
	 * 
	 * @param fileName percorso + nome file
	 * @throws ClassNotFoundException se viene letto un oggetto di cui non conosco la classe.
	 * @throws IOException problemi in lettura.
	 * @throws FileNotFoundException se il file non esiste o non � leggibile.
	 */
	public QTMiner(final String fileName) throws FileNotFoundException, IOException, ClassNotFoundException {
		final FileInputStream inFile = new FileInputStream(fileName);
		final ObjectInputStream inStream = new ObjectInputStream(inFile);
		C = (ClusterSet) inStream.readObject();
		inStream.close();
		inFile.close();
	}

	/**
	 * restituisce il ClusterSet creato.
	 * 
	 * @return C ClusterSet.
	 */
	public ClusterSet getC() {
		return C;
	}

	/**
	 * richiama buildCandidateCluster finch� tutte le tuple non sono state inserite
	 * in un cluster. restituisce il numero di cluster creati.
	 * 
	 * @param data dataSet contenente i dati da clusterizzare.
	 * @return numclusters numero di cluster generati.
	 * @throws ClusteringRadiusException se il numero di clusters � 1
	 * @throws EmptyDatasetException se la tabella � vuota
	 */
	public int compute(final Data data) throws ClusteringRadiusException, EmptyDatasetException {

		if (data.getNumberOfExamples() == 0) {
			throw new EmptyDatasetException("Empty dataset!");
		}
		int numclusters = 0;
		final boolean[] isClustered = new boolean[data.getNumberOfExamples()];
		for (int i = 0; i < isClustered.length; i++) {
			isClustered[i] = false;
		}
		int countClustered = 0;
		while (countClustered != data.getNumberOfExamples()) {
			final Cluster c = buildCandidateCluster(data, isClustered);
			C.add(c);
			numclusters++;
			for (final Integer i : c) {
				isClustered[i] = true;
			}
			countClustered += c.getSize();
		}

		if (numclusters == 1) {
			throw new ClusteringRadiusException(data.getNumberOfExamples() + "tuples in one cluster!");
		}
		return numclusters;
	}

	/**
	 * Costruisce un cluster per ciascuna tupla di data non ancora clusterizzata in
	 * un cluster di C e restituisce il cluster candidato pi� popoloso.
	 * 
	 * @param data        dataSet contenente i dati da clusterizzare.
	 * @param isClustered array di booleani che indica quali tuple sono state gi�
	 *                    clusterizzate
	 * @return il cluster candidato
	 */
	private Cluster buildCandidateCluster(final Data data, final boolean[] isClustered) {

		final Set<Cluster> C = new TreeSet<Cluster>();
		for (int i = 0; i < data.getNumberOfExamples(); i++) {
			if (!isClustered[i]) {
				final Cluster candidato = new Cluster(data.getItemSet(i));
				for (int j = 0; j < data.getNumberOfExamples(); j++) {
					if (!isClustered[j]) {
						if (data.getItemSet(i).getDistance(data.getItemSet(j)) <= radius) {
							candidato.addData(j);
						}
					}
				}
				C.add(candidato);
			}
		}
		return ((TreeSet<Cluster>) C).last();
	}

	/**
	 * serializza il ClusterSet su file.
	 * 
	 * @param fileName nome del file su cui salvare il ClusterSet.
	 * @throws FileNotFoundException se il file � una directory, se non � possibile crearlo o non si pu� aprire.
	 * @throws IOException problemi in scrittura.
	 */

	 public void salva(final String fileName) throws FileNotFoundException, IOException {
		// Definisci qui la costante per il percorso della directory
		final String DIRECTORY_PATH = "./results";
		
		Path directoryPath = Paths.get(DIRECTORY_PATH);
		File directory = directoryPath.toFile();
		
		// Stampa di debug per verificare il percorso
		System.out.println("Tentativo di creazione directory in: " + directory.getAbsolutePath());
		
		// Crea la directory se non esiste
		if (!directory.exists()) {
			boolean created = directory.mkdirs();
			System.out.println("Directory creata: " + created);
			if (!created) {
				throw new IOException("Impossibile creare la directory: " + DIRECTORY_PATH);
			}
		}
		
		// Costruisci il percorso completo del file
		Path filePath = directoryPath.resolve(fileName).normalize();
		
		System.out.println("Salvando il file in: " + filePath.toAbsolutePath());
		
		// Serializzazione del ClusterSet con try-with-resources
		try (FileOutputStream outFile = new FileOutputStream(filePath.toFile());
			 ObjectOutputStream outStream = new ObjectOutputStream(outFile)) {
			outStream.writeObject(C);
		}
		
		System.out.println("File salvato con successo in: " + filePath.toAbsolutePath());
	}

	@Override
	public String toString() {
		return C.toString();
	}
	
	public String toString(final Data data) {
		return C.toString(data);
	}
}
