package org.erachain.gui.items.polls;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.WalletItemPersonsTableModel;
import org.erachain.gui.models.WalletItemPollsTableModel;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class Polls_My_SplitPanel extends SplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;
    // для прозрачности
    int alpha = 255;
    int alpha_int;
    private WalletItemPollsTableModel my_Poll_Model;
    private MTable my_Poll_table;
    private TableRowSorter my_Sorter;

    public Polls_My_SplitPanel() {
        super("Polls_My_SplitPanel");
        this.setName(Lang.getInstance().translate("My Polls"));
        this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        // not show buttons
        this.button1ToolBarLeftPanel.setVisible(false);
        this.button2ToolBarLeftPanel.setVisible(false);
        this.jButton1_jToolBar_RightPanel.setVisible(false);
        this.jButton2_jToolBar_RightPanel.setVisible(false);
        // not show My filter
        this.searchMyJCheckBoxLeftPanel.setVisible(false);
        // TABLE
        my_Poll_Model = new WalletItemPollsTableModel();
        my_Poll_table = new MTable(my_Poll_Model);
        my_Sorter = new TableRowSorter(my_Poll_Model);
        my_Poll_table.setRowSorter(my_Sorter);
        my_Poll_table.getRowSorter();
        if (my_Poll_Model.getRowCount() > 0)
            my_Poll_Model.fireTableDataChanged();

        // CHECKBOX FOR CONFIRMED
        TableColumn confirmedColumn = my_Poll_table.getColumnModel()
                .getColumn(WalletItemPersonsTableModel.COLUMN_CONFIRMED);
        confirmedColumn.setMinWidth(250);
        confirmedColumn.setMaxWidth(250);
        confirmedColumn.setPreferredWidth(50);// .setWidth(30);

        // UPDATE FILTER ON TEXT CHANGE
        this.searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new My_Search());
        // SET VIDEO
        this.jTableJScrollPanelLeftPanel.setModel(my_Poll_Model);
        this.jTableJScrollPanelLeftPanel = my_Poll_table;
        this.jScrollPanelLeftPanel.setViewportView(this.jTableJScrollPanelLeftPanel);
        // EVENTS on CURSOR
        my_Poll_table.getSelectionModel().addListSelectionListener(new My_Tab_Listener());
        // Dimension size = MainFrame.getInstance().desktopPane.getSize();
        // this.setSize(new
        // Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
        // jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
    }

    @Override
    public void onClose() {
        // delete observer left panel
        my_Poll_Model.deleteObservers();
        // get component from right panel
        // Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        // if (c1 instanceof StatementInfo) (
        // (StatementInfo)c1).delay_on_Close();

    }

    class My_Tab_Listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            PollCls poll = null;
            if (my_Poll_table.getSelectedRow() >= 0)
                poll = my_Poll_Model.getItem(my_Poll_table.convertRowIndexToModel(my_Poll_table.getSelectedRow())).b;
            if (poll == null)
                return;
            PollsDetailPanel pollDetailsPanel = new PollsDetailPanel(poll,
                    Controller.getInstance().getAsset(AssetCls.FEE_KEY));
            jScrollPaneJPanelRightPanel.setViewportView(pollDetailsPanel);
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
            my_Poll_Model.fireTableDataChanged();

            RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
            ((DefaultRowSorter) my_Sorter).setRowFilter(filter);

            my_Poll_Model.fireTableDataChanged();

        }
    }

}
