package com.packagep.exception;
/**
 * eccezione lanciata in caso di raggio troppo grande.
 */
public class ClusteringRadiusException extends Exception {
    /**
     * Costruttore dell'eccezione ClusteringRadiusException.
     * @param s messaggio di errore da visualizzare
     */
    public ClusteringRadiusException(final String s) {
        super(s);
    }
}