package com.bsuir.spolks;

import com.bsuir.spolks.controller.Controller;

public class Server {
    public static void main(String[] args) {
        Controller.getInstance().work();
    }
}
