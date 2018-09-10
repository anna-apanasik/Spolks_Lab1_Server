package com.bsuir.spolks.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractCommand implements ICommand {

    Map<String, String> availableTokens;
    private Map<String, String> tokens;

    /**
     * Logger to getCommand logs.
     */
    static final Logger LOGGER = LogManager.getLogger();

    /**
     * Default constructor.
     */
    AbstractCommand() {
        tokens = new HashMap<>();
        availableTokens = new HashMap<>();
    }
}
