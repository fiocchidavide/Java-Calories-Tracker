package it.unibo.application;

import it.unibo.application.controller.ControllerImpl;
import it.unibo.application.model.Model;
import it.unibo.application.view.View;

public class App {

    public static String TITLE = "Application";

    public static void main(String[] args) {
        var controller = new ControllerImpl();
        var view = new View(controller);
        var model = new Model(controller);
        controller.init(view, model);
    }
}
