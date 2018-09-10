package com.bsuir.spolks.command;

public interface ICommand {

    /**
     * Execute command.
     */
    void execute();

    /**
     * Build command instance.
     *
     * @return instance
     */
    ICommand build();
}
