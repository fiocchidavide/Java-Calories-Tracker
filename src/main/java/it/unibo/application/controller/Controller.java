package it.unibo.application.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import it.unibo.application.dto.Alimento;
import it.unibo.application.dto.Tag;
import it.unibo.application.model.Model;
import it.unibo.application.view.View;

public class Controller {

    private final Model model;
    private final View view;
    private Optional<String> username;

    public Controller(Model model, View view){
        this.model = model;
        this.view = view;
        username = Optional.empty();
    }

    public void sistemaRichiedeLogin() {
        view.richiediLogin();
    }

    public void utenteRichiedeAutenticazione(String username, char[] password) {
        if(model.isValid(username, password)){
            this.username = Optional.of(username);
        }
        view.visualizzaMenuPrincipale();
    }

    public void utenteRichiedeLogout() {
        this.username = Optional.empty();
        view.richiediLogin();
    }

    public List<Alimento> utenteCercaAlimenti(String nome, Set<Tag> tag){
        return List.of();
    }

    public String utenteAttuale(){
        if (this.username.isPresent()) {
            return this.username.get();
        } else {
            throw new IllegalStateException("No user is logged in.");
        }
    }
}
