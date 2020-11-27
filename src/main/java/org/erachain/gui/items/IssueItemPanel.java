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
    // description asset type
    protected JTextArea textareasAssetTypeDescription = new JTextArea();
    protected MDecimalFormatedTextField textQuantity = new MDecimalFormatedTextField();

    protected AddImageLabel addImageLabel;
    protected AddImageLabel addLogoIconLabel;
    protected JScrollPane jScrollPane2;
    protected JScrollPane jScrollPane3 = new JScrollPane();
    protected JTextField jTextFieldReference = new JTextField();
    protected JLabel jLabelReference = new JLabel(Lang.getInstance().translate("Reference") + ":");
    protected JTextField jTextFieldItem1 = new JTextField();
    protected JLabel jLabelItem1 = new JLabel(Lang.getInstance().translate("item1") + ":");

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
//
        initComponents();
        titleJLabel.setFont(FONT_TITLE);
        titleJLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleJLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        titleJLabel.setText(Lang.getInstance().translate(title));
        textAreaDescription.setLineWrap(true);
        textQuantity.setMaskType(textQuantity.maskLong);
        textQuantity.setText("0");
        textFeePow.setModel(new DefaultComboBoxModel<>(fillAndReceiveStringArray(9)));
        textFeePow.setSelectedItem("0");

        feeJLabel.setVisible(Gui.SHOW_FEE_POWER);
        textFeePow.setVisible(Gui.SHOW_FEE_POWER);

        issueJButton.addActionListener(arg0 -> onIssueClick());

    }

    protected void initComponents() {
        GridBagConstraints gridBagConstraints;
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        layout.rowHeights = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        setLayout(layout);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(addLogoIconLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        add(accountJLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 17;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.6;
        add(fromJComboBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 17;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.6;
        add(textName, gridBagConstraints);

        nameJLabel.setToolTipText("");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        add(nameJLabel, gridBagConstraints);

        jScrollPane3.setBorder(null);
        jScrollPane3.setViewportView(addImageLabel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.gridheight = 11;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.1;
        add(jScrollPane3, gridBagConstraints);

        textareasAssetTypeDescription.setEditable(false);
        textareasAssetTypeDescription.setColumns(20);
        textareasAssetTypeDescription.setRows(5);
        textareasAssetTypeDescription.setBorder(null);
        textareasAssetTypeDescription.setDoubleBuffered(true);
        textareasAssetTypeDescription.setDragEnabled(true);
        jScrollPane1.setViewportView(textareasAssetTypeDescription);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new Insets(0, 0, 0, 8);
        add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        add(textQuantity, gridBagConstraints);

        textAreaDescription.setColumns(20);
        textAreaDescription.setRows(5);
        jScrollPane2.setViewportView(textAreaDescription);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new Insets(0, 0, 8, 8);
        add(jScrollPane2, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        add(descriptionJLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 26;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(0, 0, 0, 8);
        add(issueJButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 27;
        gridBagConstraints.insets = new Insets(7, 7, 0, 7);
        add(titleJLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        add(jLabelItem1, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 0, 0, 8);
        add(jTextFieldItem1, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        add(jLabelReference, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 0, 0, 8);
        add(jTextFieldReference, gridBagConstraints);
    }

    protected String[] fillAndReceiveStringArray(int size) {
        String[] modelTextScale = new String[size];
        for (int i = 0; i < modelTextScale.length; i++) {
            modelTextScale[i] = i + "";
        }
        return modelTextScale;
    }

    protected abstract void onIssueClick();

}
