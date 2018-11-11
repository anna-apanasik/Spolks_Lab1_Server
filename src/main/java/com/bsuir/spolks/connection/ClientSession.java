package com.bsuir.spolks.connection;

import com.bsuir.spolks.command.ICommand;
import com.bsuir.spolks.exception.CommandNotFoundException;
import com.bsuir.spolks.exception.WrongCommandFormatException;
import com.bsuir.spolks.parser.Parser;
import com.bsuir.spolks.util.SocketBuffer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientSession {

    /**
     * Logger to getCommand logs.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    public SelectionKey readSelkey;
    public SocketChannel channel;
    private SocketBuffer buffer;

    public DataInputStream dataInputStream;
    public boolean hasFileForDownload = false;
    private boolean disconnected = false;


    ClientSession(SelectionKey readSelkey, SocketChannel channel) throws IOException {
        this.readSelkey = readSelkey;
        this.channel = (SocketChannel) channel.configureBlocking(false); // asynchronous/non-blocking
        this.buffer = new SocketBuffer();
    }

    public boolean isUserDisconnected() {
        return disconnected;
    }

    public void setDownloadInfo(DataInputStream dataInputStream, boolean hasFileForDownload) {
        this.hasFileForDownload = hasFileForDownload;
        this.dataInputStream = dataInputStream;
    }

    public void disconnect() {
        Connection.clientMap.remove(readSelkey);
        disconnected = true;

        try {
            if (readSelkey != null) {
                readSelkey.cancel();
            }

            if (channel == null) {
                LOGGER.log(Level.ERROR, "Can't disconnect from server. Channel is null.");
                return;
            }

            LOGGER.log(Level.INFO, "User " + channel.getRemoteAddress() + " is disconnected!");
            channel.close();
        } catch (IOException e) {
            LOGGER.log(Level.ERROR, e.getMessage());
        }
    }

    void read() {
        try {
            int countBytes;

            if ((countBytes = channel.read((ByteBuffer) buffer.clear())) == -1) {
                disconnect();
            }

            if (countBytes < 1) {
                return;
            }

            byte[] tempData = buffer.read(countBytes);

            String cmd = new String(tempData, 0, countBytes);

            ICommand command = new Parser().handle(cmd);
            command.setChannel(channel);
            command.execute(this);

        } catch (WrongCommandFormatException | CommandNotFoundException | IOException e) {
            LOGGER.log(Level.ERROR, e.getMessage());
        }
    }

    public String readFromChannel() {
        try {
            int countBytes;

            while((countBytes = channel.read((ByteBuffer) buffer.clear())) == 0) { }

            if (countBytes  == -1) {
                LOGGER.log(Level.ERROR, "Can not read message from channel");
                return null;
            }


            byte[] tempData = buffer.read(countBytes);

            return new String(tempData, 0, countBytes);
        } catch (IOException e) {
            LOGGER.log(Level.ERROR, e.getMessage());
            return  null;
        }
    }
}

