package com.bsuir.spolks.command;

import com.bsuir.spolks.connection.Connection;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

class DownloadCommand extends AbstractCommand {

    private static final String SUCCESS = "success";
    private static final String GET_PROGRESS = "progress";
    private static final int BUFF_SIZE = 65536;

    DownloadCommand() {
        Arrays.stream(AvailableToken.values()).forEach(t -> availableTokens.put(t.getName(), t.getRegex()));
    }

    /**
     * Execute command.
     */
    @Override
    public void execute(Connection connection) {
        try {
            String path = getTokens().get(AvailableToken.PATH.getName());

            if (path != null) {
                executeDownload(path, connection);
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

    private void executeDownload(String path, Connection connection) throws IOException {
        if (connection != null)    {
            File file = new File(path);

            final long fileSize = file.length();

            if (file.exists() && !file.isDirectory()) {
                connection.write(SUCCESS + " " + fileSize);

                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));

                int receivedBytes;
                int fileProgress = Integer.parseInt(connection.read());

                connection.write(GET_PROGRESS);

                byte fileContent[] = new byte[BUFF_SIZE];
                dataInputStream.skip(fileProgress);

                Date start = new Date();

                while ((receivedBytes = dataInputStream.read(fileContent)) != -1) {
                        connection.write(fileContent, 0, receivedBytes);
                        fileProgress += BUFF_SIZE;
                }

                Date end = new Date();
                long resultTime = end.getTime() - start.getTime();

                if (fileProgress >= fileSize) {
                    LOGGER.log(Level.INFO, "File is transferred.");
                    long resultTimeInSeconds = TimeUnit.SECONDS.convert(resultTime, TimeUnit.MILLISECONDS);
                    double bitrate = ((double) fileSize / (1024 * 1024)) / ((double)resultTime / 1000);
                    LOGGER.log(Level.INFO, "Transfer time: " + ((resultTimeInSeconds > 0) ? resultTimeInSeconds + "s" : resultTime + "ms"));
                    LOGGER.log(Level.INFO,  "Bitrate: " + (double) Math.round(bitrate * 1000) / 1000 + " Mb/s");
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
