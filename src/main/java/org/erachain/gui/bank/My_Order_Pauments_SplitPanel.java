package org.erachain.gui.bank;

import org.erachain.core.item.persons.PersonCls;
import org.erachain.gui.Split_Panel;
import org.erachain.gui.library.MTable;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class My_Order_Pauments_SplitPanel extends Split_Panel {
    private static final long serialVersionUID = 2717571093561259483L;
    // для прозрачности
    int alpha = 255;
    int alpha_int;
    private Payment_Orders_TableModel payment_Orders_model;
    private MTable payment_Orders_table;
    private TableRowSorter my_Sorter;


    public My_Order_Pauments_SplitPanel() {
        super("Persons_My_SplitPanel");

        //	this.setName(Lang.getInstance().translate("My Persons"));
        this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        // not show buttons
        this.button1_ToolBar_LeftPanel.setVisible(false);
        this.button2_ToolBar_LeftPanel.setVisible(false);
        this.jButton1_jToolBar_RightPanel.setVisible(false);
        this.jButton2_jToolBar_RightPanel.setVisible(false);


        // not show My filter
        this.searth_My_JCheckBox_LeftPanel.setVisible(false);

        //TABLE
        payment_Orders_model = new Payment_Orders_TableModel();
        payment_Orders_table = new MTable<Object, Object>(payment_Orders_model);

        TableColumnModel columnModel = payment_Orders_table.getColumnModel(); // read column model
        columnModel.getColumn(0).setMaxWidth((100));

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
                payment_Orders_model.removeObservers();
            }


        });

        //		my_Sorter = new TableRowSorter(my_PersonsModel);
        //		my_Person_table.setRowSorter(my_Sorter);
        //		my_Person_table.getRowSorter();
        //		if (my_PersonsModel.getRowCount() > 0) my_PersonsModel.fireTableDataChanged();

        //CHECKBOX FOR CONFIRMED
        TableColumn confirmedColumn = payment_Orders_table.getColumnModel().getColumn(Payment_Orders_TableModel.COLUMN_CONFIRMATIONS);
        // confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));

        confirmedColumn.setMinWidth(270);
        confirmedColumn.setMaxWidth(350);
        confirmedColumn.setPreferredWidth(50);//.setWidth(30);

        // UPDATE FILTER ON TEXT CHANGE
        this.searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new My_Search());
        // SET VIDEO
        this.jTable_jScrollPanel_LeftPanel.setModel(payment_Orders_model);
        this.jTable_jScrollPanel_LeftPanel = payment_Orders_table;
        this.jScrollPanel_LeftPanel.setViewportView(this.jTable_jScrollPanel_LeftPanel);
        //		this.setRowHeightFormat(true);

        // EVENTS on CURSOR
        payment_Orders_table.getSelectionModel().addListSelectionListener(new My_Tab_Listener());

        jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = jTable_jScrollPanel_LeftPanel.rowAtPoint(p);
                jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(row, row);


                if (e.getClickCount() == 1 & e.getButton() == e.BUTTON1) {

                    //	if (jTable_jScrollPanel_LeftPanel.getSelectedColumn() == TableModelPersons.COLUMN_FAVORITE){
                    //		row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
                    //	 PersonCls asset = my_PersonsModel.getItem(row);
                    //	favorite_set( jTable_jScrollPanel_LeftPanel);


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
        payment_Orders_model.removeObservers();
        // get component from right panel
        Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
        // if Person_Info 002 delay on close
        //	  if (c1 instanceof Person_Info_002) ( (Person_Info_002)c1).delay_on_Close();

    }

    class My_Tab_Listener implements ListSelectionListener {

        //@SuppressWarnings("deprecation")
        @Override
        public void valueChanged(ListSelectionEvent arg0) {

            PersonCls person = null;
            //		if (my_Person_table.getSelectedRow() >= 0 )	person = my_PersonsModel.getItem(my_Person_table.convertRowIndexToModel(my_Person_table.getSelectedRow()));
            //		if (person == null) return;
            //		Person_Info_002 info_panel = new Person_Info_002(person, false);
            //		info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
            //		jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
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
            String search = searchTextField_SearchToolBar_LeftPanel.getText();
            // SET FILTER
            payment_Orders_model.fireTableDataChanged();

            RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
            ((DefaultRowSorter) my_Sorter).setRowFilter(filter);

            payment_Orders_model.fireTableDataChanged();

        }
    }


}




