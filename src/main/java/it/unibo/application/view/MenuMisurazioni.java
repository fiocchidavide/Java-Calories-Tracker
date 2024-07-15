package it.unibo.application.view;

import javax.swing.JMenuItem;

import it.unibo.application.controller.Controller;

public class MenuMisurazioni extends MenuElement {
    public MenuMisurazioni(final View view, Controller controller){
        super("Misurazioni", view, controller);
        this.add(aggiungiMisurazione()); //M1
        this.add(visualizzaMisurazioni()); //M2,3,4
    }

    private JMenuItem aggiungiMisurazione() {
        JMenuItem m = new JMenuItem("Aggiungi misurazione");
        m.addActionListener(a -> {});
        return m;
    }

    private JMenuItem visualizzaMisurazioni() {
        JMenuItem m = new JMenuItem("Visualizza misurazioni");
        m.addActionListener(a -> {});
        return m;
    }
}
