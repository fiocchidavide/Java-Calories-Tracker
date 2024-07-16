package it.unibo.application.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import it.unibo.application.dto.Alimento;
import it.unibo.application.dto.Tag;
import it.unibo.application.dto.Target;
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
            view.visualizzaMenuPrincipale();
        }else{
            view.displayErrorMessage("Credenziali errate.");
        }
        
    }

    public void utenteRichiedeRegistrazione(String username, char[] password) {
        if(model.registerUser(username, password)) {
            view.displayMessage("Utente registrato, procedere al login.");
        }
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

    public Optional<Target> utenteRichiedeTarget() {
        return model.leggiTarget(utenteAttuale());
    }

    public void utenteImpostaTarget(Optional<Target> target) {
        if(model.impostaTarget(utenteAttuale(), target)){
            view.displayMessage("Target impostato correttamente.");
        }else{
            view.displayErrorMessage("Target non valido.");
        }
    }

    public void utenteImpostaObbiettivo(Optional<Integer> obbiettivo) {
        if(model.impostaObbiettivo(utenteAttuale(), obbiettivo)){
            view.displayMessage("Obbiettivo impostato correttamente.");
        }else{
            view.displayErrorMessage("Obbiettivo non valido.");
        }
    }

    public Optional<Integer> utenteRichiedeObbiettivo() {
        return model.leggiObbiettivo(utenteAttuale());
    }
}
