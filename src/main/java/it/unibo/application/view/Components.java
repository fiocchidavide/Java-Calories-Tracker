package it.unibo.application.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang3.tuple.Pair;

import it.unibo.application.commons.Utilities;

public class Components {

    public final static int TEXTFIELD_MIN_WIDTH = 10;

    public static JPanel genericQuery(List<String> fieldLabels, String buttonLabel, Consumer<List<String>> consumer) {
        return genericQuery(fieldLabels, fieldLabels.stream().map(s -> Optional.<String>empty()).toList(), buttonLabel,
                consumer, true);
    }

    public static JPanel genericQuery(List<String> fieldLabels, List<Optional<String>> fieldValues, String buttonLabel,
            Consumer<List<String>> consumer, boolean enabled) {
        JPanel fieldsPanel = new JPanel(new GridLayout(fieldLabels.size() * 2, 1));
        List<JTextField> fields = new ArrayList<>();
        if (fieldLabels.size() != fieldValues.size()) {
            throw new IllegalArgumentException("Field labels and values lists must have same size.");
        }
        for (int i = 0; i < fieldLabels.size(); i++) {
            JTextField f = new JTextField(fieldValues.get(i).orElse(null));
            fieldsPanel.add(new JLabel(fieldLabels.get(i)));
            fieldsPanel.add(f);
            fields.add(f);
            f.setEnabled(enabled);
        }
        JButton submit = new JButton(buttonLabel);
        submit.setEnabled(enabled);
        submit.addActionListener(action -> {
            consumer.accept(fields.stream().map(jtf -> jtf.getText()).toList());
        });
        JPanel p = new JPanel(new BorderLayout(View.BORDER_WIDTH, View.BORDER_WIDTH));
        p.add(fieldsPanel, BorderLayout.CENTER);
        p.add(submit, BorderLayout.SOUTH);
        return p;
    }

    public static JButton button(String label, Runnable action) {
        var button = new JButton(label);
        button.addActionListener(event -> {
            button.setEnabled(false);
            SwingUtilities.invokeLater(() -> {
                action.run();
                button.setEnabled(true);
            });
        });
        return button;
    }

    public static String descrizioneOptional(Optional<?> o) {
        return o.isPresent() ? o.get().toString() : "n/a";
    }

    public static <T> JComponent clickableObjectsTable(List<T> objects, List<String> columns,
            String filterColumn, Function<T, Map<String, String>> translator, Consumer<T> onDoubleClick) {
        var model = new AbstractTableModel() {
            @Override
            public String getColumnName(int column) {
                return columns.get(column);
            }

            @Override
            public int getRowCount() {
                return objects.size();
            }

            @Override
            public int getColumnCount() {
                return columns.size();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return translator.apply(objects.get(rowIndex)).get(getColumnName(columnIndex));
            }
        };

        JTable table = new JTable(model);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    onDoubleClick.accept(objects.get(table.getSelectedRow()));
                }
            }
        });

        var searchBar = new JTextField(20);
        var searchButton = button("Cerca", () -> {
            IntStream.range(0, model.getRowCount())
                    .filter(i -> translator.apply(objects.get(i)).get(filterColumn).contains(searchBar.getText()))
                    .findFirst().ifPresent(row -> table.scrollRectToVisible(table.getCellRect(row, 0, true)));
        });
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(searchBar);
        searchPanel.add(searchButton);

        var scrollPane = new JScrollPane(table);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.SOUTH);

        return panel;
    }

    public static <T> JComponent singleObjectSelector(final List<T> objects, final List<String> columns,
            final Function<T, Map<String, String>> translator, final String searchColumn,
            final Consumer<T> onSelection, final String buttonLabel) {
        return singleObjectMultiButtonSelector(objects, columns, translator, searchColumn,
                List.of(Pair.of(buttonLabel, opt -> opt.ifPresent(onSelection))));
    }

    public static <T> JComponent singleObjectMultiButtonSelector(final List<T> objects, final List<String> columns,
            final Function<T, Map<String, String>> translator, final String searchColumn,
            final List<Pair<String, Consumer<Optional<T>>>> buttons) {
        var model = new AbstractTableModel() {

            List<T> filtered = objects;

            @Override
            public String getColumnName(int column) {
                return columns.get(column);
            }

            @Override
            public int getRowCount() {
                return filtered.size();
            }

            @Override
            public int getColumnCount() {
                return columns.size();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return translator.apply(filtered.get(rowIndex)).get(getColumnName(columnIndex));
            }

            public void filter(Optional<String> filter) {
                filter.ifPresentOrElse(string -> {
                    filtered = objects.stream().filter(t -> translator.apply(t).get(searchColumn).contains(string))
                            .toList();
                },
                        () -> {
                            filtered = objects;
                        });
                this.fireTableDataChanged();
            }

            public T getObjectAt(int index) {
                return filtered.get(index);
            }
        };

        JTable table = new JTable(model);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        var searchBar = new JTextField(20);
        var searchButton = button("Filtra", () -> model.filter(Utilities.notBlank(searchBar.getText())));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(searchBar);
        searchPanel.add(searchButton);

        JPanel southPanel = new JPanel(new GridLayout(buttons.isEmpty() ? 1 : 2, 1));
        southPanel.add(searchPanel);

        if (!buttons.isEmpty()) {
            JPanel buttonsPanel = new JPanel(new GridLayout(1, buttons.size()));
            buttons.forEach(p -> {
                buttonsPanel.add(Components.button(p.getLeft(), () -> p.getRight().accept(Optional
                        .ofNullable(table.getSelectedRow() >= 0 ? model.getObjectAt(table.getSelectedRow()) : null))));
            });
            southPanel.add(buttonsPanel);
        }
        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }

    public static <T> JComponent multipleObjectSelector(final List<T> objects, final List<String> columns,
            final Function<T, Map<String, String>> translator, final String filterColumn,
            final Consumer<List<T>> onSelection,
            final String buttonLabel) {
        var model = new AbstractTableModel() {
            @Override
            public String getColumnName(int column) {
                return columns.get(column);
            }

            @Override
            public int getRowCount() {
                return objects.size();
            }

            @Override
            public int getColumnCount() {
                return columns.size();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return translator.apply(objects.get(rowIndex)).get(getColumnName(columnIndex));
            }
        };

        JTable table = new JTable(model);

        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        var searchBar = new JTextField(20);
        var searchButton = button("Cerca", () -> {
            IntStream.range(0, model.getRowCount())
                    .filter(i -> translator.apply(objects.get(i)).get(filterColumn).contains(searchBar.getText()))
                    .findFirst().ifPresent(row -> table.scrollRectToVisible(table.getCellRect(row, 0, true)));
        });
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(searchBar);
        searchPanel.add(searchButton);

        var southPanel = new JPanel(new GridLayout(2, 1));
        southPanel.add(searchPanel);
        southPanel.add(Components.button(buttonLabel, () -> {
            onSelection.accept(Arrays.stream(table.getSelectedRows()).mapToObj(i -> objects.get(i)).toList());
        }));

        var scrollPane = new JScrollPane(table);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }
}
