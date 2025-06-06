package com.packagep.mining;

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

import com.packagep.data.Data;
import com.packagep.exception.ClusteringRadiusException;
import com.packagep.exception.EmptyDatasetException;

/**
 * Classe che implementa l'algoritmo di Quality Threshold per il clustering dei dati.
 * 
 * <p>L'algoritmo Quality Threshold (QT) è un algoritmo di clustering che garantisce
 * che tutti i punti in un cluster siano entro una distanza specificata (raggio) 
 * dal centroide del cluster. L'algoritmo costruisce iterativamente clusters candidati
 * e seleziona quello più popoloso ad ogni iterazione.</p>
 * 
 * <p>La classe supporta sia la creazione di nuovi clustering che il caricamento
 * di clustering precedentemente salvati su file.</p>
 * 
 * @author [Nome Autore]
 * @version 1.0
 * @since 1.0
 */
public class QTMiner {

	/**
	 * Il set di cluster generati dall'algoritmo.
	 */
	private ClusterSet C;
	
	/**
	 * Il raggio massimo entro cui i punti devono trovarsi per appartenere allo stesso cluster.
	 */
	private double radius;
	
	/**
	 * Percorso della directory dove salvare i risultati del clustering.
	 */
	private static final String DIRECTORY_PATH = "../../results";

	/**
	 * Costruisce un nuovo oggetto QTMiner con il raggio specificato.
	 * 
	 * <p>Inizializza un nuovo ClusterSet vuoto e imposta il raggio per il clustering.</p>
	 * 
	 * @param radius il raggio dei cluster. Deve essere un valore positivo che rappresenta
	 *               la distanza massima tra i punti appartenenti allo stesso cluster
	 * @throws IllegalArgumentException se radius è negativo o zero
	 */
	public QTMiner(final double radius) {
		if (radius <= 0) {
			throw new IllegalArgumentException("Il raggio deve essere un valore positivo");
		}
		C = new ClusterSet();
		this.radius = radius;
	}

	/**
	 * Costruisce un oggetto QTMiner caricando un ClusterSet precedentemente salvato da file.
	 * 
	 * <p>Questo costruttore permette di ripristinare uno stato di clustering
	 * precedentemente salvato utilizzando il metodo {@link #salva(String)}.</p>
	 * 
	 * @param fileName il percorso completo del file da cui caricare il ClusterSet.
	 *                 Il file deve essere stato creato utilizzando la serializzazione Java
	 * @throws FileNotFoundException se il file specificato non esiste o non è leggibile
	 * @throws IOException se si verificano problemi durante la lettura del file
	 * @throws ClassNotFoundException se viene letto un oggetto di una classe sconosciuta
	 * @throws IllegalArgumentException se fileName è null o vuoto
	 */
	public QTMiner(final String fileName) throws FileNotFoundException, IOException, ClassNotFoundException {
		if (fileName == null || fileName.trim().isEmpty()) {
			throw new IllegalArgumentException("Il nome del file non può essere null o vuoto");
		}
		
		final FileInputStream inFile = new FileInputStream(fileName);
		final ObjectInputStream inStream = new ObjectInputStream(inFile);
		C = (ClusterSet) inStream.readObject();
		inStream.close();
		inFile.close();
	}

	/**
	 * Restituisce il ClusterSet generato dall'algoritmo di clustering.
	 * 
	 * <p>Il ClusterSet contiene tutti i cluster identificati durante l'esecuzione
	 * dell'algoritmo Quality Threshold.</p>
	 * 
	 * @return il ClusterSet contenente tutti i cluster generati.
	 *         Può essere vuoto se l'algoritmo non è ancora stato eseguito
	 */
	public ClusterSet getC() {
		return C;
	}

