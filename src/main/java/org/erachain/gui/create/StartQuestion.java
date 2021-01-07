package org.erachain.gui.create;

import org.erachain.gui.library.MButton;
import org.erachain.gui.library.MTextPane;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Questions on Start
public class StartQuestion extends JDialog {

    public StartQuestion() {
        setTitle(Lang.T("Start questions"));
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        initComponents();
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screens = kit.getScreenSize();
        int h = (int) (screens.height * 0.9);
        int w = (int) (screens.width * 0.9);
        this.setModal(true);
        setPreferredSize(new Dimension(w, h));
        pack();
        this.setLocationRelativeTo(null);
        setVisible(true);

    }


        private void initComponents() {
            JPanel panelQuestoon = new JPanel();
            java.awt.GridBagConstraints gridBagConstraints;

            buttonGroup1 = new javax.swing.ButtonGroup();
            jRadioButton1 = new javax.swing.JRadioButton();
            jRadioButton2 = new javax.swing.JRadioButton();
            jRadioButton3 = new javax.swing.JRadioButton();
            jRadioButton4 = new javax.swing.JRadioButton();
            jScrollPane1 = new javax.swing.JScrollPane();
            jTextPane1 = new MTextPane();
            jPanel1 = new javax.swing.JPanel();
            jButton1 = new MButton();

            panelQuestoon.setLayout(new java.awt.GridBagLayout());

            jRadioButton1.setText("jRadioButton1");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
            buttonGroup1.add(jRadioButton1);
            panelQuestoon.add(jRadioButton1, gridBagConstraints);

            jRadioButton2.setText("jRadioButton2");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
            buttonGroup1.add(jRadioButton2);
            panelQuestoon.add(jRadioButton2, gridBagConstraints);

            jRadioButton3.setText("jRadioButton3");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
            buttonGroup1.add(jRadioButton3);
            panelQuestoon.add(jRadioButton3, gridBagConstraints);

            jRadioButton4.setText("jRadioButton4");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
            buttonGroup1.add(jRadioButton4);
            panelQuestoon.add(jRadioButton4, gridBagConstraints);

            jScrollPane1.setViewportView(jTextPane1);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 4;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.weighty = 0.1;
            gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
            panelQuestoon.add(jScrollPane1, gridBagConstraints);

            jPanel1.setLayout(new java.awt.GridBagLayout());

            jButton1.setText("jButton1");
            jPanel1.add(jButton1, new java.awt.GridBagConstraints());

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 5;
            gridBagConstraints.gridwidth = 4;
            gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
            panelQuestoon.add(jPanel1, gridBagConstraints);
            add(panelQuestoon);
        }



        private javax.swing.ButtonGroup buttonGroup1;
        private MButton jButton1;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JRadioButton jRadioButton1;
        private javax.swing.JRadioButton jRadioButton2;
        private javax.swing.JRadioButton jRadioButton3;
        private javax.swing.JRadioButton jRadioButton4;
        private javax.swing.JScrollPane jScrollPane1;
        private MTextPane jTextPane1;





}
