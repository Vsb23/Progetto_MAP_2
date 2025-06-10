package com.packagep;

import com.packagep.data.Data;
import com.packagep.database.DbAccess;
import com.packagep.exception.ClusteringRadiusException;
import com.packagep.exception.DatabaseConnectionException;
import com.packagep.exception.EmptyDatasetException;
import com.packagep.exception.EmptySetException;
import com.packagep.exception.NoValueException;
import com.packagep.mining.QTMiner;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Map;

/**
 * <h2>Classe per la gestione dei comandi del bot Telegram.</h2>
 */
public class CommandHandlers {
    
    private final MessageHandler messageHandler;
    private final Map<Long, TelegramBot.UserState> userStates;
    private final Map<Long, QTMiner> qtMinerInstances;
    private final Map<Long, Data> dataInstances;
    private final Map<Long, String> tableNames;
    private final Map<Long, Double> radiusValues;

    public CommandHandlers(MessageHandler messageHandler, 
                          Map<Long, TelegramBot.UserState> userStates,
                          Map<Long, QTMiner> qtMinerInstances,
                          Map<Long, Data> dataInstances,
                          Map<Long, String> tableNames,
                          Map<Long, Double> radiusValues) {
        this.messageHandler = messageHandler;
        this.userStates = userStates;
        this.qtMinerInstances = qtMinerInstances;
        this.dataInstances = dataInstances;
        this.tableNames = tableNames;
        this.radiusValues = radiusValues;
    }

    /**
     * <h4>Gestisce lo stato iniziale dell'utente.</h4>
     */
    public void handleStartState(long chatId, String text) throws TelegramApiException {
        switch (text) {
            case "Mostra tabelle":
                showTables(chatId);
                break;
            case "Carica tabella":
                userStates.put(chatId, TelegramBot.UserState.WAITING_TABLE_NAME);
                messageHandler.sendTextMessage(chatId, "Inserisci il nome della tabella da caricare:");
                break;
            case "Esegui algoritmo QT":
                if (!dataInstances.containsKey(chatId) || dataInstances.get(chatId) == null) {
                    messageHandler.sendTextMessage(chatId, "Prima devi caricare una tabella.");
                    break;
                }
                userStates.put(chatId, TelegramBot.UserState.WAITING_RADIUS);
                messageHandler.sendTextMessage(chatId, "Inserisci il valore del raggio per l'algoritmo QT:");
                break;
            case "Salva risultati":
                if (!qtMinerInstances.containsKey(chatId) || qtMinerInstances.get(chatId) == null) {
                    messageHandler.sendTextMessage(chatId, "Prima devi eseguire l'algoritmo QT.");
                    break;
                }
                saveResults(chatId);
                break;
            case "Carica risultati":
                userStates.put(chatId, TelegramBot.UserState.WAITING_FILE_NAME);
                messageHandler.sendTextMessage(chatId, "Inserisci il nome della tabella dei risultati da caricare:");
                break;
            default:
                messageHandler.sendTextMessage(chatId, "Comando non riconosciuto. Usa uno dei bottoni del menu principale.");
                messageHandler.sendMainMenu(chatId);
        }
    }

    /**
     * <h4>Gestisce l'input del nome della tabella dall'utente.</h4>
     */
    public void handleTableNameInput(long chatId, String tableName) throws TelegramApiException {
        try {
            Data data = new Data(tableName);
            dataInstances.put(chatId, data);
            tableNames.put(chatId, tableName);
            
            messageHandler.sendTextMessage(chatId, "Tabella " + tableName + " caricata con successo!");
            userStates.put(chatId, TelegramBot.UserState.START);
            messageHandler.sendMainMenu(chatId);
        } catch (EmptySetException e) {
            messageHandler.sendTextMessage(chatId, "La tabella è vuota. Prova con un'altra tabella.");
        } catch (SQLException e) {
            messageHandler.sendTextMessage(chatId, "Tabella non trovata. Verifica il nome e riprova.");
        } catch (DatabaseConnectionException | NoValueException e) {
            messageHandler.sendTextMessage(chatId, "Errore: " + e.getMessage());
        }
    }

    /**
     * <h4>Gestisce l'input del valore del raggio dall'utente.</h4>
     */
    public void handleRadiusInput(long chatId, String radiusText) throws TelegramApiException {
        try {
            double radius = Double.parseDouble(radiusText);
            radiusValues.put(chatId, radius);
            
            QTMiner qtMiner = new QTMiner(radius);
            qtMinerInstances.put(chatId, qtMiner);
            
            executeQTAlgorithm(chatId);
        } catch (NumberFormatException e) {
            messageHandler.sendTextMessage(chatId, "Inserisci un valore numerico valido per il raggio.");
        }
    }

