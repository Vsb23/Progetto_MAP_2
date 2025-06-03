package data;

/**
 * Modella una cella di una tabella di dati contenente un valore discreto.
 * Estende la classe {@link Item} specializzandola per gestire valori discreti,
 * con una metrica di distanza che restituisce 0 per valori uguali e 1 per valori diversi.
 */
class DiscreteItem extends Item {

    /**
     * Costruisce un DiscreteItem associandolo a un attributo discreto e al relativo valore.
     * 
     * @param attribute Attributo discreto a cui l'item appartiene (non null)
     * @param value Valore discreto dell'item (deve essere tra quelli ammessi dall'attributo)
     * @throws NullPointerException se attribute o value sono null
     */
    DiscreteItem(final DiscreteAttribute attribute, final String value) {
        super(attribute, value);
    }

    /**
     * Calcola la distanza tra questo item e un altro item discreto.
     * La distanza è 0 se i valori sono uguali, 1 se sono diversi.
     * 
     * @param a Oggetto con cui calcolare la distanza (deve essere un Item)
     * @return 0.0 se i valori coincidono, 1.0 altrimenti
     * @throws NullPointerException se a è null
     * @throws ClassCastException se a non è un'istanza di Item
     */
    @Override
    double distance(final Object a) {
        if (getValue().equals(((Item) a).getValue())) {
            return 0.0;
        } else {
            return 1.0;
        }
    }

    /**
     * Verifica l'uguaglianza tra due DiscreteItem confrontando attributo e valore.
     * 
     * @param o Oggetto da confrontare
     * @return true se hanno lo stesso attributo e valore, false altrimenti
     * @throws NullPointerException se o è null
     * @throws ClassCastException se o non è un DiscreteItem
     */
    @Override
    public boolean equals(final Object o) {
        return ((DiscreteItem) o).getAttribute().equals(getAttribute())
                && ((DiscreteItem) o).getValue().equals(getValue());
    }
}