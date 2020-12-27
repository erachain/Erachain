package org.erachain.gui.exdata;


import org.erachain.core.exdata.ExPays;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.IconPanel;
import org.erachain.gui.items.assets.ComboBoxAssetsModel;
import org.erachain.gui.models.RenderComboBoxActionFilter;
import org.erachain.gui.models.RenderComboBoxVidBalance;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


public class MultiPayOutsPanel extends IconPanel {

    public static String NAME = "MultiPayOutsPanel";
    public static String TITLE = "Payouts";
    public ComboBoxAssetsModel accountsModel;
    public ComboBoxAssetsModel accountsModel1;

    public MultiPayOutsPanel() {
        super(NAME, TITLE);
        initComponents();
        accountsModel = new ComboBoxAssetsModel();
        accountsModel1 = new ComboBoxAssetsModel();
        this.jComboBoxAssetToPay.setModel(accountsModel);
        this.jComboBoxFilterAsset.setModel(accountsModel1);
        jComboBoxBalancePosition.setModel(new javax.swing.DefaultComboBoxModel(new Integer[]{
                TransactionAmount.ACTION_SEND,
                TransactionAmount.ACTION_DEBT,
                TransactionAmount.ACTION_HOLD,
                TransactionAmount.ACTION_SPEND,
                //TransactionAmount.ACTION_PLEDGE,
        }));
        jComboBoxBalancePosition.setRenderer(new RenderComboBoxVidBalance());
        jComboBoxSideBalance.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
                Lang.getInstance().translate("Total Debit"),
                Lang.getInstance().translate("Left"),
                Lang.getInstance().translate("Total Credit")
        }));
        jComboBoxSideBalance.setSelectedIndex(1);
        jComboBoxActionFilter.setModel(new javax.swing.DefaultComboBoxModel<Integer>(Transaction.getTransactionTypes()));
        jComboBoxActionFilter.setRenderer(new RenderComboBoxActionFilter());

        jComboBoxMethodPaymentType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateLabelsByMethod();
            }
        });
        updateLabelsByMethod();

        jLabelMethodPaymentDecscription.setHorizontalAlignment(SwingConstants.LEFT);
        jComboBoxPersonFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
                Lang.getInstance().translate("All"),
                Lang.getInstance().translate("Only for Persons"),
                Lang.getInstance().translate("Only for Men"),
                Lang.getInstance().translate("Only for Women")}));
    }

    private void updateLabelsByMethod() {
        switch (jComboBoxMethodPaymentType.getSelectedIndex()) {
            case 0:
                jLabelMethodPaymentDecscription.setText(
                        Lang.getInstance().translate("PAY_METHOD_0"));
                jLabelAmount.setText(Lang.getInstance().translate("Percent"));
                jTextFieldPaymentMin.setEnabled(true);
                jTextFieldPaymentMax.setEnabled(true);
                return;
            case 1:
                jLabelMethodPaymentDecscription.setText(
                        Lang.getInstance().translate("PAY_METHOD_1"));
                jLabelAmount.setText(Lang.getInstance().translate("Total"));
                jTextFieldPaymentMin.setEnabled(true);
                jTextFieldPaymentMax.setEnabled(true);
                return;
            case 2:
                jLabelMethodPaymentDecscription.setText(
                        Lang.getInstance().translate("PAY_METHOD_2"));
                jLabelAmount.setText(Lang.getInstance().translate("Amount"));
                jTextFieldPaymentMin.setEnabled(false);
                jTextFieldPaymentMax.setEnabled(false);
                return;
        }
    }


    private void initComponents() {

        jLabel13 = new javax.swing.JLabel();
        jLabelTitle = new javax.swing.JLabel();
        jComboBoxAssetToPay = new javax.swing.JComboBox<>();
        jComboBoxAction = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jComboBoxFilterAsset = new javax.swing.JComboBox<>();
        jLabelBalancePosition = new javax.swing.JLabel();
        jComboBoxBalancePosition = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldBQ = new javax.swing.JTextField();
        jTextFieldLQ = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabelAssetToPay = new javax.swing.JLabel();
        jLabelAction = new javax.swing.JLabel();
        jLabelTitlemetod = new javax.swing.JLabel();
        jLabelMethodPaymentDecscription = new javax.swing.JLabel();
        jLabelPaymentMin = new javax.swing.JLabel();
        jTextFieldPaymentMin = new javax.swing.JTextField();
        jLabelPaymentMax = new javax.swing.JLabel();
        jTextFieldPaymentMax = new javax.swing.JTextField();
        jPanelMinMaxAmounts = new javax.swing.JPanel();
        jPanelFilterBalance = new javax.swing.JPanel();
        jPanelStartEndActions = new javax.swing.JPanel();
        jLabelDataStart = new javax.swing.JLabel();
        jTextFieldDateStart = new javax.swing.JTextField();
        jLabelDateEnd = new javax.swing.JLabel();
        jTextFieldDateEnd = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jComboBoxSideBalance = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        jComboBoxActionFilter = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        jButtonViewResult = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jButtonSend = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jCheckBoxConfirmResult = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabelMethod = new javax.swing.JLabel();
        jLabelAmount = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        jTextFieldAmount = new javax.swing.JTextField();
        jButtonCalcCompu = new javax.swing.JButton();
        jComboBoxMethodPaymentType = new javax.swing.JComboBox<>();
        jComboBoxPersonFilter = new javax.swing.JComboBox<>();

        Font ff = (Font) UIManager.get("Label.font");
        Font headFont = new java.awt.Font(ff.getFontName(), Font.BOLD, ff.getSize() + 1);

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        layout.rowHeights = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        setLayout(layout);

        java.awt.GridBagConstraints gridBagConstraints;

        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.anchor = java.awt.GridBagConstraints.LINE_END;
        labelGBC.insets = new java.awt.Insets(0, 20, 10, 0);

        GridBagConstraints fieldGBC = new GridBagConstraints();
        fieldGBC.gridx = 6;
        fieldGBC.gridwidth = 9;
        fieldGBC.fill = java.awt.GridBagConstraints.HORIZONTAL;
        fieldGBC.anchor = java.awt.GridBagConstraints.LINE_START;
        fieldGBC.weightx = 0.1;
        fieldGBC.insets = new java.awt.Insets(0, 0, 10, 20);

        GridBagConstraints headBGC = new GridBagConstraints();
        headBGC.gridwidth = 15;
        headBGC.fill = java.awt.GridBagConstraints.HORIZONTAL;
        headBGC.weightx = 0.1;
        headBGC.fill = java.awt.GridBagConstraints.HORIZONTAL;
        headBGC.anchor = java.awt.GridBagConstraints.LINE_START;
        headBGC.insets = new java.awt.Insets(10, 10, 15, 10);

        GridBagConstraints separateBGC = new GridBagConstraints();
        separateBGC.gridwidth = 15;
        separateBGC.fill = java.awt.GridBagConstraints.HORIZONTAL;
        separateBGC.weightx = 0.1;
        separateBGC.fill = java.awt.GridBagConstraints.HORIZONTAL;
        separateBGC.anchor = java.awt.GridBagConstraints.LINE_START;
        separateBGC.insets = new java.awt.Insets(0, 0, 0, 0);

        int gridy = 0;
        jLabelTitle.setFont(headFont); // NOI18N
        jLabelTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTitle.setText(Lang.getInstance().translate("Action for Asset"));
        add(jLabelTitle, headBGC);

        jLabelAssetToPay.setText(Lang.getInstance().translate("Asset"));
        labelGBC.gridy = ++gridy;
        add(jLabelAssetToPay, labelGBC);

        fieldGBC.gridy = gridy;
        add(jComboBoxAssetToPay, fieldGBC);

        jLabelAction.setText(Lang.getInstance().translate("Action"));
        labelGBC.gridy = ++gridy;
        add(jLabelAction, labelGBC);

        fieldGBC.gridy = gridy;
        add(jComboBoxAction, fieldGBC);

        separateBGC.gridy = ++gridy;
        add(jSeparator1, separateBGC);

        ////////// PAYMENT METHOD

        jLabelTitlemetod.setFont(headFont); // NOI18N
        jLabelTitlemetod.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTitlemetod.setText(Lang.getInstance().translate("Method of calculation"));
        headBGC.gridy = ++gridy;
        add(jLabelTitlemetod, headBGC);

        jLabelMethod.setText(Lang.getInstance().translate("Method"));
        labelGBC.gridy = ++gridy;
        add(jLabelMethod, labelGBC);
        jComboBoxMethodPaymentType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
                Lang.getInstance().translate("Payouts make by Total Amount and balance of recipient"),
                Lang.getInstance().translate("Payouts make by Percentage and balance of recipient"),
                Lang.getInstance().translate("Absolute Value"),
        }));
        fieldGBC.gridy = gridy;
        add(jComboBoxMethodPaymentType, fieldGBC);

        labelGBC.gridy = ++gridy;
        add(jLabelAmount, labelGBC);
        fieldGBC.gridy = gridy;
        add(jTextFieldAmount, fieldGBC);

        fieldGBC.gridy = ++gridy;
        add(jLabelMethodPaymentDecscription, fieldGBC);

        java.awt.GridBagLayout jPanelLayout = new java.awt.GridBagLayout();
        jPanelLayout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0};
        jPanelLayout.rowHeights = new int[]{0};
        jPanelMinMaxAmounts.setLayout(jPanelLayout);

        jLabelPaymentMin.setText(Lang.getInstance().translate("Minimal Volume"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelMinMaxAmounts.add(jLabelPaymentMin, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelMinMaxAmounts.add(jTextFieldPaymentMin, gridBagConstraints);

        jLabelPaymentMax.setText(Lang.getInstance().translate("Maximum Volume"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelMinMaxAmounts.add(jLabelPaymentMax, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelMinMaxAmounts.add(jTextFieldPaymentMax, gridBagConstraints);

        fieldGBC.gridy = ++gridy;
        add(jPanelMinMaxAmounts, fieldGBC);

        separateBGC.gridy = ++gridy;
        add(jSeparator2, separateBGC);

        /////////////////////
        jLabel5.setFont(headFont); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText(Lang.getInstance().translate("Filter By Asset"));
        headBGC.gridy = ++gridy;
        add(jLabel5, headBGC);

        jLabel2.setText(Lang.getInstance().translate("Asset"));
        labelGBC.gridy = ++gridy;
        add(jLabel2, labelGBC);
        fieldGBC.gridy = gridy;
        add(jComboBoxFilterAsset, fieldGBC);

        jPanelFilterBalance.setLayout(jPanelLayout);

        jLabelBalancePosition.setText(Lang.getInstance().translate("Balance Position"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelFilterBalance.add(jLabelBalancePosition, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelFilterBalance.add(jComboBoxBalancePosition, gridBagConstraints);

        jLabel19.setText(Lang.getInstance().translate("Balance Side"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelFilterBalance.add(jLabel19, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelFilterBalance.add(jComboBoxSideBalance, gridBagConstraints);

        jLabel8.setText(Lang.getInstance().translate("More then"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelFilterBalance.add(jLabel8, gridBagConstraints);
        jTextFieldBQ.setToolTipText(Lang.getInstance().translate(""));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelFilterBalance.add(jTextFieldBQ, gridBagConstraints);

        jLabel9.setText(Lang.getInstance().translate("Less then"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelFilterBalance.add(jLabel9, gridBagConstraints);
        jTextFieldLQ.setToolTipText(Lang.getInstance().translate(""));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelFilterBalance.add(jTextFieldLQ, gridBagConstraints);

        fieldGBC.gridy = ++gridy;
        add(jPanelFilterBalance, fieldGBC);

        separateBGC.gridy = ++gridy;
        add(jSeparator3, separateBGC);

        jLabel20.setFont(headFont); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText(Lang.getInstance().translate("Filter by Actions"));
        headBGC.gridy = ++gridy;
        add(jLabel20, headBGC);

        jLabel3.setText(Lang.getInstance().translate("Action"));
        labelGBC.gridy = ++gridy;
        add(jLabel3, labelGBC);

        fieldGBC.gridy = gridy;
        add(jComboBoxActionFilter, fieldGBC);

        jPanelStartEndActions.setLayout(jPanelLayout);

        jLabelDataStart.setText(Lang.getInstance().translate("Data start"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelStartEndActions.add(jLabelDataStart, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelStartEndActions.add(jTextFieldDateStart, gridBagConstraints);

        jLabelDateEnd.setText(Lang.getInstance().translate("Date end"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelStartEndActions.add(jLabelDateEnd, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelStartEndActions.add(jTextFieldDateEnd, gridBagConstraints);

        fieldGBC.gridy = ++gridy;
        add(jPanelStartEndActions, fieldGBC);

        separateBGC.gridy = ++gridy;
        add(jSeparator4, separateBGC);

        jLabel4.setFont(headFont); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(Lang.getInstance().translate("Filter by Persons"));
        headBGC.gridy = ++gridy;
        add(jLabel4, headBGC);

        jLabel1.setText(Lang.getInstance().translate("Filter"));
        labelGBC.gridy = ++gridy;
        add(jLabel1, labelGBC);

        fieldGBC.gridy = gridy;
        add(jComboBoxPersonFilter, fieldGBC);

        separateBGC.gridy = ++gridy;
        add(jSeparator5, separateBGC);

        /////////////////////// BUTTONS

        jCheckBoxConfirmResult.setText(Lang.getInstance().translate("Подтверждаю правильность результата"));
        //fieldGBC.gridy = ++gridy;
        //add(jCheckBoxConfirmResult, gridBagConstraints);

        jPanel3.setLayout(jPanelLayout);

        jButtonCalcCompu.setText(Lang.getInstance().translate("Calculate fee"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 10;
        jPanel3.add(jButtonCalcCompu, gridBagConstraints);

        jButtonViewResult.setText(Lang.getInstance().translate("View Result"));
        gridBagConstraints.gridx = 1;
        jPanel3.add(jButtonViewResult, gridBagConstraints);

        jButtonSend.setText(Lang.getInstance().translate("Make"));
        jButtonSend.setFont(headFont);
        jButtonSend.setToolTipText("");
        gridBagConstraints.gridx = 2;
        jPanel3.add(jButtonSend, gridBagConstraints);

        jButtonCancel.setText(Lang.getInstance().translate("Cancel"));
        gridBagConstraints.gridx = 3;
        jPanel3.add(jButtonCancel, gridBagConstraints);

        fieldGBC.gridy = ++gridy;
        add(jPanel3, fieldGBC);

    }

    public ExPays getPays() {
        return null;
    }

    private javax.swing.JButton jButtonCalcCompu;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonSend;
    private javax.swing.JButton jButtonViewResult;
    private javax.swing.JCheckBox jCheckBoxConfirmResult;
    private javax.swing.JComboBox<String> jComboBoxAction;
    private javax.swing.JComboBox<ItemCls> jComboBoxAssetToPay;
    private javax.swing.JComboBox<String> jComboBoxMethodPaymentType;
    private javax.swing.JComboBox<ItemCls> jComboBoxFilterAsset;
    private javax.swing.JComboBox<Integer> jComboBoxActionFilter;
    private javax.swing.JComboBox<String> jComboBoxPersonFilter;
    private javax.swing.JComboBox<String> jComboBoxSideBalance;
    private javax.swing.JComboBox<Integer> jComboBoxBalancePosition;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelMethod;
    private javax.swing.JLabel jLabelAmount;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelAssetToPay;
    private javax.swing.JLabel jLabelAction;
    private javax.swing.JLabel jLabelDataStart;
    private javax.swing.JLabel jLabelDateEnd;
    private javax.swing.JLabel jLabelMethodPaymentDecscription;
    private javax.swing.JLabel jLabelPaymentMax;
    private javax.swing.JLabel jLabelPaymentMin;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JLabel jLabelTitlemetod;
    private javax.swing.JLabel jLabelBalancePosition;
    private javax.swing.JPanel jPanelMinMaxAmounts;
    private javax.swing.JPanel jPanelFilterBalance;
    private javax.swing.JPanel jPanelStartEndActions;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTextField jTextFieldAmount;
    private javax.swing.JTextField jTextFieldBQ;
    private javax.swing.JTextField jTextFieldDateEnd;
    private javax.swing.JTextField jTextFieldDateStart;
    private javax.swing.JTextField jTextFieldLQ;
    private javax.swing.JTextField jTextFieldPaymentMax;
    private javax.swing.JTextField jTextFieldPaymentMin;

}
