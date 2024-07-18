package it.unibo.application.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import it.unibo.application.commons.Utilities;
import it.unibo.application.dto.Alimento;
import it.unibo.application.dto.Consumazione;
import it.unibo.application.dto.Misurazione;
import it.unibo.application.dto.Ricetta;
import it.unibo.application.dto.Tag;
import it.unibo.application.dto.Target;
import it.unibo.application.dto.ValoriCibo;
import it.unibo.application.dto.ValoriRicetta;
import it.unibo.application.model.Model;
import it.unibo.application.view.View;

public class Controller {

    private final Model model;
    private final View view;
    private Optional<String> username;
    private Optional<Map<Alimento, Integer>> ingredientiCorrentementeConsiderati;
    private Optional<Consumer<Map<Alimento, Integer>>> operazioneInAttesaDiIngredienti;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
        username = Optional.empty();
        ingredientiCorrentementeConsiderati = Optional.empty();
        operazioneInAttesaDiIngredienti = Optional.empty();
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
            view.displayMessage("Misurazione inserita correttamente.", this::utenteRichiedeMisurazioni);
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
        view.visualizzaTag(model.leggiTag(), t -> {
        });
    }

    public void utenteAggiungeTag(String parolaChiave) {
        if (model.aggiungiTag(new Tag(parolaChiave, utenteAttuale()))) {
            view.displayMessage("Tag inserito correttamente.");
        } else {
            view.displayErrorMessage("Errore nell'inserimento dell tag.");
        }
    }

    public void utenteRichiedeSuoiTag() {
        view.visualizzaTag(model.leggiTag(), t -> view.dettaglioTag(t));
    }

    public void utenteEliminaTag(Tag tag) {
        if (model.eliminaTag(tag)) {
            view.displayMessage("Tag eliminato correttamente.", () -> utenteRichiedeSuoiTag());
        } else {
            view.displayErrorMessage("Errore nell'eliminazione del tag.");
        }
    }

    public void utenteVuoleAggiungereCibo() {
        view.richiediValoriCibo(cibo -> utenteAggiungeCibo(cibo));
    }

    public void utenteModificaCibo(Alimento cibo, ValoriCibo nuoviValori) {
        if(cibo.tipo() != 'C' || !utenteAttuale().equals(cibo.proprietario())){
            throw new IllegalArgumentException("L'alimento " + cibo + " non è un cibo o non è dell'utente.");
        }
        if (model.modificaCibo(cibo, nuoviValori)) {
            view.displayMessage("Cibo modificato correttamente.", view::visualizzaMenuPrincipale);
        } else {
            view.displayErrorMessage("Impossibile modificare il cibo.");
        }
    }

    public void utenteEliminaAlimento(Alimento alimento) {
        if(!utenteAttuale().equals(alimento.proprietario())){
            throw new IllegalArgumentException("L'alimento non appartiene all'utente.");
        }
        if (model.eliminaAlimento(alimento)) {
            view.displayMessage("Alimento eliminato correttamente.", view::visualizzaMenuPrincipale);
        } else {
            view.displayErrorMessage("Impossibile eliminare l'alimento.");
        }
    }

    private void utenteAggiungeCibo(ValoriCibo cibo) {
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
                a -> utenteRichiedeDettaglio(a));
    }

    public void utenteRichiedeDettaglio(Alimento a) {
        view.mostraDettaglio(a, a.proprietario().equals(utenteAttuale()));
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

    public void utenteVuoleAggiungereConsumazione() {
        view.displayMessage("Per prima cosa, cerca un cibo per la consumazione.");
        view.richiediRicerca(model.leggiTag(),
                p -> utenteEffettuaRicerca(p.getLeft(), p.getRight(),
                        alimenti -> view.schermataSelezioneAlimento(alimenti, "Seleziona",
                                alimento -> view.richiediConsumazione(alimento.codAlimento(),
                                        this::utenteAggiungeConsumazione))));
    }

    private void utenteAggiungeConsumazione(Consumazione consumazione) {
        if (model.aggiungiConsumazione(new Consumazione(utenteAttuale(), -1, consumazione.data(), consumazione.ora(),
                consumazione.codAlimento(), consumazione.quantità()))) {
            view.displayMessage("Consumazione aggiunta correttamente.", this::utenteRichiedeConsumazioni);
        } else {
            view.displayErrorMessage("Impossibile aggiungere la consumazione.");
        }
    }

    public void utenteRichiedeConsumazioni() {
        view.visualizzaConsumazioni(model.leggiConsumazioni(utenteAttuale()),
                consumazione -> view.modificaConsumazione(consumazione,
                        this::utenteModificaConsumazione));
    }

    public void utenteModificaConsumazione(Consumazione nuovaConsumazione) {
        if (model.modificaConsumazione(nuovaConsumazione)) {
            view.displayMessage("Consumazione modificata correttamente.", this::utenteRichiedeConsumazioni);
        } else {
            view.displayErrorMessage("Impossibile modificare la consumazione.");
        }
    }

    public void utenteEliminaConsumazione(Consumazione consumazione) {
        if (model.rimuoviConsumazione(consumazione)) {
            view.displayMessage("Consumazione eliminata correttamente.", this::utenteRichiedeConsumazioni);
        } else {
            view.displayErrorMessage("Impossibile eliminare la consumazione.", this::utenteRichiedeConsumazioni);
        }
    }

    public void utenteVuoleAggiungereRicetta() {
        this.ingredientiCorrentementeConsiderati = Optional.of(new HashMap<>());
        this.operazioneInAttesaDiIngredienti = Optional.of(ingredienti -> {
            view.richiediValoriRicetta(valoriRicetta -> utenteAggiungeRicetta(valoriRicetta, ingredienti));
        });
        view.displayMessage("Inserisci gli ingredienti per la ricetta.", this::utenteVuoleAggiungereIngredienti);
    }

    public void utenteVuoleAggiungereIngredienti() {
        view.visualizzaIngredienti(ingredientiCorrentementeConsiderati.get(),
                Optional.of(
                        () -> view.richiediRicerca(model.leggiTag(),
                                p -> utenteCercaAlimenti(p.getLeft(), Utilities.notEmpty(p.getRight()),
                                        alimenti -> view.schermataSelezioneAlimento(alimenti, "Aggiungi ingrediente",
                                                alimento -> view.richiediNumero("Quantità dell'ingrediente:",
                                                        "Seleziona quantità",
                                                        quantità -> utenteAggiungeIngrediente(alimento, quantità)))))),
                        Optional.<Consumer<Alimento>>of(
                            alimento -> view.richiediNumero("Quantità dell'ingrediente:",
                            "Seleziona quantità",
                            quantità -> utenteModificaIngrediente(alimento, quantità))
                        ),
                        Optional.<Consumer<Alimento>>of(
                            alimento -> utenteEliminaIngrediente(alimento)
                        ),
                        Optional.of(
                            () -> operazioneInAttesaDiIngredienti.ifPresent(c -> c.accept(ingredientiCorrentementeConsiderati.get()))
                        ));
    }

    private void utenteAggiungeIngrediente(Alimento alimento, int quantità) {
        if(ingredientiCorrentementeConsiderati.get().containsKey(alimento)) {
            view.displayErrorMessage("L'ingrediente è già presente.", this::utenteVuoleAggiungereIngredienti);
        }else{
            ingredientiCorrentementeConsiderati.get().put(alimento, quantità);
            view.displayMessage("Ingrediente aggiunto correttamente.", this::utenteVuoleAggiungereIngredienti);
        }
    }

    private void utenteModificaIngrediente(Alimento alimento, int quantità) {
        ingredientiCorrentementeConsiderati.get().put(alimento, quantità);
        view.displayMessage("Ingrediente modificato correttamente.", this::utenteVuoleAggiungereIngredienti);
    }

    private void utenteEliminaIngrediente(Alimento alimento) {
        ingredientiCorrentementeConsiderati.get().remove(alimento);
        view.displayMessage("Ingrediente eliminao correttamente.", this::utenteVuoleAggiungereIngredienti);
    }

    private void utenteAggiungeRicetta(ValoriRicetta valoriRicetta, Map<Alimento, Integer> ingredienti) {
        if(model.aggiungiRicetta(new Ricetta(utenteAttuale(), valoriRicetta, ingredienti))){
            this.ingredientiCorrentementeConsiderati = Optional.empty();
            this.operazioneInAttesaDiIngredienti = Optional.empty();
            view.displayMessage("Ricetta aggiunta correttamente.", view::visualizzaMenuPrincipale);
        }else{
            view.displayErrorMessage("Impossibile aggiungere la ricetta, controllare i dati inseriti.", this::utenteVuoleAggiungereIngredienti);
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

    private String utenteAttuale() {
        if (this.username.isPresent()) {
            return this.username.get();
        } else {
            throw new IllegalStateException("No user is logged in.");
        }
    }
}
