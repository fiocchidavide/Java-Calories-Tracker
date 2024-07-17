package it.unibo.application.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang3.tuple.Pair;

public class Utils {
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

    public static JLabel clickableLabel(String labelText, Runnable action) {
        var label = new JLabel(labelText);
        label.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        SwingUtilities.invokeLater(() -> {
                            action.run();
                        });
                    }
                });
        return label;
    }

    public static JScrollPane keyValueTable(List<Pair<String, String>> values) {
        var table = new JTable(new AbstractTableModel() {
            @Override
            public String getColumnName(int column) {
                if (column == 0) {
                    return "Attributo";
                } else {
                    return "Valore";
                }
            }

            @Override
            public int getRowCount() {
                return values.size();
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return columnIndex == 0 ? values.get(rowIndex).getLeft() : values.get(rowIndex).getRight();
            }
        });
        return new JScrollPane(table);
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
                if(e.getClickCount() == 2 && table.getSelectedRow() >= 0){
                    onDoubleClick.accept(objects.get(table.getSelectedRow()));
                }
            }
        });

        return new JScrollPane(table);
    }

    public static <T> JComponent objectsList(List<T> objects, Function<T,String> translator, Consumer<T> onDoubleClick){
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
}
