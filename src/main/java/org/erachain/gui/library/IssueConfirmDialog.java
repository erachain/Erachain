package org.erachain.gui.library;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Саша
 */
public class IssueConfirmDialog extends javax.swing.JDialog {

    public static final int CANCEL = 0;
    public static final int CONFIRM = 1;
    public static final int TRY_FREE = 2;

    public int isConfirm = 0;
    public javax.swing.JButton jButton0;
    public javax.swing.JButton jButton1;
    public javax.swing.JButton jButton2;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JScrollPane jScrollPane1;
    public MTextPane jTextPane1;
    int insest = 0;
    private JLabel jStatus_Label;
    private JLabel jTitle_Label;

    /**
     * Creates new form Issue_Asset_Confirm_Dialog
     */
    public IssueConfirmDialog(java.awt.Frame parent, boolean modal, Transaction transaction, String text,
                              int w, int h, String status_Text, String title_Text) {
        super(parent, modal);
        Init(parent, modal, transaction, text, w, h, status_Text, title_Text, true);
    }

    public IssueConfirmDialog(java.awt.Frame parent, boolean modal, Transaction transaction, String text,
                              int w, int h, String status_Text, String title_Text, boolean receive) {
        super(parent, modal);
        Init(parent, modal, transaction, text, w, h, status_Text, title_Text, receive);
    }

    public IssueConfirmDialog(java.awt.Frame parent, boolean modal, Transaction transaction, String text,
                              int w, int h, String status_Text) {
        super(parent, modal);
        Init(parent, modal, transaction, text, w, h, status_Text, "", true);
    }

    public IssueConfirmDialog(java.awt.Frame parent, boolean modal, Transaction transaction,
                              int w, int h, String status_Text) {
        super(parent, modal);
        Init(parent, modal, transaction, w, h, status_Text, "");
    }

    public void Init(java.awt.Frame parent, boolean modal, Transaction transaction, String text,
                     int w, int h, String status_Text, String title_Text, boolean receive) {
        // setUndecorated(true);
        insest = UIManager.getFont("Label.font").getSize();
        if (insest <= 7) insest = 8;
        initComponents();
        jTitle_Label.setText(title_Text);
        jTextPane1.setText(text);
        if (transaction != null) {
            // кое где эта форма используется без транзакции - просто как окно - в настройках например

            // посчитаем без учета что она может быть бесплатной - для инфо нужно показать
            transaction.calcFee(false);
            String feeText = "<span style='font-size: 1.2em;'>" + Lang.T("Size") + ":&nbsp;"
                    + transaction.viewSize(Transaction.FOR_NETWORK) + " Bytes";
            if (transaction.getFee().signum() != 0) {
                feeText += ", " + Lang.T("Fee") + ":&nbsp; " + transaction.viewFeeAndFiat(insest) + "</span>";
            }
            status_Text += feeText;

            if (!receive) {
                jButton0.setVisible(false);
                jButton1.setText(Lang.T("Save"));
            } else {
                // проверим а вообще такая транзакция может быть бесплатна?
                jButton0.setVisible(BlockChain.FREE_FEE_TO_SEQNO > 0
                        && transaction.isFreeFee()
                        && BlockChain.FREE_FEE_FROM_HEIGHT < Controller.getInstance().getMyHeight()
                        && transaction.getDataLength(Transaction.FOR_NETWORK, true) < BlockChain.FREE_FEE_LENGTH);
            }
        }

        jStatus_Label.setText("<HTML>" + status_Text + "</HTML>");
        //  setMaximumSize(new Dimension(350,200));
        if (w > 0 && h > 0) {
            setSize(w, h);
        }

        jButton0.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                isConfirm = TRY_FREE;
                dispose();
            }
        });

        jButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                isConfirm = CONFIRM;
                dispose();
            }
        });

        jButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                dispose();
            }
        });
    }

    public void Init(java.awt.Frame parent, boolean modal, Transaction transaction, int w, int h, String status_Text, String title_Text) {
        // setUndecorated(true);
        insest = UIManager.getFont("Label.font").getSize();
        if (insest <= 7) insest = 8;
        initComponents();
        jTitle_Label.setText(title_Text);
        JPanel pp = TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
        jScrollPane1.setViewportView(pp);
        jStatus_Label.setText(status_Text);
        //  setMaximumSize(new Dimension(350,200));
        setSize(w, h);
        jButton0.setVisible(false);
        jButton1.setVisible(false);

        jButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                dispose();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new MTextPane();
        jPanel1 = new javax.swing.JPanel();
        jButton0 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jStatus_Label = new JLabel();
        jTitle_Label = new JLabel();


        setIconImage(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jTitle_Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        //  gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(insest, insest, insest, insest);
        getContentPane().add(jTitle_Label, gridBagConstraints);

        jScrollPane1.setBorder(null);
        jScrollPane1.setOpaque(false);

        jTextPane1.setOpaque(false);
        jScrollPane1.setViewportView(jTextPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(insest, insest, insest, insest);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jStatus_Label.setText(Lang.T("Status"));
        jStatus_Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        //  gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(insest, insest, insest, insest);
        getContentPane().add(jStatus_Label, gridBagConstraints);

        jButton0.setText(Lang.T("Try without Fee"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, insest, 0, 0);
        jPanel1.add(jButton0, gridBagConstraints);


        jButton1.setText(Lang.T("Confirm"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, insest, 0, 0);
        jPanel1.add(jButton1, gridBagConstraints);

        jButton2.setText(Lang.T("Cancel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, insest, 0, 0);
        jPanel1.add(jButton2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, insest, insest, insest);
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>

}
