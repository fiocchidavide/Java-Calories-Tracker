package it.unibo.application.view;

import javax.swing.JMenuItem;

import it.unibo.application.controller.Controller;

public class MenuAlimenti extends MenuElement {

    public MenuAlimenti(final View view, Controller controller) {
        super("Alimenti", view, controller);
        this.add(aggiungiCibo());
        this.add(creaRicetta());
        this.add(alimentiUtente());
        this.add(elencoAlimenti());
    }

    private JMenuItem aggiungiCibo() {
        JMenuItem m = new JMenuItem("Aggiungi cibo");
        m.addActionListener(a -> getController().utenteVuoleAggiungereCibo());
        return m;
    }

    private JMenuItem alimentiUtente() {
        JMenuItem m = new JMenuItem("I tuoi alimenti");
        m.addActionListener(a -> getController().utenteRichiedeSuoiAlimenti());
        return m;
    }

    private JMenuItem creaRicetta() {
        JMenuItem m = new JMenuItem("Crea ricetta");
        m.addActionListener(a -> getController().utenteVuoleAggiungereRicetta());
        return m;
    }

    private JMenuItem elencoAlimenti() {
        JMenuItem m = new JMenuItem("Elenco alimenti");
        m.addActionListener(a -> getController().utenteVuoleCercareAlimenti());
        return m;
    }
}
