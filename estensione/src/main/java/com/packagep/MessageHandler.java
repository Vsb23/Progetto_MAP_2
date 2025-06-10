package com.packagep;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

/**
 * <h2>Classe per la gestione dei messaggi e menu del bot Telegram.</h2>
 */
public class MessageHandler {
    
    private final TelegramClient telegramClient;

    public MessageHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
     * <h4>Invia un messaggio di testo a un utente specifico.</h4>
     */
    public void sendTextMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage(String.valueOf(chatId), "");
        message.setText(text);
        telegramClient.execute(message);
    }

    /**
     * <h4>Invia il menu principale all'utente.</h4>
     */
    public void sendMainMenu(long chatId) throws TelegramApiException {
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
     * <h4>Invia un messaggio lungo dividendolo in chunks se necessario.</h4>
     */
    public void sendLongMessage(long chatId, String text) throws TelegramApiException {
        if (text.length() > 4000) {
            int chunkSize = 4000;
            for (int i = 0; i < text.length(); i += chunkSize) {
                String chunk = text.substring(i, Math.min(text.length(), i + chunkSize));
                sendTextMessage(chatId, chunk);
            }
        } else {
            sendTextMessage(chatId, text);
        }
    }
}