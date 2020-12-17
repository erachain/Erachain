package org.erachain.gui.exdata;


import org.erachain.core.BlockChain;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.exdata.items.DocTypeComboBox.DocTypeComboBox;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class DocTypeAppendixPanel extends JPanel {

    ExDataPanel exPanel;
    Transaction parentTx;

    /**
     * Creates new form NewJPanel1
     */
    public DocTypeAppendixPanel(ExDataPanel exPanel) {
        this.exPanel = exPanel;

        initComponents();
        labelDocType.setText(Lang.getInstance().translate("Parent Document SeqNo or Signature"));
        parentReference.setToolTipText(Lang.getInstance().translate("Example") + ": 1234-12 or r6fas657w12Y65da..");
        estimationPanel.setVisible(false);


    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        typeDocumentLabel = new JLabel(Lang.getInstance().translate("Type"));
        typeDocymentCombox = new DocTypeComboBox();

        typeDocymentCombox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {

                    int fontSize = typeDescription.getFontMetrics(typeDescription.getFont()).getHeight();
                    String fontStyle = typeDescription.getFont().getFontName();
                    fontStyle = "<body style='font: " + (fontSize - 2) + "pt " + fontStyle + "'>";

                    exPanel.updateRecipients();
                    estimationPanel.setVisible(false);
                    // возвращаем выбранный объект
                    int item = (int) e.getItem();
                    if (item == 0) {
                        typeDescription.setVisible(false);
                        labelDocType.setVisible(false);
                        parentReference.setVisible(false);
                        parentDetails.setVisible(false);
                        estimationPanel.setVisible(false);
                    } else {
                        typeDescription.setVisible(true);
                        labelDocType.setVisible(true);
                        parentReference.setVisible(true);
                        parentDetails.setVisible(true);
                        switch (item) {
                            case ExData.LINK_APPENDIX_TYPE:
                                typeDescription.setText(fontStyle + Lang.getInstance().translate("Set parent Transaction for Appendix below")
                                        + ".<br?<br>" + Lang.getInstance().translate("LINK_APPENDIX_TYPE"));
                                estimationPanel.setVisible(false);
                                break;
                            case ExData.LINK_REPLY_COMMENT_TYPE:
                                typeDescription.setText(fontStyle + Lang.getInstance().translate("Set parent Transaction for Reply below")
                                        + ".<br><br>" + Lang.getInstance().translate("LINK_REPLY_COMMENT_TYPE"));
                                estimationPanel.setVisible(BlockChain.TEST_MODE);
                                break;
                            case ExData.LINK_COMMENT_TYPE_FOR_VIEW:
                                typeDescription.setText(fontStyle + Lang.getInstance().translate("Set parent Transaction for Comment below")
                                        + ".<br><br>" + Lang.getInstance().translate("LINK_COMMENT_TYPE_FOR_VIEW"));
                                estimationPanel.setVisible(BlockChain.TEST_MODE);
                                break;
                            default:
                                typeDescription.setText(Lang.getInstance().translate(fontStyle + "Set Parent Document"));

                        }
                    }
                }
            }
        });

        labelDocType = new JLabel();
        filler1 = new Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 32767));
        typeDescription = new JTextPane();
        typeDescription.setEditable(false);
        typeDescription.setBackground(this.getBackground());
        typeDescription.setContentType("text/html");
        //labelTitle.setEnabled(false);

        parentDetails = new JLabel();

        parentReference = new JTextField();
        estimateType1 = new JComboBox<String>(new String[]{"", "Неважно", "Бесполезно", "Важно", "Очень важно"});
        estimateTypeValue1 = new JComboBox<String>(new String[]{"1", "2"});
        estimateType2 = new JComboBox<String>(new String[]{"", "Жутко", "Страшно", "Интересно", "Весело", "Смешно", "Умора"});
        estimateTypeValue2 = new JComboBox<String>(new String[]{"1", "2"});

        this.parentReference.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                refreshParentDetails();
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                refreshParentDetails();
            }
        });

        tagsLabel = new JLabel(Lang.getInstance().translate("Tags"));
        tagsField = new JTextField();

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
       // layout.columnWidths = new int[]{0, 5, 0};
       // layout.rowHeights = new int[]{0, 5, 0, 5, 0};
        setLayout(layout);

        JPanel panel = new JPanel();
        java.awt.GridBagLayout panelLayout = new java.awt.GridBagLayout();
        panel.setLayout(panelLayout);

        int dridY = 0;

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = dridY;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        add(tagsLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = dridY;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(tagsField, gridBagConstraints);
        dridY++;

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = dridY;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        panel.add(typeDocumentLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = dridY;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panel.add(typeDocymentCombox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = dridY;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        add(panel, gridBagConstraints);

        dridY++;

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = dridY;//0;
        gridBagConstraints.gridwidth = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
        add(typeDescription, gridBagConstraints);
        dridY++;

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = dridY;//2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        add(labelDocType, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = dridY;//2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 8);
        add(parentReference, gridBagConstraints);
        dridY++;

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = dridY;//3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
        add(parentDetails, gridBagConstraints);

//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = ++dridY;
//        gridBagConstraints.gridwidth = 3;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//        gridBagConstraints.weightx = 0.3;
//        gridBagConstraints.weighty = 0.2;
//        add(filler1, gridBagConstraints);
//estimation

        estimationPanel = new JPanel();
        estimationPanel.setLayout(new GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);
        estimationPanel.add(new JLabel(Lang.getInstance().translate("Estimate") + " 1: "), gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        estimationPanel.add(estimateType1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);
        estimationPanel.add(new JLabel(Lang.getInstance().translate("Value") + " 1: "), gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        estimationPanel.add(estimateTypeValue1, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);
        estimationPanel.add(new JLabel(Lang.getInstance().translate("Estimate") + " 2: "), gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        estimationPanel.add(estimateType2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        estimationPanel.add(new JLabel(Lang.getInstance().translate("Value") + " 2: "), gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        estimationPanel.add(estimateTypeValue2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++dridY;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(estimationPanel, gridBagConstraints);

        // botoom
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++dridY;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(new JLabel(""), gridBagConstraints);

        typeDescription.setVisible(false);
        labelDocType.setVisible(false);
        parentReference.setVisible(false);
        parentDetails.setVisible(false);

    }// </editor-fold>

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    // Variables declaration - do not modify
    private Box.Filler filler1;
    private JLabel labelDocType;
    private JTextPane typeDescription;
    public JTextField parentReference;
    private JLabel parentDetails;
    private JLabel typeDocumentLabel;
    private DocTypeComboBox typeDocymentCombox;

    private JLabel tagsLabel;
    public JTextField tagsField;

    public JComboBox<String> estimateType1;
    public JComboBox<String> estimateType2;
    public JComboBox<String> estimateTypeValue1;
    public JComboBox<String> estimateTypeValue2;
    public JPanel estimationPanel;

    // End of variables declaration

    private void refreshParentDetails() {

        parentTx = null;

        String docRef = parentReference.getText();
        Long parentDBref = Transaction.parseDBRef(docRef);
        if (parentDBref == null) {
            if (Base58.isExtraSymbols(docRef)) {
                parentDetails.setText(Lang.getInstance().translate("Not Base58 signature"));
                return;
            }

            byte[] signature = Base58.decode(docRef);
            if (DCSet.getInstance().getTransactionFinalMapSigns().contains(signature)) {
                parentTx = DCSet.getInstance().getTransactionFinalMap().get(signature);
            } else {
                parentDetails.setText(Lang.getInstance().translate("Not Found"));
                return;
            }
        } else {
            if (DCSet.getInstance().getTransactionFinalMap().contains(parentDBref)) {
                parentTx = DCSet.getInstance().getTransactionFinalMap().get(parentDBref);
                if (parentTx.getCreator() == null) {
                    parentDetails.setText(Lang.getInstance().translate("Empty Creator in parent transaction"));
                    return;
                }
            } else {
                parentDetails.setText(Lang.getInstance().translate("Not Found"));
                return;
            }
        }

        parentDetails.setText(parentTx.toStringShortAsCreator());

        exPanel.updateRecipients();

    }

    public int getSelectedItem() {
        return (int) typeDocymentCombox.getSelectedItem();

    }

}