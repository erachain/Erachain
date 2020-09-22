package org.erachain.gui.items.assets;

import org.erachain.gui.IconPanel;

public class MultiPayOutsPanel extends IconPanel {


    public static String NAME = "MultiPayOutsPanel";
    public static String TITLE = "Multi paument out";

    public MultiPayOutsPanel() {
        super(NAME, TITLE);
        initComponents();
        }


        private void initComponents() {
            java.awt.GridBagConstraints gridBagConstraints;

            buttonGroup1 = new javax.swing.ButtonGroup();
            jLabel13 = new javax.swing.JLabel();
            jLabelTitle = new javax.swing.JLabel();
            jComboBoxAsset1 = new javax.swing.JComboBox<>();
            jLabel5 = new javax.swing.JLabel();
            jComboBoxOtborAsset = new javax.swing.JComboBox<>();
            jLabelVidBalance = new javax.swing.JLabel();
            jComboBox3 = new javax.swing.JComboBox<>();
            jLabel8 = new javax.swing.JLabel();
            jTextFieldBQ = new javax.swing.JTextField();
            jTextFieldLQ = new javax.swing.JTextField();
            jLabel9 = new javax.swing.JLabel();
            jLabelAsset1 = new javax.swing.JLabel();
            jLabelTitlemetod = new javax.swing.JLabel();
            jPanel1 = new javax.swing.JPanel();
            jRadioButtonABS = new javax.swing.JRadioButton();
            jRadioButtonSumm = new javax.swing.JRadioButton();
            jRadioButtonCoff = new javax.swing.JRadioButton();
            jLabelMetodPaumentItem = new javax.swing.JLabel();
            jTextFieldMetodPaument = new javax.swing.JTextField();
            jLabelMetodPaumentDecscription = new javax.swing.JLabel();
            jPanel2 = new javax.swing.JPanel();
            jLabelDataStart = new javax.swing.JLabel();
            jTextFieldDateStart = new javax.swing.JTextField();
            jLabelDateEnd = new javax.swing.JLabel();
            jTextFieldDateEnd = new javax.swing.JTextField();
            jLabel19 = new javax.swing.JLabel();
            jComboBoxStoronaBalance = new javax.swing.JComboBox<>();
            jLabel20 = new javax.swing.JLabel();
            jComboBoxOtborPoDeistviy = new javax.swing.JComboBox<>();
            jPanel3 = new javax.swing.JPanel();
            jButton2 = new javax.swing.JButton();
            jButton3 = new javax.swing.JButton();
            jButton1 = new javax.swing.JButton();
            jLabel1 = new javax.swing.JLabel();
            jCheckBoxConfirmResult = new javax.swing.JCheckBox();

            jLabel13.setText("jLabel13");

            java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
            layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
            layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
            setLayout(layout);

            jLabelTitle.setText("Multi paument");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 15;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
            add(jLabelTitle, gridBagConstraints);

            jComboBoxAsset1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 6;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridwidth = 9;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
            add(jComboBoxAsset1, gridBagConstraints);

            jLabel5.setText("отбор по активу");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 8;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            gridBagConstraints.insets = new java.awt.Insets(14, 10, 0, 0);
            add(jLabel5, gridBagConstraints);

            jComboBoxOtborAsset.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 6;
            gridBagConstraints.gridy = 8;
            gridBagConstraints.gridwidth = 9;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 0.2;
            gridBagConstraints.insets = new java.awt.Insets(14, 0, 0, 10);
            add(jComboBoxOtborAsset, gridBagConstraints);

            jLabelVidBalance.setText("Вид баланса");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 10;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
            add(jLabelVidBalance, gridBagConstraints);

            jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 6;
            gridBagConstraints.gridy = 10;
            gridBagConstraints.gridwidth = 9;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
            add(jComboBox3, gridBagConstraints);

            jLabel8.setText("Больше чем");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 6;
            gridBagConstraints.gridy = 14;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            add(jLabel8, gridBagConstraints);

            jTextFieldBQ.setToolTipText("");
            jTextFieldBQ.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jTextFieldBQActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 8;
            gridBagConstraints.gridy = 14;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 0.1;
            add(jTextFieldBQ, gridBagConstraints);

            jTextFieldLQ.setToolTipText("");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 12;
            gridBagConstraints.gridy = 14;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 0.2;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
            add(jTextFieldLQ, gridBagConstraints);

            jLabel9.setText("Меньше чем");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 10;
            gridBagConstraints.gridy = 14;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            gridBagConstraints.insets = new java.awt.Insets(0, 17, 0, 0);
            add(jLabel9, gridBagConstraints);

            jLabelAsset1.setText("Asset");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
            add(jLabelAsset1, gridBagConstraints);

            jLabelTitlemetod.setText("Metod payment");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 6;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
            gridBagConstraints.insets = new java.awt.Insets(7, 10, 0, 0);
            add(jLabelTitlemetod, gridBagConstraints);

            java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
            jPanel1Layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0};
            jPanel1Layout.rowHeights = new int[] {0, 5, 0};
            jPanel1.setLayout(jPanel1Layout);

            buttonGroup1.add(jRadioButtonABS);
            jRadioButtonABS.setText("ABS item");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            jPanel1.add(jRadioButtonABS, gridBagConstraints);

            buttonGroup1.add(jRadioButtonSumm);
            jRadioButtonSumm.setText("Summa");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 3;
            jPanel1.add(jRadioButtonSumm, gridBagConstraints);

