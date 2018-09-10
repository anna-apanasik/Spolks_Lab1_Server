package com.bsuir.spolks.parser;

import com.bsuir.spolks.command.ICommand;

abstract class AbstractParser implements IParser {

    /**
     * Handle parse text from cmd.
     *
     * @param cmd
     * @return command instance
     */
    public abstract ICommand handle(String cmd);

    /**
     * Chain the data by handlers.
     *
     * @param cmd
     */
    public ICommand parse(String cmd) {
        return handle(cmd);
    }
}
