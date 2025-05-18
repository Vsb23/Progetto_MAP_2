package com.packagep;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

/**
 * <h2>La classe {@code Main} avvia l'applicazione del bot Telegram QTMiner.</h2>
 * <p>Questa classe è responsabile della registrazione del bot con un token specificato e
 * dell'inizializzazione di un client HTTP basato su OkHttp per interagire con l'API di Telegram.
 * Il ciclo di polling viene gestito dalla classe {@link TelegramBotsLongPollingApplication}.</p>
 *
 * <p>Una volta avviata, l'applicazione rimane in esecuzione in attesa di messaggi da Telegram
 * per gestire le operazioni di QTMiner attraverso l'interfaccia del bot.</p>
 */
public class Main {
    /**
     * <h4>Metodo principale che avvia l'applicazione del bot Telegram QTMiner.</h4>
     * <p>Il bot è registrato tramite il token fornito e utilizza un client HTTP OkHttp.
     * Il ciclo di polling è gestito dalla classe {@link TelegramBotsLongPollingApplication}
     * che attende i messaggi in entrata dagli utenti di Telegram.</p>
     *
     * @param args Parametri da riga di comando (non utilizzati).
     */
    public static void main(String[] args) {
        // Il token del bot Telegram. È consigliabile spostare questo in un file di configurazione
        // o variabile d'ambiente per ragioni di sicurezza.
        String botToken = "8043499891:AAE13VTfE6fiK2voZPjVbahprppW0iR67Us";

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            // Crea un'istanza del client Telegram basato su OkHttp
            OkHttpTelegramClient telegramClient = new OkHttpTelegramClient(botToken);
            
            // Crea un'istanza del bot QTMiner e registralo nell'applicazione
            TelegramBot qtMinerBot = new TelegramBot(telegramClient);
            botsApplication.registerBot(botToken, qtMinerBot);
            
            System.out.println("Bot QTMiner avviato e in attesa di messaggi...");
            
            // Mantiene l'applicazione in esecuzione fino alla chiusura
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Errore durante l'avvio del bot QTMiner: " + e.getMessage());
            e.printStackTrace();
        }
    }
}