package org.erachain.gui.exdata;

import org.erachain.lang.Lang;

import javax.swing.*;

public class DocTypeAppendixPanel extends JPanel {

         /**
         * Creates new form NewJPanel1
         */
        public DocTypeAppendixPanel() {
            initComponents();
            labelTitle.setText(Lang.getInstance().translate("Titl"));
            labelDocType.setText(Lang.getInstance().translate("Doc type"));
            textFieldDocType.setText(Lang.getInstance().translate("tex"));
        }



        private void initComponents() {
            java.awt.GridBagConstraints gridBagConstraints;

            textFieldDocType = new JTextField();
            labelDocType = new JLabel();
            filler1 = new Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
            labelTitle = new JLabel();

            java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
            layout.columnWidths = new int[] {0, 5, 0};
            layout.rowHeights = new int[] {0, 5, 0, 5, 0};
            setLayout(layout);


            labelTitle.setText("jLabel3");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
            gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
            add(labelTitle, gridBagConstraints);

            labelDocType.setText("jLabel2");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
            add(labelDocType, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 8);
            add(textFieldDocType, gridBagConstraints);


            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.3;
            gridBagConstraints.weighty = 0.2;
            add(filler1, gridBagConstraints);


        }// </editor-fold>

        private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {
            // TODO add your handling code here:
        }


        // Variables declaration - do not modify
        private Box.Filler filler1;
        private JLabel labelDocType;
        private JLabel labelTitle;
        private JTextField textFieldDocType;
        // End of variables declaration

}

