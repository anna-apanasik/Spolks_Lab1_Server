package com.bsuir.spolks.connection;

import com.bsuir.spolks.util.Storage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bsuir.spolks.command.ICommand;
import com.bsuir.spolks.parser.Parser;
import com.bsuir.spolks.exception.CommandNotFoundException;
import com.bsuir.spolks.exception.WrongCommandFormatException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Connection {

    /**
     * Logger to getCommand logs.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    private ServerSocket socket;

    private static final int PORT = 9999;
    private static final int BACKLOG = 10;

    private DataOutputStream os;
    private DataInputStream is;

    private String clientMessage;

    public Connection() {
    }

    private Storage uuidStorage = new Storage();

    /**
     * Write to stream.
     *
     * @param data
     * @throws IOException
     */
    public void write(String data) throws IOException {
        os.writeUTF(data);
    }

    public void write(byte b[], int off, int len) throws IOException {
        os.write(b, off, len);
    }

    /**
     * Read stream data.
     *
     * @return data
     * @throws IOException
     */
    public String read() throws IOException {
        return is.readUTF();
    }

    /**
     * Run server.
     *
     * @return boolean
     */
    public boolean open() {
        try {
            socket = new ServerSocket(PORT, BACKLOG);
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
        while (true) {
            Socket client;

            try {
                client = socket.accept();

                LOGGER.log(Level.INFO, "Client is connected!");
                this.initStream(client);

                while (true) {
                    try {

                        clientMessage = is.readUTF();
                        if (clientMessage.isEmpty()) {
                            break;
                        }
                        String cmd = clientMessage;

                        if (cmd.equals("true")) {
                            continue;
                        }

                        ICommand command = new Parser().handle(cmd);
                        command.execute();
                    } catch (IOException e) {
                        LOGGER.log(Level.ERROR, "Client stopped working with server.");
                        break;
                    } catch (WrongCommandFormatException | CommandNotFoundException e) {
                        LOGGER.log(Level.ERROR, "Error: " + e.getMessage());
                    }
                }

                this.closeClientConnection(client);
            } catch (IOException e) {
                LOGGER.log(Level.ERROR, "Can't close connection.");
            }
        }
    }


    private void initStream(Socket s) throws IOException {
        is = new DataInputStream(s.getInputStream());
        os = new DataOutputStream(s.getOutputStream());
    }

    private void closeClientConnection(Socket s) throws IOException {
        is.close();
        os.close();
        s.close();
        System.out.println("Client has been disconnected!");
    }

    public Storage getUuidStorage() {
        return uuidStorage;
    }
}
