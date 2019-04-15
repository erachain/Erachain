package org.erachain.gui.library;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.erachain.gui.models.RendererBoolean;
import org.erachain.gui.models.RendererRight;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import org.erachain.gui.MainFrame;
import org.erachain.lang.Lang;

public class MTable<U, T> extends JTable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public TableModel model;
    public TableRowSorter search_Sorter;

    HashMap<Integer, Tuple2<Tuple3<U, RowFilter<TableModel, T>, ComparisonType>, Tuple3<U, RowFilter<TableModel, T>, ComparisonType>>> filters;
    MouseListener mouseListener;
    private RowFilter<TableModel, T> filter;

    private String[] items;

    private JPopupMenu menu_From_Heade_Col;

    private Tuple4<ComparisonType, String, ComparisonType, String> pp;

    protected int selectedRow;


    @SuppressWarnings({"unchecked", "rawtypes"})
    public MTable(TableModel model) {
        super();

        filters = new HashMap();
        // set model
        this.model = model;
       if (this.model!=null) setModel(this.model);
        // view filter dialog
        // show_search(true);

        // height row in table
        setRowHeight((int) (getFontMetrics(getFont()).getHeight()));
        // set renders
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        mouseListener = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                // TODO Auto-generated method stub
                mouse_Presset(arg0);
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

        };

        // Date d1 = (Date) model.getValueAt(0, 3);
        // Date d2 = (Date) model.getValueAt(model.getRowCount() - 2, 3);
        // RowFilter<TableModel, Integer> low =
        // RowFilter.dateFilter(RowFilter.ComparisonType.AFTER, d1, 3);
        // RowFilter<TableModel, Integer> high =
        // RowFilter.dateFilter(RowFilter.ComparisonType.BEFORE, d2, 3);
        // RowFilter<TableModel, Integer> num =
        // RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, (Number) 2,
        // 1);
        // List<RowFilter<TableModel, Integer>> filters =
        // Arrays.asList(num);//low, high);
        // final RowFilter<TableModel, Integer> filter =
        // RowFilter.andFilter(filters);

        /*
         * setDefaultRenderer(Integer.class, new RendererRight()); // set
         * renderer setDefaultRenderer(String.class, new RendererLeft()); //
         * set renderer setDefaultRenderer(Long.class, new RendererRight()); //
         * set renderer setDefaultRenderer(Boolean.class, new
         * RendererBoolean()); // set renderer setDefaultRenderer(Tuple2.class,
         * new RendererLeft()); // set renderer setDefaultRenderer(Date.class,
         * new RendererRight()); // set renderer
         * setDefaultRenderer(PublicKeyAccount.class, new RendererLeft()); //
         * set renderer //RenderingHints. setDefaultRenderer(Double.class, new
         * RendererRight()); // set renderer
         *
         */

        setDefaultRenderer(Boolean.class, new RendererBoolean());

        show_search(true);
        addselectSelectedRow();
    }

    // Filter from Column on/off
    public void show_search(boolean bl) {

        if (bl) {

            set_header_menu();
            search_Sorter = new TableRowSorter(this.model);
            this.setRowSorter(search_Sorter);
            this.getTableHeader().addMouseListener(mouseListener);
        } else
            this.getTableHeader().removeMouseListener(mouseListener);

    }

    private void set_header_menu() {

        menu_From_Heade_Col = new JPopupMenu();

    }

    // get nom Column fro mouse cursor coordinates
    private int getColumn(JTableHeader th, Point p) {
        TableColumnModel model = th.getColumnModel();
        for (int col = 0; col < model.getColumnCount(); col++)
            if (th.getHeaderRect(col).contains(p))
                return col;
        return -1;
    }

    // set Filter from list filters
    @SuppressWarnings("unchecked")
    private void set_Filter() {

        if (model.getRowCount() == 0)
            return;
        this.search_Sorter.setRowFilter(filter);
        this.setRowSorter(this.search_Sorter);
    }

    // show filter dialog
    public Object showEditor(Component parent, int col, String currentValue) {
        items[0] = currentValue;
        String message = "select value for column " + (col + 1) + ":";
        Object retVal = JOptionPane.showInputDialog(parent, message, "table header editor",
                JOptionPane.INFORMATION_MESSAGE, null, items, items[0]);
        if (retVal == null)
            retVal = currentValue;
        return retVal;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mouse_Presset(MouseEvent arg0) {
        if (arg0.getButton() == arg0.BUTTON3) {
            JTableHeader th = (JTableHeader) arg0.getSource();
            Point p = arg0.getPoint();
            int col = getColumn(th, p);
            TableColumn column = th.getColumnModel().getColumn(col);
            String ss = "****";
            Object sss = "";
            Object str;

//bigDecimal
// long
            if (model.getColumnClass(col) == BigDecimal.class) {
                Tuple3<U, RowFilter<TableModel, T>, ComparisonType> second_Filter = null;
                Tuple3<U, RowFilter<TableModel, T>, ComparisonType> first_Filter = null;

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
//							str = JOptionPane.showInputDialog(th, "filter BigDecimal col. = " + column.getHeaderValue(), sss);
                String first_S = (first.a == null) ? "" : (String) first.a;
                String second_S = (second.a == null) ? "" : (String) second.a;
                MTableSearchNumDialog dialog = new MTableSearchNumDialog(column, BigDecimal.class, first.c, first_S, second.c, second_S);
                int x = arg0.getXOnScreen() - dialog.getWidth() / 2;
                if (MainFrame.getInstance().getX() > x) x = MainFrame.getInstance().getX();
                int y = arg0.getYOnScreen() - dialog.getHeight();
                if (MainFrame.getInstance().getY() > y) y = MainFrame.getInstance().getY();
                dialog.setLocation(x, y);
                dialog.setVisible(true);


                pp = dialog.get_Ansver();
                //обнуляем фильтр и устанавливаем рендер по умолчанию
                filters.remove(col);
                column.setHeaderRenderer(null);

                if (pp.b != null) {
                    //если есть данные то устанавливаем рендер
                    column.setHeaderRenderer(new RendererRight());
                    //формируем первый фильтр
                    String a = pp.b;
                    ComparisonType b = pp.a;
                    BigDecimal i = new BigDecimal(pp.b);
                    first_Filter = new Tuple3(a, RowFilter.numberFilter(b, i, col), b);
                    // если есть второй фильтр
                    if (pp.d != null) {

                        second_Filter = new Tuple3(pp.d, RowFilter.numberFilter(pp.c, new BigDecimal(pp.d), col), pp.c);
                    }

                    filters.put(col, new Tuple2(first_Filter, second_Filter));
                }
            }

// long
            if (model.getColumnClass(col) == Long.class) {
                Tuple3<U, RowFilter<TableModel, T>, ComparisonType> second_Filter = null;
                Tuple3<U, RowFilter<TableModel, T>, ComparisonType> first_Filter = null;

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
//				str = JOptionPane.showInputDialog(th, "filter BigDecimal col. = " + column.getHeaderValue(), sss);
                String first_S = (first.a == null) ? "" : (String) first.a;
                String second_S = (second.a == null) ? "" : (String) second.a;
                MTableSearchNumDialog dialog = new MTableSearchNumDialog(column, Long.class, first.c, first_S, second.c, second_S);
                int x = arg0.getXOnScreen() - dialog.getWidth() / 2;
                if (MainFrame.getInstance().getX() > x) x = MainFrame.getInstance().getX();
                int y = arg0.getYOnScreen() - dialog.getHeight();
                if (MainFrame.getInstance().getY() > y) y = MainFrame.getInstance().getY();
                dialog.setLocation(x, y);
                dialog.setVisible(true);
                pp = dialog.get_Ansver();
                //обнуляем фильтр и устанавливаем рендер по умолчанию
                filters.remove(col);
                column.setHeaderRenderer(null);

                if (pp.b != null) {
                    //если есть данные то устанавливаем рендер
                    column.setHeaderRenderer(new RendererRight());
                    //формируем первый фильтр
                    String a = pp.b;
                    ComparisonType b = pp.a;
                    Integer i = new Integer(pp.b);
                    first_Filter = new Tuple3(a, RowFilter.numberFilter(b, i, col), b);
                    // если есть второй фильтр
                    if (pp.d != null) {

                        second_Filter = new Tuple3(pp.d, RowFilter.numberFilter(pp.c, new Integer(pp.d), col), pp.c);
                    }

                    filters.put(col, new Tuple2(first_Filter, second_Filter));
                }


            }

// integer
            if (model.getColumnClass(col) == Integer.class) {

                // dialog

                Tuple3<U, RowFilter<TableModel, T>, ComparisonType> second_Filter = null;
                Tuple3<U, RowFilter<TableModel, T>, ComparisonType> first_Filter = null;

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
//					str = JOptionPane.showInputDialog(th, "filter BigDecimal col. = " + column.getHeaderValue(), sss);
                String first_S = (first.a == null) ? "" : (String) first.a;
                String second_S = (second.a == null) ? "" : (String) second.a;
                MTableSearchNumDialog dialog = new MTableSearchNumDialog(column, Integer.class, first.c, first_S, second.c, second_S);
                int x = arg0.getXOnScreen() - dialog.getWidth() / 2;
                if (MainFrame.getInstance().getX() > x) x = MainFrame.getInstance().getX();
                int y = arg0.getYOnScreen() - dialog.getHeight();
                if (MainFrame.getInstance().getY() > y) y = MainFrame.getInstance().getY();
                dialog.setLocation(x, y);
                dialog.setVisible(true);
                pp = dialog.get_Ansver();
                //обнуляем фильтр и устанавливаем рендер по умолчанию
                filters.remove(col);
                column.setHeaderRenderer(null);

                if (pp.b != null) {
                    //если есть данные то устанавливаем рендер
                    column.setHeaderRenderer(new RendererRight());
                    //формируем первый фильтр
                    String a = pp.b;
                    ComparisonType b = pp.a;
                    Integer i = new Integer(pp.b);
                    first_Filter = new Tuple3(a, RowFilter.numberFilter(b, i, col), b);
                    // если есть второй фильтр
                    if (pp.d != null) {

                        second_Filter = new Tuple3(pp.d, RowFilter.numberFilter(pp.c, new Integer(pp.d), col), pp.c);
                    }

                    filters.put(col, new Tuple2(first_Filter, second_Filter));
                }
            }


// string 
            if (model.getColumnClass(col) == String.class) {
                // Диалоговое окно с полем ввода, инициализируемое
                // initialSelectionValue

                if (filters.get(col) != null) {
                    if (filters.get(col).a != null)
                        sss = filters.get(col).a.a.toString();
                }
                str = JOptionPane.showInputDialog(th, Lang.getInstance().translate("Filter column") + ": " + column.getHeaderValue().toString(), sss);
                if (str != null) {
                    if (!str.toString().equals("")) {
                        column.setHeaderRenderer(new RendererRight());
                        filters.put(col, new Tuple2(new Tuple3(str, RowFilter.regexFilter(".*" + str.toString() + ".*", col), null), new Tuple3(null, null, null)));
                    } else {
                        column.setHeaderRenderer(null);
                        filters.remove(col);
                    }
                }
            }
// date
            if (model.getColumnClass(col) == Date.class) {

                if (filters.get(col) != null) {
                    if (filters.get(col).a != null)
                        sss = filters.get(col).a;
                }
                str = JOptionPane.showInputDialog(th, Lang.getInstance().translate("Filter column") + ": " + column.getHeaderValue().toString(), "data");
                if (str != null) {
                    if (!str.toString().equals("")) {
                        column.setHeaderRenderer(new RendererRight());
                        filters.put(col, new Tuple2(new Tuple3(str, RowFilter.regexFilter(".*" + str.toString() + ".*", col), null), new Tuple3(null, null, null)));
                    } else {
                        column.setHeaderRenderer(null);
                        filters.remove(col);
                    }
                }
            }

            if (model.getColumnClass(col) == Boolean.class) {

                //	String resultString = (String) JOptionPane.showInputDialog(null, "Input an answer", "Input", JOptionPane.QUESTION_MESSAGE, flag, listArr, "Cuatro");
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

                int n = JOptionPane.showConfirmDialog(th, msgContent, Lang.getInstance().translate("Filter"), JOptionPane.OK_CANCEL_OPTION);

                boolean remember = rememberChk.isSelected();
                //	if (n != 0) {
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


                    //		}


                }
            }

            // Object value = showEditor(th, col, oldValue);
            Iterator<Tuple2<Tuple3<U, RowFilter<TableModel, T>, ComparisonType>, Tuple3<U, RowFilter<TableModel, T>, ComparisonType>>> s = filters.values().iterator();
            List<RowFilter<TableModel, T>> rowfolters = new ArrayList();
            while (s.hasNext()) {
                Tuple2<Tuple3<U, RowFilter<TableModel, T>, ComparisonType>, Tuple3<U, RowFilter<TableModel, T>, ComparisonType>> a = s.next();


                rowfolters.add(a.a.b);
                if (a.b != null) if (a.b.a != null) rowfolters.add(a.b.b);
            }
            filter = RowFilter.andFilter(rowfolters);
            // column.setHeaderValue("<HTML><B>" + value);
            set_Filter();
            th.resizeAndRepaint();
        }

    }
 // selected select row for firechange model data 
    public void addselectSelectedRow() {
        // Save selected row table
        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                // TODO Auto-generated method stub
                selectedRow = (e.getLastIndex());//jTable_Peers.getSelectedRow());
                //        selectedCol.set(jTable_Peers.getSelectedColumn());

            }

        });

        // set selected row
        this.getModel().addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                // TODO Auto-generated method stub

                if (model.getRowCount() == 0) {
                    return;
                }

                if (getSelectionModel().getMaxSelectionIndex() < 0) return;
                if (selectedRow < 0) selectedRow = 0;

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        getSelectionModel().addSelectionInterval(selectedRow, selectedRow);
                    }
                });
            }
        });

    }
   
    @Override
    public void setModel(TableModel model){
        super.setModel(model);
        addselectSelectedRow(); 
        getSelectionModel().addSelectionInterval(0, 0);
    };
    

}
