package it.unibo.application.view;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Optional;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.commons.lang3.tuple.Pair;

import it.unibo.application.controller.Controller;
import it.unibo.application.dto.Target;

public class MenuImpostazioni extends MenuElement {

    public MenuImpostazioni(final View view, Controller controller) {
        super("Impostazioni", view, controller);
        this.add(menuPrincipale());
        this.add(obbiettivoAttuale());
        this.add(impostaObbiettivo());
        this.add(targetAttuale());
        this.add(impostaTarget());
        this.add(logout());
    }

    private JMenuItem menuPrincipale() {
        JMenuItem m = new JMenuItem("Menu principale");
        m.addActionListener(a -> getView().visualizzaMenuPrincipale());
        return m;
    }

    private JMenuItem logout() {
        JMenuItem m = new JMenuItem("Logout");
        m.addActionListener(a -> getController().utenteRichiedeLogout());
        return m;
    }

    private JMenuItem targetAttuale() {
        JMenuItem m = new JMenuItem("Visualizza target valori nutrizionali");
        m.addActionListener(a -> getView().freshPane(
                cp -> {
                    var p = new JPanel(new BorderLayout());
                    var target = getController().utenteRichiedeTarget();
                    p.add(Utils.keyValueTable(
                            List.of(
                                    Pair.of("Kcal",
                                            String.valueOf(target.isPresent() ? target.get().kcal() : "non impostato")),
                                    Pair.of("Proteine",
                                            (target.isPresent() && target.get().percentualeProteine().isPresent())
                                                    ? target.get().percentualeProteine().get().toString()
                                                    : "non impostato"),
                                    Pair.of("Grassi",
                                            (target.isPresent() && target.get().percentualeGrassi().isPresent())
                                                    ? target.get().percentualeGrassi().get().toString()
                                                    : "non impostato"),
                                    Pair.of("Carboidrati",
                                            (target.isPresent() && target.get().percentualeCarboidrati().isPresent())
                                                    ? target.get().percentualeCarboidrati().get().toString()
                                                    : "non impostato"))),
                            BorderLayout.CENTER);
                    cp.add(p);
                }));
        return m;
    }

    private JMenuItem impostaTarget() {
        JMenuItem m = new JMenuItem("Imposta target di valori nutrizionali");
        m.addActionListener(a -> getView().freshPane(
                cp -> {
                    cp.add(Utils.genericQuery(
                            List.of("Kcal", "Percentuale proteine", "Percentuale grassi", "Percentuale carboidrati"),
                            "Imposta",
                            l -> {
                                List<Optional<Integer>> values = l.stream().map(s -> {
                                    try {
                                        return Optional.of(Integer.parseInt(s));
                                    } catch (NumberFormatException e) {
                                        Optional<Integer> opt = Optional.empty();
                                        return opt;
                                    }
                                }).toList();
                                if(values.get(0).isEmpty() && values.subList(1, values.size()).stream().filter(v -> v.isPresent()).findAny().isPresent()){
                                    getView().displayErrorMessage("Le Kcal sono obbligatorie.");
                                }else{
                                    getController().utenteImpostaTarget(
                                        values.get(0).isPresent() ?
                                        Optional.of(new Target(values.get(0).get(), 
                                                    values.get(1),
                                                    values.get(2),
                                                    values.get(3)
                                                )) : Optional.empty()
                                    );
                                }
                            }));
                }));
        return m;
    }

    private JMenuItem obbiettivoAttuale() {
        JMenuItem m = new JMenuItem("Obbiettivo attuale");
        m.addActionListener(a -> getView().freshPane(
                cp -> {
                    var p = new JPanel(new BorderLayout());
                    p.add(Utils.keyValueTable(List.of(
                            Pair.of("Obbiettivo di peso corporeo attuale",
                                    Utils.descrizioneOptional(getController().utenteRichiedeObbiettivo())))),
                            BorderLayout.CENTER);
                    cp.add(p);
                }));
        return m;
    }

    private JMenuItem impostaObbiettivo() {
        JMenuItem m = new JMenuItem("Imposta obbiettivo di peso corporeo");
        m.addActionListener(a -> getView().freshPane(
                cp -> {
                    cp.add(Utils.genericQuery(
                            List.of("Obbiettivo"),
                            "Imposta",
                            l -> {
                                getController().utenteImpostaObbiettivo(l.stream().map(s -> {
                                    try {
                                        return Optional.of(Integer.parseInt(s));
                                    } catch (NumberFormatException e) {
                                        Optional<Integer> opt = Optional.empty();
                                        return opt;
                                    }
                                }).findAny().get());
                            }));
                }));
        return m;
    }
}