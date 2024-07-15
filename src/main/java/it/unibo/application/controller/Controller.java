package it.unibo.application.controller;

import it.unibo.application.model.Model;
import it.unibo.application.view.View;

public class Controller {

    private final Model model;
    private final View view;

    public Controller(Model model, View view){
        this.model = model;
        this.view = view;
    }

    public void sistemaRichiedeLogin() {
        view.richiediLogin();
    }

    public void utenteRichiedeAutenticazione(String username, char[] password) {
        view.visualizzaMenuPrincipale();
    }

    public void utenteRichiedeLogout() {
        view.richiediLogin();
    }
}
