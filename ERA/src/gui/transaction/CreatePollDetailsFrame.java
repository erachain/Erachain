package gui.transaction;

import controller.Controller;
import core.item.assets.AssetCls;
import core.transaction.CreatePollTransaction;
import gui.Gui;
import gui.models.PollOptionsTableModel;
import lang.Lang;
import utils.BigDecimalStringComparator;
import utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.table.TableRowSorter;

@SuppressWarnings("serial")
public class CreatePollDetailsFrame extends Rec_DetailsFrame {
    @SuppressWarnings("unchecked")
    public CreatePollDetailsFrame(CreatePollTransaction pollCreation) {
        super(pollCreation);

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++detailGBC.gridy;
        JTextField name = new JTextField(pollCreation.getPoll().getName());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++detailGBC.gridy;
        JTextArea txtAreaDescription = new JTextArea(pollCreation.getPoll().getDescription());
        txtAreaDescription.setRows(4);
        txtAreaDescription.setBorder(name.getBorder());
        txtAreaDescription.setEditable(false);
        MenuPopupUtil.installContextMenu(txtAreaDescription);
        this.add(txtAreaDescription, detailGBC);

        //LABEL OPTIONS
        ++labelGBC.gridy;
        JLabel optionsLabel = new JLabel(Lang.getInstance().translate("Options") + ":");
        this.add(optionsLabel, labelGBC);

        //OPTIONS
        ++detailGBC.gridy;
        PollOptionsTableModel pollOptionsTableModel = new PollOptionsTableModel(pollCreation.getPoll(),
                Controller.getInstance().getAsset(AssetCls.FEE_KEY));
        JTable table = Gui.createSortableTable(pollOptionsTableModel, 0);

        TableRowSorter<PollOptionsTableModel> sorter = (TableRowSorter<PollOptionsTableModel>) table.getRowSorter();
        sorter.setComparator(PollOptionsTableModel.COLUMN_VOTES, new BigDecimalStringComparator());

        this.add(new JScrollPane(table), detailGBC);

        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //    this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
