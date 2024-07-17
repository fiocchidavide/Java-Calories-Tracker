package it.unibo.application.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Dialog.ModalityType;
import java.util.HashSet;
import java.util.List;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;

import it.unibo.application.App;
import it.unibo.application.commons.Utilities;
import it.unibo.application.controller.Controller;
import it.unibo.application.dto.Alimento;
import it.unibo.application.dto.ValoriAlimento;
import it.unibo.application.dto.Misurazione;
import it.unibo.application.dto.Tag;
import it.unibo.application.dto.Target;

public final class View {

    public final static int BORDER_WIDTH = 10;
    public final static int TEXTFIELD_MIN_WIDTH = 10;

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
            cp.add(loginPrompt, BorderLayout.CENTER);
            var buttons = new JPanel(new GridLayout(1, 2));
            buttons.add(Utils.button("Login",
                    () -> getController().utenteRichiedeAutenticazione(usernameField.getText(),
                            passwordField.getPassword())));
            buttons.add(Utils.button("Registrazione",
                    () -> getController().utenteRichiedeRegistrazione(usernameField.getText(),
                            passwordField.getPassword())));
            cp.add(buttons, BorderLayout.SOUTH);
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
        displayErrorMessage(message, () -> {
        });
    }

    public void displayErrorMessage(Object message, Runnable onClose) {
        JOptionPane.showMessageDialog(mainFrame, message, "", JOptionPane.ERROR_MESSAGE);
        onClose.run();
    }

    public void displayMessage(Object message) {
        displayMessage(message, () -> {
        });
    }

    public void displayMessage(Object message, Runnable onClose) {
        JOptionPane.showMessageDialog(mainFrame, message, "", JOptionPane.INFORMATION_MESSAGE);
        onClose.run();
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

    public void visualizzaMisurazioni(List<Misurazione> misurazioni) {
        var columns = List.of("Data", "Peso");
        freshPane(cp -> cp.add(Utils.objectsTable(misurazioni,
                columns,
                m -> Utilities.mapFromLists(columns, Utilities.stringList(m.data(), m.peso())),
                this::dettaglioMisurazione)));
    }

    public void richiediMisurazione() {
        freshPane(cp -> {
            cp.add(Utils.genericQuery(List.of("Data [YYYY-MM-DD]", "Peso"), "Inserisci",
                    l -> {
                        try {
                            getController().utenteAggiungeMisurazione(LocalDate.parse(l.get(0)),
                                    new BigDecimal(l.get(1)));
                        } catch (DateTimeParseException | NumberFormatException e) {
                            displayErrorMessage(e.getMessage(), () -> {
                            });
                        }
                    }));
        });
    }

    public void dettaglioMisurazione(Misurazione m) {
        freshPane(cp -> {
            cp.add(new JLabel("Misurazione del " + m.data()), BorderLayout.NORTH);
            var peso = new JTextField(m.peso().toString());
            cp.add(peso, BorderLayout.CENTER);
            var pulsanti = new JPanel(new GridLayout(1, 2));
            var modifica = Utils.button("Modifica", () -> {
                try {
                    getController().utenteModificaMisurazione(m, new BigDecimal(peso.getText()));
                } catch (NumberFormatException e) {
                    displayErrorMessage("Inserisci un peso valido.", () -> {
                    });
                }
            });
            var elimina = Utils.button("Elimina", () -> getController().utenteEliminaMisurazione(m));
            pulsanti.add(modifica);
            pulsanti.add(elimina);
            cp.add(pulsanti, BorderLayout.SOUTH);
        });
    }

    public void visualizzaObbiettivo(Optional<Integer> obbiettivo) {
        freshPane(cp -> {
            cp.add(new JLabel("Obbiettivo di peso corporeo: " + Utils.descrizioneOptional(obbiettivo)));
        });
    }

    public void richiediObbiettivo() {
        freshPane(cp -> {
            cp.add(Utils.genericQuery(
                    List.of("Obbiettivo"),
                    "Imposta",
                    l -> {
                        getController().utenteImpostaObbiettivo(l.stream().map(s -> {
                            try {
                                return Optional.of(Integer.parseInt(s));
                            } catch (NumberFormatException e) {
                                Optional<Integer> opt = Optional.empty();
                                return opt;
                            }
                        }).findAny().get());
                    }));
        });
    }

    public void visualizzaTarget(Optional<Target> target) {
        var columns = List.of("Kcal", "Proteine", "Grassi", "Carboidrati");
        freshPane(cp -> cp.add(Utils.objectsTable(target.isPresent() ? List.of(target.get()) : List.of(),
                columns,
                t -> Utilities.mapFromLists(columns,
                        Utilities.stringList(target.get().kcal(),
                                Utils.descrizioneOptional(target.get().percentualeProteine()),
                                Utils.descrizioneOptional(target.get().percentualeGrassi()),
                                Utils.descrizioneOptional(target.get().percentualeCarboidrati()))),
                t -> richiediTarget())));
    }

    public void richiediTarget() {
        freshPane(cp -> {
            cp.add(Utils.genericQuery(
                    List.of("Kcal", "Percentuale proteine", "Percentuale grassi", "Percentuale carboidrati"),
                    "Imposta",
                    l -> {
                        List<Optional<Integer>> values = l.stream().map(s -> {
                            try {
                                return Optional.of(Integer.parseInt(s));
                            } catch (NumberFormatException e) {
                                Optional<Integer> opt = Optional.empty();
                                return opt;
                            }
                        }).toList();
                        if (values.get(0).isEmpty() && values.subList(1, values.size()).stream()
                                .filter(v -> v.isPresent()).findAny().isPresent()) {
                            displayErrorMessage("Le Kcal sono obbligatorie.", () -> {
                            });
                        } else {
                            getController().utenteImpostaTarget(
                                    values.get(0).isPresent() ? Optional.of(new Target(values.get(0).get(),
                                            values.get(1),
                                            values.get(2),
                                            values.get(3))) : Optional.empty());
                        }
                    }));
        });
    }

    public void visualizzaTag(List<Tag> tag) {
        freshPane(cp -> cp.add(Utils.objectsList(tag, t -> t.parolaChiave(), t -> {
        })));
    }

    public void richiediTag() {
        freshPane(cp -> {
            cp.add(Utils.genericQuery(List.of("Parola chiave"), "Aggiungi", l -> {
                getController().utenteAggiungeTag(l.get(0));
            }));
        });
    }

    public void visualizzaTagModificabili(List<Tag> tag) {
        freshPane(cp -> cp.add(Utils.objectsList(tag, t -> t.parolaChiave(), this::dettaglioTag)));
    }

    public void dettaglioTag(Tag tag) {
        freshPane(cp -> {
            cp.add(new JLabel(tag.parolaChiave()), BorderLayout.CENTER);
            cp.add(Utils.button("Elimina", () -> getController().utenteEliminaTag(tag)), BorderLayout.SOUTH);
        });
    }

    public void richiediValoriAlimento(Consumer<ValoriAlimento> accettore) {
        freshPane(cp -> {
            inserisiValoriAlimento(accettore);
        });
    }

    public void dettaglioAlimento(Alimento a, boolean modificabile) {
        System.out.println("Alimento " + a.codAlimento());
    }

    public void richiediRicerca(List<Tag> tag, Consumer<Pair<Optional<String>, Set<Tag>>> accettore) {
        freshPane(cp -> cp.add(ricercaAlimento(tag, accettore)));
    }

    public void schermataSelezioneAlimento(List<Alimento> a, String buttonLabel, Consumer<Alimento> onSelection) {
        freshPane(cp -> cp.add(selezionaAlimentoSingolo(a, buttonLabel, onSelection)));
    }

    public void elencaAlimenti(List<Alimento> a) {
        freshPane(cp -> cp.add(elencoAlimenti(a, alimento -> {
        })));
    }

    private JComponent selezionaAlimentoSingolo(List<Alimento> a, String buttonLabel, Consumer<Alimento> onSelection) {
        var columns = List.of("Nome", "Kcal", "Proteine", "Grassi", "Carboidrati", "Porzione", "Brand");
        return Utils.singleObjectSelector(a,
                columns,
                t -> Utilities.mapFromLists(columns, Utilities.stringList(
                        t.nome(),
                        t.kcal(),
                        t.proteine(),
                        t.grassi(),
                        t.carboidrati(),
                        Utils.descrizioneOptional(t.porzione()),
                        Utils.descrizioneOptional(t.brand()))),
                "Nome",
                onSelection,
                buttonLabel);
    }

    private JComponent selezionaAlimentiMultipli(List<Alimento> a, String buttonLabel,
            Consumer<List<Alimento>> onSelection) {
        var columns = List.of("Nome", "Kcal", "Proteine", "Grassi", "Carboidrati", "Porzione", "Brand");
        return Utils.multipleObjectSelector(a,
                columns,
                t -> Utilities.mapFromLists(columns, Utilities.stringList(
                        t.nome(),
                        t.kcal(),
                        t.proteine(),
                        t.grassi(),
                        t.carboidrati(),
                        Utils.descrizioneOptional(t.porzione()),
                        Utils.descrizioneOptional(t.brand()))),
                "Nome",
                onSelection,
                buttonLabel);
    }

    private JComponent elencoAlimenti(List<Alimento> a, Consumer<Alimento> onDoubleClick) {
        var columns = List.of("Nome", "Kcal", "Proteine", "Grassi", "Carboidrati", "Porzione", "Brand");
        return Utils.objectsTable(a,
                columns,
                t -> Utilities.mapFromLists(columns, Utilities.stringList(
                        t.nome(),
                        t.kcal(),
                        t.proteine(),
                        t.grassi(),
                        t.carboidrati(),
                        Utils.descrizioneOptional(t.porzione()),
                        Utils.descrizioneOptional(t.brand()))),
                onDoubleClick);
    }

    private void freshPane(Consumer<Container> consumer) {
        var cp = this.mainFrame.getContentPane();
        cp.removeAll();
        consumer.accept(cp);
        cp.validate();
        cp.repaint();
        this.mainFrame.pack();
    }

    private JComponent selezionaTagMultipli(List<Tag> tags, String buttonLabel, Consumer<List<Tag>> onSelection) {
        var columns = List.of("Parola chiave");
        return Utils.multipleObjectSelector(tags,
                columns,
                t -> Utilities.mapFromLists(columns, Utilities.stringList(t.parolaChiave())),
                "Parola chiave",
                onSelection,
                buttonLabel);
    }

    private JComponent ricercaAlimento(List<Tag> tag, Consumer<Pair<Optional<String>, Set<Tag>>> accettore) {

        JPanel panel = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Nome dell'alimento: "));
        JTextField search = new JTextField(TEXTFIELD_MIN_WIDTH);
        searchPanel.add(search);

        panel.add(searchPanel, BorderLayout.CENTER);

        panel.add(selezionaTagMultipli(tag, "Cerca alimenti",
                t -> accettore.accept(
                        Pair.of(Utilities.notBlank(search.getText()), new HashSet<>(t)))),
                BorderLayout.SOUTH);

        return panel;
    }

    private JComponent inserisiValoriAlimento(Consumer<ValoriAlimento> accettore) {
        var panel = new JPanel(new BorderLayout());
        var checkbox = new JCheckBox("Aggiungi privatamente");
        checkbox.setSelected(false);
        panel.add(checkbox, BorderLayout.NORTH);
        panel.add(Utils.genericQuery(List.of("Nome", "Kcal", "Carboidrati", "Grassi", "Proteine", "Porzione", "Brand"),
                "Aggiungi", l -> {
                    try {
                        accettore.accept(
                                new ValoriAlimento(l.get(0),
                                        Integer.parseInt(l.get(1)),
                                        Integer.parseInt(l.get(2)),
                                        Integer.parseInt(l.get(3)),
                                        Integer.parseInt(l.get(4)),
                                        Utilities.parseOptionalInt(l.get(5)),
                                        Utilities.notBlank(l.get(6)),
                                        checkbox.isSelected()));
                    } catch (NumberFormatException e) {
                        displayErrorMessage("Inserisci dei valori nutrizionali corretti.", () -> {
                        });
                    }
                }), BorderLayout.CENTER);
        return panel;
    }
}
