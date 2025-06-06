package com.packagep.exception;

/**
 * Eccezione lanciata quando l'operatore aggregato non da risultati.
 */
public class NoValueException extends Exception {
    
    /**
     * Costruisce una nuova istanza di NoValueException con il messaggio specificato.
     *
     * @param s il messaggio di dettaglio dell'eccezione
     */
    public NoValueException(final String s) {
        super(s);
    }
}