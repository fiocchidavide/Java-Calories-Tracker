package it.unibo.application.view;

import javax.swing.JMenuItem;

import it.unibo.application.controller.Controller;

public class MenuTag extends MenuElement {

    public MenuTag(final View view, Controller controller){
        super("Tag", view, controller);
        this.add(aggiungiTag());
        this.add(visualizzaTag());
        this.add(tagUtente());
    }

    private JMenuItem aggiungiTag() {
        JMenuItem m = new JMenuItem("Aggiungi tag");
        m.addActionListener(a -> {});
        return m;
    }

    private JMenuItem visualizzaTag() {
        JMenuItem m = new JMenuItem("Visualizza tag");
        m.addActionListener(a -> {});
        return m;
    }

    private JMenuItem tagUtente() {
        JMenuItem m = new JMenuItem("I tuoi tag");
        m.addActionListener(a -> {});
        return m;
    }
}