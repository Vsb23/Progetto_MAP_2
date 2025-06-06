package com.packagep.database;

import java.util.ArrayList;
import java.util.List;

/**
 * Modella una tupla della tabella del database.
 * Questa classe rappresenta una riga di dati dalla tabella del database,
 * contenente una lista di oggetti che rappresentano i valori delle colonne.
 */
public class Example implements Comparable<Example> {
    
    /**
     * Lista degli oggetti che compongono la tupla.
     */
    private List<Object> example = new ArrayList<Object>();
    
    /**
     * Costruttore di default per Example.
     * Inizializza una nuova istanza con una lista vuota di oggetti.
     */
    public Example() {
        // Il costruttore di default è sufficiente
    }
    
    /**
     * Aggiunge un oggetto alla lista della tupla.
     *
     * @param o oggetto da aggiungere alla tupla
     */
    public void add(final Object o) {
        example.add(o);
    }
    
    /**
     * Restituisce l'oggetto in posizione i nella tupla.
     *
     * @param i posizione dell'oggetto da restituire
     * @return oggetto alla posizione specificata
     */
    public Object get(final int i) {
        return example.get(i);
    }
    
    /**
     * Confronta questa tupla con un'altra tupla per l'ordinamento.
     * Il confronto viene effettuato elemento per elemento utilizzando
     * l'ordine naturale degli oggetti comparabili.
     *
     * @param ex l'altra tupla da confrontare
     * @return valore negativo, zero o positivo se questa tupla è rispettivamente
     *         minore, uguale o maggiore della tupla specificata
     */
    @Override
    public int compareTo(final Example ex) {
        int i = 0;
        for (final Object o : ex.example) {
            if (!o.equals(example.get(i))) {
                return ((Comparable) o).compareTo(example.get(i));
            }
            i++;
        }
        return 0;
    }
    
    /**
     * Verifica se questa tupla è uguale ad un altro oggetto.
     * Due tuple sono uguali se contengono gli stessi elementi nello stesso ordine.
     *
     * @param o oggetto da confrontare con questa tupla
     * @return true se le tuple sono uguali, false altrimenti
     */
    @Override
    public boolean equals(final Object o) {
        return example.equals(((Example) o).example);
    }
    
    /**
     * Restituisce una rappresentazione testuale della tupla.
     * Gli elementi vengono concatenati separati da spazi.
     *
     * @return stringa contenente tutti gli elementi della tupla
     */
    @Override
    public String toString() {
        String str = "";
        for (final Object o : example) {
            str += o.toString() + " ";
        }
        return str;
    }
}