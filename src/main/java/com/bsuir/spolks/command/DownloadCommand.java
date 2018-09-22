package com.bsuir.spolks.command;

import com.bsuir.spolks.connection.Connection;
import com.bsuir.spolks.controller.Controller;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

class DownloadCommand extends AbstractCommand {

    private static final String SUCCESS = "success";
    private static final int BUFF_SIZE = 12288;

    DownloadCommand() {
        Arrays.stream(AvailableToken.values()).forEach(t -> availableTokens.put(t.getName(), t.getRegex()));
    }

    /**
     * Execute command.
     */
    @Override
    public void execute() {
        try {
            String path = getTokens().get(AvailableToken.PATH.getName());

            if (path != null) {
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

        if (connection != null)    {
            File file = new File(path);

            final long fileSize = file.length();

            if (file.exists() && !file.isDirectory()) {
                connection.write(SUCCESS + " " + fileSize);

                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));

                int receivedBytes;
                int fileProgress = connection.getUuidStorage().getFileProgress(path);
                int progressFromClient = Integer.parseInt(connection.read());

                if(progressFromClient > 0 && progressFromClient != fileProgress) {
                    connection.getUuidStorage().updateFileProgress(path, progressFromClient);
                    fileProgress = connection.getUuidStorage().getFileProgress(path);
                }

                byte fileContent[] = new byte[BUFF_SIZE];

                Date start = new Date();

                dataInputStream.skip(fileProgress);
                while ((receivedBytes = dataInputStream.read(fileContent)) != -1) {
                    if (Boolean.valueOf(connection.read())) {
                        connection.write(fileContent, receivedBytes);
                        fileProgress += BUFF_SIZE;
                        connection.getUuidStorage().updateFileProgress(path, fileProgress);
                    }
                }

                Date end = new Date();
                long resultTime = end.getTime() - start.getTime();

                if (fileProgress >= fileSize) {
                    connection.getUuidStorage().deleteCurrentClient();
                    LOGGER.log(Level.INFO, "File is transferred.");
                    long resultTimeInSeconds = TimeUnit.SECONDS.convert(resultTime, TimeUnit.MILLISECONDS);
                    LOGGER.log(Level.INFO, "Transfer time: " + ((resultTimeInSeconds > 0) ? resultTimeInSeconds + "s" : resultTime + "ms"));
                } else {
                    LOGGER.log(Level.INFO, "Something went wrong! File isn't transferred.");
                }

            } else {
                final String message = "File does not exists or something went wrong.";
                connection.write(message);
                LOGGER.log(Level.ERROR, message);
            }
        } else {
            LOGGER.log(Level.WARN, "You're not connected to server.");
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
