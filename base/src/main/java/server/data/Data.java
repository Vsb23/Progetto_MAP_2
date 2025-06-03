package data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import exception.DatabaseConnectionException;
import database.DbAccess;
import exception.EmptySetException;
import database.Example;
import exception.NoValueException;
import database.QUERY_TYPE;
import database.TableData;
import database.TableSchema;

/**
 * Rappresenta una collezione di dati estratti da una tabella del database per
 * l'applicazione dell'algoritmo Quality Threshold clustering.
 * 
 * <p>Questa classe modella i dati necessari per il clustering QT, fornendo
 * accesso strutturato agli esempi (tuple) e ai loro attributi. I dati vengono
 * caricati dinamicamente dal database e organizzati in attributi continui o
 * discreti in base al loro tipo.</p>
 * 
 * <p>Il Quality Threshold clustering è un algoritmo che raggruppa i dati in
 * cluster di qualità garantita, dove ogni cluster deve soddisfare un threshold
 * di qualità minimo specificato.</p>
 * 
 * @author [Nome Autore]
 * @version 1.0
 * @since 1.0
 */
public class Data {

	/**
	 * Lista contenente tutti gli esempi (tuple) estratti dalla tabella del database.
	 * Ogni Example rappresenta una riga della tabella con i suoi valori.
	 */
	private List<Example> data = new ArrayList<Example>();
	
	/**
	 * Numero totale di esempi presenti nel dataset.
	 * Corrisponde alla cardinalità di {@code data}.
	 */
	private int numberOfExamples;
	
	/**
	 * Schema degli attributi del dataset. Contiene la descrizione di tutti
	 * gli attributi (colonne) presenti nei dati, sia continui che discreti.
	 */
	private List<Attribute> attributeSet = new LinkedList<Attribute>();

	/**
	 * Costruisce un oggetto Data caricando i dati dalla tabella specificata del database.
	 * 
	 * <p>Il costruttore esegue le seguenti operazioni:</p>
	 * <ol>
	 *   <li>Stabilisce una connessione al database</li>
	 *   <li>Carica tutte le transazioni distinte dalla tabella</li>
	 *   <li>Analizza lo schema della tabella per determinare i tipi di attributi</li>
	 *   <li>Crea oggetti {@link ContinuousAttribute} per attributi numerici</li>
	 *   <li>Crea oggetti {@link DiscreteAttribute} per attributi categorici</li>
	 * </ol>
	 * 
	 * <p>Per gli attributi continui vengono calcolati automaticamente i valori
	 * minimo e massimo presenti nella colonna corrispondente.</p>
	 *
	 * @param table il nome della tabella del database da cui caricare i dati
	 * @throws EmptySetException           se la tabella specificata è vuota o non contiene dati validi
	 * @throws SQLException                se si verifica un errore SQL (es. tabella inesistente, 
	 *                                     errore di sintassi nella query)
	 * @throws NoValueException            se l'operatore aggregato (MIN/MAX) non restituisce risultati
	 *                                     per un attributo continuo
	 * @throws DatabaseConnectionException se non è possibile stabilire la connessione al database
	 * 
	 * @see TableData#getDistinctTransazioni(String)
	 * @see TableSchema#TableSchema(DbAccess, String)
	 * @see ContinuousAttribute
	 * @see DiscreteAttribute
	 */
	public Data(final String table)
			throws EmptySetException, SQLException, NoValueException, DatabaseConnectionException {

		final DbAccess db = new DbAccess();
		final TableData tData = new TableData(db);

		db.initConnection();
		data = tData.getDistinctTransazioni(table);
		final TableSchema tSchema = new TableSchema(db, table);
		numberOfExamples = data.size();

		for (int i = 0; i < tSchema.getNumberOfAttributes(); i++) {

			if (tSchema.getColumn(i).isNumber()) {
				attributeSet.add(new ContinuousAttribute(tSchema.getColumn(i).getColumnName(),
						i,
						(float) tData.getAggregateColumnValue(table,
								tSchema.getColumn(i), QUERY_TYPE.MIN),
						(float) tData.getAggregateColumnValue(table,
								tSchema.getColumn(i), QUERY_TYPE.MAX)));
			} else {
				attributeSet.add(new DiscreteAttribute(tSchema.getColumn(i).getColumnName(), i,
						tData.getDistinctColumnValues(table, tSchema.getColumn(i))
								.toArray(new String[0])));
			}
		}
	}

	/**
	 * Restituisce il numero totale di esempi (righe) presenti nel dataset.
	 * 
	 * @return il numero di esempi nel dataset
	 */
	public int getNumberOfExamples() {
		return numberOfExamples;
	}

	/**
	 * Restituisce il numero totale di attributi (colonne) presenti nel dataset.
	 * 
	 * @return il numero di attributi nel dataset
	 */
	public int getNumberOfAttributes() {
		return attributeSet.size();
	}

