package org.erachain.gui.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.CreatePollTransaction;
import org.erachain.gui.Gui;
import org.erachain.gui.models.PollOptionsTableModel;
import org.erachain.lang.Lang;
import org.erachain.utils.BigDecimalStringComparator;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.table.TableRowSorter;

@SuppressWarnings("serial")
public class CreatePollDetailsFrame extends RecDetailsFrame {
    @SuppressWarnings("unchecked")
    public CreatePollDetailsFrame(CreatePollTransaction pollCreation) {
        super(pollCreation, true);

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.T("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++fieldGBC.gridy;
        JTextField name = new JTextField(pollCreation.getPoll().getName());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, fieldGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.T("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++fieldGBC.gridy;
        JTextArea txtAreaDescription = new JTextArea(pollCreation.getPoll().getDescription());
        txtAreaDescription.setRows(4);
        txtAreaDescription.setBorder(name.getBorder());
        txtAreaDescription.setEditable(false);
        MenuPopupUtil.installContextMenu(txtAreaDescription);
        this.add(txtAreaDescription, fieldGBC);

        //LABEL OPTIONS
        ++labelGBC.gridy;
        JLabel optionsLabel = new JLabel(Lang.T("Options") + ":");
        this.add(optionsLabel, labelGBC);

        //OPTIONS
        ++fieldGBC.gridy;
        PollOptionsTableModel pollOptionsTableModel = new PollOptionsTableModel(pollCreation.getPoll(),
                Controller.getInstance().getAsset(AssetCls.FEE_KEY));
        JTable table = Gui.createSortableTable(pollOptionsTableModel, 0);

        TableRowSorter<PollOptionsTableModel> sorter = (TableRowSorter<PollOptionsTableModel>) table.getRowSorter();
        sorter.setComparator(PollOptionsTableModel.COLUMN_VOTES, new BigDecimalStringComparator());

        this.add(new JScrollPane(table), fieldGBC);

        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //    this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
