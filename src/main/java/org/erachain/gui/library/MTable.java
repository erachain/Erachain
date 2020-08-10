package org.erachain.gui.library;

import org.erachain.gui.MainFrame;
import org.erachain.gui.models.RendererRight;
import org.erachain.lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import javax.swing.*;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;

public class MTable<U, T> extends JTable {
    private static final long serialVersionUID = 1L;

    public TableModel model;
    private TableRowSorter searchSorter;

    HashMap<Integer, Tuple2<Tuple3<U, RowFilter<TableModel, T>, ComparisonType>,
            Tuple3<U, RowFilter<TableModel, T>, ComparisonType>>> filters = new HashMap();

    private MouseListener mouseListener;

    private RowFilter<TableModel, T> filter;

    private Tuple4<ComparisonType, String, ComparisonType, String> typeStringTuple4;

    private int selectedRow;

    public MTable(TableModel model) {
        super();
        this.model = model;
        if (this.model != null) {
            setModel(this.model);
        }
        setRowHeight(getFontMetrics(getFont()).getHeight() + 2);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent arg0) {
                mousePreset(arg0);
            }
        };
        ///setDefaultRenderer(Boolean.class, new RendererBoolean());
        showSearch(true);
        addselectSelectedRow();
    }

    @Override
    public void setModel(TableModel model) {
        super.setModel(model);
        addselectSelectedRow();
        getSelectionModel().addSelectionInterval(0, 0);
    }



    private void mousePreset(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseEvent.BUTTON3) {
            return;
        }
        JTableHeader mouseEventSource = (JTableHeader) mouseEvent.getSource();
        Point p = mouseEvent.getPoint();
        int col = getColumn(mouseEventSource, p);
        TableColumn column = mouseEventSource.getColumnModel().getColumn(col);
        Object sss = "";
        String str;
        if (model.getColumnClass(col) == BigDecimal.class) {
            Tuple3<U, RowFilter<TableModel, T>, ComparisonType> secondFilter = processSecondFilter(mouseEvent, col, column);
            Tuple3<U, RowFilter<TableModel, T>, ComparisonType> firstFilter;
            if (typeStringTuple4.b != null) {
                //если есть данные то устанавливаем рендер
                column.setHeaderRenderer(new RendererRight());
                //формируем первый фильтр
                String a = typeStringTuple4.b;
                ComparisonType b = typeStringTuple4.a;
                BigDecimal i = new BigDecimal(typeStringTuple4.b);
                firstFilter = new Tuple3(a, RowFilter.numberFilter(b, i, col), b);
                // если есть второй фильтр
                if (typeStringTuple4.d != null) {
                    secondFilter = new Tuple3(typeStringTuple4.d, RowFilter.numberFilter(typeStringTuple4.c, new BigDecimal(typeStringTuple4.d), col), typeStringTuple4.c);
                }
                filters.put(col, new Tuple2(firstFilter, secondFilter));
            }
        }

// long
        if (model.getColumnClass(col) == Long.class) {
            Tuple3<U, RowFilter<TableModel, T>, ComparisonType> secondFilter = processSecondFilter(mouseEvent, col, column);
            process(col, column, secondFilter);
        }
// integer
        if (model.getColumnClass(col) == Integer.class) {
            Tuple3<U, RowFilter<TableModel, T>, ComparisonType> secondFilter = processSecondFilter(mouseEvent, col, column);;
            process(col, column, secondFilter);
        }
// string
        if (model.getColumnClass(col) == String.class) {
            if (filters.get(col) != null) {
                if (filters.get(col).a != null)
                    sss = filters.get(col).a.a.toString();
            }
            str = JOptionPane.showInputDialog(mouseEventSource, Lang.getInstance().translate("Filter column") + ": " + column.getHeaderValue().toString(), sss);
            processFilters(col, column, str);
        }
