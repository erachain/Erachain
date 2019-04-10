package org.erachain.gui.items.assets;

import org.erachain.core.item.assets.Order;
import org.erachain.gui.CoreRowSorter;
import org.erachain.gui.models.WalletOrdersTableModel;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class MyOrdersFrame extends JFrame {

    private WalletOrdersTableModel ordersTableModel;

    public MyOrdersFrame() {

        super(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("My Orders"));

        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //CLOSE
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //LAYOUT
        this.setLayout(new GridBagLayout());

        //PADDING
        ((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));

        //SEACH LABEL GBC
        GridBagConstraints searchLabelGBC = new GridBagConstraints();
        searchLabelGBC.insets = new Insets(0, 5, 5, 0);
        searchLabelGBC.fill = GridBagConstraints.HORIZONTAL;
        searchLabelGBC.anchor = GridBagConstraints.NORTHWEST;
        searchLabelGBC.weightx = 0;
        searchLabelGBC.gridwidth = 1;
        searchLabelGBC.gridx = 0;
        searchLabelGBC.gridy = 0;

        //SEACH GBC
        GridBagConstraints searchGBC = new GridBagConstraints();
        searchGBC.insets = new Insets(0, 5, 5, 0);
        searchGBC.fill = GridBagConstraints.HORIZONTAL;
        searchGBC.anchor = GridBagConstraints.NORTHWEST;
        searchGBC.weightx = 1;
        searchGBC.gridwidth = 1;
        searchGBC.gridx = 1;
        searchGBC.gridy = 0;

        //TABLE GBC
        GridBagConstraints tableGBC = new GridBagConstraints();
        tableGBC.insets = new Insets(0, 5, 5, 0);
        tableGBC.fill = GridBagConstraints.BOTH;
        tableGBC.anchor = GridBagConstraints.NORTHWEST;
        tableGBC.weightx = 1;
        tableGBC.weighty = 1;
        tableGBC.gridwidth = 2;
        tableGBC.gridx = 0;
        tableGBC.gridy = 1;

        //CREATE TABLE
        this.ordersTableModel = new WalletOrdersTableModel();
        final JTable ordersTable = new JTable(this.ordersTableModel);

        //ASSETS SORTER
        Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
        CoreRowSorter sorter = new CoreRowSorter(this.ordersTableModel, indexes);
        ordersTable.setRowSorter(sorter);


        // MENU
        JPopupMenu ordersMenu = new JPopupMenu();
        JMenuItem trades = new JMenuItem(Lang.getInstance().translate("Trades"));
        trades.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = ordersTable.getSelectedRow();
                row = ordersTable.convertRowIndexToModel(row);

                Order order = ordersTableModel.getItem(row).b;
                new TradesFrame(order);
            }
        });
        ordersMenu.add(trades);
        JMenuItem cancel = new JMenuItem(Lang.getInstance().translate("Cancel"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = ordersTable.getSelectedRow();
                row = ordersTable.convertRowIndexToModel(row);

                Order order = ordersTableModel.getItem(row).b;
                new CancelOrderFrame(order);
            }
        });
        ordersMenu.add(cancel);
   //     ordersTable.setComponentPopupMenu(ordersMenu);
        TableMenuPopupUtil.installContextMenu(ordersTable, ordersMenu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON



        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = ordersTable.rowAtPoint(p);
                ordersTable.setRowSelectionInterval(row, row);

                if (e.getClickCount() == 2) {
                    row = ordersTable.convertRowIndexToModel(row);
                    Order order = ordersTableModel.getItem(row).b;
                    new TradesFrame(order);
                }
            }
        });

        this.add(new JScrollPane(ordersTable), tableGBC);

        //PACK
        this.pack();
        //this.setSize(500, this.getHeight());
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void removeObservers() {
        this.ordersTableModel.deleteObservers();
    }
}
