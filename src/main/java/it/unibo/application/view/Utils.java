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

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import it.unibo.application.commons.Utilities;

public class Utils {

    public final static int TEXTFIELD_MIN_WIDTH = 10;

    public static JPanel genericQuery(List<String> fieldLabels, String buttonLabel, Consumer<List<String>> consumer) {
        JPanel fieldsPanel = new JPanel(new GridLayout(fieldLabels.size() * 2, 1));
        List<JTextField> fields = new ArrayList<>();
        fieldLabels.forEach(l -> {
            JTextField f = new JTextField();
            fieldsPanel.add(new JLabel(l));
            fieldsPanel.add(f);
            fields.add(f);
        });
        JButton submit = new JButton(buttonLabel);
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
        return o.isPresent() ? o.get().toString() : "non impostato";
    }

    public static <T> JComponent objectsTable(List<T> objects, List<String> columns,
            Function<T, Map<String, String>> translator, Consumer<T> onDoubleClick) {
        JTable table = new JTable(new AbstractTableModel() {
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
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    onDoubleClick.accept(objects.get(table.getSelectedRow()));
                }
            }
        });

        return new JScrollPane(table);
    }

    public static <T> JComponent objectsList(List<T> objects, Function<T, String> translator,
            Consumer<T> onDoubleClick) {
        JList<String> l = new JList<>(new AbstractListModel<>() {
            @Override
            public int getSize() {
                return objects.size();
            }

            @Override
            public String getElementAt(int index) {
                return translator.apply(objects.get(index));
            }
        });
        return new JScrollPane(l);
    }

    public static <T> JComponent singleObjectSelector(final List<T> objects, final List<String> columns,
            final Function<T, Map<String, String>> translator, final String searchColumn, final Consumer<T> onSelection,
            final String buttonLabel) {
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

        JPanel southPanel = new JPanel(new GridLayout(2, 1));
        southPanel.add(searchPanel);
        southPanel.add(Utils.button(buttonLabel, () -> {
            if (table.getSelectedRow() >= 0) {
                onSelection.accept(model.getObjectAt(table.getSelectedRow()));
            }
        }));

        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }

    public static <T> JComponent multipleObjectSelector(final List<T> objects, final List<String> columns,
            final Function<T, Map<String, String>> translator, final String searchColumn, final Consumer<List<T>> onSelection,
            final String buttonLabel) {
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
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        var searchBar = new JTextField(20);
        var searchButton = button("Filtra", () -> model.filter(Utilities.notBlank(searchBar.getText())));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(searchBar);
        searchPanel.add(searchButton);

        JPanel southPanel = new JPanel(new GridLayout(2, 1));
        southPanel.add(searchPanel);
        southPanel.add(Utils.button(buttonLabel, () -> onSelection.accept(Arrays.stream(table.getSelectedRows()).mapToObj(i -> model.getObjectAt(i)).toList())));

        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }
}
