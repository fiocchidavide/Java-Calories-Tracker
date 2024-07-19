package it.unibo.application.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.apache.commons.lang3.tuple.Pair;

import it.unibo.application.App;
import it.unibo.application.commons.Utilities;
import it.unibo.application.controller.Controller;
import it.unibo.application.dto.Alimento;
import it.unibo.application.dto.Consumazione;
import it.unibo.application.dto.Misurazione;
import it.unibo.application.dto.Tag;
import it.unibo.application.dto.Target;
import it.unibo.application.dto.Valori;
import it.unibo.application.dto.ValoriCibo;
import it.unibo.application.dto.ValoriRicetta;

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
            buttons.add(Components.button("Login",
                    () -> getController().utenteRichiedeAutenticazione(usernameField.getText(),
                            passwordField.getPassword())));
            buttons.add(Components.button("Registrazione",
                    () -> getController().utenteRichiedeRegistrazione(usernameField.getText(),
                            passwordField.getPassword())));
            buttons.setBorder(defaultBorder());
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
            p.add(new JLabel("Scegli una voce del menù per inziare."));
            cp.add(p);
        });
    }

    public void visualizzaMisurazioni(List<Misurazione> misurazioni) {
        var columns = List.of("Data", "Peso");
        freshPane(cp -> cp.add(Components.clickableObjectsTable(misurazioni,
                columns,
                "Data",
                m -> Utilities.mapFromLists(columns, Utilities.stringList(m.data(), m.peso())),
                this::dettaglioMisurazione)));
    }

    public void richiediMisurazione() {
        freshPane(cp -> {
            cp.add(Components.genericQuery(List.of("Data [YYYY-MM-DD]", "Peso"), "Inserisci",
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
            var modifica = Components.button("Modifica", () -> {
                try {
                    getController().utenteModificaMisurazione(m, new BigDecimal(peso.getText()));
                } catch (NumberFormatException e) {
                    displayErrorMessage("Inserisci un peso valido.", () -> {
                    });
                }
            });
            var elimina = Components.button("Elimina", () -> getController().utenteEliminaMisurazione(m));
            pulsanti.add(modifica);
            pulsanti.add(elimina);
            cp.add(pulsanti, BorderLayout.SOUTH);
        });
    }

    public void visualizzaBigDecimal(String label, Optional<BigDecimal> bigDecimal) {
        freshPane(cp -> cp.add(Components.keyValueTable(
                List.of(Pair.of(label, Components.descrizioneOptional(bigDecimal))))));
    }

    public void richiediObbiettivo() {
        freshPane(cp -> {
            cp.add(Components.genericQuery(
                    List.of("Obbiettivo"),
                    "Imposta",
                    l -> {
                        try {
                            getController().utenteImpostaObbiettivo(Optional.of(new BigDecimal(l.get(0))));
                        } catch (NumberFormatException e) {
                            displayErrorMessage("Inserisci un numero valido.");
                        }
                    }));
        });
    }

    public void richiediDate(List<String> labels, Consumer<List<LocalDate>> accettore) {
        freshPane(
                cp -> cp.add(Components.genericQuery(labels.stream().map(s -> new String(s + " [YYYY-MM-DD]")).toList(),
                        "Conferma date",
                        l -> {
                            try {
                                accettore.accept(l.stream().map(s -> LocalDate.parse(s)).toList());
                            } catch (DateTimeParseException e) {
                                displayErrorMessage("Controllare i dati inseriti.");
                            }
                        })));
    }

    public void visualizzaValori(String label, Valori valori) {
        freshPane(cp -> {
            cp.add(Components.keyValueTable(
                    List.of(
                            Utilities.stringPair("Kcal", valori.kcal()),
                            Utilities.stringPair("Proteine", valori.proteine()),
                            Utilities.stringPair("Grassi", valori.grassi()),
                            Utilities.stringPair("Carboidrati", valori.carboidrati()))),BorderLayout.CENTER);
            cp.add(new JLabel(label), BorderLayout.NORTH);
        });
    }

    public void visualizzaTarget(Optional<Target> target) {

        freshPane(cp -> cp.add(Components.keyValueTable(
                List.of(
                        Pair.of("Kcal", Components.descrizioneOptional(target.map(t -> t.kcal()))),
                        Pair.of("Percentuale proteine", Components.descrizioneOptional(
                                target.isPresent() ? target.get().percentualeProteine() : Optional.empty())),
                        Pair.of("Percentuale grassi", Components.descrizioneOptional(
                                target.isPresent() ? target.get().percentualeGrassi() : Optional.empty())),
                        Pair.of("Percentuale carboidrati", Components.descrizioneOptional(
                                target.isPresent() ? target.get().percentualeCarboidrati() : Optional.empty()))))));
    }

    public void richiediTarget() {
        freshPane(cp -> {
            cp.add(Components.genericQuery(
                    List.of("Kcal", "Percentuale proteine", "Percentuale grassi", "Percentuale carboidrati"),
                    "Imposta",
                    l -> {
                        try {
                            List<Optional<Integer>> values = l.stream()
                                    .map(s -> Optional.ofNullable(s.isBlank() ? null : Integer.parseInt(s))).toList();
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
                        } catch (NumberFormatException e) {
                            displayErrorMessage("I valori inseriti non sono validi.");
                        }
                    }));
        });
    }

    public void richiediTagSingolo(List<Tag> tags, List<Pair<String, Consumer<Optional<Tag>>>> buttons) {
        var columns = List.of("Parola chiave");
        freshPane(cp -> cp.add(Components.<Tag>singleObjectMultiButtonSelector(tags, columns,
                t -> Utilities.mapFromLists(columns, Utilities.stringList(t.parolaChiave())), "Parola chiave",
                buttons)));
    }

    public void richiediTag() {
        freshPane(cp -> {
            cp.add(Components.genericQuery(List.of("Parola chiave"), "Aggiungi", l -> {
                getController().utenteAggiungeTag(l.get(0));
            }));
        });
    }

    public void richiediValoriCibo(Consumer<ValoriCibo> accettore) {
        freshPane(cp -> cp.add(editorValoriCibo(Optional.empty(), accettore, "Aggiungi")));
    }

    public void richiediRicerca(List<Tag> tag, Consumer<Pair<Optional<String>, Set<Tag>>> accettore) {
        freshPane(cp -> cp.add(ricercaAlimento(tag, accettore)));
    }

    public void schermataSelezioneAlimento(List<Alimento> a, String buttonLabel, Consumer<Alimento> onSelection) {
        freshPane(cp -> cp.add(selezionaAlimentoSingolo(a, buttonLabel, onSelection)));
    }

    public void mostraDettaglio(Alimento a, boolean modificabile) {
        freshPane(cp -> cp.add(dettaglioAlimento(a, modificabile)));
    }

    public void visualizzaConsumazioni(List<Consumazione> consumazioni, Consumer<Consumazione> onDoubleClick) {
        var columns = List.of("Numero", "Data", "Ora", "CodAlimento", "Quantità");
        freshPane(cp -> cp.add(Components.clickableObjectsTable(consumazioni,
                columns,
                "Data",
                consumazione -> Utilities.mapFromLists(columns,
                        Utilities.stringList(consumazione.numero(), consumazione.data(), consumazione.ora(),
                                consumazione.codAlimento(), consumazione.quantità())),
                onDoubleClick)));
    }

    public void richiediConsumazione(int codAlimento, Consumer<Consumazione> accettore) {
        freshPane(cp -> {
            cp.add(editorConsumazione(Optional.empty(), codAlimento, accettore, "Aggiungi"));
        });
    }

    public void modificaConsumazione(Consumazione attuale, Consumer<Consumazione> accettore) {
        freshPane(
                cp -> cp.add(editorConsumazione(Optional.of(attuale), attuale.codAlimento(), accettore, "Modifica")));
    }

    public void visualizzaIngredienti(Map<Alimento, Integer> ingredientiAttuali, Optional<Runnable> aggiunta,
            Optional<Consumer<Alimento>> modifica, Optional<Consumer<Alimento>> eliminazione,
            Optional<Runnable> conferma) {
        freshPane(cp -> cp.add(editorIngredienti(ingredientiAttuali, aggiunta, modifica, eliminazione, conferma)));
    }

    public void richiediNumero(String prompt, String buttonLabel, Consumer<Integer> accettore) {
        freshPane(cp -> {
            cp.add(Components.genericQuery(List.of(prompt), buttonLabel,
                    l -> {
                        try {
                            accettore.accept(Integer.parseInt(l.get(0)));
                        } catch (NumberFormatException e) {
                            displayErrorMessage("Inserisci un numero valido.");
                        }
                    }));

        });
    }

    public void richiediValoriRicetta(Consumer<ValoriRicetta> accettore) {
        freshPane(cp -> cp.add(editorValoriRicetta(Optional.empty(), accettore, "Crea ricetta")));
    }

    public void richiediTagMultipli(List<Tag> tag, Optional<List<Boolean>> selezionati, String buttonLabel,
            Consumer<List<Tag>> accettore) {
        freshPane(cp -> cp.add(selezionaTagMultipli(tag, selezionati, buttonLabel, accettore)));
    }

    private JComponent selezionaAlimentoSingolo(List<Alimento> a, String buttonLabel, Consumer<Alimento> onSelection) {
        var columns = List.of("Nome", "Kcal", "Proteine", "Grassi", "Carboidrati", "Tipo", "Porzione", "Brand");
        return Components.<Alimento>singleObjectSelector(a,
                columns,
                t -> Utilities.mapFromLists(columns, Utilities.stringList(
                        t.nome(),
                        t.kcal(),
                        t.proteine(),
                        t.grassi(),
                        t.carboidrati(),
                        t.tipo() == 'R' ? "Ricetta" : "Cibo",
                        Components.descrizioneOptional(t.porzione()),
                        Components.descrizioneOptional(t.brand()))),
                "Nome",
                onSelection,
                buttonLabel);
    }

    private void freshPane(Consumer<Container> consumer) {
        var cp = this.mainFrame.getContentPane();
        cp.removeAll();
        consumer.accept(cp);
        cp.validate();
        cp.repaint();
        this.mainFrame.pack();
    }

    private JComponent selezionaTagMultipli(List<Tag> tags, Optional<List<Boolean>> selezionati, String buttonLabel,
            Consumer<List<Tag>> onSelection) {
        var columns = List.of("Tag");
        return Components.multipleObjectSelector(tags,
                selezionati,
                columns,
                t -> Utilities.mapFromLists(columns, Utilities.stringList(t.parolaChiave())),
                "Tag",
                onSelection,
                buttonLabel);
    }

    private JComponent ricercaAlimento(List<Tag> tag, Consumer<Pair<Optional<String>, Set<Tag>>> accettore) {

        JPanel panel = new JPanel(new BorderLayout());
        var title = new JLabel("Inserisci il nome e i tag per la ricerca.");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Nome dell'alimento: "));
        JTextField search = new JTextField(TEXTFIELD_MIN_WIDTH);
        searchPanel.add(search);

        panel.add(searchPanel, BorderLayout.CENTER);

        panel.add(selezionaTagMultipli(tag, Optional.empty(), "Cerca alimenti visibili con i tag e il nome selezionati",
                t -> accettore.accept(
                        Pair.of(Utilities.notBlank(search.getText()), new HashSet<>(t)))),
                BorderLayout.SOUTH);

        return panel;
    }

    private JComponent editorValoriCibo(Optional<Alimento> attuale, Consumer<ValoriCibo> accettore,
            String buttonLabel) {
        var panel = new JPanel(new BorderLayout());
        var checkbox = new JCheckBox("Privato");
        checkbox.setSelected(attuale.map(alimento -> alimento.privato()).orElse(false));
        panel.add(checkbox, BorderLayout.NORTH);
        panel.add(Components.genericQuery(
                List.of("Nome", "Kcal", "Proteine", "Grassi", "Carboidrati", "Porzione", "Brand"),
                List.of(attuale.map(a -> a.nome()), attuale.map(a -> String.valueOf(a.kcal())),
                        attuale.map(a -> String.valueOf(a.proteine())),
                        attuale.map(a -> String.valueOf(a.grassi())), attuale.map(a -> String.valueOf(a.carboidrati())),
                        attuale.map(a -> Components.descrizioneOptional(a.porzione())),
                        attuale.map(a -> Components.descrizioneOptional(a.brand()))),
                buttonLabel, l -> {
                    try {
                        accettore.accept(
                                new ValoriCibo(l.get(0),
                                        Integer.parseInt(l.get(1)),
                                        Integer.parseInt(l.get(2)),
                                        Integer.parseInt(l.get(3)),
                                        Integer.parseInt(l.get(4)),
                                        Utilities.parseOptionalStrictlyPositiveInt(l.get(5)),
                                        Utilities.notBlank(l.get(6)),
                                        checkbox.isSelected()));
                    } catch (NumberFormatException e) {
                        displayErrorMessage("Inserisci dei valori nutrizionali corretti.", () -> {
                        });
                    }
                }), BorderLayout.CENTER);
        return panel;
    }

    private JComponent editorIngredienti(Map<Alimento, Integer> ingredientiAttuali, Optional<Runnable> aggiunta,
            Optional<Consumer<Alimento>> modifica, Optional<Consumer<Alimento>> eliminazione,
            Optional<Runnable> conferma) {
        var columns = List.of("Codice cibo", "Nome cibo", "Quantità");
        var pulsanti = new ArrayList<Pair<String, Consumer<Optional<Entry<Alimento, Integer>>>>>();
        aggiunta.ifPresent(runnable -> pulsanti.add(Pair.of("Aggiungi", opt -> runnable.run())));
        modifica.ifPresent(
                consumer -> pulsanti.add(Pair.of("Modifica", opt -> opt.ifPresent(a -> consumer.accept(a.getKey())))));
        eliminazione
                .ifPresent(consumer -> pulsanti
                        .add(Pair.of("Elimina", opt -> opt.ifPresent(a -> consumer.accept(a.getKey())))));
        conferma.ifPresent(runnable -> pulsanti.add(Pair.of("Conferma", opt -> runnable.run())));
        return Components.<Entry<Alimento, Integer>>singleObjectMultiButtonSelector(
                List.copyOf(ingredientiAttuali.entrySet()),
                columns,
                ingrediente -> Utilities.mapFromLists(columns, Utilities.stringList(
                        ingrediente.getKey().codAlimento(), ingrediente.getKey().nome(), ingrediente.getValue())),
                "Nome cibo",
                pulsanti);
    }

    private JComponent editorConsumazione(Optional<Consumazione> attuale, int codAlimento,
            Consumer<Consumazione> accettore, String buttonLabel) {

        JPanel panel = new JPanel(new BorderLayout());
        var query = Components.genericQuery(List.of("Data [YYYY-MM-DD]", "Ora [HH:MM]", "Quantità"),
                List.of(attuale.<String>map(c -> c.data().toString()), attuale.<String>map(c -> c.ora().toString()),
                        attuale.<String>map(c -> String.valueOf(c.quantità()))),
                buttonLabel,
                l -> {
                    try {
                        accettore.accept(
                                new Consumazione(attuale.map(c -> c.username()).orElse(null),
                                        attuale.map(c -> c.numero()).orElse(-1), LocalDate.parse(l.get(0)),
                                        LocalTime.parse(l.get(1)),
                                        codAlimento, Integer.parseInt(l.get(2))));
                    } catch (DateTimeParseException | NumberFormatException e) {
                        displayErrorMessage("Errore nei dati inseriti: " + e.getMessage());
                    }

                });
        panel.add(query, BorderLayout.CENTER);
        attuale.ifPresent(consumazione -> panel
                .add(Components.button("Elimina", () -> getController().utenteEliminaConsumazione(consumazione)),
                        BorderLayout.SOUTH));
        return panel;
    }

    private JComponent editorValoriRicetta(Optional<ValoriRicetta> attuale, Consumer<ValoriRicetta> accettore,
            String buttonLabel) {
        var panel = new JPanel(new BorderLayout());
        var checkbox = new JCheckBox("Privato");
        checkbox.setSelected(attuale.map(v -> v.privata()).orElse(false));
        panel.add(checkbox, BorderLayout.NORTH);
        panel.add(Components.genericQuery(List.of("Nome", "Porzione"),
                List.of(attuale.map(v -> v.nome()),
                        attuale.map((ValoriRicetta v) -> Components.descrizioneOptional(v.porzione()))),
                buttonLabel,
                l -> {
                    try {
                        accettore.accept(new ValoriRicetta(l.get(0), Optional.of(Integer.parseInt(l.get(1))), false));
                    } catch (NumberFormatException e) {
                        accettore.accept(new ValoriRicetta(l.get(0), Optional.empty(), checkbox.isSelected()));
                    }
                }), BorderLayout.CENTER);
        return panel;
    }

    private JComponent dettaglioAlimento(Alimento a, boolean modificabile) {
        var panel = new JPanel(new BorderLayout());
        panel.add(Components.keyValueTable(
                List.of(
                        Utilities.stringPair("Codice alimento", a.codAlimento()),
                        Utilities.stringPair("Nome", a.nome()),
                        Utilities.stringPair("Kcal per 100g", a.kcal()),
                        Utilities.stringPair("Proteine per 100g", a.proteine()),
                        Utilities.stringPair("Grassi per 100g", a.grassi()),
                        Utilities.stringPair("Porzione di riferimento", Components.descrizioneOptional(a.porzione())),
                        Utilities.stringPair("Brand", Components.descrizioneOptional(a.brand())),
                        Utilities.stringPair("Proprietario", modificabile ? "tu" : "altro utente"),
                        Utilities.stringPair("Privato", a.privato()))),
                BorderLayout.CENTER);
        List<Pair<String, Runnable>> buttons = new ArrayList<>();
        if (a.tipo() == 'R') {
            buttons.add(Pair.of("Ingredienti", () -> getController().utenteRichiedeIngredienti(a)));
        }
        if (modificabile) {
            buttons.add(Pair.of("Modifica valori", () -> {
                if (a.tipo() == 'R') {
                    freshPane(cp -> cp.add(
                            editorValoriRicetta(Optional.of(new ValoriRicetta(a.nome(), a.porzione(), a.privato())),
                                    v -> getController().utenteModificaValoriRicetta(a, v), "Modifica")));
                } else {
                    freshPane(cp -> cp.add(
                            editorValoriCibo(Optional.of(a),
                                    v -> getController().utenteModificaValoriCibo(a, v), "Modifica")));
                }
            }));
            var buttonPanel = new JPanel(new GridLayout(1, buttons.size()));
            buttons.forEach(b -> buttonPanel.add(Components.button(b.getLeft(), b.getRight())));
            if (!buttons.isEmpty()) {
                panel.add(buttonPanel, BorderLayout.SOUTH);
            }
        }
        return panel;
    }

}
