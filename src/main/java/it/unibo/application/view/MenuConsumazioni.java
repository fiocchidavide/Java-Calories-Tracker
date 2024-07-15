package it.unibo.application.view;

import javax.swing.JMenuItem;

import it.unibo.application.controller.Controller;

public class MenuConsumazioni extends MenuElement {

    public MenuConsumazioni(final View view, Controller controller){
        super("Consumazioni", view, controller);
        this.add(aggiungiConsumazione());
        this.add(visualizzaConsumazioni());
    }

    private JMenuItem aggiungiConsumazione() {
        JMenuItem m = new JMenuItem("Aggiungi consumazione");
        m.addActionListener(a -> {});
        return m;
    }

    private JMenuItem visualizzaConsumazioni() {
        JMenuItem m = new JMenuItem("Visualizza consumazioni");
        m.addActionListener(a -> {});
        return m;
    }
}
