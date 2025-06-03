package data;
import java.io.Serializable;

/**
 * Modella una colonna della tabella del Database.
 * Questa classe astratta rappresenta un attributo generico con un nome
 * e un identificativo numerico, implementando l'interfaccia Serializable
 * per supportare la persistenza degli oggetti.
 */
abstract class Attribute implements Serializable {
    
    /**
     * Nome dell'attributo
     */
    private String name;
    
    /**
     * Identificativo numerico dell'attributo
     */
    private int index;
    
    /**
     * Inizializza i valori dei membri name e index.
     *
     * @param name il nome dell'attributo
     * @param index l'identificativo numerico dell'attributo
     */
    Attribute(final String name, final int index) { // la classe è astratta pertanto non può essere public
        this.name = name;
        this.index = index;
    }
    
    /**
     * Restituisce il nome dell'attributo.
     * 
     * @return il nome dell'attributo
     */
    String getName() {
        return name; // oppure this.name
    }
    
    /**
     * Restituisce l'identificativo numerico dell'attributo.
     *
     * @return l'identificativo numerico dell'attributo
     */
    int getIndex() {
        return index;
    }
    
    /**
     * Restituisce una rappresentazione testuale dell'attributo.
     * 
     * @return il nome dell'attributo come stringa
     */
    @Override
    public String toString() {
        return name;
    }
}