	/**
	 * Restituisce il valore dell'attributo specificato per l'esempio indicato.
	 * 
	 * <p>Questo metodo permette di accedere ai singoli valori del dataset
	 * utilizzando gli indici di riga (esempio) e colonna (attributo).</p>
	 *
	 * @param exampleIndex   l'indice dell'esempio (riga) nel dataset (0-based)
	 * @param attributeIndex l'indice dell'attributo (colonna) nel dataset (0-based)
	 * @return il valore dell'attributo per l'esempio specificato.
	 *         Il tipo dipende dall'attributo: {@code String} per attributi discreti,
	 *         {@code Number} per attributi continui
	 * @throws IndexOutOfBoundsException se gli indici sono fuori dai limiti validi
	 * 
	 * @see #getNumberOfExamples()
	 * @see #getNumberOfAttributes()
	 */
	public Object getAttributeValue(final int exampleIndex, final int attributeIndex) {
		return data.get(exampleIndex).get(attributeIndex);
	}

	/**
	 * Restituisce l'oggetto Attribute specificato dall'indice.
	 * 
	 * <p>L'oggetto restituito può essere un'istanza di {@link ContinuousAttribute}
	 * o {@link DiscreteAttribute} a seconda del tipo di dati contenuti nella
	 * colonna corrispondente.</p>
	 *
	 * @param index l'indice dell'attributo da recuperare (0-based)
	 * @return l'oggetto Attribute alla posizione specificata
	 * @throws IndexOutOfBoundsException se l'indice è fuori dai limiti validi
	 * 
	 * @see Attribute
	 * @see ContinuousAttribute
	 * @see DiscreteAttribute
	 */
	public Attribute getAttribute(final int index) {
		return attributeSet.get(index);
	}

	/**
	 * Restituisce la lista completa degli attributi che definiscono lo schema del dataset.
	 * 
	 * <p>Questa lista contiene tutti gli attributi (continui e discreti) presenti
	 * nel dataset, mantenendo l'ordine originale delle colonne della tabella.</p>
	 * 
	 * @return una lista non modificabile contenente tutti gli attributi del dataset
	 * 
	 * @see Attribute
	 */
	public List<Attribute> getAttributeSchema() {
		return attributeSet;
	}

	/**
	 * Costruisce e restituisce una tupla ({@link Tuple}) completa per l'esempio specificato.
	 * 
	 * <p>La tupla risultante contiene tutti gli attributi dell'esempio sotto forma di
	 * oggetti {@link Item} tipizzati:</p>
	 * <ul>
	 *   <li>{@link DiscreteItem} per attributi categorici</li>
	 *   <li>{@link ContinuousItem} per attributi numerici</li>
	 * </ul>
	 * 
	 * <p>Questo metodo è particolarmente utile per l'algoritmo di clustering QT
	 * che opera su tuple complete di dati.</p>
	 *
	 * @param index l'indice dell'esempio per cui costruire la tupla (0-based)
	 * @return una tupla completa contenente tutti gli attributi dell'esempio
	 * @throws IndexOutOfBoundsException se l'indice è fuori dai limiti validi
	 * 
	 * @see Tuple
	 * @see DiscreteItem
	 * @see ContinuousItem
	 */
	public Tuple getItemSet(final int index) {
		final Tuple tuple = new Tuple(attributeSet.size());
		for (int i = 0; i < attributeSet.size(); i++) {
			if (attributeSet.get(i) instanceof DiscreteAttribute) {
				tuple.add(new DiscreteItem((DiscreteAttribute) attributeSet.get(i),
						(String) data.get(index).get(i)), i);
			} else {
				tuple.add(new ContinuousItem((ContinuousAttribute) attributeSet.get(i),
						(double) data.get(index).get(i)), i);
			}
		}
		return tuple;
	}

	/**
	 * Restituisce una rappresentazione testuale completa del dataset.
	 * 
	 * <p>La stringa risultante include:</p>
	 * <ol>
	 *   <li>L'intestazione con i nomi di tutti gli attributi</li>
	 *   <li>Tutte le righe del dataset numerate progressivamente</li>
	 *   <li>I valori separati da virgole per una facile lettura</li>
	 * </ol>
	 * 
	 * <p>Questo formato è utile per debug, logging e visualizzazione
	 * del contenuto del dataset.</p>
	 * 
	 * @return una stringa contenente la rappresentazione completa del dataset
	 * 
	 * @see Attribute#toString()
	 */
	@Override
	public String toString() {
		String s = "";
		for (int i = 0; i < getNumberOfAttributes(); i++) {
			// se non avessi ridefinito il toString in Attribute, avrei la stampa di default
			// di Object
			s += attributeSet.get(i);
			if (i != getNumberOfAttributes() - 1) {
				s += ", ";
			}
		}
		s += '\n';
		for (int i = 0; i < getNumberOfExamples(); i++) {
			s += i + 1 + ":";
			for (int j = 0; j < getNumberOfAttributes(); j++) {
				s += getAttributeValue(i, j);
				if (j != getNumberOfAttributes() - 1) {
					s += ", ";
				}
			}
			s += "\n";
		}
		return s;
	}
}