	/**
	 * Esegue l'algoritmo di clustering Quality Threshold sui dati forniti.
	 * 
	 * <p>L'algoritmo funziona nel seguente modo:</p>
	 * <ol>
	 * <li>Per ogni punto non ancora clusterizzato, costruisce un cluster candidato</li>
	 * <li>Seleziona il cluster candidato più popoloso</li>
	 * <li>Aggiunge il cluster selezionato al ClusterSet</li>
	 * <li>Marca tutti i punti del cluster come clusterizzati</li>
	 * <li>Ripete fino a quando tutti i punti sono stati assegnati a un cluster</li>
	 * </ol>
	 * 
	 * @param data il dataset contenente i dati da clusterizzare. Non può essere null
	 * @return il numero di cluster generati dall'algoritmo
	 * @throws EmptyDatasetException se il dataset è vuoto (nessun esempio)
	 * @throws ClusteringRadiusException se viene generato un solo cluster, 
	 *                                   indicando che il raggio è troppo grande
	 * @throws IllegalArgumentException se data è null
	 */
	public int compute(final Data data) throws ClusteringRadiusException, EmptyDatasetException {
		if (data == null) {
			throw new IllegalArgumentException("Il dataset non può essere null");
		}

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
			throw new ClusteringRadiusException(data.getNumberOfExamples() + " tuples in one cluster!");
		}
		return numclusters;
	}

	/**
	 * Costruisce un cluster candidato per ogni tupla non ancora clusterizzata
	 * e restituisce quello più popoloso.
	 * 
	 * <p>Per ogni punto non ancora assegnato a un cluster:</p>
	 * <ul>
	 * <li>Crea un nuovo cluster candidato centrato su quel punto</li>
	 * <li>Aggiunge al cluster tutti i punti entro il raggio specificato</li>
	 * <li>Seleziona il cluster con il maggior numero di punti</li>
	 * </ul>
	 * 
	 * @param data il dataset contenente i dati da clusterizzare
	 * @param isClustered array booleano che indica quali tuple sono già state
	 *                    assegnate a un cluster. L'indice corrisponde all'indice
	 *                    della tupla nel dataset
	 * @return il cluster candidato con il maggior numero di punti
	 * @throws IllegalArgumentException se data è null o se isClustered è null
	 *                                  o ha dimensione diversa dal numero di esempi
	 */
	private Cluster buildCandidateCluster(final Data data, final boolean[] isClustered) {
		if (data == null) {
			throw new IllegalArgumentException("Il dataset non può essere null");
		}
		if (isClustered == null || isClustered.length != data.getNumberOfExamples()) {
			throw new IllegalArgumentException("Array isClustered non valido");
		}

		final Set<Cluster> candidateClusters = new TreeSet<Cluster>();
		
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
				candidateClusters.add(candidato);
			}
		}
		return ((TreeSet<Cluster>) candidateClusters).last();
	}

	/**
	 * Serializza il ClusterSet corrente su file per un successivo utilizzo.
	 * 
	 * <p>Il metodo crea automaticamente la directory di destinazione se non esiste
	 * e salva il ClusterSet utilizzando la serializzazione Java. Il file può essere
	 * successivamente caricato utilizzando il costruttore {@link #QTMiner(String)}.</p>
	 * 
	 * <p>Il file viene salvato nella directory "./results" relativa alla posizione
	 * di esecuzione del programma.</p>
	 * 
	 * @param fileName il nome del file in cui salvare il ClusterSet (senza percorso).
	 *                 Non può essere null o vuoto
	 * @throws FileNotFoundException se il file è una directory, se non è possibile 
	 *                               crearlo o non si può aprire in scrittura
	 * @throws IOException se si verificano problemi durante la scrittura del file
	 *                     o la creazione della directory
	 * @throws IllegalArgumentException se fileName è null o vuoto
	 */
	public void salva(final String fileName) throws FileNotFoundException, IOException {
		if (fileName == null || fileName.trim().isEmpty()) {
			throw new IllegalArgumentException("Il nome del file non può essere null o vuoto");
		}
		
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

	/**
	 * Restituisce una rappresentazione in formato stringa del ClusterSet.
	 * 
	 * <p>Utilizza il metodo toString() del ClusterSet per generare
	 * una rappresentazione testuale di tutti i cluster.</p>
	 * 
	 * @return una stringa che rappresenta tutti i cluster del ClusterSet
	 */
	@Override
	public String toString() {
		return C.toString();
	}
	
	/**
	 * Restituisce una rappresentazione dettagliata in formato stringa del ClusterSet
	 * includendo informazioni sui dati originali.
	 * 
	 * <p>Questa versione del toString fornisce informazioni più dettagliate
	 * sui cluster includendo i dati originali dal dataset.</p>
	 * 
	 * @param data il dataset originale utilizzato per il clustering.
	 *             Utilizzato per fornire informazioni dettagliate sui punti
	 * @return una stringa dettagliata che rappresenta tutti i cluster
	 *         con informazioni sui dati originali
	 * @throws IllegalArgumentException se data è null
	 */
	public String toString(final Data data) {
		if (data == null) {
			throw new IllegalArgumentException("Il dataset non può essere null");
		}
		return C.toString(data);
	}
}