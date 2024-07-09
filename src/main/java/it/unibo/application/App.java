package it.unibo.application;

import it.unibo.application.controller.Controller;
import it.unibo.application.model.Model;
import it.unibo.application.view.View;

public class App {

    public static String TITLE = "Application";

    public static void main(String[] args) {
        var controller = new Controller();
        var view = new View(controller);
        var model = new Model(controller);
        controller.init(view, model);
    }
}
