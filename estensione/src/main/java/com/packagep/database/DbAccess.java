package com.packagep.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.packagep.exception.DatabaseConnectionException;

/**
 * Gestisce la connessione al database MySQL.
 * Questa classe fornisce i metodi per stabilire, mantenere e chiudere
 * la connessione al database utilizzando i parametri predefiniti.
 */
public class DbAccess {
    
    /**
     * Nome del driver JDBC per MySQL.
     * (Per utilizzare questo Driver scaricare e aggiungere al classpath il
     * connettore mysql connector).
     */
    private static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    
    /**
     * Protocollo JDBC per MySQL.
     */
    private static final String DBMS = "jdbc:mysql";
    
    /**
     * Contiene l'identificativo del server su cui risiede la base di dati
     * (per esempio localhost).
     */
    private static final String SERVER = "localhost";
    
    /**
     * Contiene il nome della base di dati.
     */
    private static final String DATABASE = "MapDB";
    
    /**
     * La porta su cui il DBMS MySQL accetta le connessioni.
     */
    private static final String PORT = "3306";
    
    /**
     * Contiene il nome dell'utente per l'accesso alla base di dati.
     */
    private static final String USER_ID = "MapUser";
    
    /**
     * Contiene la password di autenticazione per l'utente identificato da USER_ID.
     */
    private static final String PASSWORD = "map";
    
    /**
     * Gestisce una connessione al database.
     */
    private Connection conn;
    
    /**
     * Costruttore di default per DbAccess.
     * Inizializza un'istanza della classe senza stabilire la connessione.
     */
    public DbAccess() {
        // Il costruttore di default è sufficiente
    }
    
    /**
     * Impartisce al class loader l'ordine di caricare il driver MySQL e inizializza
     * la connessione riferita da conn. Il metodo solleva e propaga una eccezione di
     * tipo DatabaseConnectionException in caso di fallimento nella connessione al
     * database.
     *
     * @throws DatabaseConnectionException problemi di connessione al database
     */
    public void initConnection() throws DatabaseConnectionException {
        try {
            Class.forName(DRIVER_CLASS_NAME).newInstance();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(DBMS + "://" + SERVER + ":" + PORT + "/" + DATABASE
                    + "?user=" + USER_ID + "&password=" + PASSWORD + "&serverTimezone=UTC");
        } catch (final SQLException e) {
            throw new DatabaseConnectionException("connessione fallita");
        }
    }
    
    /**
     * Restituisce la connessione attiva al database.
     *
     * @return oggetto Connection rappresentante la connessione al database
     */
    public Connection getConnection() {
        return conn;
    }
    
    /**
     * Chiude la connessione al database.
     * Se si verifica un errore durante la chiusura, stampa lo stack trace
     * dell'eccezione ma non la rilancia.
     */
    public void closeConnection() {
        try {
            conn.close();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}