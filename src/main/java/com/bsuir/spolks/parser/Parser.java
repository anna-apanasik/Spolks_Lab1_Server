package com.bsuir.spolks.parser;

import com.bsuir.spolks.command.ICommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser extends AbstractParser {

    private static final String CMD_COMMON_REGEX = "^([a-z]+)( -[a-z]+((?==)='[\\w .-:\\\\]+')*)*$";
    private static final int COMMAND_GROUP_INDEX = 1;

    /**
     * Handle parse text from cmd.
     *
     * @param cmd
     * @return command instance
     */
    @Override
    public ICommand handle(String cmd)  {
        Pattern pattern = Pattern.compile(CMD_COMMON_REGEX);
        Matcher matcher = pattern.matcher(cmd);
        final String command = matcher.group(COMMAND_GROUP_INDEX);
    }
}
