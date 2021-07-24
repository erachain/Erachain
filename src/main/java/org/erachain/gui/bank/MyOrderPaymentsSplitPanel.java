package org.erachain.gui.bank;

import org.erachain.core.item.persons.PersonCls;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.WalletConfirmsRenderer;
import org.erachain.gui.library.MTable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class MyOrderPaymentsSplitPanel extends SplitPanel {

    public static String NAME = "MyOrderPaymentsSplitPanel";
    public static String TITLE = "My Payments Orders";

    private static final long serialVersionUID = 2717571093561259483L;
    private PaymentOrdersTableModel payment_Orders_model;
    private MTable payment_Orders_table;
    private TableRowSorter my_Sorter;

    public MyOrderPaymentsSplitPanel() {
        super(NAME, TITLE);

        // not show buttons
        this.button1ToolBarLeftPanel.setVisible(false);
        this.button2ToolBarLeftPanel.setVisible(false);
        this.jButton1_jToolBar_RightPanel.setVisible(false);
        this.jButton2_jToolBar_RightPanel.setVisible(false);


        // not show My filter
        this.searchMyJCheckBoxLeftPanel.setVisible(false);

        //TABLE
        payment_Orders_model = new PaymentOrdersTableModel();
        payment_Orders_table = new MTable<Object, Object>(payment_Orders_model);

        TableColumnModel columnModel = payment_Orders_table.getColumnModel(); // read column model
        //CHECKBOX FOR CONFIRMED
        TableColumn confirmedColumn = columnModel.getColumn(PaymentOrdersTableModel.COLUMN_CONFIRMATIONS);
        confirmedColumn.setMinWidth(50);
        confirmedColumn.setMaxWidth(100);
        confirmedColumn.setPreferredWidth(50);
        confirmedColumn.setCellRenderer(new WalletConfirmsRenderer());

        this.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent arg0) {
                // TODO Auto-generated method stub
                payment_Orders_model.addObservers();
            }

            @Override
            public void ancestorMoved(AncestorEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void ancestorRemoved(AncestorEvent arg0) {
                // TODO Auto-generated method stub
                payment_Orders_model.deleteObservers();
            }


        });

        //		my_Sorter = new TableRowSorter(my_PersonsModel);
        //		my_Person_table.setRowSorter(my_Sorter);
        //		my_Person_table.getRowSorter();
        //		if (my_PersonsModel.getRowCount() > 0) my_PersonsModel.fireTableDataChanged();

        // UPDATE FILTER ON TEXT CHANGE
        this.searchTextField2.getDocument().addDocumentListener(new My_Search());
        // SET VIDEO
        this.jTableJScrollPanelLeftPanel.setModel(payment_Orders_model);
        this.jTableJScrollPanelLeftPanel = payment_Orders_table;
        this.jScrollPanelLeftPanel.setViewportView(this.jTableJScrollPanelLeftPanel);
        //		this.setRowHeightFormat(true);

        // EVENTS on CURSOR
        payment_Orders_table.getSelectionModel().addListSelectionListener(new My_Tab_Listener());

        jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = jTableJScrollPanelLeftPanel.rowAtPoint(p);
                jTableJScrollPanelLeftPanel.setRowSelectionInterval(row, row);


                if (e.getClickCount() == 1 & e.getButton() == e.BUTTON1) {

                    //	if (jTableJScrollPanelLeftPanel.getSelectedColumn() == ItemsPersonsTableModel.COLUMN_FAVORITE){
                    //		row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                    //	 PersonCls asset = my_PersonsModel.getItem(row);
                    //	favoriteSet( jTableJScrollPanelLeftPanel);


                    //	}


                }
            }
        });


        //		 Dimension size = MainFrame.getInstance().desktopPane.getSize();
        //		 this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
        // jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));


    }

    @Override
    public void onClose() {
        // delete observer left panel
        payment_Orders_model.deleteObservers();
        // get component from right panel
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        //	  if (c1 instanceof PersonInfo002) ( (PersonInfo002)c1).delay_on_Close();

    }

    class My_Tab_Listener implements ListSelectionListener {

        //@SuppressWarnings("deprecation")
        @Override
        public void valueChanged(ListSelectionEvent arg0) {

            PersonCls person = null;
            //		if (my_Person_table.getSelectedRow() >= 0 )	person = my_PersonsModel.getItem(my_Person_table.convertRowIndexToModel(my_Person_table.getSelectedRow()));
            //		if (person == null) return;
            //		PersonInfo002 info_panel = new PersonInfo002(person, false);
            //		info_panel.setPreferredSize(new Dimension(jScrollPaneJPanelRightPanel.getSize().width-50,jScrollPaneJPanelRightPanel.getSize().height-50));
            //		jScrollPaneJPanelRightPanel.setViewportView(info_panel);
        }

    }

    class My_Search implements DocumentListener {
        public void changedUpdate(DocumentEvent e) {
            onChange();
        }

        public void removeUpdate(DocumentEvent e) {
            onChange();
        }

        public void insertUpdate(DocumentEvent e) {
            onChange();
        }

        public void onChange() {
            // GET VALUE
            String search = searchTextField2.getText();
            // SET FILTER
            payment_Orders_model.fireTableDataChanged();

            RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
            ((DefaultRowSorter) my_Sorter).setRowFilter(filter);

            payment_Orders_model.fireTableDataChanged();

        }
    }
}