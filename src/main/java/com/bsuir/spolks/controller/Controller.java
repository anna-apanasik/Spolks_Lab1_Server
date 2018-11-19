package com.bsuir.spolks.controller;

import com.bsuir.spolks.util.InputManager;
import com.bsuir.spolks.connection.Connection;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public final class Controller {

    private static final int PORT = 9999;
    private static final int BACKLOG = 10;
    /**
     * Logger to getCommand logs.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Instance of Controller.
     */
    private static Controller instance;

    /**
     * Instance creation flag.
     */
    private static AtomicBoolean createdInstance = new AtomicBoolean(false);

    /**
     * Reentrant lock.
     */
    private static ReentrantLock lock = new ReentrantLock();
    private ServerSocket socket;

    private ArrayList<Connection> connections  = new ArrayList<>();
    private InputManager keyboard;

    private Controller() {
        keyboard = new InputManager();
    }

    /**
     * Get instance of controller.
     *
     * @return instance
     */
    public static Controller getInstance() {
        if (!createdInstance.get()) {
            try {
                lock.lock();

                if (instance == null) {
                    instance = new Controller();
                    createdInstance.set(true);

                    LOGGER.log(Level.INFO, Controller.class.getName() + " instance created!");
                }
            } finally {
                lock.unlock();
            }
        }

        return instance;
    }

    /**
     * Start working controller.
     */
    public void work() {
        try {
            socket = new ServerSocket(PORT, BACKLOG);
            LOGGER.log(Level.INFO, "Server started.");
            while (!socket.isClosed()) {
                Socket client = socket.accept();
                connections.add(new Connection(client));

            }
        } catch (IOException e) {
            LOGGER.log(Level.ERROR, "Couldn't listen to port " + PORT);
        }
    }


    /**
     * Get keyboard instance.
     *
     * @return keyboard
     */
    public InputManager getKeyboard() {
        return keyboard;
    }
}
