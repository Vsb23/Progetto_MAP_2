package com.packagep;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.packagep.data.Data;
import com.packagep.mining.QTMiner;
import com.packagep.MessageHandler;
import com.packagep.CommandHandlers;

/**
 * <h2>La classe {@code TelegramBot} implementa un bot Telegram per l'interazione con il servizio QTMiner.</h2>
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
    
    /** <h4>Gestore dei messaggi e menu.</h4> */
    private final MessageHandler messageHandler;
    
    /** <h4>Gestore dei comandi.</h4> */
    private final CommandHandlers commandHandlers;
    
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
     * <h4>Costruttore della classe {@code TelegramBot}.</h4>
     * <p>Inizializza il bot con un client OkHttp per la comunicazione con l'API di Telegram.</p>
     *
     * @param telegramClient Il client Telegram da utilizzare per eseguire le richieste.
     */
    public TelegramBot(OkHttpTelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.messageHandler = new MessageHandler(telegramClient);
        this.commandHandlers = new CommandHandlers(messageHandler, userStates, qtMinerInstances, 
                                                  dataInstances, tableNames, radiusValues);
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
                messageHandler.sendTextMessage(chatId, "Benvenuto al bot QTMiner! 🤖\n\n" +
                    "Questo bot ti permette di interagire con il sistema QTMiner per l'analisi dei dati.\n" +
                    "Puoi visualizzare tabelle, caricare dati, eseguire l'algoritmo QT e gestire i risultati.\n\n\n");
                messageHandler.sendMainMenu(chatId);
                return;
            }
            
            // Gestisci il comando "Torna al menu principale"
            if (text.equals("Torna al menu principale")) {
                userStates.put(chatId, UserState.START);
                messageHandler.sendMainMenu(chatId);
                return;
            }
            
            // Gestisci lo stato corrente dell'utente
            UserState currentState = userStates.get(chatId);
            switch (currentState) {
                case START:
                    commandHandlers.handleStartState(chatId, text);
                    break;
                case WAITING_TABLE_NAME:
                    commandHandlers.handleTableNameInput(chatId, text);
                    break;
                case WAITING_RADIUS:
                    commandHandlers.handleRadiusInput(chatId, text);
                    break;
                case WAITING_FILE_NAME:
                    commandHandlers.handleFileNameInput(chatId, text);
                    break;
                case WAITING_RADIUS_FOR_LOAD:
                    commandHandlers.handleRadiusForLoadInput(chatId, text);
                    break;
                default:
                    messageHandler.sendTextMessage(chatId, "Comando non riconosciuto. Usa /start per ricominciare.");
            }
        }
    }

    /**
     * <h4>Enumerazione degli stati possibili per un utente del bot.</h4>
     */
    public enum UserState {
        START,
        WAITING_TABLE_NAME,
        WAITING_RADIUS,
        WAITING_FILE_NAME,
        WAITING_RADIUS_FOR_LOAD
    }
}