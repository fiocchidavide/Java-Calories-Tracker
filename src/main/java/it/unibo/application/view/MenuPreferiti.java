package it.unibo.application.view;
import javax.swing.JMenuItem;

import it.unibo.application.controller.Controller;

public class MenuPreferiti extends MenuElement {

    public MenuPreferiti(final View view, Controller controller){
        super("Preferiti", view, controller);
        this.add(aggiungiPreferito()); //P1
        this.add(visualizzaPreferiti()); //P2,3
    }

    private JMenuItem aggiungiPreferito() {
        JMenuItem m = new JMenuItem("Aggiungi preferito");
        m.addActionListener(a -> {});
        return m;
    }

    private JMenuItem visualizzaPreferiti() {
        JMenuItem m = new JMenuItem("Visualizza preferiti");
        m.addActionListener(a -> {});
        return m;
    }
}