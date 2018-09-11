package com.bsuir.spolks.util;

import com.bsuir.spolks.command.ICommand;
import com.bsuir.spolks.parser.Parser;

import java.util.Scanner;

public class InputManager {

    private Scanner scanner;

    public InputManager() {
        scanner = new Scanner(System.in);
    }
    /**
     * Get command from user's input.
     *
     * @return command interface
     */
    public ICommand getCommand() {
        String cmd = scanner.nextLine();
        return new Parser().parse(cmd);
    }
}
