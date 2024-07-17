package it.unibo.application.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import it.unibo.application.commons.Utilities;
import it.unibo.application.dto.Alimento;
import it.unibo.application.dto.Misurazione;
import it.unibo.application.dto.Tag;
import it.unibo.application.dto.Target;
import it.unibo.application.dto.ValoriAlimento;
import it.unibo.application.model.Model;
import it.unibo.application.view.View;

public class Controller {

    private final Model model;
    private final View view;
    private Optional<String> username;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
        username = Optional.empty();
    }

    public void sistemaRichiedeLogin() {
        view.richiediLogin();
    }

    public void utenteRichiedeAutenticazione(String username, char[] password) {
        if (model.isValid(username, password)) {
            this.username = Optional.of(username);
            view.visualizzaMenuPrincipale();
        } else {
            view.displayErrorMessage("Credenziali errate.");
        }

    }

    public void utenteRichiedeRegistrazione(String username, char[] password) {
        if (model.registerUser(username, password)) {
            view.displayMessage("Utente registrato, procedere al login.");
        }
    }

    public void utenteRichiedeLogout() {
        this.username = Optional.empty();
        view.richiediLogin();
    }

    public void utenteCercaAlimenti(Optional<String> nome, Optional<Set<Tag>> tag,
            Consumer<List<Alimento>> destinatario) {
        destinatario.accept(model.cercaAlimenti(utenteAttuale(), nome, tag));
    }

    public String utenteAttuale() {
        if (this.username.isPresent()) {
            return this.username.get();
        } else {
            throw new IllegalStateException("No user is logged in.");
        }
    }

    public void utenteRichiedeTarget() {
        view.visualizzaTarget(model.leggiTarget(utenteAttuale()));
    }

    public void utenteImpostaTarget(Optional<Target> target) {
        if (model.impostaTarget(utenteAttuale(), target)) {
            view.displayMessage("Target impostato correttamente.");
        } else {
            view.displayErrorMessage("Target non valido.");
        }
    }

    public void utenteImpostaObbiettivo(Optional<Integer> obbiettivo) {
        if (model.impostaObbiettivo(utenteAttuale(), obbiettivo)) {
            view.displayMessage("Obbiettivo impostato correttamente.");
        } else {
            view.displayErrorMessage("Obbiettivo non valido.");
        }
    }

    public void utenteRichiedeObbiettivo() {
        view.visualizzaObbiettivo(model.leggiObbiettivo(utenteAttuale()));
    }

    public void utenteRichiedeMisurazioni() {
        view.visualizzaMisurazioni(model.leggiMisurazioni(utenteAttuale()));
    }

    public void utenteAggiungeMisurazione(LocalDate data, BigDecimal peso) {
        if (model.aggiungiMisurazione(new Misurazione(utenteAttuale(), data, peso))) {
            view.displayMessage("Misurazione inserita correttamente.");
        } else {
            view.displayErrorMessage("Errore nell'inserimento della misurazione.");
        }
    }

    public void utenteModificaMisurazione(Misurazione misurazione, BigDecimal nuovoPeso) {
        if (model.modificaMisurazione(misurazione, nuovoPeso)) {
            view.displayMessage("Misurazione modificata correttamente.", () -> utenteRichiedeMisurazioni());
        } else {
            view.displayErrorMessage("Errore nella modifica della misurazione.");
        }
    }

    public void utenteEliminaMisurazione(Misurazione misurazione) {
        if (model.eliminaMisurazione(misurazione)) {
            view.displayMessage("Misurazione eliminata correttamente.", () -> utenteRichiedeMisurazioni());
        } else {
            view.displayErrorMessage("Errore nell'eliminazione della misurazione.");
        }
    }

    public void utenteRichiedeTag() {
        view.visualizzaTag(model.leggiTag());
    }

    public void utenteAggiungeTag(String parolaChiave) {
        if (model.aggiungiTag(new Tag(parolaChiave, utenteAttuale()))) {
            view.displayMessage("Tag inserito correttamente.");
        } else {
            view.displayErrorMessage("Errore nell'inserimento dell tag.");
        }
    }

    public void utenteRichiedeSuoiTag() {
        view.visualizzaTagModificabili(model.leggiTag(utenteAttuale()));
    }

    public void utenteEliminaTag(Tag tag) {
        if (model.eliminaTag(tag)) {
            view.displayMessage("Tag eliminato correttamente.", () -> utenteRichiedeSuoiTag());
        } else {
            view.displayErrorMessage("Errore nell'eliminazione del tag.");
        }
    }

    public void utenteAggiungeCibo(ValoriAlimento cibo) {
        System.out.println(cibo);
        if (model.aggiungiAlimento(
                new Alimento(-1, cibo.nome(), cibo.kcal(), cibo.carboidrati(), cibo.grassi(), cibo.proteine(),
                        cibo.porzione(), 'C', cibo.brand(), utenteAttuale(), cibo.privato()))) {
            view.displayMessage("Cibo inserito correttamente.", () -> utenteRichiedeSuoiAlimenti());
        } else {
            view.displayErrorMessage("Errore nell'inserimento del cibo.");
        }
    }

    public void utenteRichiedeSuoiAlimenti() {
        view.schermataSelezioneAlimento(model.alimentiPropri(utenteAttuale()), "Modifica",
                a -> utenteVuoleModificareAlimento(a));
    }

    public void utenteVuoleModificareAlimento(Alimento a) {

    }

    public void utenteVuoleCercareAlimenti() {
        view.richiediRicerca(model.leggiTag(),
                p -> utenteEffettuaRicerca(p.getLeft(), p.getRight(), view::elencaAlimenti));
    }

    public void utenteVuoleAggiungerePreferito() {
        view.richiediRicerca(model.leggiTag(), p -> utenteEffettuaRicerca(p.getLeft(), p.getRight(),
                alimenti -> view.schermataSelezioneAlimento(alimenti, "Aggiungi ai preferiti",
                        this::utenteAggiungePreferito)));
    }

    public void utenteRichiedeTag(Consumer<List<Tag>> destinatario) {
        destinatario.accept(model.leggiTag());
    }

    public void utenteRichiedeDettaglio(Alimento a) {

    }

    public void utenteRichiedePreferiti() {
        view.schermataSelezioneAlimento(model.leggiPreferiti(utenteAttuale()), "Rimuovi preferito",
                this::utenteEliminaPreferito);
    }

    public void utenteEliminaPreferito(Alimento preferito) {
        if (model.eliminaPreferito(utenteAttuale(), preferito)) {
            view.displayMessage("Preferito eliminato correttamente.", this::utenteRichiedePreferiti);
        } else {
            view.displayErrorMessage("Impossibile eliminare il preferito.");
        }
    }

    private void utenteAggiungePreferito(Alimento preferito) {
        if (model.aggiungiPreferito(utenteAttuale(), preferito)) {
            view.displayMessage("Preferito aggiunto correttamente.");
        } else {
            view.displayErrorMessage("Impossibile aggiungere il preferito.");
        }
    }

    private void utenteEffettuaRicerca(Optional<String> nome, Set<Tag> tags, Consumer<List<Alimento>> nextStep) {
        nextStep.accept(model.cercaAlimenti(utenteAttuale(), nome, Utilities.notEmpty(tags)));
    }
}
