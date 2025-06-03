package data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Modella una colonna di attributi discreti, con un insieme predefinito di valori possibili.
 * I valori sono memorizzati in un {@link TreeSet}, che garantisce unicità e ordinamento naturale.
 * Implementa {@link Iterable} per permettere l'iterazione sui valori in ordine crescente.
 */
class DiscreteAttribute extends Attribute implements Iterable<String> {

    private TreeSet<String> values = new TreeSet<String>();

    /**
     * Costruisce un DiscreteAttribute inizializzando nome, indice e valori possibili.
     * I valori forniti vengono convertiti in un {@link TreeSet}, rimuovendo duplicati e ordinandoli.
     *
     * @param name   Nome simbolico dell'attributo
     * @param index  Identificativo numerico dell'attributo
     * @param values Array contenente i valori ammissibili per l'attributo
     */
    DiscreteAttribute(final String name, final int index, final String[] values) {
        super(name, index);
        this.values.addAll(Arrays.asList(values));
    }

    /**
     * Restituisce un iteratore per scorrere i valori dell'attributo in ordine naturale.
     *
     * @return Iteratore sui valori ordinati
     */
    @Override
    public Iterator<String> iterator() {
        return values.iterator();
    }

    /**
     * Confronta due DiscreteAttribute verificando l'uguaglianza di nome, indice e valori.
     *
     * @param o Oggetto da confrontare
     * @return true se gli attributi hanno stessa identità e valori, false altrimenti
     * @throws NullPointerException Se l'oggetto passato è null
     * @throws ClassCastException Se l'oggetto non è un DiscreteAttribute
     */
    @Override
    public boolean equals(final Object o) {
        DiscreteAttribute da = (DiscreteAttribute) o;
        return da.getName().equals(getName()) && 
               da.getIndex() == getIndex() && 
               da.values.equals(values);
    }

    /**
     * Calcola il numero di valori distinti ammessi per l'attributo.
     *
     * @return Numero di elementi unici nell'insieme dei valori
     */
    int getNumberOfDistinctValues() {
        return values.size();
    }
}