// date
        if (model.getColumnClass(col) == Date.class) {
            str = JOptionPane.showInputDialog(mouseEventSource, Lang.getInstance().translate("Filter column") + ": " + column.getHeaderValue().toString(), "data");
            processFilters(col, column, str);
        }

        if (model.getColumnClass(col) == Boolean.class) {
            JCheckBox rememberChk = new JCheckBox(Lang.getInstance().translate("Filter"));
            if (filters.get(col) != null) {
                if (filters.get(col).a != null) {
                    if (filters.get(col).a.a != null) {
                        //			 cx = filters.get(col).a.a;
                        rememberChk.setSelected(Boolean.parseBoolean(filters.get(col).a.a.toString()));
                    }
                }
            }
            String msg = Lang.getInstance().translate("Filter column") + ": " + column.getHeaderValue().toString();

            Object[] msgContent = {msg, rememberChk};

            int n = JOptionPane.showConfirmDialog(mouseEventSource, msgContent, Lang.getInstance().translate("Filter"), JOptionPane.OK_CANCEL_OPTION);
            if (n == 2) {
                column.setHeaderRenderer(null);
                filters.remove(col);
            } else if (n == 0) {
                if (rememberChk.isSelected()) {
                    column.setHeaderRenderer(new RendererRight());
                    filters.remove(col);
                    filters.put(col, new Tuple2(new Tuple3(true, RowFilter.regexFilter(".*true.*", col), null), new Tuple3(null, null, null)));
                } else {
                    column.setHeaderRenderer(new RendererRight());
                    filters.remove(col);
                    filters.put(col, new Tuple2(new Tuple3(false, RowFilter.regexFilter(".*false.*", col), null), new Tuple3(null, null, null)));
                }
            }
        }

        Iterator<Tuple2<Tuple3<U, RowFilter<TableModel, T>, ComparisonType>, Tuple3<U, RowFilter<TableModel, T>, ComparisonType>>>
                s = filters.values().iterator();
        List<RowFilter<TableModel, T>> rowfolters = new ArrayList();
        while (s.hasNext()) {
            Tuple2<Tuple3<U, RowFilter<TableModel, T>, ComparisonType>, Tuple3<U, RowFilter<TableModel, T>, ComparisonType>> a = s.next();
            rowfolters.add(a.a.b);
            if (a.b != null) if (a.b.a != null) rowfolters.add(a.b.b);
        }
        filter = RowFilter.andFilter(rowfolters);
        setFilter();
        mouseEventSource.resizeAndRepaint();
    }

    private void process(int col, TableColumn column, Tuple3<U, RowFilter<TableModel, T>, ComparisonType> secondFilter) {
        Tuple3<U, RowFilter<TableModel, T>, ComparisonType> firstFilter;
        if (typeStringTuple4.b != null) {
            //если есть данные то устанавливаем рендер
            column.setHeaderRenderer(new RendererRight());
            //формируем первый фильтр
            String a = typeStringTuple4.b;
            ComparisonType b = typeStringTuple4.a;
            Integer i = new Integer(typeStringTuple4.b);
            firstFilter = new Tuple3(a, RowFilter.numberFilter(b, i, col), b);
            // если есть второй фильтр
            if (typeStringTuple4.d != null) {
                secondFilter = new Tuple3(typeStringTuple4.d, RowFilter.numberFilter(typeStringTuple4.c, new Integer(typeStringTuple4.d), col), typeStringTuple4.c);
            }
            filters.put(col, new Tuple2(firstFilter, secondFilter));
        }
    }

    private Tuple3<U, RowFilter<TableModel, T>, ComparisonType> processSecondFilter(MouseEvent mouseEvent, int col, TableColumn column) {
        Tuple3<U, RowFilter<TableModel, T>, ComparisonType> secondFilter = null;
        Tuple3<U, RowFilter<TableModel, T>, ComparisonType> first = new Tuple3(null, null, null);
        Tuple3<U, RowFilter<TableModel, T>, ComparisonType> second = new Tuple3(null, null, null);
        if (filters.get(col) != null) {
            if (filters.get(col).a != null) {
                first = filters.get(col).a;
            }
            if (filters.get(col).b != null) {
                second = filters.get(col).b;
            }
        }
        String firstS = (first.a == null) ? "" : (String) first.a;
        String secondS = (second.a == null) ? "" : (String) second.a;
        MTableSearchNumDialog dialog = new MTableSearchNumDialog(column, BigDecimal.class, first.c, firstS, second.c, secondS);
        int x = mouseEvent.getXOnScreen() - dialog.getWidth() / 2;
        if (MainFrame.getInstance().getX() > x) x = MainFrame.getInstance().getX();
        int y = mouseEvent.getYOnScreen() - dialog.getHeight();
        if (MainFrame.getInstance().getY() > y) y = MainFrame.getInstance().getY();
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        typeStringTuple4 = dialog.get_Ansver();
        //обнуляем фильтр и устанавливаем рендер по умолчанию
        filters.remove(col);
        column.setHeaderRenderer(null);
        return secondFilter;
    }

    private void processFilters(int col, TableColumn column, String str) {
        if (str != null) {
            if (!str.equals("")) {
                column.setHeaderRenderer(new RendererRight());
                filters.put(col, new Tuple2(new Tuple3(str, RowFilter.regexFilter(".*" + str.toString() + ".*", col), null), new Tuple3(null, null, null)));
            } else {
                column.setHeaderRenderer(null);
                filters.remove(col);
            }
        }
    }

    // selected select row for firechange model data
    private void addselectSelectedRow() {
        // Save selected row table
        getSelectionModel().addListSelectionListener(e -> selectedRow = (e.getLastIndex()));
        // set selected row
        getModel().addTableModelListener(e -> {
            if (model.getRowCount() == 0) {
                return;
            }
            if (getSelectionModel().getMaxSelectionIndex() < 0) {
                return;
            }
            if (selectedRow < 0) {
                selectedRow = 0;
            }
            SwingUtilities.invokeLater(() -> getSelectionModel().addSelectionInterval(selectedRow, selectedRow));
        });

    }
    // Filter from Column on/off
    private void showSearch(boolean show) {
        if (show) {
            searchSorter = new TableRowSorter(model);
            setRowSorter(searchSorter);
            getTableHeader().addMouseListener(mouseListener);
        } else {
            getTableHeader().removeMouseListener(mouseListener);
        }
    }

    // get nom Column fro mouse cursor coordinates
    private int getColumn(JTableHeader jTableHeader, Point p) {
        TableColumnModel model = jTableHeader.getColumnModel();
        for (int column = 0; column < model.getColumnCount(); column++) {
            if (jTableHeader.getHeaderRect(column).contains(p)) {
                return column;
            }
        }
        return -1;
    }

    // set Filter from list filters
    private void setFilter() {
        if (model.getRowCount() == 0) {
            return;
        }
        searchSorter.setRowFilter(filter);
        setRowSorter(searchSorter);
    }
}
