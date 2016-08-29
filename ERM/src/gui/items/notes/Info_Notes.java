package gui.items.notes;

import core.item.notes.NoteCls;
import lang.Lang;

public class Info_Notes extends javax.swing.JPanel {

    /**
     * Creates new form Info_Notes
     * @param note 
     */
    public Info_Notes(NoteCls note) {
        initComponents();
        jTextField_Account_Creator.setText(note.getCreator().getAddress());
        jTextField_Title.setText(note.getName());
        jTextArea_Content.setText(note.getDescription());
    
    
    
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel4 = new javax.swing.JLabel();
        jLabel_Account_Creator = new javax.swing.JLabel();
        jTextField_Account_Creator = new javax.swing.JTextField();
        jLabel_Title = new javax.swing.JLabel();
        jTextField_Title = new javax.swing.JTextField();
        jLabel_Content = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea_Content = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable_Statuses = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable_Signatures = new javax.swing.JTable();
        jLabel_Statuses = new javax.swing.JLabel();
        jLabel_Signatures = new javax.swing.JLabel();

        jLabel4.setText("jLabel4");

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 8, 0, 8, 0};
        layout.rowHeights = new int[] {0, 4, 0, 4, 0, 4, 0, 4, 0};
        setLayout(layout);

        jLabel_Account_Creator.setText(Lang.getInstance().translate("Account Creator")+":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(9, 8, 0, 0);
        add(jLabel_Account_Creator, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 10);
        add(jTextField_Account_Creator, gridBagConstraints);

        jLabel_Title.setText(Lang.getInstance().translate("Title")+":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(jLabel_Title, gridBagConstraints);

        jTextField_Title.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jTextField_Title, gridBagConstraints);

        jLabel_Content.setText(Lang.getInstance().translate("Content")+":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(jLabel_Content, gridBagConstraints);

        jTextArea_Content.setColumns(20);
        jTextArea_Content.setRows(6);
        jTextArea_Content.setAlignmentY(1.0F);
        jTextArea_Content.setLineWrap(true);
        jScrollPane1.setViewportView(jTextArea_Content);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jScrollPane1, gridBagConstraints);

        jTable_Statuses.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable_Statuses);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 9, 0);
        add(jScrollPane2, gridBagConstraints);

        jTable_Signatures.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jTable_Signatures);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.16;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 10);
        add(jScrollPane3, gridBagConstraints);

        jLabel_Statuses.setText(Lang.getInstance().translate("Statuses"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
        add(jLabel_Statuses, gridBagConstraints);

        jLabel_Signatures.setText(Lang.getInstance().translate("Signatures"));
        jLabel_Signatures.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jLabel_Signatures, gridBagConstraints);
    }// </editor-fold> 

    // Variables declaration - do not modify                     
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel_Account_Creator;
    private javax.swing.JLabel jLabel_Content;
    private javax.swing.JLabel jLabel_Signatures;
    private javax.swing.JLabel jLabel_Statuses;
    private javax.swing.JLabel jLabel_Title;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable_Signatures;
    private javax.swing.JTable jTable_Statuses;
    private javax.swing.JTextArea jTextArea_Content;
    private javax.swing.JTextField jTextField_Account_Creator;
    private javax.swing.JTextField jTextField_Title;
    // End of variables declaration                   
}
