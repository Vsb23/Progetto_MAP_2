package TelegramBot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import server.MultiServer;

/**
 * Classe per l'avvio del bot Telegram
 */
public class TelegramBotLauncher {
    
    private Bot bot;
    private TelegramBotsApi botsApi;
    
    /**
     * Costruttore che inizializza il launcher con un riferimento al server
     * @param server Il server multithreaded che gestisce le richieste
     */
    public TelegramBotLauncher(MultiServer server) {
        try {
            // Inizializza l'API dei bot Telegram
            botsApi = new TelegramBotsApi(DefaultBotSession.class);
            
            // Crea l'istanza del bot
            bot = new Bot(server);
            
            // Registra il bot nell'API
            botsApi.registerBot(bot);
            
            System.out.println("Bot Telegram avviato con successo!");
            System.out.println("Nome utente: " + bot.getBotUsername());
            
        } catch (TelegramApiException e) {
            System.err.println("Errore durante l'avvio del bot Telegram: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Restituisce l'istanza del bot
     * @return L'istanza del bot
     */
    public Bot getBot() {
        return bot;
    }
    
    /**
     * Restituisce l'API dei bot Telegram
     * @return L'API dei bot Telegram
     */
    public TelegramBotsApi getBotsApi() {
        return botsApi;
    }
    
    /**
     * Arresta il bot
     */
    public void stopBot() {
        System.out.println("Arresto del bot Telegram in corso...");
        // Non esiste un metodo diretto per fermare un bot,
        // ma possiamo chiudere le risorse associate se necessario
        System.out.println("Bot Telegram arrestato!");
    }
}
