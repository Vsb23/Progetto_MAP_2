package com.packagep.exception;

/**
 * Eccezione lanciata in caso di dataset vuoto.
 * Questa eccezione viene sollevata quando si tenta di eseguire operazioni
 * su un dataset che non contiene alcun dato o esempio.
 */
public class EmptyDatasetException extends Exception {
    
    /**
     * Costruttore dell'eccezione EmptyDatasetException.
     * 
     * @param s messaggio di errore che descrive il motivo per cui il dataset è vuoto
     */
    public EmptyDatasetException(final String s) {
        super(s);
    }
}