    /**
     * <h4>Gestisce l'input del nome del file dall'utente per il caricamento dei risultati.</h4>
     */
    public void handleFileNameInput(long chatId, String fileName) throws TelegramApiException {
        tableNames.put(chatId, fileName);
        userStates.put(chatId, TelegramBot.UserState.WAITING_RADIUS_FOR_LOAD);
        messageHandler.sendTextMessage(chatId, "Ora inserisci il valore del raggio utilizzato:");
    }

    /**
     * <h4>Gestisce l'input del valore del raggio per il caricamento dei risultati.</h4>
     */
    public void handleRadiusForLoadInput(long chatId, String radiusText) throws TelegramApiException {
        try {
            double radius = Double.parseDouble(radiusText);
            radiusValues.put(chatId, radius);
            loadResults(chatId);
        } catch (NumberFormatException e) {
            messageHandler.sendTextMessage(chatId, "Inserisci un valore numerico valido per il raggio.");
        }
    }

    /**
     * <h4>Mostra le tabelle disponibili nel database.</h4>
     */
    private void showTables(long chatId) throws TelegramApiException {
        final DbAccess db = new DbAccess();
        final LinkedList<String> tables = new LinkedList<>();
        try {
            db.initConnection();
            final Connection c = db.getConnection();
            final Statement s = c.createStatement();
            final ResultSet r = s.executeQuery("show tables");
            
            StringBuilder message = new StringBuilder("Tabelle disponibili:\n");
            while (r.next()) {
                String tableName = r.getString(1);
                tables.add(tableName);
                message.append("- ").append(tableName).append("\n");
            }
            
            if (tables.isEmpty()) {
                messageHandler.sendTextMessage(chatId, "Non ci sono tabelle disponibili nel database.");
            } else {
                messageHandler.sendTextMessage(chatId, message.toString());
            }
            
            s.close();
        } catch (final DatabaseConnectionException | SQLException e) {
            messageHandler.sendTextMessage(chatId, "Errore durante il recupero delle tabelle: " + e.getMessage());
        } finally {
            db.closeConnection();
        }
    }

    /**
     * <h4>Esegue l'algoritmo QT sui dati caricati.</h4>
     */
    private void executeQTAlgorithm(long chatId) throws TelegramApiException {
        QTMiner qtMiner = qtMinerInstances.get(chatId);
        Data data = dataInstances.get(chatId);
        
        try {
            int numClusters = qtMiner.compute(data);
            String result = qtMiner.toString(data);
            
            messageHandler.sendLongMessage(chatId, result);
            messageHandler.sendTextMessage(chatId, "Algoritmo QT eseguito con successo! Numero di cluster: " + numClusters);
            userStates.put(chatId, TelegramBot.UserState.START);
            messageHandler.sendMainMenu(chatId);
        } catch (EmptyDatasetException e) {
            messageHandler.sendTextMessage(chatId, "Il dataset è vuoto.");
        } catch (ClusteringRadiusException e) {
            messageHandler.sendTextMessage(chatId, "Il raggio specificato genera un solo cluster. Prova con un valore diverso.");
        }
    }

    /**
     * <h4>Salva i risultati dell'algoritmo QT in un file.</h4>
     */
    private void saveResults(long chatId) throws TelegramApiException {
        QTMiner qtMiner = qtMinerInstances.get(chatId);
        String tableName = tableNames.get(chatId);
        double radius = radiusValues.get(chatId);
        
        try {
            String fileName = tableName + "_" + radius + ".dmp";
            qtMiner.salva(fileName);
            messageHandler.sendTextMessage(chatId, "Risultati salvati con successo nel file: " + fileName);
            userStates.put(chatId, TelegramBot.UserState.START);
            messageHandler.sendMainMenu(chatId);
        } catch (IOException e) {
            messageHandler.sendTextMessage(chatId, "Errore durante il salvataggio: " + e.getMessage());
        }
    }

    /**
     * <h4>Carica i risultati dell'algoritmo QT da un file.</h4>
     */
    private void loadResults(long chatId) throws TelegramApiException {
        String tableName = tableNames.get(chatId);
        double radius = radiusValues.get(chatId);
        String fileName = "./results/" + tableName + "_" + radius + ".dmp";
        
        try {
            QTMiner qtMiner = new QTMiner(fileName);
            qtMinerInstances.put(chatId, qtMiner);
            
            String result = qtMiner.toString();
            messageHandler.sendLongMessage(chatId, result);
            messageHandler.sendTextMessage(chatId, "Risultati caricati con successo dal file: " + fileName);
            userStates.put(chatId, TelegramBot.UserState.START);
            messageHandler.sendMainMenu(chatId);
        } catch (FileNotFoundException e) {
            messageHandler.sendTextMessage(chatId, "File non trovato: " + fileName);
        } catch (IOException | ClassNotFoundException e) {
            messageHandler.sendTextMessage(chatId, "Errore durante il caricamento: " + e.getMessage());
        }
    }
}