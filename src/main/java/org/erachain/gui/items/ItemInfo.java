package org.erachain.gui.items;

import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.library.MAccoutnTextField;
import org.erachain.gui.library.MTextPane;
import org.erachain.gui.library.SignLibraryPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

public class ItemInfo extends JPanel {

    ItemCls item;

    private JLabel jLabel_Account_Creator;
    private JLabel jLabelIcon = new JLabel();
    private JLabel jLabelContent;
    private JLabel jLabelName;
    private JTextPane textPaneDesc;
    private JScrollPane jScrollPaneDesc;
    private MTextPane jTextArea_Content;
    private MAccoutnTextField jTextField_Account_Creator;
    private JTextField textName;
    private JPanel jPanel_Image;
    protected GridBagConstraints labelGBC;
    protected GridBagConstraints fieldGBC;

    public ItemInfo(ItemCls item, boolean useIcon) {
        this.item = item;

        labelGBC = new java.awt.GridBagConstraints();
        labelGBC.anchor = java.awt.GridBagConstraints.EAST;
        labelGBC.gridx = labelGBC.gridy = 0;
        labelGBC.insets = new java.awt.Insets(0, 0, 5, 0);

        fieldGBC = new java.awt.GridBagConstraints();
        fieldGBC.gridx = 1;
        fieldGBC.gridy = labelGBC.gridy;
        fieldGBC.fill = java.awt.GridBagConstraints.HORIZONTAL;
        fieldGBC.weightx = 0.1;
        fieldGBC.insets = new java.awt.Insets(0, 5, 5, 8);

        textName = new JTextField();
        textName.setText(item.viewName());

        textPaneDesc = new JTextPane();
        jScrollPaneDesc = new JScrollPane();

        jTextArea_Content = new MTextPane();
        jPanel_Image = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[]{0, 0, 8, 0, 8, 0};
        layout.rowHeights = new int[]{0, 4, 0, 4, 0, 4, 0, 4, 0};
        setLayout(layout);
        jLabel_Account_Creator = new JLabel();

        jLabel_Account_Creator.setText(Lang.T("Account Creator") + ":");
        add(jLabel_Account_Creator, labelGBC);

        jTextField_Account_Creator = new MAccoutnTextField(item.getMaker());
        jTextField_Account_Creator.setEditable(false);
        add(jTextField_Account_Creator, fieldGBC);

        jLabelName = new JLabel(Lang.T("Name") + ":");
        ++labelGBC.gridy;
        add(jLabelName, labelGBC);

        textName.setToolTipText("");
        textName.setEditable(false);
        fieldGBC.gridy = labelGBC.gridy;
        add(textName, fieldGBC);

        if (item.getTagsStr() != null) {
            ++labelGBC.gridy;
            add(new JLabel(Lang.T("Tags") + ":"), labelGBC);

            JTextField textTags = new JTextField();
            textTags.setText(item.getTagsStr());
            textTags.setToolTipText("");
            textTags.setEditable(false);
            fieldGBC.gridy = labelGBC.gridy;
            add(textTags, fieldGBC);
        }

        ++labelGBC.gridy;
        if (useIcon) {
            byte[] iconBytes = item.getIcon();
            if (iconBytes != null && iconBytes.length > 0) {
                int rowSize = getFont().getSize() << 2;
                ImageIcon image = new ImageIcon(iconBytes);
                jLabelIcon.setIcon(new ImageIcon(image.getImage().getScaledInstance(rowSize, rowSize, 1)));

                add(jLabelIcon, labelGBC);

            }
        }

    }

    public void initFoot() {

        byte[] imageBytes = item.getImage();
        if (imageBytes != null && imageBytes.length > 0) {
            ImageIcon image = new ImageIcon(imageBytes);
            jLabelContent = new JLabel();
            jLabelContent.setIcon(image);

            fieldGBC.gridy = labelGBC.gridy;
            add(jLabelContent, fieldGBC);

        } else {
            jLabelContent = new JLabel(Lang.T("Content") + ":");
        }

        ++labelGBC.gridy;
        add(jLabelContent, labelGBC);

        textPaneDesc.setContentType("text/html");
        String color = "#" + Integer.toHexString(UIManager.getColor("Panel.background").getRGB()).substring(2);
        String text = "<body style= 'font-family:"
                + UIManager.getFont("Label.font").getFamily() + "; font-size: " + UIManager.getFont("Label.font").getSize() + "pt;'>";

        textPaneDesc.setText(text + item.makeHTMLFootView(false));

        fieldGBC.gridy = labelGBC.gridy;
        add(jScrollPaneDesc, fieldGBC);
        jScrollPaneDesc.setViewportView(textPaneDesc);

        // убирает прокрутка по горизонтали
        jScrollPaneDesc.setPreferredSize(new Dimension(0, 555));

        // vouch panel
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++labelGBC.gridy;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = labelGBC.insets;
        JTabbedPane jTabbedPane1 = new JTabbedPane();

        add(jTabbedPane1, gridBagConstraints);

        Transaction trans = Transaction.findByDBRef(DCSet.getInstance(), item.getReference());
        jTabbedPane1.add(new SignLibraryPanel(trans));

    }

}
