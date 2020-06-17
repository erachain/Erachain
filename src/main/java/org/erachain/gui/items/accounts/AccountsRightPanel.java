package org.erachain.gui.items.accounts;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.RendererBigDecimals;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AccountsRightPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public AccountsTransactionsTableModel table_Model;
    @SuppressWarnings("rawtypes")
    public MTable jTable1;
    // Variables declaration - do not modify
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    public javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private JPopupMenu mainMenu;
    private JMenuItem viewInfo;
    private AccountsRightPanel th;
    protected int row;
    /**
     * Creates new form НовыйJPanel
     */
    public AccountsRightPanel() {
        initComponents();
    }

     @SuppressWarnings({ "rawtypes", "unchecked" })
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        th = this;
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
         jMenuBar2 = new javax.swing.JMenuBar();
         jMenu3 = new javax.swing.JMenu();
         jMenu4 = new javax.swing.JMenu();
         jToggleButton1 = new javax.swing.JToggleButton();
         new javax.swing.JPopupMenu();
         jMenu5 = new javax.swing.JMenu();
         jToggleButton2 = new javax.swing.JToggleButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         table_Model = new AccountsTransactionsTableModel();
         jTable1 = new MTable(table_Model);

         if (false) {
             // не правильная сортировка - по существующим только и не дает неподтвержденные сюда внести
             // и при этом еще у записей Номера блоков обновляет и присваивает для неподтвержденных как будто они включенв в +1 блок верхний

             // sort from column
             @SuppressWarnings("unchecked")
             TableRowSorter t = new TableRowSorter(table_Model);
             // comparator
             t.setComparator(table_Model.COLUMN_TRANSACTION, new Comparator<String>() {
                 @Override
                 public int compare(String o1, String o2) {
                     BigDecimal transaction1 = Library.getBlockSegToBigInteger(DCSet.getInstance().getTransactionFinalMap().getRecord(o1));
                     BigDecimal transaction2 = Library.getBlockSegToBigInteger(DCSet.getInstance().getTransactionFinalMap().getRecord(o2));
                     return transaction1.compareTo(transaction2);
                 }
             });

             // sort list  - AUTO sort
             List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
             sortKeys.add(new RowSorter.SortKey(table_Model.COLUMN_TRANSACTION, SortOrder.DESCENDING));
             t.setSortKeys(sortKeys);
             // sort table
             jTable1.setRowSorter(t);

         }

         jMenu1.setText("File");
         jMenuBar1.add(jMenu1);

         jMenu2.setText("Edit");
         jMenuBar1.add(jMenu2);

         jMenu3.setText("File");
         jMenuBar2.add(jMenu3);

        jMenu4.setText("Edit");
        jMenuBar2.add(jMenu4);

        jToggleButton1.setText("jToggleButton1");

        jMenu5.setText("jMenu5");

        jToggleButton2.setText("jToggleButton2");

        setLayout(new java.awt.GridBagLayout());
/*
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        */
       jScrollPane1.setViewportView(jTable1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(jScrollPane1, gridBagConstraints);
        
        mainMenu = new JPopupMenu();
        mainMenu.addPopupMenuListener(new PopupMenuListener() {

           

            @Override
            public void popupMenuCanceled(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub
                int row1 = jTable1.getSelectedRow();
                if (row1 < 0) return;

                row = jTable1.convertRowIndexToModel(row1);


            }
        });
        viewInfo = new JMenuItem(Lang.getInstance().translate("View Transaction"));
        viewInfo.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                AccountsTransactionsTableModel.Trans transaction = table_Model.getItem(th.row);
                IssueConfirmDialog dd = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction.transaction, (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2), Lang.getInstance().translate("Transaction"));
                dd.setLocationRelativeTo(th);
                dd.setVisible(true);
            }

        });
         mainMenu.add(viewInfo);
         //   jTable1.setComponentPopupMenu(mainMenu);
         TableMenuPopupUtil.installContextMenu(jTable1, mainMenu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON
     }// </editor-fold>

    public void setAsset(AssetCls asset) {
        table_Model.setAsset(asset);
        table_Model.setEncryption(false);
        table_Model.getInterval();
        table_Model.fireTableDataChanged();
        jTable1.setDefaultRenderer(BigDecimal.class, new RendererBigDecimals(asset.getScale()));
    }


}
