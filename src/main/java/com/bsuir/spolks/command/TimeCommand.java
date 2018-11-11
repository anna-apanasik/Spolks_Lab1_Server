package com.bsuir.spolks.command;

import com.bsuir.spolks.connection.ClientSession;
import com.bsuir.spolks.connection.Connection;
import com.bsuir.spolks.controller.Controller;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class TimeCommand extends AbstractCommand {

    private static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    /**
     * Execute command.
     */
    @Override
    public void execute(ClientSession session) {
        Connection connection = Controller.getInstance().getConnection();

        if(connection != null) {
            try {
                DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
                Date date = new Date();

                ByteBuffer buff = ByteBuffer.wrap(dateFormat.format(date).getBytes());
                channel.write(buff);
                LOGGER.log(Level.INFO, "Time sent");
            } catch (IOException e) {
                LOGGER.log(Level.ERROR, e.getMessage());
            }
        }
    }

    /**
     * Build command instance.
     *
     * @return instance
     */
    @Override
    public ICommand build() {
        return new TimeCommand();
    }
}
