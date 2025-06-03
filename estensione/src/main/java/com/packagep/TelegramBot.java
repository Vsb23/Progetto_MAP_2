package com.packagep;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.packagep.data.Data;
import com.packagep.database.DbAccess;
import com.packagep.exception.ClusteringRadiusException;
import com.packagep.exception.DatabaseConnectionException;
import com.packagep.exception.EmptyDatasetException;
import com.packagep.exception.EmptySetException;
import com.packagep.exception.NoValueException;
import com.packagep.mining.QTMiner;

/**
 * <h2>La classe {@code QTMinerTelegramBot} implementa un bot Telegram per l'interazione con il servizio QTMiner.</h2>
 * <p>Questa classe funge da interfaccia tra l'API di Telegram e le funzionalità di mining offerte da QTMiner.
 * Gestisce i comandi degli utenti e permette di eseguire operazioni come la lettura di tabelle dal database,
 * l'esecuzione dell'algoritmo QT, il salvataggio e caricamento di risultati.</p>
 *
 * <p>Implementa una macchina a stati per gestire il flusso di interazione con l'utente, dove ogni utente
 * ha un proprio stato indipendente dagli altri.</p>
 */
public class TelegramBot implements LongPollingUpdateConsumer {
    
    /** <h4>Il client Telegram utilizzato per inviare e ricevere messaggi.</h4> */
    private final TelegramClient telegramClient;
    
    /** <h4>Mappa che associa ogni ID chat utente al suo stato corrente.</h4> */
    private final Map<Long, UserState> userStates = new HashMap<>();
    
    /** <h4>Mappa che associa ogni ID chat utente alla sua istanza di QTMiner.</h4> */
    private final Map<Long, QTMiner> qtMinerInstances = new HashMap<>();
    
    /** <h4>Mappa che associa ogni ID chat utente alla sua istanza di Data.</h4> */
    private final Map<Long, Data> dataInstances = new HashMap<>();
    
    /** <h4>Mappa che associa ogni ID chat utente al nome della tabella corrente.</h4> */
    private final Map<Long, String> tableNames = new HashMap<>();
    
    /** <h4>Mappa che associa ogni ID chat utente al valore del raggio corrente.</h4> */
    private final Map<Long, Double> radiusValues = new HashMap<>();

