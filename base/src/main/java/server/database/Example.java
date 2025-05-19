package database;

import java.util.ArrayList;
import java.util.List;

/**
 * Modella una tupla (riga) di una tabella di database come una sequenza ordinata di oggetti.
 * <p>
 * La classe rappresenta una singola riga di risultati da una query SQL, dove ogni colonna
 * è memorizzata come un Object nella lista interna. Implementa {@link Comparable} per
 * permettere l'ordinamento tra istanze.
 * </p>
 */
public class Example implements Comparable<Example> {

    private List<Object> example = new ArrayList<Object>();

    /**
     * Aggiunge un elemento alla tupla.
     * 
     * @param o Oggetto da aggiungere alla tupla (può essere null)
     */
    public void add(final Object o) {
        example.add(o);
    }

    /**
     * Recupera un elemento della tupla per indice.
     * 
     * @param i Indice dell'elemento da recuperare (0-based)
     * @return L'oggetto nella posizione specificata
     * @throws IndexOutOfBoundsException se l'indice è fuori dal range valido
     */
    public Object get(final int i) {
        return example.get(i);
    }

    /**
     * Confronta questa tupla con un'altra tupla in ordine lessicografico.
     * <p>
     * Il confronto avviene elemento per elemento fino a trovare una differenza.
     * Tutti gli elementi devono implementare {@link Comparable}.
     * </p>
     * 
     * @param ex Tupla da confrontare
     * @return Un valore negativo, zero o positivo se questa tupla è minore, uguale o maggiore
     * @throws ClassCastException se gli elementi non sono mutualmente confrontabili
     * @throws NullPointerException se ex è null
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
     * Verifica l'uguaglianza tra questa tupla e un'altra tupla.
     * 
     * @param o Oggetto da confrontare
     * @return true se le tuple contengono gli stessi elementi nello stesso ordine
     * @throws NullPointerException se o è null
     * @throws ClassCastException se o non è un'istanza di Example
     */
    @Override
    public boolean equals(final Object o) {
        return example.equals(((Example) o).example);
    }

    /**
     * Restituisce una rappresentazione stringa della tupla.
     * <p>
     * Gli elementi sono concatenati separati da spazi nell'ordine in cui sono stati aggiunti.
     * </p>
     * 
     * @return Stringa rappresentante la tupla
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