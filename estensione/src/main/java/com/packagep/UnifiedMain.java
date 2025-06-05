package com.packagep;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * <h2>La classe {@code UnifiedMain} avvia sia il server MultiServer che il bot Telegram QTMiner.</h2>
 * <p>Questa classe unifica l'avvio dell'applicazione gestendo contemporaneamente:
 * <ul>
 *   <li>Il server socket per le connessioni client dirette</li>
 *   <li>Il bot Telegram per l'interfaccia di messaggistica</li>
 * </ul>
 * </p>
 * 
 * <p>I due servizi vengono eseguiti in thread separati per garantire che possano
 * funzionare simultaneamente senza interferenze.</p>
 */
public class UnifiedMain {
    
    private static final String BOT_TOKEN = "8037173633:AAEQWCEQ0jGHYxm2MER2ci4W58ZdvsTE3-M";
    private static final int SERVER_PORT = 8080;
    
    /**
     * <h4>Metodo principale che avvia sia il server che il bot Telegram.</h4>
     * <p>L'applicazione avvia prima il server socket in un thread separato,
     * poi inizializza il bot Telegram nel thread principale.</p>
     *
     * @param args Parametri da riga di comando (non utilizzati).
     */
    public static void main(String[] args) {
        System.out.println("=== Avvio QTMiner Application ===");
        
        // Validazione della porta
        if (SERVER_PORT < 1024 || SERVER_PORT > 65535) {
            System.err.println("Porta server non valida: " + SERVER_PORT);
            return;
        }
        
        // Executor per gestire i thread
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        try {
            // 1. Avvio del server socket in un thread separato
            executor.submit(() -> {
                startMultiServer(SERVER_PORT);
            });
            
            // Piccola pausa per assicurarsi che il server sia avviato
            Thread.sleep(1000);
            
            // 2. Avvio del bot Telegram nel thread principale
            startTelegramBot(BOT_TOKEN);
            
        } catch (Exception e) {
            System.err.println("Errore durante l'avvio dell'applicazione: " + e.getMessage());
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
    
    /**
     * <h4>Avvia il server socket per gestire le connessioni client dirette.</h4>
     * <p>Questo metodo incapsula la logica del MultiServer originale,
     * gestendo le connessioni socket in entrata.</p>
     *
     * @param port La porta su cui il server deve essere in ascolto.
     */
    private static void startMultiServer(int port) {
        System.out.println("Avvio MultiServer sulla porta " + port + "...");
        
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            System.out.println("✓ MultiServer avviato con successo sulla porta " + port);
            
            while (true) {
                System.out.println("Server in attesa di connessioni...");
                final Socket socket = server.accept();
                
                // Gestione della connessione in un thread separato
                new Thread(() -> {
                    try {
                        new ServerOneClient(socket);
                    } catch (IOException e) {
                        System.err.println("Errore nella gestione del client: " + e.getMessage());
                        e.printStackTrace();
                    }
                }).start();
                
                System.out.println("✓ Servito client " + socket.getInetAddress());
            }
        } catch (final IOException e) {
            System.err.println("Errore nel MultiServer: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (final IOException e) {
                    System.err.println("Errore nella chiusura del server: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * <h4>Avvia il bot Telegram per l'interfaccia di messaggistica.</h4>
     * <p>Questo metodo incapsula la logica del Main originale del bot Telegram,
     * gestendo la registrazione e l'inizializzazione del bot.</p>
     *
     * @param botToken Il token di autenticazione del bot Telegram.
     */
    private static void startTelegramBot(String botToken) {
        System.out.println("Avvio Bot Telegram...");
        
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            // Crea un'istanza del client Telegram basato su OkHttp
            OkHttpTelegramClient telegramClient = new OkHttpTelegramClient(botToken);
            
            // Crea un'istanza del bot QTMiner e registralo nell'applicazione
            TelegramBot qtMinerBot = new TelegramBot(telegramClient);
            botsApplication.registerBot(botToken, qtMinerBot);
            
            System.out.println("✓ Bot Telegram QTMiner avviato con successo");
            System.out.println("=== Applicazione QTMiner completamente operativa ===");
            System.out.println("- Server socket: localhost:" + SERVER_PORT);
            System.out.println("- Bot Telegram: Attivo e in ascolto");
            
            // Mantiene l'applicazione in esecuzione fino alla chiusura
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("Errore durante l'avvio del bot Telegram: " + e.getMessage());
            e.printStackTrace();
        }
    }
}