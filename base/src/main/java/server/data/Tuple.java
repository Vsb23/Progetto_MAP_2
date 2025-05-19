package data;

import java.io.Serializable;
import java.util.Set;

/**
 * Modella una tupla (riga) di una tabella di dati, composta da una sequenza ordinata di {@link Item}.
 * Fornisce metodi per il calcolo di distanze e confronti con altre tuple.
 * <p>
 * Implementa {@link Serializable} per supportare la serializzazione degli oggetti.
 * </p>
 * 
 * @see Item
 * @see DiscreteItem
 * @see ContinuousItem
 */
public class Tuple implements Serializable {

    private Item[] tuple;

    /**
     * Costruisce una tupla vuota della dimensione specificata.
     * 
     * @param size Dimensione della tupla (numero di elementi/colonne)
     * @throws IllegalArgumentException se size è minore di 1
     */
    Tuple(final int size) {
        tuple = new Item[size];
    }

    /**
     * Restituisce la lunghezza della tupla (numero di elementi).
     * 
     * @return La dimensione della tupla
     */
    public int getLength() {
        return tuple.length;
    }

    /**
     * Restituisce l'item nella posizione specificata.
     * 
     * @param i Indice della posizione (0-based)
     * @return L'item alla posizione i
     * @throws ArrayIndexOutOfBoundsException se l'indice è fuori dai limiti
     */
    public Item get(final int i) {
        return tuple[i];
    }

    /**
     * Inserisce un item nella posizione specificata della tupla.
     * 
     * @param c Item da inserire
     * @param i Posizione in cui inserire l'item
     * @throws ArrayIndexOutOfBoundsException se l'indice è fuori dai limiti
     * @throws NullPointerException se c è null
     */
    void add(final Item c, final int i) {
        tuple[i] = c;
    }

    /**
     * Calcola la distanza tra questa tupla e un'altra tupla.
     * <p>
     * La distanza è la somma delle distanze tra gli item nella stessa posizione
     * nelle due tuple, calcolata usando il metodo {@link Item#distance(Object)}.
     * </p>
     * 
     * @param obj Tupla con cui calcolare la distanza
     * @return La distanza totale tra le tuple
     * @throws NullPointerException se obj è null
     * @throws IllegalArgumentException se le tuple hanno lunghezze diverse
     */
    public double getDistance(final Tuple obj) {
        double distance = 0.0;
        for (int i = 0; i < obj.getLength(); i++) {
            distance += get(i).distance(obj.get(i));
        }
        return distance;
    }

    /**
     * Calcola la distanza media tra questa tupla e un insieme di tuple clusterizzate.
     * 
     * @param data Tabella dei dati contenente tutte le tuple
     * @param clusteredData Insieme degli indici delle tuple clusterizzate
     * @return La distanza media calcolata
     * @throws NullPointerException se data o clusteredData sono null
     * @throws IllegalArgumentException se clusteredData è vuoto
     */
    public double avgDistance(final Data data, final Set<Integer> clusteredData) {
        double p = 0.0, sumD = 0.0;
        for (final Integer i : clusteredData) {
            final double d = getDistance(data.getItemSet(i));
            sumD += d;
        }
        p = sumD / clusteredData.size();
        return p;
    }

    /**
     * Restituisce una rappresentazione stringa della tupla.
     * <p>
     * Il formato è: "valore1, valore2, ..., valoreN"
     * </p>
     * 
     * @return Stringa rappresentante la tupla
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < tuple.length - 1; i++) {
            str.append(tuple[i].getValue()).append(", ");
        }
        str.append(tuple[tuple.length - 1].getValue());
        return str.toString();
    }

    /**
     * Verifica l'uguaglianza tra questa tupla e un'altra tupla.
     * <p>
     * Due tuple sono considerate uguali se hanno la stessa lunghezza e
     * distanza 0 tra loro (tutti gli item corrispondenti sono uguali).
     * </p>
     * 
     * @param o Oggetto da confrontare
     * @return true se le tuple sono uguali, false altrimenti
     * @throws NullPointerException se o è null
     * @throws ClassCastException se o non è una Tuple
     */
    @Override
    public boolean equals(final Object o) {
        if (getLength() != ((Tuple) o).getLength()) {
            return false;
        }
        return ((Tuple) o).getDistance(this) == 0;
    }
}