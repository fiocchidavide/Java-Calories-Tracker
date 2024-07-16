package it.unibo.application.view;

import javax.swing.JMenuItem;
import it.unibo.application.controller.Controller;

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
        m.addActionListener(a -> getController().utenteRichiedeTarget());
        return m;
    }

    private JMenuItem impostaTarget() {
        JMenuItem m = new JMenuItem("Imposta target di valori nutrizionali");
        m.addActionListener(a -> getView().richiediTarget());
        return m;
    }

    private JMenuItem obbiettivoAttuale() {
        JMenuItem m = new JMenuItem("Obbiettivo attuale");
        m.addActionListener(a -> getController().utenteRichiedeObbiettivo());
        return m;
    }

    private JMenuItem impostaObbiettivo() {
        JMenuItem m = new JMenuItem("Imposta obbiettivo di peso corporeo");
        m.addActionListener(a -> getView().richiediObbiettivo());
        return m;
    }
}