package org.erachain.gui.library;

import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class CloseDialog extends javax.swing.JDialog implements Observer {

      public CloseDialog(java.awt.Frame parent) {
            super(parent, false);
            //setUndecorated(true);
       //     setLocationRelativeTo(null);
           setLocationRelativeTo(parent);
            initComponents();
          jLabel1.setText(Lang.getInstance().translate("Closing program. Please wait..."));
          jLabel2.setText("");
          setResizable(false);
          setPreferredSize(new Dimension(320,150));
          pack();
        }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0};
        layout.rowHeights = new int[] {0, 8, 0};
        getContentPane().setLayout(layout);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(19, 0, 0, 0);
        getContentPane().add(jLabel1, gridBagConstraints);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 7, 10);
        getContentPane().add(jLabel2, gridBagConstraints);

//        pack();
    }//
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;

        @Override
        public void update(Observable o, Object arg) {
            ObserverMessage mes = (ObserverMessage) arg;
            if (mes.getType() == ObserverMessage.GUI_ABOUT_TYPE){
                jLabel2.setText(mes.getValue().toString());
            }
        }

}

