package com.bsuir.spolks.controller;

import com.bsuir.spolks.util.InputManager;
import com.bsuir.spolks.connection.Connection;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public final class Controller {

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

    private Connection connection;

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
        connection = new Connection();

        if (connection.open()) {
            connection.listen();
        }
    }

    /**
     * Set connection.
     *
     * @param c connection instance
     */
    public void setConnection(Connection c) {
        this.connection = c;
    }

    /**
     * Get opened connection.
     *
     * @return connection
     */
    public Connection getConnection() {
        return connection;
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