    /**
     * <h4>Costruttore della classe {@code QTMinerTelegramBot}.</h4>
     * <p>Inizializza il bot con un client OkHttp per la comunicazione con l'API di Telegram.</p>
     *
     * @param telegramClient Il client Telegram da utilizzare per eseguire le richieste.
     */
    public TelegramBot(OkHttpTelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
     * <h4>Metodo che gestisce una lista di aggiornamenti ricevuti da Telegram.</h4>
     * <p>Ogni aggiornamento viene elaborato in un thread separato.</p>
     *
     * @param updates La lista di aggiornamenti ricevuti dal server Telegram.
     */
    @Override
    public void consume(List<Update> updates) {
        updates.forEach(update -> {
            Thread thread = new Thread(() -> {
                try {
                    handleUpdate(update);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        });
    }

    /**
     * <h4>Gestisce l'elaborazione di un singolo aggiornamento ricevuto.</h4>
     * <p>Processa i messaggi di testo ricevuti dagli utenti e aggiorna lo stato dell'utente
     * in base al messaggio ricevuto.</p>
     *
     * @param update L'aggiornamento ricevuto da Telegram da elaborare.
     * @throws TelegramApiException Se si verifica un errore durante l'elaborazione dell'aggiornamento.
     */
    private void handleUpdate(Update update) throws TelegramApiException {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            
            System.out.println("Messaggio ricevuto da " + chatId + ": " + text);
            
            // Se l'utente non ha uno stato o invia /start, inizializziamo lo stato
            if (!userStates.containsKey(chatId) || text.equals("/start")) {
                userStates.put(chatId, UserState.START);
                // Invia il messaggio iniziale
                sendTextMessage(chatId, "Benvenuto al bot QTMiner! 🤖\n\n" +
                    "Questo bot ti permette di interagire con il sistema QTMiner per l'analisi dei dati.\n" +
                    "Puoi visualizzare tabelle, caricare dati, eseguire l'algoritmo QT e gestire i risultati.\n\n\n");
                    sendMainMenu(chatId);
                return;
            }
            
            // Gestisci il comando "Torna al menu principale"
            if (text.equals("Torna al menu principale")) {
                userStates.put(chatId, UserState.START);
                sendMainMenu(chatId);
                return;
            }
            
            // Gestisci lo stato corrente dell'utente
            UserState currentState = userStates.get(chatId);
            switch (currentState) {
                case START:
                    handleStartState(chatId, text);
                    break;
                case WAITING_TABLE_NAME:
                    handleTableNameInput(chatId, text);
                    break;
                case WAITING_RADIUS:
                    handleRadiusInput(chatId, text);
                    break;
                case WAITING_FILE_NAME:
                    handleFileNameInput(chatId, text);
                    break;
                case WAITING_RADIUS_FOR_LOAD:
                    handleRadiusForLoadInput(chatId, text);
                    break;
                default:
                    sendTextMessage(chatId, "Comando non riconosciuto. Usa /start per ricominciare.");
            }
        }
    }

    /**
     * <h4>Gestisce lo stato iniziale dell'utente.</h4>
     * <p>Processa i comandi principali dal menu principale.</p>
     *
     * @param chatId ID della chat dell'utente.
     * @param text Testo del messaggio inviato dall'utente.
     * @throws TelegramApiException Se si verifica un errore durante l'invio del messaggio.
     */
    private void handleStartState(long chatId, String text) throws TelegramApiException {
        switch (text) {
            case "Mostra tabelle":
                showTables(chatId);
                break;
            case "Carica tabella":
                userStates.put(chatId, UserState.WAITING_TABLE_NAME);
                sendTextMessage(chatId, "Inserisci il nome della tabella da caricare:");
                break;
            case "Esegui algoritmo QT":
                if (!dataInstances.containsKey(chatId) || dataInstances.get(chatId) == null) {
                    sendTextMessage(chatId, "Prima devi caricare una tabella.");
                    break;
                }
                userStates.put(chatId, UserState.WAITING_RADIUS);
                sendTextMessage(chatId, "Inserisci il valore del raggio per l'algoritmo QT:");
                break;
            case "Salva risultati":
                if (!qtMinerInstances.containsKey(chatId) || qtMinerInstances.get(chatId) == null) {
                    sendTextMessage(chatId, "Prima devi eseguire l'algoritmo QT.");
                    break;
                }
                saveResults(chatId);
                break;
            case "Carica risultati":
                userStates.put(chatId, UserState.WAITING_FILE_NAME);
                sendTextMessage(chatId, "Inserisci il nome della tabella dei risultati da caricare:");
                break;
            default:
                sendTextMessage(chatId, "Comando non riconosciuto. Usa uno dei bottoni del menu principale.");
                sendMainMenu(chatId);
        }
    }

    /**
     * <h4>Gestisce l'input del nome della tabella dall'utente.</h4>
     *
     * @param chatId ID della chat dell'utente.
     * @param tableName Nome della tabella fornito dall'utente.
     * @throws TelegramApiException Se si verifica un errore durante l'invio del messaggio.
     */
    private void handleTableNameInput(long chatId, String tableName) throws TelegramApiException {
        try {
            Data data = new Data(tableName);
            dataInstances.put(chatId, data);
            tableNames.put(chatId, tableName);
            
            sendTextMessage(chatId, "Tabella " + tableName + " caricata con successo!");
            userStates.put(chatId, UserState.START);
            sendMainMenu(chatId);
        } catch (EmptySetException e) {
            sendTextMessage(chatId, "La tabella è vuota. Prova con un'altra tabella.");
        } catch (SQLException e) {
            sendTextMessage(chatId, "Tabella non trovata. Verifica il nome e riprova.");
        } catch (DatabaseConnectionException | NoValueException e) {
            sendTextMessage(chatId, "Errore: " + e.getMessage());
        }
    }

    /**
     * <h4>Gestisce l'input del valore del raggio dall'utente.</h4>
     *
     * @param chatId ID della chat dell'utente.
     * @param radiusText Valore del raggio fornito dall'utente.
     * @throws TelegramApiException Se si verifica un errore durante l'invio del messaggio.
     */
    private void handleRadiusInput(long chatId, String radiusText) throws TelegramApiException {
        try {
            double radius = Double.parseDouble(radiusText);
            radiusValues.put(chatId, radius);
            
            QTMiner qtMiner = new QTMiner(radius);
            qtMinerInstances.put(chatId, qtMiner);
            
            executeQTAlgorithm(chatId);
        } catch (NumberFormatException e) {
            sendTextMessage(chatId, "Inserisci un valore numerico valido per il raggio.");
        }
    }

    /**
     * <h4>Gestisce l'input del nome del file dall'utente per il caricamento dei risultati.</h4>
     *
     * @param chatId ID della chat dell'utente.
     * @param fileName Nome del file fornito dall'utente.
     * @throws TelegramApiException Se si verifica un errore durante l'invio del messaggio.
     */
    private void handleFileNameInput(long chatId, String fileName) throws TelegramApiException {
        tableNames.put(chatId, fileName);
        userStates.put(chatId, UserState.WAITING_RADIUS_FOR_LOAD);
        sendTextMessage(chatId, "Ora inserisci il valore del raggio utilizzato:");
    }

    /**
     * <h4>Gestisce l'input del valore del raggio per il caricamento dei risultati.</h4>
     *
     * @param chatId ID della chat dell'utente.
     * @param radiusText Valore del raggio fornito dall'utente.
     * @throws TelegramApiException Se si verifica un errore durante l'invio del messaggio.
     */
    private void handleRadiusForLoadInput(long chatId, String radiusText) throws TelegramApiException {
        try {
            double radius = Double.parseDouble(radiusText);
            radiusValues.put(chatId, radius);
            loadResults(chatId);
        } catch (NumberFormatException e) {
            sendTextMessage(chatId, "Inserisci un valore numerico valido per il raggio.");
        }
    }

    /**
     * <h4>Mostra le tabelle disponibili nel database.</h4>
     *
     * @param chatId ID della chat dell'utente.
     * @throws TelegramApiException Se si verifica un errore durante l'invio del messaggio.
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
                sendTextMessage(chatId, "Non ci sono tabelle disponibili nel database.");
            } else {
                sendTextMessage(chatId, message.toString());
            }
            
            s.close();
        } catch (final DatabaseConnectionException | SQLException e) {
            sendTextMessage(chatId, "Errore durante il recupero delle tabelle: " + e.getMessage());
        } finally {
            db.closeConnection();
        }
    }

    /**
     * <h4>Esegue l'algoritmo QT sui dati caricati.</h4>
     *
     * @param chatId ID della chat dell'utente.
     * @throws TelegramApiException Se si verifica un errore durante l'invio del messaggio.
     */
    private void executeQTAlgorithm(long chatId) throws TelegramApiException {
        QTMiner qtMiner = qtMinerInstances.get(chatId);
        Data data = dataInstances.get(chatId);
        
        try {
            int numClusters = qtMiner.compute(data);
            String result = qtMiner.toString(data);
            
            // Dividi il messaggio se è troppo lungo
            if (result.length() > 4000) {
                int chunkSize = 4000;
                for (int i = 0; i < result.length(); i += chunkSize) {
                    String chunk = result.substring(i, Math.min(result.length(), i + chunkSize));
                    sendTextMessage(chatId, chunk);
                }
            } else {
                sendTextMessage(chatId, result);
            }
            
            sendTextMessage(chatId, "Algoritmo QT eseguito con successo! Numero di cluster: " + numClusters);
            userStates.put(chatId, UserState.START);
            sendMainMenu(chatId);
        } catch (EmptyDatasetException e) {
            sendTextMessage(chatId, "Il dataset è vuoto.");
        } catch (ClusteringRadiusException e) {
            sendTextMessage(chatId, "Il raggio specificato genera un solo cluster. Prova con un valore diverso.");
        }
    }

    /**
     * <h4>Salva i risultati dell'algoritmo QT in un file.</h4>
     *
     * @param chatId ID della chat dell'utente.
     * @throws TelegramApiException Se si verifica un errore durante l'invio del messaggio.
     */
    private void saveResults(long chatId) throws TelegramApiException {
        QTMiner qtMiner = qtMinerInstances.get(chatId);
        String tableName = tableNames.get(chatId);
        double radius = radiusValues.get(chatId);
        
        try {
            String fileName = tableName + "_" + radius + ".dmp";
            qtMiner.salva(fileName);
            sendTextMessage(chatId, "Risultati salvati con successo nel file: " + fileName);
            userStates.put(chatId, UserState.START);
            sendMainMenu(chatId);
        } catch (IOException e) {
            sendTextMessage(chatId, "Errore durante il salvataggio: " + e.getMessage());
        }
    }

    /**
     * <h4>Carica i risultati dell'algoritmo QT da un file.</h4>
     *
     * @param chatId ID della chat dell'utente.
     * @throws TelegramApiException Se si verifica un errore durante l'invio del messaggio.
     */
    private void loadResults(long chatId) throws TelegramApiException {
        String tableName = tableNames.get(chatId);
        double radius = radiusValues.get(chatId);
        String fileName = "./results/" + tableName + "_" + radius + ".dmp";
        
        try {
            QTMiner qtMiner = new QTMiner(fileName);
            qtMinerInstances.put(chatId, qtMiner);
            
            String result = qtMiner.toString();
            // Dividi il messaggio se è troppo lungo
            if (result.length() > 4000) {
                int chunkSize = 4000;
                for (int i = 0; i < result.length(); i += chunkSize) {
                    String chunk = result.substring(i, Math.min(result.length(), i + chunkSize));
                    sendTextMessage(chatId, chunk);
                }
            } else {
                sendTextMessage(chatId, result);
            }
            
            sendTextMessage(chatId, "Risultati caricati con successo dal file: " + fileName);
            userStates.put(chatId, UserState.START);
            sendMainMenu(chatId);
        } catch (FileNotFoundException e) {
            sendTextMessage(chatId, "File non trovato: " + fileName);
        } catch (IOException | ClassNotFoundException e) {
            sendTextMessage(chatId, "Errore durante il caricamento: " + e.getMessage());
        }
    }

    /**
     * <h4>Invia un messaggio di testo a un utente specifico.</h4>
     *
     * @param chatId ID della chat dell'utente.
     * @param text Testo del messaggio da inviare.
     * @throws TelegramApiException Se si verifica un errore durante l'invio del messaggio.
     */
    private void sendTextMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage(String.valueOf(chatId), "");
        message.setText(text);
        telegramClient.execute(message);
    }

    /**
     * <h4>Invia il menu principale all'utente.</h4>
     *
     * @param chatId ID della chat dell'utente.
     * @throws TelegramApiException Se si verifica un errore durante l'invio del messaggio.
     */
    private void sendMainMenu(long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage(String.valueOf(chatId), "Seleziona un'operazione:");
        
        // Crea la tastiera personalizzata
        ReplyKeyboardMarkup keyboardMarkup = ReplyKeyboardMarkup.builder()
            .resizeKeyboard(true)
            .oneTimeKeyboard(false)
            .build();
        
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Mostra tabelle");
        row1.add("Carica tabella");
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Esegui algoritmo QT");
        row2.add("Salva risultati");
        
        KeyboardRow row3 = new KeyboardRow();
        row3.add("Carica risultati");
        row3.add("Torna al menu principale");
        
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        
        telegramClient.execute(message);
    }

    /**
     * <h4>Enumerazione degli stati possibili per un utente del bot.</h4>
     */
    private enum UserState {
        START,
        WAITING_TABLE_NAME,
        WAITING_RADIUS,
        WAITING_FILE_NAME,
        WAITING_RADIUS_FOR_LOAD
    }
}