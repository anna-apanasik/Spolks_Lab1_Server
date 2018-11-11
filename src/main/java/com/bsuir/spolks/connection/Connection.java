package com.bsuir.spolks.connection;

import com.bsuir.spolks.command.DownloadCommand;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Connection {

    /**
     * Logger to getCommand logs.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    //    private static final String ADDRESS = "192.168.10.10";
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 9999;
    private static final int LISTEN_PERIOD_TIME_MS = 500;

    static HashMap<SelectionKey, ClientSession> clientMap = new HashMap<>();

    private ServerSocketChannel serverChannel;
    private Selector selector;
    private SelectionKey serverKey;

    public Connection() {
    }

    /**
     * Run server.
     *
     * @return boolean
     */
    public boolean open() {
        try {
            InetSocketAddress sockAddr = new InetSocketAddress(ADDRESS, PORT);

            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverKey = serverChannel.register(selector = Selector.open(), SelectionKey.OP_ACCEPT);
            serverChannel.bind(sockAddr);

            LOGGER.log(Level.INFO, "Server started.");

            return true;
        } catch (IOException e) {
            LOGGER.log(Level.ERROR, "Couldn't listen to port " + PORT);
            return false;
        }
    }

    /**
     * Listen for clients.
     */
    public void listen() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                listenExecutor();
            } catch (IOException e) {
                LOGGER.log(Level.ERROR, e.getMessage());
            }
        }, 0, LISTEN_PERIOD_TIME_MS, TimeUnit.MILLISECONDS);
    }

    private void listenExecutor() throws IOException {
        selector.selectNow();

        for (SelectionKey key : selector.selectedKeys()) {
            try {
                if (!key.isValid()) {
                    LOGGER.log(Level.WARN, "Key is invalid...");
                    continue;
                }

                if (key == serverKey) {
                    SocketChannel acceptedChannel = serverChannel.accept();

                    if (acceptedChannel == null) {
                        LOGGER.log(Level.ERROR, "Accepted channel is null.");
                        continue;
                    }

                    acceptedChannel.configureBlocking(false);

                    SelectionKey readKey = acceptedChannel.register(selector, SelectionKey.OP_READ);
                    ClientSession newClientSession = new ClientSession(readKey, acceptedChannel);

                    clientMap.put(readKey, newClientSession);

                    LOGGER.log(Level.INFO, "Client " + acceptedChannel.getRemoteAddress() + " connected.");
                    LOGGER.log(Level.INFO, "Total clients: " + clientMap.size());
                }

                boolean isUserDisconnected = false;


                if (key.isReadable()) {
                    ClientSession session = clientMap.get(key);
                    if (session == null) {
                        LOGGER.log(Level.ERROR, "Client session not found.");
                        continue;
                    }

                    session.read();
                    isUserDisconnected = session.isUserDisconnected();
                }

                if (!isUserDisconnected && key.isWritable()) {
                    ClientSession session = clientMap.get(key);
                    if (session == null) {
                        LOGGER.log(Level.ERROR, "Client session not found.");
                        continue;
                    }

                    if (session.hasFileForDownload) {
                        DownloadCommand.continueDownload(session);
                    }
                }


            } catch (IOException e) {
                LOGGER.log(Level.ERROR, e.getMessage());
            }
        }

        selector.selectedKeys().clear();
    }
}
