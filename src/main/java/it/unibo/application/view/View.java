package it.unibo.application.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;

import it.unibo.application.App;
import it.unibo.application.commons.Utilities;
import it.unibo.application.controller.Controller;
import it.unibo.application.dto.Cibo;
import it.unibo.application.dto.Misurazione;
import it.unibo.application.dto.Tag;
import it.unibo.application.dto.Target;

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
        JOptionPane.showMessageDialog(mainFrame, message, "", JOptionPane.ERROR_MESSAGE);
    }

    public void displayMessage(Object message) {
        JOptionPane.showMessageDialog(mainFrame, message, "", JOptionPane.INFORMATION_MESSAGE);
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

    private void freshPane(Consumer<Container> consumer) {
        var cp = this.mainFrame.getContentPane();
        cp.removeAll();
        consumer.accept(cp);
        cp.validate();
        cp.repaint();
        this.mainFrame.pack();
    }

    public void visualizzaMisurazioni(List<Misurazione> misurazioni) {
        freshPane(cp -> {
            var table = new JTable(new AbstractTableModel() {
                @Override
                public String getColumnName(int column) {
                    if (column == 0) {
                        return "Data";
                    } else {
                        return "Peso";
                    }
                }

                @Override
                public int getRowCount() {
                    return misurazioni.size();
                }

                @Override
                public int getColumnCount() {
                    return 2;
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    return columnIndex == 0 ? misurazioni.get(rowIndex).data()
                            : misurazioni.get(rowIndex).peso();
                }
            });
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && table.getSelectedRow() >= 0
                            && table.getSelectedRow() < misurazioni.size()) {
                        dettaglioMisurazione(misurazioni.get(table.getSelectedRow()));
                    }
                }
            });
            cp.add(new JScrollPane(table));
        });
    }

    public void richiediMisurazione() {
        freshPane(cp -> {
            cp.add(Utils.genericQuery(List.of("Data [YYYY-MM-DD]", "Peso"), "Inserisci",
                    l -> {
                        try {
                            getController().utenteAggiungeMisurazione(LocalDate.parse(l.get(0)),
                                    new BigDecimal(l.get(1)));
                        } catch (DateTimeParseException | NumberFormatException e) {
                            displayErrorMessage(e.getMessage());
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
                    displayErrorMessage("Inserisci un peso valido.");
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
        freshPane(cp -> {
            cp.add(new JScrollPane(new JTable(new AbstractTableModel() {
                @Override
                public String getColumnName(int columnIndex) {
                    switch (columnIndex) {
                        case 0:
                            return "Kcal";
                        case 1:
                            return "Proteine";
                        case 2:
                            return "Grassi";
                        case 3:
                            return "Carboidrati";
                        default:
                            return null;
                    }
                }

                @Override
                public int getRowCount() {
                    return 1;
                }

                @Override
                public int getColumnCount() {
                    return 4;
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    if (target.isPresent()) {
                        switch (columnIndex) {
                            case 0:
                                return target.get().kcal();
                            case 1:
                                return Utils.descrizioneOptional(target.get().percentualeProteine());
                            case 2:
                                return Utils.descrizioneOptional(target.get().percentualeGrassi());
                            case 3:
                                return Utils.descrizioneOptional(target.get().percentualeCarboidrati());
                            default:
                                return null;
                        }
                    } else {
                        return "non impostato";
                    }
                }

            })));
        });
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
                            displayErrorMessage("Le Kcal sono obbligatorie.");
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
        freshPane(cp -> cp.add(new JScrollPane(new JList<>(new AbstractListModel<>() {
            @Override
            public int getSize() {
                return tag.size();
            }

            @Override
            public String getElementAt(int index) {
                return tag.get(index).parolaChiave();
            }
        }))));
    }

    public void richiediTag() {
        freshPane(cp -> {
            cp.add(Utils.genericQuery(List.of("Parola chiave"), "Aggiungi", l -> {
                getController().utenteAggiungeTag(l.get(0));
            }));
        });
    }

    public void visualizzaTagModificabili(List<Tag> tag) {
        freshPane(cp -> {
            JList<String> list = new JList<>(new AbstractListModel<>() {
                @Override
                public int getSize() {
                    return tag.size();
                }

                @Override
                public String getElementAt(int index) {
                    return tag.get(index).parolaChiave();
                }
            });
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && list.getSelectedIndex() >= 0) {
                        dettaglioTag(tag.get(list.getSelectedIndex()));
                    }
                }
            });
            cp.add(new JScrollPane(list));
        });
    }

    public void dettaglioTag(Tag tag) {
        freshPane(cp -> {
            cp.add(new JLabel(tag.parolaChiave()), BorderLayout.CENTER);
            cp.add(Utils.button("Elimina", () -> getController().utenteEliminaTag(tag)), BorderLayout.SOUTH);
        });
    }

    public void richiediCibo() {
        freshPane(cp -> {
            var checkbox = new JCheckBox("Aggiungi privatamente");
            checkbox.setSelected(false);
            cp.add(checkbox, BorderLayout.NORTH);
            cp.add(Utils.genericQuery(List.of("Nome", "Kcal", "Carboidrati", "Grassi", "Proteine", "Porzione", "Brand"),
                    "Aggiungi", l -> {
                        try{
                            getController().utenteAggiungeCibo(
                                new Cibo(l.get(0),
                                        Integer.parseInt(l.get(1)),
                                        Integer.parseInt(l.get(2)),
                                        Integer.parseInt(l.get(3)),
                                        Integer.parseInt(l.get(4)),
                                        Utilities.parseOptionalInt(l.get(5)),
                                        Utilities.notBlank(l.get(6)),
                                        checkbox.isSelected()));
                        } catch (NumberFormatException e) {
                            displayErrorMessage("Inserisci dei valori nutrizionali corretti.");
                        }
                    }), BorderLayout.CENTER);
        });
    }

    public void visualizzaAlimentiUtente() {

    }
}
