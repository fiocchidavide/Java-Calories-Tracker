package it.unibo.application.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.List;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;

import it.unibo.application.App;
import it.unibo.application.controller.Controller;

public final class View {

    public final static int BORDER_WIDTH = 10;

    private Optional<Controller> controller;
    private final JFrame mainFrame;

    public View(Runnable onClose) {
        this.controller = Optional.empty();
        this.mainFrame = this.setupMainFrame(onClose);
    }

    private Border defaultBorder() {
        return BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH);
    }

    private JFrame setupMainFrame(Runnable onClose) {
        var frame = new JFrame(App.TITLE);
        var contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(defaultBorder());
        frame.setContentPane(contentPane);
        frame.setMinimumSize(new Dimension((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 4,
                (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 4));
        frame.pack();

        frame.setVisible(true);
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        onClose.run();
                        System.exit(0);
                    }
                });

        return frame;
    }

    private Controller getController() {
        if (this.controller.isPresent()) {
            return this.controller.get();
        } else {
            throw new IllegalStateException("Controller has not been set");
        }
    }

    public void setController(Controller controller) {
        Objects.requireNonNull(controller, "Set null controller in view");
        this.controller = Optional.of(controller);
        mainFrame.setJMenuBar(buildMenu());
    }

    public void richiediLogin() {
        mainFrame.getJMenuBar().setVisible(false);
        freshPane(cp -> {
            var loginPrompt = new JPanel(new GridLayout(4, 1));
            var usernameField = new JTextField();
            var passwordField = new JPasswordField();
            loginPrompt.add(new JLabel("Username:"));
            loginPrompt.add(usernameField);
            loginPrompt.add(new JLabel("Password:"));
            loginPrompt.add(passwordField);
            loginPrompt.setBorder(defaultBorder());
            cp.add(loginPrompt, BorderLayout.CENTER);
            cp.add(Utils.button("Login",
                    () -> getController().utenteRichiedeAutenticazione(usernameField.getText(),
                            passwordField.getPassword())),
                    BorderLayout.SOUTH);
        });
    }

    private JMenuBar buildMenu() {
        JMenuBar menuBar = new JMenuBar();
        List.of(MenuImpostazioni.class, MenuAlimenti.class, MenuPreferiti.class,
                MenuConsumazioni.class, MenuTag.class,
                MenuMisurazioni.class, MenuStatistiche.class).forEach(
                        menu -> {
                            try {
                                menuBar.add(menu.getConstructor(View.class, Controller.class).newInstance(this,
                                        getController()));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
        return menuBar;
    }

    public void displayErrorMessage(Object message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    public void visualizzaMenuPrincipale() {
        this.mainFrame.getJMenuBar().setVisible(true);
        this.freshPane(cp -> {
            var p = new JPanel(new FlowLayout());
            p.add(new JLabel("<html>Ciao, " + getController().utenteAttuale()
                    + "!<br/>Scegli una voce del menù per inziare.</html>"));
            cp.add(p);
        });
    }

    void freshPane(Consumer<Container> consumer) {
        var cp = this.mainFrame.getContentPane();
        cp.removeAll();
        consumer.accept(cp);
        cp.validate();
        cp.repaint();
        this.mainFrame.pack();
    }
}
