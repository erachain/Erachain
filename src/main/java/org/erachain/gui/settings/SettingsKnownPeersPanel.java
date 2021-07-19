package org.erachain.gui.settings;
// 16 03

import org.erachain.controller.Controller;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.KnownPeersTableModel;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.erachain.utils.IPAddressFormatValidator;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class SettingsKnownPeersPanel extends JPanel {
    public KnownPeersTableModel knownPeersTableModel;
    private JTextField textAddress;
    private JTable knownPeersTable;

    public SettingsKnownPeersPanel() {
        //PADDING
        this.setBorder(new EmptyBorder(10, 5, 5, 10));

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{87, 202, 44, 37, 0};
        gridBagLayout.rowHeights = new int[]{281, 23, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        this.knownPeersTableModel = new KnownPeersTableModel();

        knownPeersTable = new MTable(knownPeersTableModel);

        GridBagConstraints gbc_knownPeersTable = new GridBagConstraints();
        gbc_knownPeersTable.fill = GridBagConstraints.BOTH;
        gbc_knownPeersTable.gridwidth = 5;
        gbc_knownPeersTable.anchor = GridBagConstraints.SOUTHWEST;
        gbc_knownPeersTable.insets = new Insets(0, 0, 5, 0);
        gbc_knownPeersTable.gridx = 0;
        gbc_knownPeersTable.gridy = 0;
        this.add(new JScrollPane(knownPeersTable), gbc_knownPeersTable);

        //CHECKBOX FOR CONNECTED
        TableColumn confirmedColumn = knownPeersTable.getColumnModel().getColumn(1);
        //     confirmedColumn.setCellRenderer(knownPeersTable.getDefaultRenderer(Boolean.class));
        confirmedColumn.setPreferredWidth(70);
        confirmedColumn.setMaxWidth(100);

        JLabel lblAddNewAddress = new JLabel(Lang.T("Add new address") + ":");
        GridBagConstraints gbc_lblAddNewAddress = new GridBagConstraints();
        gbc_lblAddNewAddress.anchor = GridBagConstraints.NORTHEAST;
        gbc_lblAddNewAddress.insets = new Insets(4, 0, 0, 5);
        gbc_lblAddNewAddress.gridx = 0;
        gbc_lblAddNewAddress.gridy = 1;
        add(lblAddNewAddress, gbc_lblAddNewAddress);


        GridBagConstraints gbc_textAddress = new GridBagConstraints();
        gbc_textAddress.insets = new Insets(0, 0, 0, 5);
        gbc_textAddress.fill = GridBagConstraints.HORIZONTAL;
        gbc_textAddress.gridx = 1;
        gbc_textAddress.gridy = 1;

        textAddress = new JTextField();
        add(textAddress, gbc_textAddress);
        textAddress.setColumns(10);
        textAddress.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textAddress.setPreferredSize(new Dimension(150, 24));

        JButton btnAdd = new JButton(Lang.T("Add"));
        GridBagConstraints gbc_btnAdd = new GridBagConstraints();
        gbc_btnAdd.fill = GridBagConstraints.BOTH;
        gbc_btnAdd.gridwidth = 2;
        gbc_btnAdd.anchor = GridBagConstraints.SOUTHWEST;
        gbc_btnAdd.gridx = 2;
        gbc_btnAdd.gridy = 1;
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onAddClick();
            }
        });
        add(btnAdd, gbc_btnAdd);

        JPopupMenu menu = new JPopupMenu();

        JMenuItem deleteaddressmenu = new JMenuItem(Lang.T("Delete address"));
        deleteaddressmenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = knownPeersTable.getSelectedRow();
                knownPeersTableModel.deleteAddress(row);
            }
        });
        menu.add(deleteaddressmenu);

        JMenuItem connectItem = new JMenuItem(Lang.T("Connect"));
        connectItem.addActionListener(arg0 -> {
            // чтобы развязат задержку и не тормозить GUI
            new Thread(() -> {
                int row = knownPeersTable.getSelectedRow();
                Peer peer = knownPeersTableModel.getItem(row);
                ;
                Controller.getInstance().network.addPeer(peer, 0); // reset BAN if exists
                peer.connect(null, Controller.getInstance().network,
                        "connected as recircled by USER!!! ");
            }).start();
        });
        menu.add(connectItem);

        knownPeersTable.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    int row = knownPeersTable.getSelectedRow();
                    knownPeersTableModel.deleteAddress(row);
                }
            }
        });

        knownPeersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableMenuPopupUtil.installContextMenu(knownPeersTable, menu);

    }

    public void close() {
        //REMOVE OBSERVERS/HANLDERS
        this.knownPeersTableModel.removeObservers();
    }

    public void onAddClick() {
        String addip = this.textAddress.getText();
        IPAddressFormatValidator iPAddressFormatValidator = new IPAddressFormatValidator();
        if (!iPAddressFormatValidator.validate(addip)) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.T("IP Address is not correct!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
        } else {
            knownPeersTableModel.addAddress(addip);
        }
    }


}
