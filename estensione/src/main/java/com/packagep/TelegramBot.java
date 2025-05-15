package com.packagep;


import java.util.List;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * <h2>La classe {@code TelegramBot} implementa un bot Telegram utilizzando il framework TelegramBots.</h2>
 * <p>Essa gestisce la ricezione e l'elaborazione degli aggiornamenti tramite il metodo di polling,
 * ed è responsabile della gestione dello stato degli utenti tramite una macchina a stati.
 * Ogni utente è associato a un contesto di stato che viene aggiornato durante l'interazione con il bot.
 * Il bot reagisce ai messaggi inviati dagli utenti e, in particolare, gestisce il comando {@code /start}
 * per iniziare un nuovo flusso di interazione con l'utente.</p>
 *
 * <p>La classe utilizza un client Telegram basato su OkHttp per eseguire le richieste API e
 * un'implementazione di una macchina a stati per gestire le risposte in base agli input degli utenti.</p>
 */
public class TelegramBot implements LongPollingUpdateConsumer {
    /** <h4>Il client Telegram utilizzato per inviare e ricevere messaggi.</h4> */
    private final TelegramClient telegramClient;

    /**
     * <h4>Costruttore della classe {@code TelegramBot}.</h4>
     * <p>Inizializza il bot con un client OkHttp per la comunicazione con l'API di Telegram.</p>
     *
     * @param telegramClient Il client Telegram da utilizzare per eseguire le richieste.
     */
    public TelegramBot(OkHttpTelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
     * <h4>Metodo che gestisce una lista di aggiornamenti (aggiornamenti ricevuti da Telegram).</h4>
     * <p>Ogni aggiornamento viene elaborato in un thread separato per evitare blocchi nel
     * flusso principale di esecuzione. Ogni thread invoca {@link #handleUpdate(Update)} per
     * elaborare il singolo aggiornamento.</p>
     *
     * @param updates La lista di aggiornamenti ricevuti dal server Telegram.
     */
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
     * <p>Se il messaggio contiene il comando {@code /start} o se l'utente non ha uno stato
     * associato, viene creato un nuovo contesto di stato per l'utente e l'aggiornamento viene
     * elaborato in base alla logica definita dal contesto di stato.</p>
     *
     * @param update L'aggiornamento ricevuto da Telegram da elaborare.
     * @throws TelegramApiException Se si verifica un errore durante l'elaborazione dell'aggiornamento.
     */
    private void handleUpdate(Update update) throws TelegramApiException {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // long chatId = update.getMessage().getChatId();
            // if((update.getMessage().getText().equals("/start")) || !userStates.containsKey(chatId)) {
            //     userStates.put(chatId, new StateContext(new Start()));
            // }
            // telegramClient.execute(userStates.get(chatId).handleMessage(update));
            System.out.println(update.getMessage().getText());
        }
    }
}