            buttonGroup1.add(jRadioButtonCoff);
            jRadioButtonCoff.setText("Koficient");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 6;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            jPanel1.add(jRadioButtonCoff, gridBagConstraints);

            jLabelMetodPaumentItem.setText("Значение");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            jPanel1.add(jLabelMetodPaumentItem, gridBagConstraints);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 0.2;
            jPanel1.add(jTextFieldMetodPaument, gridBagConstraints);

            jLabelMetodPaumentDecscription.setText("по общеме значению");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 4;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 0.5;
            jPanel1.add(jLabelMetodPaumentDecscription, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 6;
            gridBagConstraints.gridy = 6;
            gridBagConstraints.gridwidth = 9;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 0.3;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
            add(jPanel1, gridBagConstraints);

            java.awt.GridBagLayout jPanel2Layout = new java.awt.GridBagLayout();
            jPanel2Layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0};
            jPanel2Layout.rowHeights = new int[] {0};
            jPanel2.setLayout(jPanel2Layout);

            jLabelDataStart.setText("Data start");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            jPanel2.add(jLabelDataStart, gridBagConstraints);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.ipadx = 50;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            jPanel2.add(jTextFieldDateStart, gridBagConstraints);

            jLabelDateEnd.setText("Date end");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 4;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            jPanel2.add(jLabelDateEnd, gridBagConstraints);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 6;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.ipadx = 50;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            jPanel2.add(jTextFieldDateEnd, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 20;
            gridBagConstraints.gridwidth = 15;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 10);
            add(jPanel2, gridBagConstraints);

            jLabel19.setText("Сторона баланса");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 12;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
            add(jLabel19, gridBagConstraints);

            jComboBoxStoronaBalance.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 6;
            gridBagConstraints.gridy = 12;
            gridBagConstraints.gridwidth = 9;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
            add(jComboBoxStoronaBalance, gridBagConstraints);

            jLabel20.setText("Отбор по действию");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 18;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            gridBagConstraints.insets = new java.awt.Insets(19, 10, 0, 0);
            add(jLabel20, gridBagConstraints);

            jComboBoxOtborPoDeistviy.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 6;
            gridBagConstraints.gridy = 18;
            gridBagConstraints.gridwidth = 9;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.insets = new java.awt.Insets(19, 0, 0, 10);
            add(jComboBoxOtborPoDeistviy, gridBagConstraints);

            java.awt.GridBagLayout jPanel3Layout = new java.awt.GridBagLayout();
            jPanel3Layout.columnWidths = new int[] {0, 5, 0, 5, 0};
            jPanel3Layout.rowHeights = new int[] {0};
            jPanel3.setLayout(jPanel3Layout);

            jButton2.setText("ViewResult");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            jPanel3.add(jButton2, gridBagConstraints);

            jButton3.setText("Cancel");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            jPanel3.add(jButton3, gridBagConstraints);

            jButton1.setText("Send");
            jButton1.setToolTipText("");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 4;
            gridBagConstraints.gridy = 0;
            jPanel3.add(jButton1, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 24;
            gridBagConstraints.gridwidth = 15;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
            add(jPanel3, gridBagConstraints);

            jLabel1.setToolTipText("");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 28;
            gridBagConstraints.gridwidth = 15;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.3;
            gridBagConstraints.weighty = 0.2;
            add(jLabel1, gridBagConstraints);

            jCheckBoxConfirmResult.setText("Подтверждаю правильность регультата");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 22;
            gridBagConstraints.gridwidth = 9;
            gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
            add(jCheckBoxConfirmResult, gridBagConstraints);
        }// </editor-fold>

        private void jTextFieldBQActionPerformed(java.awt.event.ActionEvent evt) {
            // TODO add your handling code here:
        }


        // Variables declaration - do not modify
        private javax.swing.ButtonGroup buttonGroup1;
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JButton jButton3;
        private javax.swing.JCheckBox jCheckBoxConfirmResult;
        private javax.swing.JComboBox<String> jComboBox3;
        private javax.swing.JComboBox<String> jComboBoxAsset1;
        private javax.swing.JComboBox<String> jComboBoxOtborAsset;
        private javax.swing.JComboBox<String> jComboBoxOtborPoDeistviy;
        private javax.swing.JComboBox<String> jComboBoxStoronaBalance;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel13;
        private javax.swing.JLabel jLabel19;
        private javax.swing.JLabel jLabel20;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JLabel jLabel8;
        private javax.swing.JLabel jLabel9;
        private javax.swing.JLabel jLabelAsset1;
        private javax.swing.JLabel jLabelDataStart;
        private javax.swing.JLabel jLabelDateEnd;
        private javax.swing.JLabel jLabelMetodPaumentDecscription;
        private javax.swing.JLabel jLabelMetodPaumentItem;
        private javax.swing.JLabel jLabelTitle;
        private javax.swing.JLabel jLabelTitlemetod;
        private javax.swing.JLabel jLabelVidBalance;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JPanel jPanel3;
        private javax.swing.JRadioButton jRadioButtonABS;
        private javax.swing.JRadioButton jRadioButtonCoff;
        private javax.swing.JRadioButton jRadioButtonSumm;
        private javax.swing.JTextField jTextFieldBQ;
        private javax.swing.JTextField jTextFieldDateEnd;
        private javax.swing.JTextField jTextFieldDateStart;
        private javax.swing.JTextField jTextFieldLQ;
        private javax.swing.JTextField jTextFieldMetodPaument;
        // End of variables declaration



}
