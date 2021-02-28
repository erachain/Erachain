package org.erachain.gui.transaction;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.transaction.RSetStatusToItem;
import org.erachain.gui.library.MTextPane;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("serial")
public class SetStatusToItemDetailsFrame extends RecDetailsFrame {
    public SetStatusToItemDetailsFrame(RSetStatusToItem setStatusToItem) {
        super(setStatusToItem, true);

        ItemCls item = setStatusToItem.getItem();
        StatusCls status = setStatusToItem.getStatus();

        //LABEL RESULT
        ++labelGBC.gridy;
        JLabel resutLabel = new JLabel(Lang.T("Result") + ":");
        this.add(resutLabel, labelGBC);

        //RESULT
        ++fieldGBC.gridy;
        JTextField result = new JTextField(setStatusToItem.getResultText());
        result.setEditable(false);
        MenuPopupUtil.installContextMenu(result);
        this.add(result, fieldGBC);

        // STATUS
        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.T("Status Name") + ":");
        this.add(nameLabel, labelGBC);

        ////// STATUS
        ++fieldGBC.gridy;
        JTextField statusName = new JTextField(status.viewName());
        statusName.setEditable(false);
        MenuPopupUtil.installContextMenu(statusName);
        this.add(statusName, fieldGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.T("Status Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++fieldGBC.gridy;
        MTextPane txtAreaDescription = new MTextPane(status.getDescription());
        //txtAreaDescription.setRows(4);
        txtAreaDescription.setBorder(statusName.getBorder());
        //txtAreaDescription.setEditable(false);
        MenuPopupUtil.installContextMenu(txtAreaDescription);
        this.add(txtAreaDescription, fieldGBC);

        // FROM - TO DATE
        ++labelGBC.gridy;
        this.add(new JLabel(Lang.T("From - To") + ":"), labelGBC);
        ++fieldGBC.gridy;
        long beginDate = setStatusToItem.getBeginDate();
        long endDate = setStatusToItem.getEndDate();

        JTextField fromToDate = new JTextField((beginDate == Long.MIN_VALUE ? "?" : DateTimeFormat.timestamptoString(beginDate))
                + " - " + (endDate == Long.MAX_VALUE ? "?" : DateTimeFormat.timestamptoString(endDate)));
        fromToDate.setEditable(false);
        MenuPopupUtil.installContextMenu(fromToDate);
        this.add(fromToDate, fieldGBC);

        if (setStatusToItem.getValue1() != 0) {
            //LABEL VALUE 1
            ++labelGBC.gridy;
            this.add(new JLabel(Lang.T("Value") + " 1:"), labelGBC);
            //VALUE 1
            ++fieldGBC.gridy;
            JTextField statusValue1 = new JTextField("" + setStatusToItem.getValue1());
            statusValue1.setEditable(false);
            MenuPopupUtil.installContextMenu(statusValue1);
            this.add(statusValue1, fieldGBC);
        }

        if (setStatusToItem.getValue2() != 0) {
            //LABEL VALUE 2
            ++labelGBC.gridy;
            this.add(new JLabel(Lang.T("Value") + " 2:"), labelGBC);
            //VALUE 2
            ++fieldGBC.gridy;
            JTextField statusValue2 = new JTextField("" + setStatusToItem.getValue2());
            statusValue2.setEditable(false);
            MenuPopupUtil.installContextMenu(statusValue2);
            this.add(statusValue2, fieldGBC);
        }

        if (setStatusToItem.getData1() != null) {

            //LABEL ADDITION DATA
            ++labelGBC.gridy;
            this.add(new JLabel(Lang.T("DATA") + " 1:"), labelGBC);
            //DATA
            ++fieldGBC.gridy;
            JTextField statusAData = new JTextField(new String(setStatusToItem.getData1(), StandardCharsets.UTF_8));
            statusAData.setEditable(false);
            MenuPopupUtil.installContextMenu(statusAData);
            this.add(statusAData, fieldGBC);
        }
        if (setStatusToItem.getData2() != null) {

            //LABEL ADDITION DATA
            ++labelGBC.gridy;
            this.add(new JLabel(Lang.T("DATA") + " 2:"), labelGBC);
            //DATA
            ++fieldGBC.gridy;
            JTextField statusAData = new JTextField(new String(setStatusToItem.getData2(), StandardCharsets.UTF_8));
            statusAData.setEditable(false);
            MenuPopupUtil.installContextMenu(statusAData);
            this.add(statusAData, fieldGBC);
        }

        if (setStatusToItem.getRefParent() != 0l) {

            //LABEL PARENT
            ++labelGBC.gridy;
            this.add(new JLabel(Lang.T("Parent") + ":"), labelGBC);
            //DATA
            ++fieldGBC.gridy;
            JTextField statusRefParent = new JTextField("" + setStatusToItem.viewRefParent());
            statusRefParent.setEditable(false);
            MenuPopupUtil.installContextMenu(statusRefParent);
            this.add(statusRefParent, fieldGBC);
        }

        if (setStatusToItem.getDescription() != null) {

            //LABEL ADDITION DATA
            ++labelGBC.gridy;
            this.add(new JLabel(Lang.T("Description") + ":"), labelGBC);
            //DATA
            ++fieldGBC.gridy;
            JTextArea descrData = new JTextArea(new String(setStatusToItem.getDescription(), StandardCharsets.UTF_8));
            descrData.setRows(4);
            descrData.setBorder(statusName.getBorder());
            descrData.setEditable(false);
            MenuPopupUtil.installContextMenu(descrData);
            this.add(descrData, fieldGBC);

        }

        // //// ITEM
        //LABEL NAME
        ++labelGBC.gridy;
        JLabel itemNameLabel = new JLabel(Lang.T("Item Name") + ":");
        this.add(itemNameLabel, labelGBC);

        //NAME
        ++fieldGBC.gridy;
        JTextField itemName = new JTextField(item.getItemTypeName() + " - " + item.getItemSubType()
                + ": " + item.viewName());
        itemName.setEditable(false);
        MenuPopupUtil.installContextMenu(itemName);
        this.add(itemName, fieldGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel itemDescriptionLabel = new JLabel(Lang.T("Item Description") + ":");
        this.add(itemDescriptionLabel, labelGBC);

        //DESCRIPTION
        ++fieldGBC.gridy;
        MTextPane txtAreaItemDescription = new MTextPane(item.getDescription());
        //txtAreaItemDescription.setRows(4);
        txtAreaItemDescription.setBorder(itemName.getBorder());
        //txtAreaItemDescription.setEditable(false);
        MenuPopupUtil.installContextMenu(txtAreaItemDescription);
        this.add(txtAreaItemDescription, fieldGBC);

        //PACK
        //	this.pack();
        //     this.setResizable(false);
        //     this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
