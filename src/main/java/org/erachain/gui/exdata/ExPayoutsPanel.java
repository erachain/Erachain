package org.erachain.gui.exdata;


import com.toedter.calendar.JDateChooser;
import org.erachain.core.account.Account;
import org.erachain.core.exdata.ExPays;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.IconPanel;
import org.erachain.gui.items.assets.ComboBoxAssetsModel;
import org.erachain.gui.models.RenderComboBoxActionFilter;
import org.erachain.gui.models.RenderComboBoxAssetActions;
import org.erachain.gui.models.RenderComboBoxViewBalance;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.TimeZone;


public class ExPayoutsPanel extends IconPanel {

    public static String NAME = "ExPayoutsPanel";
    public static String TITLE = "Payouts";

    private ExDataPanel parent;
    public ComboBoxAssetsModel accountsModel;
    public ComboBoxAssetsModel accountsModel1;

    public ExPayoutsPanel(ExDataPanel parent) {
        super(NAME, TITLE);
        this.parent = parent;
        initComponents();

        accountsModel = new ComboBoxAssetsModel();
        accountsModel1 = new ComboBoxAssetsModel();
        this.jComboBoxPayoutAsset.setModel(accountsModel);
        this.jComboBoxFilterAsset.setModel(accountsModel1);
        jComboBoxFilterBalancePosition.setModel(new javax.swing.DefaultComboBoxModel(new Integer[]{
                TransactionAmount.ACTION_SEND,
                TransactionAmount.ACTION_DEBT,
                TransactionAmount.ACTION_HOLD,
                TransactionAmount.ACTION_SPEND,
                //TransactionAmount.ACTION_PLEDGE,
        }));
        jComboBoxFilterBalancePosition.setRenderer(new RenderComboBoxViewBalance());
        jComboBoxFilterSideBalance.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
                Lang.getInstance().translate("Total Debit"), // Transaction.BALANCE_SIDE_DEBIT = 1
                Lang.getInstance().translate("Left"), // BALANCE_SIDE_LEFT = 2
                Lang.getInstance().translate("Total Credit") // BALANCE_SIDE_CREDIT = 3
        }));
        jComboBoxFilterSideBalance.setSelectedIndex(1);

        jComboBoxTXTypeFilter.setModel(new javax.swing.DefaultComboBoxModel<Integer>(Transaction.getTransactionTypes()));
        jComboBoxTXTypeFilter.addItem(-1);
        jComboBoxTXTypeFilter.setRenderer(new RenderComboBoxActionFilter());

        jComboBoxPayoutAction.setRenderer(new RenderComboBoxAssetActions());
        jComboBoxPayoutAsset.addItemListener(new ItemListener() {
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

        jLabelMethodPaymentDecscription.setHorizontalAlignment(SwingConstants.LEFT);
        jComboBoxPersonFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
                Lang.getInstance().translate("All"),
                Lang.getInstance().translate("Only for Persons"),
                Lang.getInstance().translate("Only for Men"),
                Lang.getInstance().translate("Only for Women")}));

        jCheckBoxPayoutsUse.setSelected(false);
        jCheckBoxPayoutsUse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jPanelMain.setVisible(jCheckBoxPayoutsUse.isSelected());
            }
        });

        jButtonViewResult.addActionListener(new ActionListener() {
            // create Hashs
            @Override
            public void actionPerformed(ActionEvent e) {
                ExPays pays = getPayouts().a;
                //pays.process();
            }
        });

        jButtonCalcCompu.addActionListener(new ActionListener() {
            // create Hashs
            @Override
            public void actionPerformed(ActionEvent e) {
                ExPays pays = getPayouts().a;
                //pays.orphan()
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
        AssetCls asset = (AssetCls) jComboBoxPayoutAsset.getSelectedItem();
        if (asset == null)
            return;

        Account creator = (Account) parent.parentPanel.jComboBox_Account_Work.getSelectedItem();
        if (creator == null)
            return;

        int selected = jComboBoxPayoutAction.getSelectedIndex();
        jComboBoxPayoutAction.setModel(new javax.swing.DefaultComboBoxModel(
                asset.viewAssetTypeActionsList(creator.equals(asset.getOwner()), false).toArray()));
        if (selected >= 0)
            jComboBoxPayoutAction.setSelectedIndex(selected);

    }

    private void updateLabelsByMethod() {
        switch (jComboBoxMethodPaymentType.getSelectedIndex()) {
            case 0:
                jLabelMethodPaymentDecscription.setText("<html>" +
                        Lang.getInstance().translate("PAY_METHOD_0_D"));
                jLabelAmount.setText(Lang.getInstance().translate("Total"));
                jTextFieldPaymentMin.setEnabled(true);
                jTextFieldPaymentMax.setEnabled(true);
                return;
            case 1:
                jLabelMethodPaymentDecscription.setText("<html>" +
                        Lang.getInstance().translate("PAY_METHOD_1_D"));
                jLabelAmount.setText(Lang.getInstance().translate("Percent"));
                jTextFieldPaymentMin.setEnabled(true);
                jTextFieldPaymentMax.setEnabled(true);
                return;
            case 2:
                jLabelMethodPaymentDecscription.setText("<html>" +
                        Lang.getInstance().translate("PAY_METHOD_2_D"));
                jLabelAmount.setText(Lang.getInstance().translate("Amount"));
                jTextFieldPaymentMin.setEnabled(false);
                jTextFieldPaymentMax.setEnabled(false);
                return;
        }
    }


    private void initComponents() {

        jPanelMain = new JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabelActionAssetTitle = new javax.swing.JLabel();
        jCheckBoxUseFilterAsset = new javax.swing.JCheckBox();
        jLabelFilterAsset = new javax.swing.JLabel();
        jComboBoxPayoutAsset = new javax.swing.JComboBox<>();
        jComboBoxPayoutAction = new javax.swing.JComboBox<>();
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
        jLabelMethodPaymentDecscription = new javax.swing.JLabel();
        jLabelPaymentMin = new javax.swing.JLabel();
        jTextFieldPaymentMin = new javax.swing.JTextField();
        jLabelPaymentMax = new javax.swing.JLabel();
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
        jCheckBoxPayoutsUse = new javax.swing.JCheckBox();
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

        jCheckBoxPayoutsUse.setText(Lang.getInstance().translate("Make Payouts"));
        add(jCheckBoxPayoutsUse, fieldGBC);

        jPanelMain.setLayout(layout);
        jPanelMain.setVisible(false);

        jLabelActionAssetTitle.setFont(headFont); // NOI18N
        jLabelActionAssetTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelActionAssetTitle.setText(Lang.getInstance().translate("Action for Asset"));
        jPanelMain.add(jLabelActionAssetTitle, headBGC);

        jLabelAssetToPay.setText(Lang.getInstance().translate("Asset"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelAssetToPay, labelGBC);

        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxPayoutAsset, fieldGBC);

        jLabelAction.setText(Lang.getInstance().translate("Action"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelAction, labelGBC);

        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxPayoutAction, fieldGBC);

        ////////// PAYMENT METHOD

        jLabelTitlemetod.setFont(headFont); // NOI18N
        jLabelTitlemetod.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTitlemetod.setText(Lang.getInstance().translate("Method of calculation"));
        headBGC.gridy = ++gridy;
        jPanelMain.add(jLabelTitlemetod, headBGC);

        jLabelMethod.setText(Lang.getInstance().translate("Method"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelMethod, labelGBC);
        jComboBoxMethodPaymentType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
                Lang.getInstance().translate("PAY_METHOD_0"),
                Lang.getInstance().translate("PAY_METHOD_1"),
                Lang.getInstance().translate("PAY_METHOD_2"),
        }));
        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxMethodPaymentType, fieldGBC);

        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelAmount, labelGBC);
        fieldGBC.gridy = gridy;
        jPanelMain.add(jTextFieldAmount, fieldGBC);

        fieldGBC.gridy = ++gridy;
        jPanelMain.add(jLabelMethodPaymentDecscription, fieldGBC);

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
        jPanelMain.add(jPanelMinMaxAmounts, fieldGBC);

        separateBGC.gridy = ++gridy;
        jPanelMain.add(jSeparator2, separateBGC);

        /////////////////////
        //jCheckBoxUseFilterAsset.setSelected(true);
        jLabelFilterAsset.setFont(headFont); // NOI18N
        jLabelFilterAsset.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelFilterAsset.setText(Lang.getInstance().translate("Filter By Asset"));
        headBGC.gridy = ++gridy;
        jPanelMain.add(jLabelFilterAsset, headBGC);

        jLabel2.setText(Lang.getInstance().translate("Asset"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel2, labelGBC);
        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxFilterAsset, fieldGBC);

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
        jPanelFilterBalance.add(jComboBoxFilterBalancePosition, gridBagConstraints);

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
        jPanelFilterBalance.add(jComboBoxFilterSideBalance, gridBagConstraints);

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
        jPanelMain.add(jPanelFilterBalance, fieldGBC);

        separateBGC.gridy = ++gridy;
        jPanelMain.add(jSeparator3, separateBGC);

        jLabel20.setFont(headFont); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText(Lang.getInstance().translate("Filter by Actions"));
        headBGC.gridy = ++gridy;
        jPanelMain.add(jLabel20, headBGC);

        jLabel3.setText(Lang.getInstance().translate("Action"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel3, labelGBC);

        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxTXTypeFilter, fieldGBC);

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
        //jTextFieldDateStart.setToolTipText(Lang.getInstance().translate(
        //        "Empty or %1 or as timestamp in seconds").replace("%1","yyyy-MM-dd hh:mm:00"));

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
        //jTextFieldDateEnd.setToolTipText(Lang.getInstance().translate(
        //        "Empty or %1 or as timestamp in seconds").replace("%1","yyyy-MM-dd hh:mm:00"));
        jPanelStartEndActions.add(jTextFieldDateEnd, gridBagConstraints);

        fieldGBC.gridy = ++gridy;
        jPanelMain.add(jPanelStartEndActions, fieldGBC);

        separateBGC.gridy = ++gridy;
        jPanelMain.add(jSeparator4, separateBGC);

        jLabel4.setFont(headFont); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(Lang.getInstance().translate("Filter by Persons"));
        headBGC.gridy = ++gridy;
        jPanelMain.add(jLabel4, headBGC);

        jLabel1.setText(Lang.getInstance().translate("Filter"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel1, labelGBC);

        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxPersonFilter, fieldGBC);

        separateBGC.gridy = ++gridy;
        jPanelMain.add(jSeparator5, separateBGC);

        jCheckBoxSelfPay.setText(Lang.getInstance().translate("Payout to Self too"));
        jCheckBoxSelfPay.setSelected(true);
        fieldGBC.gridy = ++gridy;
        jPanelMain.add(jCheckBoxSelfPay, fieldGBC);

        /////////////////////// BUTTONS

        jPanel3.setLayout(jPanelLayout);

        jButtonCalcCompu.setText(Lang.getInstance().translate("Calculate fee"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 10;
        jPanel3.add(jButtonCalcCompu, gridBagConstraints);

        jButtonViewResult.setText(Lang.getInstance().translate("Preview Results"));
        gridBagConstraints.gridx = 1;
        jPanel3.add(jButtonViewResult, gridBagConstraints);

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

    public Fun.Tuple2<ExPays, String> getPayouts() {

        if (!jPanelMain.isVisible())
            return new Fun.Tuple2<>(null, null);

        Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String> balancePosition
                = (Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String>) jComboBoxPayoutAction.getSelectedItem();

        Integer txTypeFilter = (Integer) jComboBoxTXTypeFilter.getSelectedItem();

        String jTextFieldDateStartStr;
        try {
            jTextFieldDateStartStr = "" + jTextFieldDateStart.getCalendar().getTimeInMillis() * 0.001;
        } catch (Exception ed1) {
            jTextFieldDateStartStr = null;
        }
        String jTextFieldDateEndStr;
        try {
            jTextFieldDateEndStr = "" + jTextFieldDateEnd.getCalendar().getTimeInMillis() * 0.001;
        } catch (Exception ed1) {
            jTextFieldDateEndStr = null;
        }

        return ExPays.make(
                ((AssetCls) jComboBoxPayoutAsset.getSelectedItem()).getKey(),
                balancePosition.a.a, balancePosition.a.b,
                jComboBoxMethodPaymentType.getSelectedIndex(),
                jTextFieldAmount.getText(),
                jTextFieldPaymentMin.getText(),
                jTextFieldPaymentMax.getText(),
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
    private javax.swing.JCheckBox jCheckBoxPayoutsUse;
    private javax.swing.JCheckBox jCheckBoxSelfPay;
    private javax.swing.JComboBox<Fun.Tuple2<Fun.Tuple2, String>> jComboBoxPayoutAction;
    public javax.swing.JComboBox<ItemCls> jComboBoxPayoutAsset;
    private javax.swing.JComboBox<String> jComboBoxMethodPaymentType;
    private javax.swing.JComboBox<ItemCls> jComboBoxFilterAsset;
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
    private javax.swing.JLabel jLabelMethodPaymentDecscription;
    private javax.swing.JLabel jLabelPaymentMax;
    private javax.swing.JLabel jLabelPaymentMin;
    private javax.swing.JLabel jLabelTitlemetod;
    private javax.swing.JLabel jLabelBalancePosition;
    private javax.swing.JPanel jPanelMinMaxAmounts;
    private javax.swing.JPanel jPanelFilterBalance;
    private javax.swing.JPanel jPanelStartEndActions;
    private javax.swing.JPanel jPanelMain;
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

}
