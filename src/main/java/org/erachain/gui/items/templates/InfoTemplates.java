package org.erachain.gui.items.templates;

import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.library.MAccoutnTextField;
import org.erachain.gui.library.MTextPane;
import org.erachain.gui.library.VouchLibraryPanel;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InfoTemplates extends javax.swing.JPanel {

    /**
     * Creates new form InfoTemplates
     *
     * @param template
     */
    TemplateCls template;
    // Variables declaration - do not modify
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel_Account_Creator;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel_Content;
    private javax.swing.JLabel jLabel_Title;
    private javax.swing.JScrollPane jScrollPane1;
    private MTextPane jTextArea_Content;
    private MAccoutnTextField jTextField_Account_Creator;
    private javax.swing.JTextField jTextField_Title;
    private javax.swing.JPanel jPanel_Image;
    private GridBagConstraints gridBagConstraints_1;
    private GridBagConstraints gridBagConstraints_2;

    public InfoTemplates(TemplateCls template) {
        this.template = template;
        initComponents();

        jTextField_Title.setText(template.viewName());
        jTextArea_Content.setText(template.getDescription());

    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel4 = new javax.swing.JLabel();
        jTextField_Title = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLabel2 = new javax.swing.JLabel();

        jTextArea_Content = new MTextPane();
        jPanel_Image = new javax.swing.JPanel();
        new javax.swing.JScrollPane();
        new javax.swing.JTable();
        new javax.swing.JScrollPane();
        new javax.swing.JTable();
        new javax.swing.JLabel();
        new javax.swing.JLabel();

        jLabel4.setText("jLabel4");

        ImageIcon image = new ImageIcon(template.getImage());
        int x = image.getIconWidth();
        int y = image.getIconHeight();
        int x1 = 250;
        double k = ((double) x / (double) x1);
        y = (int) ((double) y / k);

        if (y != 0) {
            Image Im = image.getImage().getScaledInstance(x1, y, 1);

            jLabel2.setIcon(new ImageIcon(Im));
        }

        jLabel2.addMouseListener(new Image_mouse_Clikl());

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[]{0, 0, 8, 0, 8, 0};
        layout.rowHeights = new int[]{0, 4, 0, 4, 0, 4, 0, 4, 0};
        setLayout(layout);
        jLabel_Account_Creator = new javax.swing.JLabel();

        jLabel_Account_Creator.setText(Lang.getInstance().translate("Account Creator") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(9, 8, 5, 5);
        add(jLabel_Account_Creator, gridBagConstraints);

        jTextField_Account_Creator = new MAccoutnTextField(template.getOwner());
        jTextField_Account_Creator.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(9, 0, 5, 10);
        add(jTextField_Account_Creator, gridBagConstraints);
        jLabel_Title = new javax.swing.JLabel();

        jLabel_Title.setText(Lang.getInstance().translate("Title") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(0, 8, 5, 5);
        add(jLabel_Title, gridBagConstraints);

        jTextField_Title.setToolTipText("");
        jTextField_Title.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 10);
        add(jTextField_Title, gridBagConstraints);
        jLabel_Content = new javax.swing.JLabel();

        jLabel_Content.setText(Lang.getInstance().translate("Content") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new Insets(0, 8, 5, 5);
        add(jLabel_Content, gridBagConstraints);

        gridBagConstraints_2 = new java.awt.GridBagConstraints();
        gridBagConstraints_2.gridx = 0;
        gridBagConstraints_2.gridy = 0;
        gridBagConstraints_2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints_2.anchor = GridBagConstraints.SOUTH;
        gridBagConstraints_2.weightx = 1;
        gridBagConstraints_2.weighty = 1;
        gridBagConstraints_2.weightx = 0.2;
        gridBagConstraints_2.weighty = 0.2;
        jPanel_Image.add(jLabel2, gridBagConstraints_2);

        //jTextArea_Content.setColumns(20);
        //jTextArea_Content.setRows(6);
        jTextArea_Content.setAlignmentY(1.0F);
        //jTextArea_Content.setWrapStyleWord(true);
        //jTextArea_Content.setLineWrap(true);
        //jTextArea_Content.setEditable(false);
        MenuPopupUtil.installContextMenu(jTextArea_Content);
        jScrollPane1.setViewportView(jTextArea_Content);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new Insets(0, 0, 5, 10);
        add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 9, 0);

        javax.swing.JTabbedPane jTabbedPane1 = new javax.swing.JTabbedPane();

        add(jTabbedPane1, gridBagConstraints);
        GridBagLayout gbl_jPanel_Image = new GridBagLayout();
        gbl_jPanel_Image.columnWidths = new int[]{81};
        gbl_jPanel_Image.rowHeights = new int[]{152};
        gbl_jPanel_Image.rowWeights = new double[]{1.0};
        gbl_jPanel_Image.columnWeights = new double[]{0.0};
        jPanel_Image.setLayout(gbl_jPanel_Image);
        gridBagConstraints_1 = new java.awt.GridBagConstraints();
        gridBagConstraints_1.gridheight = 6;
        gridBagConstraints_1.gridx = 1;
        gridBagConstraints_1.gridy = 1;
        // gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        // gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints_1.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        // gridBagConstraints.weightx = 1.0;
        gridBagConstraints_1.weighty = 0.6;
        gridBagConstraints_1.insets = new Insets(0, 8, 8, 5);
        add(jPanel_Image, gridBagConstraints_1);

        // vouch panel
        Transaction trans = Transaction.findByDBRef(DCSet.getInstance(), template.getReference());
        jTabbedPane1.add(new VouchLibraryPanel(trans));

    }

    // End of variables declaration
    // End of variables declaration
    class Image_mouse_Clikl extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            // Point p = e.getPoint();
            // int row = search_Table.rowAtPoint(p);
            if (e.getClickCount() == 2) {
            }

            // if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
            if (e.getButton() == MouseEvent.BUTTON1) {

                /*
                 * row = search_Table.convertRowIndexToModel(row); PersonCls person =
                 * search_Table_Model.getPerson(row); //выводим меню
                 * всплывающее if(Controller.getInstance().isItemFavorite(person)) {
                 * Search_run_menu.jButton3.setText(Lang.getInstance().
                 * translate("Remove Favorite")); } else {
                 * Search_run_menu.jButton3.setText(Lang.getInstance().translate("Add Favorite")
                 * ); } // alpha = 255; alpha_int = 5; Search_run_menu.setBackground(new
                 * Color(1,204,102,255)); Search_run_menu.setLocation(e.getXOnScreen(),
                 * e.getYOnScreen()); Search_run_menu.repaint();
                 * Search_run_menu.setVisible(true);
                 *
                 */

            }

        }
    }
}
