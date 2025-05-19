package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import exception.DatabaseConnectionException;

/**
 * Gestisce la connessione a un database MySQL.
 * <p>
 * La classe si occupa di:
 * <ul>
 *   <li>Caricare il driver JDBC per MySQL</li>
 *   <li>Stabilire una connessione al database</li>
 *   <li>Fornire accesso alla connessione</li>
 *   <li>Chiudere la connessione</li>
 * </ul>
 * Tutti i parametri di connessione sono definiti come costanti della classe.
 */
public class DbAccess {

    /**
     * Nome della classe del driver JDBC per MySQL (versione Connector/J).
     * Necessario aggiungere il JAR del driver al classpath.
     */
    private static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver"; // org.gjt.mm.mysql.Driver";
    
    /**
     * Protocollo JDBC per il DBMS MySQL.
     */
    private static final String DBMS = "jdbc:mysql";
    
    /**
     * Indirizzo del server database (localhost per installazioni locali).
     */
    private static final String SERVER = "localhost";
    
    /**
     * Nome del database a cui connettersi.
     */
    private static final String DATABASE = "MapDB";
    
    /**
     * Porta TCP su cui MySQL è in ascolto (default: 3306).
     */
    private static final String PORT = "3306";
    
    /**
     * Username per l'autenticazione al database.
     */
    private static final String USER_ID = "MapUser";
    
    /**
     * Password per l'autenticazione al database.
     */
    private static final String PASSWORD = "map";
    
    /**
     * Oggetto Connection che gestisce la sessione con il database.
     */
    private Connection conn;

    /**
     * Inizializza la connessione al database.
     * <p>
     * Esegue due operazioni principali:
     * <ol>
     *   <li>Carica il driver JDBC specificato in DRIVER_CLASS_NAME</li>
     *   <li>Stabilisce una connessione usando i parametri configurati</li>
     * </ol>
     *
     * @throws DatabaseConnectionException se si verificano errori durante:
     *         - Il caricamento del driver
     *         - La connessione al database
     *         - L'autenticazione
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
     * Restituisce l'oggetto Connection attivo.
     *
     * @return L'oggetto Connection corrente
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Chiude la connessione al database.
     * <p>
     * Se la connessione è già chiusa o non esiste,
     * il metodo termina silenziosamente.
     */
    public void closeConnection() {
        try {
            conn.close();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}