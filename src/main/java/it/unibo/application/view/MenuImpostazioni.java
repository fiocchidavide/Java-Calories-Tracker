package it.unibo.application.view;

import javax.swing.JMenuItem;

import it.unibo.application.controller.Controller;

public class MenuImpostazioni extends MenuElement {

    public MenuImpostazioni(final View view, Controller controller){
        super("Impostazioni", view, controller);
        this.add(menuPrincipale());
        this.add(logout());
    }

    JMenuItem menuPrincipale(){
        var m = new JMenuItem("Menu principale");
        m.addActionListener(a -> getView().visualizzaMenuPrincipale());
        return m;
    }

    JMenuItem logout() {
        var m = new JMenuItem("Logout");
        m.addActionListener(a -> getController().utenteRichiedeLogout());
        return m;
    }
}