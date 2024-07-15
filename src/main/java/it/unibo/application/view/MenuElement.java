package it.unibo.application.view;

import javax.swing.JMenu;

import it.unibo.application.controller.Controller;

public class MenuElement extends JMenu {

    private final View view;
    private final Controller controller;

    protected MenuElement(final String label, final View view, final Controller controller) {
        super(label);
        this.view = view;
        this.controller = controller;
    }

    protected View getView() {
        return view;
    }
    
    protected Controller getController() {
        return controller;
    }
}
