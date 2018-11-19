package com.bsuir.spolks.connection;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bsuir.spolks.command.ICommand;
import com.bsuir.spolks.parser.Parser;
import com.bsuir.spolks.exception.CommandNotFoundException;
import com.bsuir.spolks.exception.WrongCommandFormatException;

import java.io.*;

import java.net.Socket;


public class Connection extends Thread {

    /**
     * Logger to getCommand logs.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    private DataOutputStream os;
    private DataInputStream is;

    private String clientMessage;

    private Socket client;

    public Connection(Socket client) {
        this.client = client;
        start();
    }


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
     * Listen for clients.
     */
    public void run() {
            try {
                LOGGER.log(Level.INFO, "Client is connected!");
                this.initStream();

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
                        command.execute(this);
                    } catch (IOException e) {
                        LOGGER.log(Level.ERROR, "Client stopped working with server.");
                        break;
                    } catch (WrongCommandFormatException | CommandNotFoundException e) {
                        LOGGER.log(Level.ERROR, "Error: " + e.getMessage());
                    }
                }

                this.closeClientConnection();
            } catch (IOException e) {
                LOGGER.log(Level.ERROR, "Can't close connection.");
            }
    }


    private void initStream() throws IOException {
        is = new DataInputStream(client.getInputStream());
        os = new DataOutputStream(client.getOutputStream());
    }

    private void closeClientConnection() throws IOException {
        is.close();
        os.close();
        client.close();
        System.out.println("Client has been disconnected!");
    }

}
