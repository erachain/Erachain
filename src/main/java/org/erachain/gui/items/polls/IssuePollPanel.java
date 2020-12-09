package org.erachain.gui.items.polls;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.IssuePollRecord;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.CreateOptionsTableModel;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class IssuePollPanel extends IssueItemPanel {

    public static String NAME = "IssuePollPanel";
    public static String TITLE = "Issue Poll";

    private CreateOptionsTableModel optionsTableModel;
    private final MTable table;

    public IssuePollPanel() {
        super(NAME, TITLE, "Poll issue has been sent");

        initComponents();

        optionsTableModel = new CreateOptionsTableModel(new Object[]{Lang.getInstance().translate("Name")}, 0);

        // вывод верхней панели
        int gridy = initTopArea();

        JLabel optionsLabel = new JLabel(Lang.getInstance().translate("Options") + ":");
        labelGBC.gridy = gridy;
        jPanelMain.add(optionsLabel, labelGBC);

        // TABLE OPTIONS
        GridBagConstraints gbcOptionalTable = new GridBagConstraints();
        gbcOptionalTable.gridx = 8;
        gbcOptionalTable.gridy = gridy++;
        gbcOptionalTable.weighty = 0.9;
        gbcOptionalTable.gridwidth = 3;
        gbcOptionalTable.fill = GridBagConstraints.BOTH;
        gbcOptionalTable.anchor = GridBagConstraints.CENTER;
        table = new MTable(optionsTableModel);
        JScrollPane scrollPaneOptionalTable = new JScrollPane();
        scrollPaneOptionalTable.setViewportView(table);
        jPanelMain.add(scrollPaneOptionalTable, gbcOptionalTable);

        JButton deleteButton = new JButton(Lang.getInstance().translate("Delete"));
        deleteButton.addActionListener(e -> deleteRow());
        GridBagConstraints gbcDeleteButton = new GridBagConstraints();
        gbcDeleteButton.gridx = 8;
        gbcDeleteButton.gridy = gridy++;
        gbcDeleteButton.fill = GridBagConstraints.HORIZONTAL;
        gbcDeleteButton.gridwidth = 2;
        jPanelMain.add(deleteButton, gbcDeleteButton);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Delete"));
        copyAddress.addActionListener(e -> deleteRow());
        menu.add(copyAddress);
        TableMenuPopupUtil.installContextMenu(table, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON

        // вывод подвала
        initBottom(gridy);

        setVisible(true);
    }

    protected boolean checkValues() {

        if (optionsTableModel.getRowCount() < 1) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Null Options!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (optionsTableModel.getRowCount() == 1 && optionsTableModel.getValueAt(0, 0).equals("")) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Null Options!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    protected void makeTransaction() {

        transaction = (IssuePollRecord) Controller.getInstance().issuePoll(creator,
                exLink, textName.getText(), textAreaDescription.getText(),
                optionsTableModel.getOptions(),
                addLogoIconLabel.getImgBytes(), addImageLabel.getImgBytes(), feePow);

    }

    protected String makeTransactionView() {

        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Issue Voting") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + transaction.getCreator() + "<br>"
                + (exLink == null ? "" : Lang.getInstance().translate("Append to") + ":&nbsp;<b>" + exLink.viewRef() + "</b><br>");
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + this.textName.getText() + "<br>";
        text += "<br>" + Lang.getInstance().translate("Description") + ":<br>"
                + Library.to_HTML(this.textAreaDescription.getText()) + "<br>";
        text += "<br>" + Lang.getInstance().translate("Options") + ":<br>";

        List<String> options = optionsTableModel.getOptions();

        for (int i = 0; i < options.size(); i++) {
            text += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + options.get(i);

        }
        text += "<br>    ";

        return text;

    }

    private void deleteRow() {
        if (optionsTableModel.getRowCount() > 1) {
            int selRow = table.getSelectedRow();
            if (selRow != -1) {
                optionsTableModel.removeRow(selRow);
            }
        }
    }
}
