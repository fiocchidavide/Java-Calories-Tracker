package it.unibo.application.view;

import javax.swing.JMenuItem;

import it.unibo.application.controller.Controller;

public class MenuAlimenti extends MenuElement {

    public MenuAlimenti(final View view, Controller controller) {
        super("Alimenti", view, controller);
        this.add(aggiungiCibo());
        this.add(cibiUtente());
        this.add(creaRicetta());
        this.add(ricetteUtente());
        this.add(elencoAlimenti());
    }

    private JMenuItem aggiungiCibo() {
        JMenuItem m = new JMenuItem("Aggiungi cibo");
        m.addActionListener(a -> getView().richiediCibo());
        return m;
    }

    private JMenuItem cibiUtente() {
        JMenuItem m = new JMenuItem("I tuoi cibi");
        m.addActionListener(a -> {
        });
        return m;
    }

    private JMenuItem creaRicetta() {
        JMenuItem m = new JMenuItem("Crea ricetta");
        m.addActionListener(a -> {
        });
        return m;
    }

    private JMenuItem ricetteUtente() {
        JMenuItem m = new JMenuItem("Le tue ricette");
        m.addActionListener(a -> {
        });
        return m;
    }

    private JMenuItem elencoAlimenti() {
        JMenuItem m = new JMenuItem("Elenco alimenti");
        m.addActionListener(a -> {
        });
        return m;
    }
}
