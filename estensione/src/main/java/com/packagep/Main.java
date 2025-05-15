package com.packagep;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

/**
 * <h2>La classe {@code Main} avvia l'applicazione del bot Telegram utilizzando il framework TelegramBots.</h2>
 * <p>Questa classe è responsabile della registrazione del bot con un token specificato e
 * dell'inizializzazione di un client HTTP basato su OkHttp per interagire con l'API di Telegram.
 * Il ciclo di polling viene gestito dalla classe {@link TelegramBotsLongPollingApplication}.</p>
 *
 * <p>Una volta avviata, l'applicazione rimane in esecuzione in attesa di messaggi da Telegram.</p>
 */
public class Main {
    /**
     * <h4>Metodo principale che avvia l'applicazione bot Telegram.</h4>
     * <p>Il bot è registrato tramite il token fornito e un client HTTP OkHttp. Il ciclo di polling
     * è gestito dalla classe {@link TelegramBotsLongPollingApplication} che attende i messaggi in entrata.</p>
     *
     * @param args Parametri da riga di comando (non utilizzati).
     */
    public static void main(String[] args) {
        String botToken = "5265564537:AAGQv_s2yisu32lxPiptkuXIlmj6hRLi0j8";

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new TelegramBot(new OkHttpTelegramClient(botToken)));
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
