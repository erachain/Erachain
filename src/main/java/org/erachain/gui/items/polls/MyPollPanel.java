package org.erachain.gui.items.polls;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.voting.Poll;
import org.erachain.database.wallet.PollMap;
import org.erachain.gui.CoreRowSorter;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.WalletVotesTableModel;
import org.erachain.gui.voting.PollFrame;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class MyPollPanel extends JPanel {
    public MyPollPanel() {
        this.setLayout(new GridBagLayout());
        setName(Lang.getInstance().translate("My Votings"));

        //PADDING
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        //TABLE GBC
        GridBagConstraints tableGBC = new GridBagConstraints();
        tableGBC.fill = GridBagConstraints.BOTH;
        tableGBC.anchor = GridBagConstraints.NORTHWEST;
        tableGBC.weightx = 1;
        tableGBC.weighty = 1;
        tableGBC.gridwidth = 10;
        tableGBC.gridx = 0;
        tableGBC.gridy = 0;

        //BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(10, 0, 0, 10);
        buttonGBC.fill = GridBagConstraints.NONE;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridx = 0;
        buttonGBC.gridy = 1;

        //TABLE
        final WalletVotesTableModel pollsModel = new WalletVotesTableModel();
        final MTable table = new MTable(pollsModel);

        //POLLS SORTER
        Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
        indexes.put(WalletVotesTableModel.COLUMN_NAME, PollMap.NAME_INDEX);
        indexes.put(WalletVotesTableModel.COLUMN_ADDRESS, PollMap.CREATOR_INDEX);
        CoreRowSorter sorter = new CoreRowSorter(pollsModel, indexes);
        table.setRowSorter(sorter);

        //CHECKBOX FOR CONFIRMED
        TableColumn confirmedColumn = table.getColumnModel().getColumn(3);
        confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                table.setRowSelectionInterval(row, row);
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                table.setRowSelectionInterval(row, row);

                if (e.getClickCount() == 2) {
                    row = table.convertRowIndexToModel(row);
                    Poll poll = pollsModel.getItem(row);
                    new PollFrame(poll, Controller.getInstance().getAsset(AssetCls.FEE_KEY));
                }
            }
        });

        //ADD NAMING SERVICE TABLE
        this.add(new JScrollPane(table), tableGBC);


    }

    public void onCreateClick() {
//		new CreatePollFrame();
    }

    public void onAllClick() {
//		new AllPollsFrame();
    }
}
