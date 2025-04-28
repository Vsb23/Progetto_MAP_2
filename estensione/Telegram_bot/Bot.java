package TelegramBot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import server.MultiServer;
import mining.KMeansMiner;
import data.Data;
import database.DatabaseConnectionException;
import database.DbAccess;
import database.EmptySetException;
import database.TableData;
import utility.EmptyDatasetException;

/**
 * Classe che implementa un bot Telegram per interagire con il sistema A-CLUS
 */
public class Bot extends TelegramLongPollingBot {
    
    private final String BOT_TOKEN = "inserisci_qui_il_tuo_token_bot";
    private final String BOT_USERNAME = "inserisci_qui_il_nome_utente_del_bot";
    
    private MultiServer server;
    
    /**
     * Costruttore che inizializza il bot con un riferimento al server
     * @param server Il server multithreaded che gestisce le richieste
     */
    public Bot(MultiServer server) {
        this.server = server;
    }
    
    /**
     * Restituisce il nome utente del bot
     * @return Il nome utente del bot
     */
    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }
    
    /**
     * Restituisce il token del bot
     * @return Il token del bot
     */
    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
    
    /**
     * Gestisce gli aggiornamenti ricevuti dal bot
     * @param update L'aggiornamento ricevuto
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            
            // Prepara un messaggio di risposta
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            
            try {
                // Gestisce i diversi comandi
                if (messageText.equals("/start")) {
                    handleStartCommand(message);
                } else if (messageText.equals("/help")) {
                    handleHelpCommand(message);
                } else if (messageText.startsWith("/learn")) {
                    handleLearnCommand(message, messageText);
                } else if (messageText.startsWith("/cluster")) {
                    handleClusterCommand(message, messageText);
                } else if (messageText.startsWith("/storeTableFromDB")) {
                    handleStoreTableCommand(message, messageText);
                } else if (messageText.startsWith("/executeQuery")) {
                    handleExecuteQueryCommand(message, messageText);
                } else {
                    message.setText("Comando non riconosciuto. Digita /help per visualizzare i comandi disponibili.");
                }
            } catch (Exception e) {
                message.setText("Si è verificato un errore durante l'elaborazione della richiesta: " + e.getMessage());
            }
            
            try {
                // Invia il messaggio di risposta
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Gestisce il comando /start
     * @param message Il messaggio da inviare
     */
    private void handleStartCommand(SendMessage message) {
        message.setText("Benvenuto nel bot A-CLUS!\n\n" +
                "Questo bot ti permette di interagire con il sistema di clustering A-CLUS.\n\n" +
                "Usa /help per visualizzare i comandi disponibili.");
        
        // Crea una tastiera personalizzata con i comandi principali
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add("/help");
        row1.add("/learn");
        keyboard.add(row1);
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add("/cluster");
        row2.add("/storeTableFromDB");
        keyboard.add(row2);
        
        KeyboardRow row3 = new KeyboardRow();
        row3.add("/executeQuery");
        keyboard.add(row3);
        
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        message.setReplyMarkup(keyboardMarkup);
    }
    
    /**
     * Gestisce il comando /help
     * @param message Il messaggio da inviare
     */
    private void handleHelpCommand(SendMessage message) {
        message.setText("Comandi disponibili:\n\n" +
                "/help - Mostra questo messaggio di aiuto\n" +
                "/learn <table> <k> - Avvia l'apprendimento dal database\n" +
                "/cluster <table> <k> - Esegue il clustering su una tabella\n" +
                "/storeTableFromDB <table> - Memorizza una tabella dal database\n" +
                "/executeQuery <query> - Esegue una query SQL sul database\n\n" +
                "Esempio:\n" +
                "/learn playtennis 3 - Apprende i cluster dalla tabella 'playtennis' con k=3\n" +
                "/cluster playtennis 3 - Esegue il clustering sulla tabella 'playtennis' con k=3\n" +
                "/storeTableFromDB playtennis - Memorizza la tabella 'playtennis'\n" +
                "/executeQuery SELECT * FROM playtennis - Esegue la query SQL");
    }
    
    /**
     * Gestisce il comando /learn
     * @param message Il messaggio da inviare
     * @param messageText Il testo del messaggio ricevuto
     * @throws Exception Se si verifica un errore durante l'elaborazione
     */
    private void handleLearnCommand(SendMessage message, String messageText) throws Exception {
        // Estrae i parametri dal comando
        String[] parts = messageText.split(" ");
        if (parts.length != 3) {
            message.setText("Formato del comando errato. Uso corretto: /learn <table> <k>");
            return;
        }
        
        String table = parts[1];
        int k = Integer.parseInt(parts[2]);
        
        try {
            // Utilizza il server per eseguire l'operazione di apprendimento
            String result = server.learningFromDBTable(table, k);
            message.setText("Apprendimento completato con successo!\n\n" + result);
        } catch (Exception e) {
            message.setText("Errore durante l'apprendimento: " + e.getMessage());
        }
    }
    
    /**
     * Gestisce il comando /cluster
     * @param message Il messaggio da inviare
     * @param messageText Il testo del messaggio ricevuto
     * @throws Exception Se si verifica un errore durante l'elaborazione
     */
    private void handleClusterCommand(SendMessage message, String messageText) throws Exception {
        // Estrae i parametri dal comando
        String[] parts = messageText.split(" ");
        if (parts.length != 3) {
            message.setText("Formato del comando errato. Uso corretto: /cluster <table> <k>");
            return;
        }
        
        String table = parts[1];
        int k = Integer.parseInt(parts[2]);
        
        try {
            // Utilizza il server per eseguire l'operazione di clustering
            String result = server.clusteringFromDBTable(table, k);
            message.setText("Clustering completato con successo!\n\n" + result);
        } catch (Exception e) {
            message.setText("Errore durante il clustering: " + e.getMessage());
        }
    }
    
    /**
     * Gestisce il comando /storeTableFromDB
     * @param message Il messaggio da inviare
     * @param messageText Il testo del messaggio ricevuto
     * @throws Exception Se si verifica un errore durante l'elaborazione
     */
    private void handleStoreTableCommand(SendMessage message, String messageText) throws Exception {
        // Estrae i parametri dal comando
        String[] parts = messageText.split(" ");
        if (parts.length != 2) {
            message.setText("Formato del comando errato. Uso corretto: /storeTableFromDB <table>");
            return;
        }
        
        String table = parts[1];
        
        try {
            // Utilizza il server per eseguire l'operazione di memorizzazione della tabella
            String result = server.storeTableFromDB(table);
            message.setText("Tabella memorizzata con successo!\n\n" + result);
        } catch (Exception e) {
            message.setText("Errore durante la memorizzazione della tabella: " + e.getMessage());
        }
    }
    
    /**
     * Gestisce il comando /executeQuery
     * @param message Il messaggio da inviare
     * @param messageText Il testo del messaggio ricevuto
     * @throws Exception Se si verifica un errore durante l'elaborazione
     */
    private void handleExecuteQueryCommand(SendMessage message, String messageText) throws Exception {
        // Estrae la query dal comando
        String query = messageText.substring("/executeQuery ".length());
        
        try {
            // Utilizza il server per eseguire la query
            String result = server.executeQuery(query);
            message.setText("Query eseguita con successo!\n\n" + result);
        } catch (Exception e) {
            message.setText("Errore durante l'esecuzione della query: " + e.getMessage());
        }
    }
}
