package org.erachain.gui.exdata;

import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.exdata.items.DocTypeComboBox.DocTypeComboBox;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class DocTypeAppendixPanel extends JPanel {

         /**
         * Creates new form NewJPanel1
         */
        public DocTypeAppendixPanel() {
            initComponents();
            labelDocType.setText(Lang.getInstance().translate("Parent Document SeqNo or Signature"));
            parentReference.setToolTipText(Lang.getInstance().translate("Example") + ": 1234-12 or r6fas657w12Y65da..");
        }

        private void initComponents() {
            java.awt.GridBagConstraints gridBagConstraints;
            typeDocumentLabel = new JLabel(Lang.getInstance().translate("Type"));
            typeDocymentCombox = new DocTypeComboBox();
            typeDocymentCombox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange()==ItemEvent.SELECTED) {
                        // возвращаем выбранный объект
                        int item = (int) e.getItem();
                        if (item == 0) {
                            labelTitle.setVisible(false);
                            labelDocType.setVisible(false);
                            parentReference.setVisible(false);
                            parentDetails.setVisible(false);
                        } else {
                            labelTitle.setVisible(true);
                            labelDocType.setVisible(true);
                            parentReference.setVisible(true);
                            parentDetails.setVisible(true);
                            switch (item) {
                                case ExData.LINK_APPENDIX_TYPE:
                                    labelTitle.setText(Lang.getInstance().translate("Set parent Document for Appendix")
                                            + ". " + Lang.getInstance().translate("will be reset Recipients list to involved accounts list by default")
                                            + ". " + Lang.getInstance().translate("But You may edit Recipients list"));
                                    break;
                                case ExData.LINK_REPLY_COMMENT_TYPE:
                                    labelTitle.setText(Lang.getInstance().translate("Set parent Document for Reply")
                                            + ". " + Lang.getInstance().translate("will be reset Recipients list to involved accounts list by default")
                                            + ". " + Lang.getInstance().translate("But You may edit Recipients list"));
                                    break;
                                case ExData.LINK_COMMENT_TYPE_FOR_VIEW:
                                    labelTitle.setText(Lang.getInstance().translate("Set parent Document for Comment")
                                            + ". " + Lang.getInstance().translate("will be erase Recipients list by default")
                                            + ". " + Lang.getInstance().translate("But You may edit Recipients list"));
                                    break;
                                default:
                                    labelTitle.setText(Lang.getInstance().translate("Set Parent Document"));
                            }
                        }
                    }
                }
            });

            labelDocType = new JLabel();
            filler1 = new Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0),
                    new java.awt.Dimension(0, 32767));
            labelTitle = new JTextArea();
            labelTitle.setEditable(false);
            //labelTitle.setEnabled(false);

            parentDetails = new JLabel();

            parentReference = new JTextField();
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

            java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
            layout.columnWidths = new int[]{0, 5, 0};
            layout.rowHeights = new int[]{0, 5, 0, 5, 0};
            setLayout(layout);

            JPanel panel = new JPanel();
            java.awt.GridBagLayout panelLayout = new java.awt.GridBagLayout();
            panel.setLayout(panelLayout);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
            panel.add(typeDocumentLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
            panel.add(typeDocymentCombox, gridBagConstraints);

             int dridY = 0;

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
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
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
            gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
            add(labelTitle, gridBagConstraints);
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


            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.3;
            gridBagConstraints.weighty = 0.2;
            add(filler1, gridBagConstraints);

            labelTitle.setVisible(false);
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
    private JTextArea labelTitle;
    public JTextField parentReference;
    private JLabel parentDetails;
    private JLabel typeDocumentLabel;
    private DocTypeComboBox typeDocymentCombox;
    // End of variables declaration

    private void refreshParentDetails() {

        Transaction parent = null;

        String docRef = parentReference.getText();
        Long parentDBref = Transaction.parseDBRef(docRef);
        if (parentDBref == null) {
            if (Base58.isExtraSymbols(docRef)) {
                parentDetails.setText(Lang.getInstance().translate("Not Base58 signature"));
                return;
            }

            byte[] signature = Base58.decode(docRef);
            if (DCSet.getInstance().getTransactionFinalMapSigns().contains(signature)) {
                parent = DCSet.getInstance().getTransactionFinalMap().get(signature);
            } else {
                parentDetails.setText(Lang.getInstance().translate("Not Found"));
                return;
            }
        } else {
            if (DCSet.getInstance().getTransactionFinalMap().contains(parentDBref)) {
                parent = DCSet.getInstance().getTransactionFinalMap().get(parentDBref);
                if (parent.getCreator() == null) {
                    parentDetails.setText(Lang.getInstance().translate("Empty Creator in parent transaction"));
                    return;
                }
            } else {
                parentDetails.setText(Lang.getInstance().translate("Not Found"));
                return;
            }
        }

        parentDetails.setText(parent.getCreator().getAddress() + " " + parent.getTitle());

    }

}

