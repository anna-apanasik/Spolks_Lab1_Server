package com.bsuir.spolks.command;

import com.bsuir.spolks.connection.ClientSession;
import com.bsuir.spolks.connection.Connection;
import com.bsuir.spolks.controller.Controller;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Arrays;



public class DownloadCommand extends AbstractCommand {

    private static final String SUCCESS = "success";
    private static final String GET_PROGRESS = "progress";
    private static final int BUFF_SIZE = 65000;

    private ClientSession session;

    DownloadCommand() {
        Arrays.stream(AvailableToken.values()).forEach(t -> availableTokens.put(t.getName(), t.getRegex()));
    }

    /**
     * Execute command.
     */
    @Override
    public void execute(ClientSession session) {
        try {
            String path = getTokens().get(AvailableToken.PATH.getName());

            if (path != null) {
                this.session = session;
                executeDownload(path);
            }
        } catch (IOException e) {
            LOGGER.log(Level.ERROR, "Error: " + e.getMessage());
        }
    }

    /**
     * Build command instance.
     *
     * @return instance
     */
    @Override
    public ICommand build() {
        return new DownloadCommand();
    }

    private void executeDownload(String path) throws IOException {
        Connection connection = Controller.getInstance().getConnection();
        if (connection != null) {
            File file = new File(path);

            final long fileSize = file.length();

            if (file.exists() && !file.isDirectory()) {
                String message = SUCCESS + " " + fileSize;
                channel.write(ByteBuffer.wrap(message.getBytes()));

                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));

                session.readSelkey.interestOps(SelectionKey.OP_WRITE);
                session.setDownloadInfo(dataInputStream, true);

                String fileProgressMessage = session.readFromChannel();
                if(fileProgressMessage == null) {
                    throw new IOException("Smth went wrong... ");
                }
                int fileProgress = Integer.parseInt(fileProgressMessage);

                channel.write(ByteBuffer.wrap(GET_PROGRESS.getBytes()));

                byte fileContent[] = new byte[BUFF_SIZE];
                dataInputStream.skip(fileProgress);

                LOGGER.log(Level.INFO, "Start transfer file " + path);

                int receivedBytes;
                if ((receivedBytes = dataInputStream.read(fileContent)) != -1) {
                    channel.write(ByteBuffer.wrap(fileContent, 0, receivedBytes));
                    fileProgress += receivedBytes;
                }

                if (fileProgress >= fileSize) {
                    LOGGER.log(Level.INFO, "File is transferred.");
                }

            } else {
                final String message = "File does not exists or something went wrong.";
                channel.write(ByteBuffer.wrap(message.getBytes()));
                LOGGER.log(Level.ERROR, message);
            }
        } else {
            LOGGER.log(Level.WARN, "You're not connected to server.");
        }
    }


    static public void continueDownload(ClientSession session) {
        byte fileContent[] = new byte[BUFF_SIZE];
        int receivedBytes;
        try {
            receivedBytes = session.dataInputStream.read(fileContent);

            if (receivedBytes <= 0) {
                session.dataInputStream.close();
                session.readSelkey.interestOps(SelectionKey.OP_READ);
                session.setDownloadInfo(null, false);
                LOGGER.log(Level.INFO, "File is transferred.");
            } else {
                session.channel.write(ByteBuffer.wrap(fileContent, 0, receivedBytes));
            }
        } catch (IOException e) {
            LOGGER.log(Level.ERROR,e.getMessage());
            session.disconnect();
        }
    }

    private enum AvailableToken {
        PATH("path", "^[\\w .-:\\\\]+$"),
        NAME("name", "^[\\w .-:\\\\]+$");

        private String name;
        private String regex;

        AvailableToken(String name, String regex) {
            this.name = name;
            this.regex = regex;
        }

        public String getName() {
            return name;
        }

        public String getRegex() {
            return regex;
        }
    }
}
