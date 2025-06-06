package com.packagep.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.packagep.exception.DatabaseConnectionException;
import com.packagep.database.DbAccess;
import com.packagep.exception.EmptySetException;
import com.packagep.database.Example;
import com.packagep.exception.NoValueException;
import com.packagep.database.QUERY_TYPE;
import com.packagep.database.TableData;
import com.packagep.database.TableSchema;

/**
 * Modella una tabella del database come dataset per il clustering.
 * Questa classe rappresenta un insieme di dati strutturati contenenti
 * esempi con attributi continui e discreti.
 */
public class Data {

	/**
	 * Lista degli esempi contenuti nel dataset.
	 */
	private List<Example> data = new ArrayList<Example>();
	
	/**
	 * Numero totale di esempi nel dataset.
	 */
	private int numberOfExamples;
	
	/**
	 * Lista degli attributi che caratterizzano il dataset.
	 */
	private List<Attribute> attributeSet = new LinkedList<Attribute>();

	/**
	 * Costruttore che popola l'attributeSet, data e inizializza numberOfExamples 
	 * utilizzando una tabella del database.
	 * Stabilisce una connessione al database, recupera i dati distinti dalla tabella
	 * specificata e costruisce lo schema degli attributi.
	 *
	 * @param table nome della tabella del database da cui caricare i dati
	 * @throws EmptySetException           se la tabella è vuota o non contiene dati
	 * @throws SQLException                se la tabella non esiste o errori di accesso al database
	 * @throws NoValueException            eccezione lanciata quando l'operatore
	 *                                     aggregato non restituisce risultati
	 * @throws DatabaseConnectionException se la connessione al database fallisce
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
	 * Restituisce il numero totale di esempi presenti nel dataset.
	 * 
	 * @return il numero di esempi nel dataset
	 */
	public int getNumberOfExamples() {
		return numberOfExamples;
	}

	/**
	 * Restituisce il numero totale di attributi che caratterizzano il dataset.
	 * 
	 * @return il numero di attributi nel dataset
	 */
	public int getNumberOfAttributes() {
		return attributeSet.size();
	}

	/**
	 * Restituisce il valore dell'attributo alla riga exampleIndex e colonna
	 * attributeIndex specificati.
	 *
	 * @param exampleIndex   indice della riga (esempio) da cui prelevare il valore
	 * @param attributeIndex indice della colonna (attributo) da cui prelevare il valore
	 * @return il valore dell'attributo alla posizione specificata
	 */
	public Object getAttributeValue(final int exampleIndex, final int attributeIndex) {
		return data.get(exampleIndex).get(attributeIndex);
	}

	/**
	 * Restituisce l'attributo in posizione index nello schema del dataset.
	 *
	 * @param index indice dell'attributo da restituire
	 * @return l'oggetto Attribute alla posizione specificata
	 */
	public Attribute getAttribute(final int index) {
		return attributeSet.get(index);
	}

	/**
	 * Restituisce la lista completa degli attributi che compongono lo schema del dataset.
	 * 
	 * @return lista degli attributi del dataset
	 */
	public List<Attribute> getAttributeSchema() {
		return attributeSet;
	}

	/**
	 * Restituisce la tupla completa all'indice specificato.
	 * Crea un oggetto Tuple contenente tutti i valori degli attributi
	 * per l'esempio alla posizione index.
	 *
	 * @param index indice di riga dell'esempio da restituire come tupla
	 * @return tupla contenente tutti i valori dell'esempio
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
	 * Restituisce una rappresentazione testuale del dataset.
	 * Include la lista degli attributi seguita da tutti gli esempi
	 * con i rispettivi valori.
	 * 
	 * @return stringa contenente la rappresentazione completa del dataset
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