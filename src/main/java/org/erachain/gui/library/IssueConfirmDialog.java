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
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Base64;

/**
 * @author Саша
 */
public class IssueConfirmDialog extends javax.swing.JDialog {

    public static final int CANCEL = 0;
    public static final int CONFIRM = 1;
    public static final int TRY_FREE = 2;
    public static final int GER_RAW = -1;

    public int isConfirm = 0;
    public javax.swing.JButton jButtonRAW;
    public javax.swing.JButton jButtonFREE;
    public javax.swing.JButton jButtonGO;
    public javax.swing.JButton jButtonCancel;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JScrollPane jScrollPane1;
    public MTextPane jTextPane1;
    int fontSize = 0;
    private JLabel jStatus_Label;
    private JLabel jTitle_Label;

    /**
     * Creates new form Issue_Asset_Confirm_Dialog
     */
    public IssueConfirmDialog(java.awt.Frame parent, String text) {
        super(parent, Lang.T(text), true);
        Init(parent, true, null, 0, 0, "", "");
    }

    public IssueConfirmDialog(java.awt.Frame parent, boolean modal, Transaction transaction, String text,
                              int w, int h, String status_Text, String title_Text) {
        super(parent, modal);
        Init(parent, modal, transaction, text, w, h, status_Text, title_Text, false);
    }

    public IssueConfirmDialog(java.awt.Frame parent, boolean modal, Transaction transaction, String text,
                              int w, int h, String status_Text, String title_Text, boolean useSave) {
        super(parent, modal);
        Init(parent, modal, transaction, text, w, h, status_Text, title_Text, useSave);
    }

    public IssueConfirmDialog(java.awt.Frame parent, boolean modal, Transaction transaction, String text,
                              int w, int h, String status_Text) {
        super(parent, modal);
        Init(parent, modal, transaction, text, w, h, status_Text, "", false);
    }

    public IssueConfirmDialog(java.awt.Frame parent, boolean modal, Transaction transaction,
                              int w, int h, String title) {
        super(parent, modal);
        Init(parent, modal, transaction, w, h, null, title);
    }

    public void Init(java.awt.Frame parent, boolean modal, Transaction transaction, String text,
                     int w, int h, String status_Text, String title_Text, boolean useSave) {
        // setUndecorated(true);
        fontSize = UIManager.getFont("Label.font").getSize();
        if (fontSize <= 7) fontSize = 8;
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
                feeText += ", " + Lang.T("Fee") + ":&nbsp; " + transaction.viewFeeAndFiat(fontSize) + "</span>";
            }
            status_Text += feeText;

            if (useSave) {
                jButtonFREE.setVisible(false);
                jButtonGO.setVisible(false);
            } else {
                // проверим а вообще такая транзакция может быть бесплатна?
                jButtonFREE.setVisible(BlockChain.FREE_FEE_TO_SEQNO > 0
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

        jButtonRAW.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                isConfirm = GER_RAW;
                StringSelection stringSelection = new StringSelection(Base64.getEncoder().encodeToString(
                        transaction.toBytes(Transaction.FOR_NETWORK, true)));
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.T("Bytecode has been copy to buffer") + ".",
                        Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });

        jButtonFREE.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                isConfirm = TRY_FREE;
                dispose();
            }
        });

        jButtonGO.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                isConfirm = CONFIRM;
                dispose();
            }
        });

        jButtonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                dispose();
            }
        });
    }

    public void Init(java.awt.Frame parent, boolean modal, Transaction transaction, int w, int h, String status_Text, String title_Text) {
        // setUndecorated(true);
        fontSize = UIManager.getFont("Label.font").getSize();
        if (fontSize <= 7) fontSize = 8;
        initComponents();
        jTitle_Label.setText(title_Text);

        if (status_Text == null || status_Text.isEmpty()) {
            jStatus_Label.setVisible(false);
        } else {
            jStatus_Label.setText(status_Text);
        }
        //  setMaximumSize(new Dimension(350,200));
        setSize(w, h);
        jButtonFREE.setVisible(false);
        jButtonGO.setVisible(false);

        if (transaction == null) {
            jButtonRAW.setVisible(false);
            jButtonCancel.setText(Lang.T("OK"));
        } else {

            JPanel pp = TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
            jScrollPane1.setViewportView(pp);

            jButtonRAW.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    isConfirm = GER_RAW;
                    StringSelection stringSelection = new StringSelection(Base64.getEncoder().encodeToString(
                            transaction.toBytes(Transaction.FOR_NETWORK, true)));
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                    JOptionPane.showMessageDialog(new JFrame(),
                            Lang.T("Bytecode has been copy to buffer") + ".",
                            Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            });
        }

        jButtonCancel.addActionListener(new ActionListener() {
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
        jButtonRAW = new javax.swing.JButton();
        jButtonFREE = new javax.swing.JButton();
        jButtonGO = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
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
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(fontSize, fontSize, fontSize, fontSize);
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
        gridBagConstraints.insets = new java.awt.Insets(fontSize, fontSize, fontSize, fontSize);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jStatus_Label.setText(Lang.T("Status"));
        jStatus_Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(fontSize, fontSize, fontSize, fontSize);
        getContentPane().add(jStatus_Label, gridBagConstraints);

        // BUTTONS PANEL
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jButtonRAW.setText(Lang.T("Bytecode"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, fontSize, 0, 0);
        jPanel1.add(jButtonRAW, gridBagConstraints);

        jButtonFREE.setText(Lang.T("Try without Fee"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, fontSize, 0, 0);
        jPanel1.add(jButtonFREE, gridBagConstraints);

        jButtonGO.setText(Lang.T("Confirm"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, fontSize, 0, 0);
        jPanel1.add(jButtonGO, gridBagConstraints);

        jButtonCancel.setText(Lang.T("Cancel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, fontSize, 0, 0);
        jPanel1.add(jButtonCancel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, fontSize, fontSize, fontSize);
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>

}
