package org.erachain.gui.items;

import org.erachain.core.account.Account;
import org.erachain.core.item.ItemCls;
import org.erachain.gui.Gui;
import org.erachain.gui.IconPanel;
import org.erachain.gui.library.AddImageLabel;
import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

import static org.erachain.gui.items.utils.GUIConstants.*;

/**
 * @author Саша
 *   insert item issue info
 *        use  cells[x,y] = [4,3]....[26,29]
 *
 */
public abstract class IssueItemPanel extends IconPanel {

    protected JLabel titleJLabel = new JLabel();
    protected JLabel accountJLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
    protected JLabel nameJLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
    protected JLabel descriptionJLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
    protected JLabel feeJLabel = new JLabel(Lang.getInstance().translate("Fee Power") + ":");
    protected JComboBox<String> textFeePow = new JComboBox<>();
    protected JComboBox<Account> fromJComboBox = new JComboBox<>(new AccountsComboBoxModel());
    protected JButton issueJButton = new JButton(Lang.getInstance().translate("Issue"));
    protected JScrollPane jScrollPane1 = new JScrollPane();
    protected JTextField textName = new JTextField("");
    protected JTextArea textAreaDescription = new JTextArea("");
    protected AddImageLabel addImageLabel;
    protected AddImageLabel addLogoIconLabel;
    protected JScrollPane jScrollPane2;
    protected JScrollPane jScrollPane3 = new JScrollPane();
    protected JScrollPane mainJScrollPane  = new javax.swing.JScrollPane();
    protected JPanel jPanelMain = new javax.swing.JPanel();
    protected JPanel jPanelLeft = new javax.swing.JPanel();
    protected GridBagConstraints gridBagConstraints;
    protected JLabel exLinkTextLabel = new JLabel (Lang.getInstance().translate("Append to") + ":");
    protected JLabel exLinkDescriptionLabel = new JLabel(Lang.getInstance().translate("Parent") + ":");
    protected JTextField exLinkText = new JTextField();
    protected JTextField exLinkDescription = new JTextField();


    public IssueItemPanel(String name, String title) {
        super(name, title);
// init
        jScrollPane2 = new JScrollPane();
        addImageLabel = new AddImageLabel(
                Lang.getInstance().translate("Add image"), WIDTH_IMAGE, HEIGHT_IMAGE, TypeOfImage.JPEG,
                0, ItemCls.MAX_IMAGE_LENGTH, WIDTH_IMAGE_INITIAL, HEIGHT_IMAGE_INITIAL);
        addImageLabel.setBorder(null);
        addLogoIconLabel = new AddImageLabel(Lang.getInstance().translate("Add Logo"),
                WIDTH_LOGO, HEIGHT_LOGO, TypeOfImage.GIF,
                0, ItemCls.MAX_ICON_LENGTH, WIDTH_LOGO_INITIAL, HEIGHT_LOGO_INITIAL);
        addLogoIconLabel.setBorder(null);
        addImageLabel.setImageHorizontalAlignment(SwingConstants.LEFT);
        addLogoIconLabel.setImageHorizontalAlignment(SwingConstants.LEFT);
        titleJLabel.setFont(FONT_TITLE);
        titleJLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleJLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        titleJLabel.setText(Lang.getInstance().translate(title));
        textAreaDescription.setLineWrap(true);
        textFeePow.setModel(new DefaultComboBoxModel<>(fillAndReceiveStringArray(9)));
        textFeePow.setSelectedItem("0");
        feeJLabel.setVisible(Gui.SHOW_FEE_POWER);
        textFeePow.setVisible(Gui.SHOW_FEE_POWER);
        issueJButton.addActionListener(arg0 -> onIssueClick());
    }

    protected void initComponents() {


        setLayout(new java.awt.BorderLayout());

        mainJScrollPane.setBorder(null);

        jPanelMain.setLayout(new java.awt.GridBagLayout());

        jPanelLeft.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
        jPanelLeft.add(addLogoIconLabel, gridBagConstraints);

        jScrollPane3.setBorder(null);

        jScrollPane3.setViewportView(addImageLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanelLeft.add(addImageLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 38;
     //   gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    //    gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        jPanelMain.add(jPanelLeft, gridBagConstraints);


        mainJScrollPane.setViewportView(jPanelMain);

        this.add(mainJScrollPane, java.awt.BorderLayout.CENTER);
    }

    protected String[] fillAndReceiveStringArray(int size) {
        String[] modelTextScale = new String[size];
        for (int i = 0; i < modelTextScale.length; i++) {
            modelTextScale[i] = i + "";
        }
        return modelTextScale;
    }

    protected abstract void onIssueClick();
    //
    // выводит верхние поля панели
    // возвращает номер сроки с которой можно продолжать вывод инфы на панель
    protected  int initTopArea(){
        int y = 0;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 27;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanelMain.add(titleJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(accountJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelMain.add(fromJComboBox, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(nameJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelMain.add(textName, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(exLinkTextLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanelMain.add(exLinkText, gridBagConstraints);

        exLinkDescriptionLabel.setText(Lang.getInstance().translate("Parent") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 17;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(exLinkDescriptionLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 19;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelMain.add(exLinkDescription, gridBagConstraints);


        return y;
    }

    // выводит нижние поля панели
    // принимает номер сроки с которой  продолжать вывод полей на нижнюю панель
    protected void initBottom(int y){


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(descriptionJLabel, gridBagConstraints);

        textAreaDescription.setColumns(20);
        textAreaDescription.setRows(5);
        jScrollPane1.setViewportView(textAreaDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 20;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelMain.add(jScrollPane1, gridBagConstraints);



        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(feeJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        jPanelMain.add(textFeePow, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 5, 8);
        jPanelMain.add(issueJButton, gridBagConstraints);


    }

}
