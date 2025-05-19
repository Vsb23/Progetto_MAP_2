package data;

import java.io.Serializable;

/**
 * Classe astratta che modella una cella generica di una tabella di dati,
 * contenente un valore (continuo o discreto) associato a un attributo.
 * <p>
 * Implementa {@link Serializable} per permettere la serializzazione degli oggetti.
 * Le sottoclassi concrete devono implementare la logica specifica per il calcolo
 * delle distanze tra valori.
 * </p>
 * 
 * @see DiscreteItem
 * @see ContinuousItem
 */
abstract class Item implements Serializable {

    private Attribute attribute;
    private Object value;

    /**
     * Costruisce un Item associandolo a un attributo e al relativo valore.
     * 
     * @param attribute Attributo a cui l'item appartiene (non null)
     * @param value Valore dell'item (può essere null a seconda dell'implementazione)
     * @throws NullPointerException se attribute è null
     */
    Item(final Attribute attribute, final Object value) {
        this.attribute = attribute;
        this.value = value;
    }

    /**
     * Restituisce l'attributo a cui questo item è associato.
     * 
     * @return l'attributo dell'item
     */
    Attribute getAttribute() {
        return attribute;
    }

    /**
     * Restituisce il valore contenuto in questo item.
     * 
     * @return il valore dell'item
     */
    Object getValue() {
        return value;
    }

    /**
     * Restituisce una rappresentazione stringa del valore dell'item.
     * 
     * @return la rappresentazione stringa del valore
     */
    @Override
    public String toString() {
        return value.toString();
    }

    /**
     * Metodo astratto per calcolare la distanza tra questo item e un altro oggetto.
     * <p>
     * L'implementazione concreta dipende dal tipo di valore (discreto o continuo).
     * </p>
     * 
     * @param a Oggetto con cui calcolare la distanza
     * @return la distanza tra i due valori
     * @throws NullPointerException se a è null
     * @throws ClassCastException se a non è di un tipo compatibile
     */
    abstract double distance(Object a);
}