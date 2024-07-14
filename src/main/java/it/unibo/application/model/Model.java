package it.unibo.application.model;

import java.util.Optional;

import it.unibo.application.controller.ControllerImpl;
import it.unibo.application.dto.Alimento;

public class Model {
    private final ControllerImpl controller;

    public Model(final ControllerImpl controller){
        this.controller = controller;
    }

    public static void main(String[] args){
        var prova = new Alimento(1, "prova", 1, 1, 1, 1, Optional.of(3), 'c', null, "io", false);
        System.out.println(prova);
        System.out.println(Optional.ofNullable(Optional.of(3)));
    }
}
