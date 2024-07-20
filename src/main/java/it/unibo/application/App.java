package it.unibo.application;

import java.sql.Connection;

import it.unibo.application.controller.Controller;
import it.unibo.application.model.Model;
import it.unibo.application.view.View;

public class App {

    public static String TITLE = "MyCalorieTracker";

    public static void main(String[] args) {
        final Connection connection = args.length == 2 ? ConnectionManager.getConnection(args[0], args[1])
                : ConnectionManager.getConnection("root", "");
        var model = new Model(connection);
        var view = new View(() -> {
            try {
                connection.close();
            } catch (Exception e) {
            }
        });
        var controller = new Controller(model, view);
        view.setController(controller);
        controller.sistemaRichiedeLogin();
    }
}
