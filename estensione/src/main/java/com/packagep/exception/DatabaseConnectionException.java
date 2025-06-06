package com.packagep.exception;

/**
 * Eccezione lanciata in caso di fallimento della connessione al database.
 * Questa eccezione viene sollevata quando si verificano problemi durante
 * il tentativo di stabilire o mantenere una connessione con il database.
 */
public class DatabaseConnectionException extends Exception {
    
    /**
     * Costruttore dell'eccezione DatabaseConnectionException.
     * 
     * @param s messaggio di errore che descrive la causa del fallimento della connessione
     */
    public DatabaseConnectionException(final String s) {
        super(s);
    }
}