package TelegramBot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Classe per la gestione dei messaggi del bot Telegram
 */
public class MessageHandler {
    
    private Bot bot;
    
    /**
     * Costruttore che inizializza l'handler con un riferimento al bot
     * @param bot Il bot Telegram
     */
    public MessageHandler(Bot bot) {
        this.bot = bot;
    }
    
    /**
     * Invia un messaggio di testo a un utente specifico
     * @param chatId L'ID della chat a cui inviare il messaggio
     * @param text Il testo del messaggio da inviare
     * @return Il messaggio inviato
     * @throws TelegramApiException Se si verifica un errore durante l'invio
     */
    public Message sendTextMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        return bot.execute(message);
    }
    
    /**
     * Formatta i risultati del clustering per la visualizzazione
     * @param clusteringResult Il risultato del clustering
     * @return Il testo formattato per la visualizzazione
     */
    public String formatClusteringResult(String clusteringResult) {
        StringBuilder formatted = new StringBuilder();
        formatted.append("Risultato del clustering:\n\n");
        
        // Rimuove eventuali caratteri di escape e formatta il risultato
        String cleanResult = clusteringResult.replaceAll("\\\\n", "\n");
        
        // Aggiunge il risultato formattato
        formatted.append(cleanResult);
        
        return formatted.toString();
    }
    
    /**
     * Formatta i risultati di una query per la visualizzazione
     * @param queryResult Il risultato della query
     * @return Il testo formattato per la visualizzazione
     */
    public String formatQueryResult(String queryResult) {
        StringBuilder formatted = new StringBuilder();
        formatted.append("Risultato della query:\n\n");
        
        // Rimuove eventuali caratteri di escape e formatta il risultato
        String cleanResult = queryResult.replaceAll("\\\\n", "\n");
        
        // Aggiunge il risultato formattato
        formatted.append(cleanResult);
        
        return formatted.toString();
    }
    
    /**
     * Formatta un messaggio di errore per la visualizzazione
     * @param errorMessage Il messaggio di errore
     * @return Il testo formattato per la visualizzazione
     */
    public String formatErrorMessage(String errorMessage) {
        return "❌ Errore: " + errorMessage;
    }
    
    /**
     * Formatta un messaggio di successo per la visualizzazione
     * @param successMessage Il messaggio di successo
     * @return Il testo formattato per la visualizzazione
     */
    public String formatSuccessMessage(String successMessage) {
        return "✅ Successo: " + successMessage;
    }
}
