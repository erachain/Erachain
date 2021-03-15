package org.erachain.gui.exdata;


import com.toedter.calendar.JDateChooser;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.exdata.ExPays;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.gui.IconPanel;
import org.erachain.gui.items.assets.ComboBoxAssetsModel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.RenderComboBoxActionFilter;
import org.erachain.gui.models.RenderComboBoxAssetActions;
import org.erachain.gui.models.RenderComboBoxViewBalance;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;


public class ExAccrualsPanel extends IconPanel {

    public static String NAME = "ExAccrualsPanel";
    public static String TITLE = "Accruals";

    private ExDataPanel parent;
    public ComboBoxAssetsModel assetsModel;
    public ComboBoxAssetsModel assetsModel1;
    private Boolean lock = new Boolean(false);

    public ExAccrualsPanel(ExDataPanel parent) {
        super(NAME, TITLE);
        this.parent = parent;
        initComponents();

        assetsModel = new ComboBoxAssetsModel();
        assetsModel1 = new ComboBoxAssetsModel();
        this.jComboBoxAccrualAsset.setModel(assetsModel);
        this.jComboBoxFilterAsset.setModel(assetsModel1);
        jComboBoxFilterBalancePosition.setModel(new javax.swing.DefaultComboBoxModel(new Integer[]{
                TransactionAmount.ACTION_SEND,
                TransactionAmount.ACTION_DEBT,
                TransactionAmount.ACTION_HOLD,
                TransactionAmount.ACTION_SPEND,
                //TransactionAmount.ACTION_PLEDGE,
        }));
        jComboBoxFilterBalancePosition.setRenderer(new RenderComboBoxViewBalance());
        jComboBoxFilterSideBalance.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
                Lang.T("Total Debit"), // Transaction.BALANCE_SIDE_DEBIT = 1
                Lang.T("Left # Остаток"), // BALANCE_SIDE_LEFT = 2
                Lang.T("Total Credit") // BALANCE_SIDE_CREDIT = 3
        }));
        jComboBoxFilterSideBalance.setSelectedIndex(1);

        jComboBoxTXTypeFilter.setModel(new javax.swing.DefaultComboBoxModel<Integer>(Transaction.getTransactionTypes(true)));
        ///jComboBoxTXTypeFilter.addItem(-1);
        jComboBoxTXTypeFilter.setRenderer(new RenderComboBoxActionFilter());

        jComboBoxAccrualAction.setRenderer(new RenderComboBoxAssetActions());
        jComboBoxAccrualAsset.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateAction();
            }
        });
        updateAction();

        jComboBoxMethodPaymentType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateLabelsByMethod();
            }
        });
        updateLabelsByMethod();

        jLabelMethodPaymentDescription.setHorizontalAlignment(SwingConstants.LEFT);
        jComboBoxPersonFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
                Lang.T(ExPays.viewFilterPersMode(0)),
                Lang.T(ExPays.viewFilterPersMode(1)),
                Lang.T(ExPays.viewFilterPersMode(2)),
                Lang.T(ExPays.viewFilterPersMode(3))}));

        jCheckBoxAccrualsUse.setSelected(false);
        jCheckBoxAccrualsUse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jPanelMain.setVisible(jCheckBoxAccrualsUse.isSelected());
            }
        });

        jButtonCalcCompu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lock)
                    return;
                synchronized (lock) {
                    try {
                        lock = new Boolean(true);
                        jButtonCalcCompu.setEnabled(false);
                        jButtonViewResult.setEnabled(false);

                        jScrollPaneAccruals.setVisible(false);

                        Fun.Tuple2<ExPays, String> exPaysRes = getAccruals();
                        if (exPaysRes.b != null) {
                            jLabel_FeesResult.setText(exPaysRes.a == null ? Lang.T(exPaysRes.b) :
                                    Lang.T(exPaysRes.b) + (exPaysRes.a.errorValue == null ? "" : Lang.T(exPaysRes.a.errorValue)));
                            return;
                        }

                        ExPays pays = exPaysRes.a;
                        pays.setDC(DCSet.getInstance());
                        List<Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>>> accruals = pays.precalcFilteredAccruals(
                                Controller.getInstance().getMyHeight(), (Account) parent.parentPanel.jComboBox_Account_Work.getSelectedItem());
                        pays.calcTotalFeeBytes();
                        jLabel_FeesResult.setText("<html>" + Lang.T("Count # кол-во") + ": <b>" + pays.getFilteredAccrualsCount()
                                + "</b>, " + Lang.T("Additional Fee") + ": <b>" + BlockChain.feeBG(pays.getTotalFeeBytes())
                                + "</b>, " + Lang.T("Total") + ": <b>" + pays.getTotalPay());
                    } finally {
                        jButtonCalcCompu.setEnabled(true);
                        jButtonViewResult.setEnabled(true);

                        //jScrollPaneAccruals.setVisible(true);
                        lock = new Boolean(false);
                    }
                }
            }
        });

        jButtonViewResult.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lock)
                    return;
                synchronized (lock) {
                    try {
                        lock = new Boolean(true);
                        jButtonCalcCompu.setEnabled(false);
                        jButtonViewResult.setEnabled(false);

                        jScrollPaneAccruals.setVisible(false);

                        Fun.Tuple2<ExPays, String> exPaysRes = getAccruals();
                        if (exPaysRes.b != null) {
                            jLabel_FeesResult.setText(exPaysRes.a == null ? Lang.T(exPaysRes.b) :
                                    Lang.T(exPaysRes.b) + (exPaysRes.a.errorValue == null ? "" : Lang.T(exPaysRes.a.errorValue)));
                            jButtonViewResult.setEnabled(true);
                            return;
                        }

                        ExPays pays = exPaysRes.a;
                        pays.setDC(DCSet.getInstance());
                        List<Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>>> accrual = pays.precalcFilteredAccruals(
                                Controller.getInstance().getMyHeight(), (Account) parent.parentPanel.jComboBox_Account_Work.getSelectedItem());
                        pays.calcTotalFeeBytes();
                        String result = "<html>" + Lang.T("Count # кол-во") + ": <b>" + pays.getFilteredAccrualsCount()
                                + "</b>, " + Lang.T("Additional Fee") + ": <b>" + BlockChain.feeBG(pays.getTotalFeeBytes())
                                + "</b>, " + Lang.T("Total") + ": <b>" + pays.getTotalPay();
                        jLabel_FeesResult.setText(result);

                        AccrualsModel model = new AccrualsModel(accrual);
                        jTablePreviewAccruals.setModel(model);
                        TableColumnModel columnModel = jTablePreviewAccruals.getColumnModel();

                        TableColumn columnNo = columnModel.getColumn(0);
                        columnNo.setMinWidth(50);
                        columnNo.setMaxWidth(100);
                        columnNo.setPreferredWidth(70);
                        columnNo.setWidth(70);
                        columnNo.sizeWidthToFit();

                        TableColumn columnBal = columnModel.getColumn(1);
                        columnBal.setMinWidth(100);
                        columnBal.setMaxWidth(200);
                        columnBal.setPreferredWidth(150);
                        columnBal.setWidth(150);
                        columnBal.sizeWidthToFit();

                        TableColumn columnPay = columnModel.getColumn(3);
                        columnPay.setMinWidth(100);
                        columnPay.setMaxWidth(200);
                        columnPay.setPreferredWidth(150);
                        columnPay.setWidth(150);
                        columnPay.sizeWidthToFit();

                    } finally {
                        jButtonCalcCompu.setEnabled(true);
                        jButtonViewResult.setEnabled(true);

                        jScrollPaneAccruals.setVisible(true);
                        lock = new Boolean(false);

                    }
                }
            }
        });

        /*
        jCheckBoxUseFilterAsset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = jCheckBoxUseFilterAsset.isSelected();
                jComboBoxFilterAsset.setEnabled(enabled);
                jComboBoxFilterBalancePosition.setEnabled(enabled);
                jComboBoxFilterSideBalance.setEnabled(enabled);
                jTextFieldBQ.setEnabled(enabled);
                jTextFieldLQ.setEnabled(enabled);
            }
        });
         */
    }

    public void updateAction() {
        AssetCls asset = (AssetCls) jComboBoxAccrualAsset.getSelectedItem();
        if (asset == null)
            return;

        Account creator = (Account) parent.parentPanel.jComboBox_Account_Work.getSelectedItem();
        if (creator == null)
            return;

        int selected = jComboBoxAccrualAction.getSelectedIndex();
        jComboBoxAccrualAction.setModel(new javax.swing.DefaultComboBoxModel(
                asset.viewAssetTypeActionsList(creator.equals(asset.getMaker()), false).toArray()));
        if (selected >= 0)
            jComboBoxAccrualAction.setSelectedIndex(selected);

    }

    private void updateLabelsByMethod() {
        switch (jComboBoxMethodPaymentType.getSelectedIndex()) {
            case ExPays.PAYMENT_METHOD_TOTAL:
                jLabelMethodPaymentDescription.setText("<html>" +
                        Lang.T("PAY_METHOD_0_D"));
                jLabelAmount.setText(Lang.T("Total Amount"));
                jTextFieldPaymentMin.setEnabled(false);
                jTextFieldPaymentMax.setEnabled(false);

                jCheckBoxSelfPay.setVisible(true);
                return;
            case ExPays.PAYMENT_METHOD_COEFF:
                jLabelMethodPaymentDescription.setText("<html>" +
                        Lang.T("PAY_METHOD_1_D"));
                jLabelAmount.setText(Lang.T("Coefficient"));
                jTextFieldPaymentMin.setEnabled(true);
                jTextFieldPaymentMax.setEnabled(true);

                jCheckBoxSelfPay.setSelected(false);
                jCheckBoxSelfPay.setVisible(false);

                return;
            case ExPays.PAYMENT_METHOD_ABSOLUTE:
                jLabelMethodPaymentDescription.setText("<html>" +
                        Lang.T("PAY_METHOD_2_D"));
                jLabelAmount.setText(Lang.T("Amount"));
                jTextFieldPaymentMin.setEnabled(false);
                jTextFieldPaymentMax.setEnabled(false);

                jCheckBoxSelfPay.setSelected(false);
                jCheckBoxSelfPay.setVisible(false);
                return;
        }
    }


    private void initComponents() {

        jPanelMain = new JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabelActionAssetTitle = new javax.swing.JLabel();
        jCheckBoxUseFilterAsset = new javax.swing.JCheckBox();
        jLabelFilterAsset = new javax.swing.JLabel();
        jComboBoxAccrualAsset = new javax.swing.JComboBox<>();
        jComboBoxAccrualAction = new javax.swing.JComboBox<>();
        jComboBoxFilterAsset = new javax.swing.JComboBox<>();
        jLabelBalancePosition = new javax.swing.JLabel();
        jComboBoxFilterBalancePosition = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldBQ = new javax.swing.JTextField();
        jTextFieldLQ = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabelAssetToPay = new javax.swing.JLabel();
        jLabelAction = new javax.swing.JLabel();
        jLabelTitlemetod = new javax.swing.JLabel();
        jLabelMethodPaymentDescription = new javax.swing.JLabel();
        jLabelPaymentMin = new javax.swing.JLabel();
        jTextFieldPaymentMin = new javax.swing.JTextField();
        jLabelPaymentMax = new javax.swing.JLabel();
        jLabel_FeesResult = new javax.swing.JLabel();
        jTextFieldPaymentMax = new javax.swing.JTextField();
        jPanelMinMaxAmounts = new javax.swing.JPanel();
        jPanelFilterBalance = new javax.swing.JPanel();
        jPanelStartEndActions = new javax.swing.JPanel();
        jLabelDataStart = new javax.swing.JLabel();
        jLabelDateEnd = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jComboBoxFilterSideBalance = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        jComboBoxTXTypeFilter = new javax.swing.JComboBox<>();
        jPanelMain = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jButtonViewResult = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jCheckBoxSelfPay = new javax.swing.JCheckBox();
        jCheckBoxAccrualsUse = new javax.swing.JCheckBox();
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

        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        jTextFieldDateStart = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
        jTextFieldDateEnd = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
        TimeZone.setDefault(tz);

        jTextFieldDateStart.setFont(UIManager.getFont("TextField.font"));
        jTextFieldDateEnd.setFont(UIManager.getFont("TextField.font"));

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

        jCheckBoxAccrualsUse.setText(Lang.T("Make Accruals"));
        add(jCheckBoxAccrualsUse, fieldGBC);

        jPanelMain.setLayout(layout);
        jPanelMain.setVisible(false);

        jLabelActionAssetTitle.setFont(headFont); // NOI18N
        jLabelActionAssetTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelActionAssetTitle.setText(Lang.T("Action for Asset"));
        jPanelMain.add(jLabelActionAssetTitle, headBGC);

        jLabelAssetToPay.setText(Lang.T("Asset"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelAssetToPay, labelGBC);

        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxAccrualAsset, fieldGBC);

        jLabelAction.setText(Lang.T("Action"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelAction, labelGBC);

        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxAccrualAction, fieldGBC);

        ////////// PAYMENT METHOD

        jLabelTitlemetod.setFont(headFont); // NOI18N
        jLabelTitlemetod.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTitlemetod.setText(Lang.T("Method of calculation"));
        headBGC.gridy = ++gridy;
        jPanelMain.add(jLabelTitlemetod, headBGC);

        jLabelMethod.setText(Lang.T("Method"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelMethod, labelGBC);
        jComboBoxMethodPaymentType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
                Lang.T(ExPays.viewPayMethod(0)),
                Lang.T(ExPays.viewPayMethod(1)),
                Lang.T(ExPays.viewPayMethod(2)),
        }));
        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxMethodPaymentType, fieldGBC);

        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelAmount, labelGBC);
        fieldGBC.gridy = gridy;
        jPanelMain.add(jTextFieldAmount, fieldGBC);

        fieldGBC.gridy = ++gridy;
        jPanelMain.add(jLabelMethodPaymentDescription, fieldGBC);

        java.awt.GridBagLayout jPanelLayout = new java.awt.GridBagLayout();
        jPanelLayout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0};
        jPanelLayout.rowHeights = new int[]{0};
        jPanelMinMaxAmounts.setLayout(jPanelLayout);

        jLabelPaymentMin.setText(Lang.T("Minimal Accrual"));
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

        jLabelPaymentMax.setText(Lang.T("Maximum Accrual"));
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
        jPanelMain.add(jPanelMinMaxAmounts, fieldGBC);

        separateBGC.gridy = ++gridy;
        jPanelMain.add(jSeparator2, separateBGC);

        /////////////////////
        //jCheckBoxUseFilterAsset.setSelected(true);
        jLabelFilterAsset.setFont(headFont); // NOI18N
        jLabelFilterAsset.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelFilterAsset.setText(Lang.T("Filter By Asset and Balance"));
        headBGC.gridy = ++gridy;
        jPanelMain.add(jLabelFilterAsset, headBGC);

        jLabel2.setText(Lang.T("Asset"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel2, labelGBC);
        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxFilterAsset, fieldGBC);

        jPanelFilterBalance.setLayout(jPanelLayout);

        jLabelBalancePosition.setText(Lang.T("Balance Position"));
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
        jPanelFilterBalance.add(jComboBoxFilterBalancePosition, gridBagConstraints);

        jLabel19.setText(Lang.T("Balance Side"));
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
        jPanelFilterBalance.add(jComboBoxFilterSideBalance, gridBagConstraints);

        jLabel8.setText(Lang.T("More or Equal"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelFilterBalance.add(jLabel8, gridBagConstraints);
        jTextFieldBQ.setToolTipText(Lang.T(""));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelFilterBalance.add(jTextFieldBQ, gridBagConstraints);

        jLabel9.setText(Lang.T("Less or Equal"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelFilterBalance.add(jLabel9, gridBagConstraints);
        jTextFieldLQ.setToolTipText(Lang.T(""));
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
        jPanelMain.add(jPanelFilterBalance, fieldGBC);

        separateBGC.gridy = ++gridy;
        jPanelMain.add(jSeparator3, separateBGC);

        jLabel20.setFont(headFont); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText(Lang.T("Filter by Actions and Period"));
        headBGC.gridy = ++gridy;
        jPanelMain.add(jLabel20, headBGC);

        jLabel3.setText(Lang.T("Action"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel3, labelGBC);

        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxTXTypeFilter, fieldGBC);

        jPanelStartEndActions.setLayout(jPanelLayout);

        jLabelDataStart.setText(Lang.T("Data start"));
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
        //jTextFieldDateStart.setToolTipText(Lang.T(
        //        "Empty or %1 or as timestamp in seconds").replace("%1","yyyy-MM-dd hh:mm:00"));

        jLabelDateEnd.setText(Lang.T("Date end"));
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
        //jTextFieldDateEnd.setToolTipText(Lang.T(
        //        "Empty or %1 or as timestamp in seconds").replace("%1","yyyy-MM-dd hh:mm:00"));
        jPanelStartEndActions.add(jTextFieldDateEnd, gridBagConstraints);

        fieldGBC.gridy = ++gridy;
        jPanelMain.add(jPanelStartEndActions, fieldGBC);

        separateBGC.gridy = ++gridy;
        jPanelMain.add(jSeparator4, separateBGC);

        jLabel4.setFont(headFont); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(Lang.T("Filter by Persons"));
        headBGC.gridy = ++gridy;
        jPanelMain.add(jLabel4, headBGC);

        jLabel1.setText(Lang.T("Filter"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel1, labelGBC);

        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxPersonFilter, fieldGBC);

        separateBGC.gridy = ++gridy;
        jPanelMain.add(jSeparator5, separateBGC);

        jCheckBoxSelfPay.setText(Lang.T("Accrual by creator account too"));
        jCheckBoxSelfPay.setSelected(true);
        fieldGBC.gridy = ++gridy;
        jPanelMain.add(jCheckBoxSelfPay, fieldGBC);

        /////////////////////// BUTTONS

        jPanel3.setLayout(jPanelLayout);

        jButtonCalcCompu.setText(Lang.T("Preview Fee"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 10;
        jPanel3.add(jButtonCalcCompu, gridBagConstraints);

        jButtonViewResult.setText(Lang.T("Preview Results"));
        gridBagConstraints.gridx = 1;
        jPanel3.add(jButtonViewResult, gridBagConstraints);

        headBGC.gridy = ++gridy;
        jPanel3.add(jLabel_FeesResult, headBGC);

        jTablePreviewAccruals = new MTable(new AccrualsModel(new ArrayList<>()));

        jTablePreviewAccruals.setAutoCreateRowSorter(true);
        jScrollPaneAccruals.setViewportView(jTablePreviewAccruals);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.2;

        headBGC.gridy = ++gridy;
        jPanel3.add(jScrollPaneAccruals, gridBagConstraints);

        ///////// PANEL 3
        fieldGBC.gridy = ++gridy;
        jPanelMain.add(jPanel3, fieldGBC);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        fieldGBC.weightx = 0.1;
        add(jPanelMain, gridBagConstraints);

    }

    public Fun.Tuple2<ExPays, String> getAccruals() {

        if (!jPanelMain.isVisible())
            return new Fun.Tuple2<>(null, null);

        Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String> balancePosition
                = (Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String>) jComboBoxAccrualAction.getSelectedItem();

        Integer txTypeFilter = (Integer) jComboBoxTXTypeFilter.getSelectedItem();

        String jTextFieldDateStartStr;
        try {
            jTextFieldDateStartStr = "" + jTextFieldDateStart.getCalendar().getTimeInMillis() / 1000L;
        } catch (Exception ed1) {
            jTextFieldDateStartStr = null;
        }
        String jTextFieldDateEndStr;
        try {
            jTextFieldDateEndStr = "" + jTextFieldDateEnd.getCalendar().getTimeInMillis() / 1000L;
        } catch (Exception ed1) {
            jTextFieldDateEndStr = null;
        }

        boolean minMaxUse = jComboBoxMethodPaymentType.getSelectedIndex() == ExPays.PAYMENT_METHOD_COEFF;
        return ExPays.make(
                ((AssetCls) jComboBoxAccrualAsset.getSelectedItem()).getKey(),
                balancePosition.a.a, balancePosition.a.b,
                jComboBoxMethodPaymentType.getSelectedIndex(),
                jTextFieldAmount.getText(),
                minMaxUse ? jTextFieldPaymentMin.getText() : null,
                minMaxUse ? jTextFieldPaymentMax.getText() : null,
                ((AssetCls) jComboBoxFilterAsset.getSelectedItem()).getKey(),
                jComboBoxFilterBalancePosition.getSelectedIndex() + 1, jComboBoxFilterSideBalance.getSelectedIndex() + 1,
                jTextFieldBQ.getText(), jTextFieldLQ.getText(),
                txTypeFilter,
                jTextFieldDateStartStr, jTextFieldDateEndStr,
                jComboBoxPersonFilter.getSelectedIndex(), jCheckBoxSelfPay.isSelected());
    }

    private javax.swing.JButton jButtonCalcCompu;
    private javax.swing.JButton jButtonViewResult;
    private javax.swing.JLabel jLabelFilterAsset;
    private javax.swing.JCheckBox jCheckBoxUseFilterAsset;
    public javax.swing.JCheckBox jCheckBoxAccrualsUse;
    private javax.swing.JCheckBox jCheckBoxSelfPay;
    private javax.swing.JComboBox<Fun.Tuple2<Fun.Tuple2, String>> jComboBoxAccrualAction;
    public javax.swing.JComboBox<ItemCls> jComboBoxAccrualAsset;
    private javax.swing.JComboBox<String> jComboBoxMethodPaymentType;
    public javax.swing.JComboBox<ItemCls> jComboBoxFilterAsset;
    private javax.swing.JComboBox<Integer> jComboBoxTXTypeFilter;
    private javax.swing.JComboBox<String> jComboBoxPersonFilter;
    private javax.swing.JComboBox<String> jComboBoxFilterSideBalance;
    private javax.swing.JComboBox<Integer> jComboBoxFilterBalancePosition;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelMethod;
    private javax.swing.JLabel jLabelActionAssetTitle;
    private javax.swing.JLabel jLabelAmount;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelAssetToPay;
    private javax.swing.JLabel jLabelAction;
    private javax.swing.JLabel jLabelDataStart;
    private javax.swing.JLabel jLabelDateEnd;
    private javax.swing.JLabel jLabelMethodPaymentDescription;
    private javax.swing.JLabel jLabelPaymentMax;
    private javax.swing.JLabel jLabelPaymentMin;
    private javax.swing.JLabel jLabelTitlemetod;
    private javax.swing.JLabel jLabel_FeesResult;
    private javax.swing.JLabel jLabelBalancePosition;
    private javax.swing.JPanel jPanelMinMaxAmounts;
    private javax.swing.JPanel jPanelFilterBalance;
    private javax.swing.JPanel jPanelStartEndActions;
    public javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTextField jTextFieldAmount;
    private javax.swing.JTextField jTextFieldBQ;
    private JDateChooser jTextFieldDateStart;
    private JDateChooser jTextFieldDateEnd;

    private javax.swing.JTextField jTextFieldLQ;
    private javax.swing.JTextField jTextFieldPaymentMax;
    private javax.swing.JTextField jTextFieldPaymentMin;

    private MTable jTablePreviewAccruals;
    private JScrollPane jScrollPaneAccruals = new JScrollPane();
}
