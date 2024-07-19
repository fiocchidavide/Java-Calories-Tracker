package it.unibo.application.view;

import javax.swing.JMenuItem;

import it.unibo.application.controller.Controller;

public class MenuStatistiche extends MenuElement {

    public MenuStatistiche(final View view, Controller controller){
        super("Statistiche", view, controller);
        this.add(valoriGiorno());
        this.add(valoriMedia());
        this.add(variazionePeso());
        this.add(stimaTdee());
    }

    private JMenuItem valoriGiorno() {
        JMenuItem m = new JMenuItem("Nutrienti assunti");
        m.addActionListener(a -> getController().utenteVuoleVisualizzareValoriGiorno());
        return m;
    }

    private JMenuItem valoriMedia() {
        JMenuItem m = new JMenuItem("Nutrienti assunti medi");
        m.addActionListener(a -> getController().utenteVuoleVisualizzareValoriMedi());
        return m;
    }

    private JMenuItem variazionePeso() {
        JMenuItem m = new JMenuItem("Variazione peso corporeo");
        m.addActionListener(a -> getController().utenteVuoleVisualizzareDifferenzaDiPeso());
        return m;
    }

    private JMenuItem stimaTdee() {
        JMenuItem m = new JMenuItem("Stima TDEE");
        m.addActionListener(a -> getController().utenteVuoleVisualizzareTdee());
        return m;
    }
}