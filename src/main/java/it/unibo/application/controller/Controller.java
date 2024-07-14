package it.unibo.application.controller;

import java.net.PasswordAuthentication;

public interface Controller {
    //O1
    Boolean userAsksRegistration(PasswordAuthentication user);
    Boolean userAsksLogin(PasswordAuthentication credentials);
    void userAsksLogout();

    //O2
    void userAddsFood(String name);
}
