package data;

/**
 * Modella una colonna di attributi continui, con minimo e massimo che un
 * singolo attributo può assumere.
 * Questa classe estende Attribute per rappresentare attributi numerici continui
 * che possono essere normalizzati in un intervallo specifico.
 */
class ContinuousAttribute extends Attribute {
    
    /**
     * Valore massimo che l'attributo può assumere
     */
    private double max;
    
    /**
     * Valore minimo che l'attributo può assumere
     */
    private double min;
    
    /**
     * Precisione utilizzata per i confronti tra valori double
     */
    private static final double accuracy = 0.01;
    
    /**
     * Inizializza i valori dei membri name, index, min e max.
     *
     * @param name il nome dell'attributo
     * @param index l'identificativo numerico dell'attributo
     * @param min il valore minimo che l'attributo può assumere
     * @param max il valore massimo che l'attributo può assumere
     */
    ContinuousAttribute(final String name, final int index, final double min, final double max) {
        super(name, index);
        this.max = max;
        this.min = min;
    }
    
    /**
     * Normalizza il valore passato come parametro nell'intervallo [0,1].
     * La normalizzazione viene effettuata utilizzando la formula: (v - min) / (max - min)
     *
     * @param v il valore da normalizzare
     * @return il valore normalizzato nell'intervallo [0,1]
     */
    double getScaledValue(final double v) {
        return (v - min) / (max - min);
    }
    
    /**
     * Confronta questo attributo continuo con un altro oggetto per verificarne l'uguaglianza.
     * Due attributi continui sono considerati uguali se hanno lo stesso nome, indice,
     * e valori min/max che differiscono meno della precisione specificata.
     * 
     * @param o l'oggetto da confrontare con questo attributo
     * @return true se gli oggetti sono uguali, false altrimenti
     */
    @Override
    public boolean equals(final Object o) {
        ContinuousAttribute ca = (ContinuousAttribute) o;
        return (ca.min - min < accuracy) && (ca.max - max < accuracy) && ca.getName().equals(getName())
                && ca.getIndex() == getIndex();
    }
}