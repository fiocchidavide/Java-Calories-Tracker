package it.unibo.application.view;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import it.unibo.application.App;
import it.unibo.application.controller.ControllerImpl;

public class View {

    private final ControllerImpl controller;
    public JFrame mainJFrame;
    public JMenuBar menu;
    public JPanel content;

    public View(final ControllerImpl controller){
        this.controller = controller;
        this.mainJFrame = new JFrame(App.TITLE);
        mainJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.content = new JPanel();
        this.mainJFrame.setContentPane(content);

        content.add(new JButton("Try me"));
    }

    public void show(){
        this.mainJFrame.pack();
        this.mainJFrame.setVisible(true);
    }
}
