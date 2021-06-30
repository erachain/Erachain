package org.erachain.gui.items.polls;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.IssuePollRecord;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.CreateOptionsTableModel;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import java.util.List;

public class IssuePollPanel extends IssueItemPanel {

    public static String NAME = "IssuePollPanel";
    public static String TITLE = "Issue Poll";

    private CreateOptionsTableModel optionsTableModel;
    private final MTable table;

    public IssuePollPanel() {
        super(NAME, TITLE, "Poll issue has been sent", true, GUIConstants.WIDTH_IMAGE, GUIConstants.WIDTH_IMAGE, true, true);

        initComponents();

        optionsTableModel = new CreateOptionsTableModel(new Object[]{Lang.T("Name")}, 0);

        // вывод верхней панели
        int gridy = initTopArea();

        JLabel optionsLabel = new JLabel(Lang.T("Options") + ":");
        labelGBC.gridy = gridy;
        jPanelAdd.add(optionsLabel, labelGBC);

        // TABLE OPTIONS
        table = new MTable(optionsTableModel);
        JScrollPane scrollPaneOptionalTable = new JScrollPane();
        scrollPaneOptionalTable.setViewportView(table);
        fieldGBC.gridy = gridy++;
        jPanelAdd.add(scrollPaneOptionalTable, fieldGBC);

        JButton deleteButton = new JButton(Lang.T("Delete"));
        deleteButton.addActionListener(e -> deleteRow());
        fieldGBC.gridy = gridy++;
        jPanelAdd.add(deleteButton, fieldGBC);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem copyAddress = new JMenuItem(Lang.T("Delete"));
        copyAddress.addActionListener(e -> deleteRow());
        menu.add(copyAddress);
        TableMenuPopupUtil.installContextMenu(table, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON

        // вывод подвала
        initBottom(gridy);

        setVisible(true);
    }

    protected boolean checkValues() {

        if (optionsTableModel.getRowCount() < 1) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.T("Null Options!"),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (optionsTableModel.getRowCount() == 1 && optionsTableModel.getValueAt(0, 0).equals("")) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.T("Null Options!"),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    protected void makeTransaction() {

        transaction = (IssuePollRecord) Controller.getInstance().issuePoll(itemAppData, creator,
                exLink, nameField.getText(), textAreaDescription.getText(),
                optionsTableModel.getOptions(),
                addIconLabel.getMediaBytes(), addImageLabel.getMediaBytes(), feePow);

    }

    protected String makeTransactionView() {

        String text = "<HTML><body>";
        text += Lang.T("Confirmation Transaction") + ":&nbsp;"
                + Lang.T("Issue Voting") + "<br><br><br>"
                + makeHeadView("Title");
        text += "<br>" + Lang.T("Description") + ":<br>"
                + Library.to_HTML(this.textAreaDescription.getText()) + "<br>";
        text += "<br>" + Lang.T("Options") + ":<br>";

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
