package it.unibo.application.controller;

import java.util.Objects;
import java.util.Scanner;

import it.unibo.application.model.Model;
import it.unibo.application.view.View;

public class Controller {

    private boolean initialized;
    private View view;
    private Model model;

    public Controller(){
    }

    public void init(View view, Model model) throws IllegalStateException {
        if(initialized){
            throw new IllegalStateException("Controller already initialized.");
        }
        this.view = Objects.requireNonNull(view);
        this.model = Objects.requireNonNull(model);
        this.view.show();
    }
}
