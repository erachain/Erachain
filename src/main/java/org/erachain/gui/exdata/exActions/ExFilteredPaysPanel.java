package org.erachain.gui.exdata.exActions;


import com.toedter.calendar.JDateChooser;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.exdata.exActions.ExAction;
import org.erachain.core.exdata.exActions.ExPays;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.gui.IconPanel;
import org.erachain.gui.exdata.AccrualsModel;
import org.erachain.gui.exdata.ExDataPanel;
import org.erachain.gui.items.assets.ComboBoxAssetsModel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.RenderComboBoxActionFilter;
import org.erachain.gui.models.RenderComboBoxAssetActions;
import org.erachain.gui.models.RenderComboBoxViewBalance;
import org.erachain.gui.transaction.OnDealClick;
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


public class ExFilteredPaysPanel extends IconPanel implements ExActionPanelInt {

    public static String NAME = "ExActionPanel";
    public static String TITLE = "Accruals";

    private ExDataPanel parent;
    public ComboBoxAssetsModel assetsModel;
    public ComboBoxAssetsModel assetsModel1;
    private Boolean lock = new Boolean(false);
    private AssetCls asset;

    public ExFilteredPaysPanel(ExDataPanel parent) {
        super(NAME, TITLE);
        this.parent = parent;
        initComponents();

        assetsModel = new ComboBoxAssetsModel();
        assetsModel1 = new ComboBoxAssetsModel();
        this.jComboBoxAccrualAsset.setModel(assetsModel);
        this.jComboBoxFilterAsset.setModel(assetsModel1);
        jComboBoxFilterBalancePosition.setModel(new DefaultComboBoxModel(new Integer[]{
                TransactionAmount.ACTION_SEND,
                TransactionAmount.ACTION_DEBT,
                TransactionAmount.ACTION_HOLD,
                TransactionAmount.ACTION_SPEND,
                //TransactionAmount.ACTION_PLEDGE,
        }));
        jComboBoxFilterBalancePosition.setRenderer(new RenderComboBoxViewBalance());
        jComboBoxFilterSideBalance.setModel(new DefaultComboBoxModel(new String[]{
                Lang.T("Total Debit"), // Transaction.BALANCE_SIDE_DEBIT = 1
                Lang.T("Left # Остаток"), // BALANCE_SIDE_LEFT = 2
                Lang.T("Total Credit") // BALANCE_SIDE_CREDIT = 3
        }));
        jComboBoxFilterSideBalance.setSelectedIndex(1);

        jComboBoxTXTypeFilter.setModel(new DefaultComboBoxModel<Integer>(Transaction.getTransactionTypes(true)));
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
        jComboBoxPersonFilter.setModel(new DefaultComboBoxModel<>(new String[]{
                Lang.T(ExPays.viewFilterPersMode(0)),
                Lang.T(ExPays.viewFilterPersMode(1)),
                Lang.T(ExPays.viewFilterPersMode(2)),
                Lang.T(ExPays.viewFilterPersMode(3))}));

        jCheckBoxAccrualsUse.setSelected(false);
        jCheckBoxAccrualsUse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jLabel_Help.setVisible(!jCheckBoxAccrualsUse.isSelected());
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

                        Fun.Tuple2<ExAction, String> exActionRes = getResult();
                        if (exActionRes.b != null) {
                            jLabel_FeesResult.setText(Lang.T("Error") + "! " + exActionRes.a == null ? Lang.T(exActionRes.b) :
                                    Lang.T(exActionRes.b) + (exActionRes.a.errorValue == null ? "" : Lang.T(exActionRes.a.errorValue)));
                            return;
                        }

                        ExPays pays = (ExPays) exActionRes.a;
                        pays.setDC(DCSet.getInstance());
                        pays.preProcess(Controller.getInstance().getMyHeight(), (Account) parent.parentPanel.jComboBox_Account_Work.getSelectedItem(), false);
                        List<Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>>> accruals = pays.getResults();
                        pays.calcTotalFeeBytes();
                        jLabel_FeesResult.setText("<html>" + Lang.T("Count # кол-во") + ": <b>" + accruals.size()
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

                        Fun.Tuple2<ExAction, String> exActionRes = getResult();
                        if (exActionRes.b != null) {
                            jLabel_FeesResult.setText(Lang.T("Error") + "! " + (exActionRes.a == null ? Lang.T(exActionRes.b) :
                                    Lang.T(exActionRes.b) + (exActionRes.a.errorValue == null ? "" : Lang.T(exActionRes.a.errorValue))));
                            jButtonViewResult.setEnabled(true);
                            return;
                        }

                        ExPays pays = (ExPays) exActionRes.a;
                        pays.setDC(DCSet.getInstance());
                        pays.preProcess(Controller.getInstance().getMyHeight(), (Account) parent.parentPanel.jComboBox_Account_Work.getSelectedItem(), true);
                        List<Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>>> results = pays.getResults();
                        pays.calcTotalFeeBytes();

                        String result = "<html>";
                        if (pays.resultCode != Transaction.VALIDATE_OK) {
                            result += "<b>" + Lang.T("Error") + "!<b> " + Lang.T(OnDealClick.resultMess(pays.resultCode)) + "<br>";
                            result += Lang.T("Found errors") + ":<b> " + pays.getResultsCount() + "<br>";
                        } else {
                            result += Lang.T("Count # кол-во") + ": <b>" + pays.getResultsCount()
                                    + "</b>, " + Lang.T("Additional Fee") + ": <b>" + BlockChain.feeBG(pays.getTotalFeeBytes())
                                    + "</b>, " + Lang.T("Total") + ": <b>" + pays.getTotalPay();
                        }
                        jLabel_FeesResult.setText(result);

                        AccrualsModel model = new AccrualsModel(results, pays.resultCode != Transaction.VALIDATE_OK);
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

    }

    public void updateAction() {
        asset = (AssetCls) jComboBoxAccrualAsset.getSelectedItem();
        if (asset == null)
            return;

        Account creator = (Account) parent.parentPanel.jComboBox_Account_Work.getSelectedItem();
        if (creator == null)
            return;

        //////// для восстановления Выделенного
        int selected = jComboBoxAccrualAction.getSelectedIndex();
        jComboBoxAccrualAction.setModel(new DefaultComboBoxModel(
                asset.viewAssetTypeActionsList(creator.equals(asset.getMaker()), false).toArray()));
        /////////// у некоторых активов нет действий вообще!
        if (selected >= 0 && selected < jComboBoxAccrualAction.getModel().getSize())
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
        jLabel13 = new JLabel();
        jLabelActionAssetTitle = new JLabel();
        jLabelFilterAsset = new JLabel();
        jComboBoxAccrualAsset = new JComboBox<>();
        jComboBoxAccrualAction = new JComboBox<>();
        jComboBoxFilterAsset = new JComboBox<>();
        jLabelBalancePosition = new JLabel();
        jComboBoxFilterBalancePosition = new JComboBox<>();
        jLabel8 = new JLabel();
        jTextFieldBQ = new JTextField();
        jTextFieldLQ = new JTextField();
        jLabel9 = new JLabel();
        jLabelAssetToPay = new JLabel();
        jLabelAction = new JLabel();
        jLabelTitlemetod = new JLabel();
        jLabelMethodPaymentDescription = new JLabel();
        jLabelPaymentMin = new JLabel();
        jTextFieldPaymentMin = new JTextField();
        jLabelPaymentMax = new JLabel();
        jLabel_FeesResult = new JLabel();
        jTextFieldPaymentMax = new JTextField();
        jPanelMinMaxAmounts = new JPanel();
        jPanelFilterBalance = new JPanel();
        jPanelStartEndActions = new JPanel();
        jLabelDataStart = new JLabel();
        jLabelDateEnd = new JLabel();
        jLabel19 = new JLabel();
        jComboBoxFilterSideBalance = new JComboBox<>();
        jLabel20 = new JLabel();
        jComboBoxTXTypeFilter = new JComboBox<>();
        jPanelMain = new JPanel();
        jPanel3 = new JPanel();
        jButtonViewResult = new JButton();
        jLabel1 = new JLabel();
        jCheckBoxSelfPay = new JCheckBox();
        jCheckBoxAccrualsUse = new JCheckBox();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        jLabelMethod = new JLabel();
        jLabelAmount = new JLabel();
        jSeparator1 = new JSeparator();
        jSeparator2 = new JSeparator();
        jSeparator3 = new JSeparator();
        jSeparator4 = new JSeparator();
        jSeparator5 = new JSeparator();
        jTextFieldAmount = new JTextField();
        jButtonCalcCompu = new JButton();
        jComboBoxMethodPaymentType = new JComboBox<>();
        jComboBoxPersonFilter = new JComboBox<>();

        Font ff = (Font) UIManager.get("Label.font");
        Font headFont = new Font(ff.getFontName(), Font.BOLD, ff.getSize() + 1);

        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        jTextFieldDateStart = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
        jTextFieldDateEnd = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
        TimeZone.setDefault(tz);

        jTextFieldDateStart.setFont(UIManager.getFont("TextField.font"));
        jTextFieldDateEnd.setFont(UIManager.getFont("TextField.font"));

        GridBagLayout layout = new GridBagLayout();
        //layout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        //layout.rowHeights = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        setLayout(layout);

        GridBagConstraints gridBagConstraints;

        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.anchor = GridBagConstraints.LINE_END;
        labelGBC.insets = new Insets(0, 20, 10, 10);

        GridBagConstraints fieldGBC = new GridBagConstraints();
        fieldGBC.gridx = 6;
        fieldGBC.gridwidth = 9;
        fieldGBC.fill = GridBagConstraints.HORIZONTAL;
        fieldGBC.anchor = GridBagConstraints.LINE_START;
        fieldGBC.weightx = 0.1;
        fieldGBC.insets = new Insets(0, 0, 10, 20);

        GridBagConstraints headBGC = new GridBagConstraints();
        headBGC.gridwidth = 15;
        headBGC.fill = GridBagConstraints.HORIZONTAL;
        headBGC.weightx = 0.1;
        headBGC.fill = GridBagConstraints.HORIZONTAL;
        headBGC.anchor = GridBagConstraints.LINE_START;
        headBGC.insets = new Insets(10, 10, 15, 10);

        GridBagConstraints separateBGC = new GridBagConstraints();
        separateBGC.gridwidth = 15;
        separateBGC.fill = GridBagConstraints.HORIZONTAL;
        separateBGC.weightx = 0.1;
        separateBGC.fill = GridBagConstraints.HORIZONTAL;
        separateBGC.anchor = GridBagConstraints.LINE_START;
        separateBGC.insets = new Insets(0, 0, 0, 0);

        int gridy = 0;

        jCheckBoxAccrualsUse.setText(Lang.T("Make Accruals"));
        add(jCheckBoxAccrualsUse, fieldGBC);

        jLabel_Help.setText("<html>" + Lang.T("ExAccrualsPanel_Help") + "</html>");
        fieldGBC.gridy = ++gridy;
        //JPanel panel1 = new JPanel(new BorderLayout());
        //panel1.add(jLabel_Help, BorderLayout.CENTER);
        add(jLabel_Help, fieldGBC);
        jLabel_Help.setPreferredSize(new Dimension(500, 200));

        jPanelMain.setLayout(layout);
        jPanelMain.setVisible(false);

        ImageIcon helpIcon = new ImageIcon("images/icons/tip.png");
        int x = helpIcon.getIconWidth();
        int y = helpIcon.getIconHeight();
        int x1 = headFont.getSize() * 2;
        double k = ((double) x / (double) x1);
        y = (int) ((double) y / k);
        helpIcon = new ImageIcon(helpIcon.getImage().getScaledInstance(x1, y, Image.SCALE_SMOOTH));

        jLabelActionAssetTitle.setFont(headFont); // NOI18N
        jLabelActionAssetTitle.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelActionAssetTitle.setText(Lang.T("Action for Asset"));

        jPanelMain.add(jLabelActionAssetTitle, headBGC);

        jLabelAssetToPay.setText(Lang.T("Asset"));
        //jLabelAssetToPay.setIcon(helpIcon);
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelAssetToPay, labelGBC);

        fieldGBC.gridy = gridy;
        jComboBoxAccrualAsset.setToolTipText(Lang.T("ExActionPanel.jComboBoxAccrualAsset"));
        jPanelMain.add(jComboBoxAccrualAsset, fieldGBC);

        jLabelAction.setText(Lang.T("Action"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelAction, labelGBC);

        fieldGBC.gridy = gridy;
        jComboBoxAccrualAction.setToolTipText(Lang.T("ExActionPanel.jComboBoxAccrualAction"));
        jPanelMain.add(jComboBoxAccrualAction, fieldGBC);

        ////////// PAYMENT METHOD

        jLabelTitlemetod.setFont(headFont); // NOI18N
        jLabelTitlemetod.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelTitlemetod.setText(Lang.T("Method of calculation"));
        headBGC.gridy = ++gridy;
        jPanelMain.add(jLabelTitlemetod, headBGC);

        jLabelMethod.setText(Lang.T("Method"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelMethod, labelGBC);
        jComboBoxMethodPaymentType.setModel(new DefaultComboBoxModel<>(new String[]{
                Lang.T(ExPays.viewPayMethod(0)),
                Lang.T(ExPays.viewPayMethod(1)),
                Lang.T(ExPays.viewPayMethod(2)),
        }));
        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxMethodPaymentType, fieldGBC);
        jComboBoxMethodPaymentType.setToolTipText(Lang.T("ExActionPanel.jComboBoxMethodPaymentType"));

        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelAmount, labelGBC);
        fieldGBC.gridy = gridy;
        jPanelMain.add(jTextFieldAmount, fieldGBC);
        jTextFieldAmount.setToolTipText(Lang.T("ExActionPanel.jTextFieldAmount"));


        fieldGBC.gridy = ++gridy;
        jPanelMain.add(jLabelMethodPaymentDescription, fieldGBC);

        GridBagLayout jPanelLayout = new GridBagLayout();
        jPanelLayout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0};
        jPanelLayout.rowHeights = new int[]{0};
        jPanelMinMaxAmounts.setLayout(jPanelLayout);

        jLabelPaymentMin.setText(Lang.T("Minimal Accrual"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelMinMaxAmounts.add(jLabelPaymentMin, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelMinMaxAmounts.add(jTextFieldPaymentMin, gridBagConstraints);

        jLabelPaymentMax.setText(Lang.T("Maximum Accrual"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelMinMaxAmounts.add(jLabelPaymentMax, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelMinMaxAmounts.add(jTextFieldPaymentMax, gridBagConstraints);

        fieldGBC.gridy = ++gridy;
        jPanelMain.add(jPanelMinMaxAmounts, fieldGBC);

        separateBGC.gridy = ++gridy;
        jPanelMain.add(jSeparator2, separateBGC);

        /////////////////////
        jLabelFilterAsset.setFont(headFont); // NOI18N
        jLabelFilterAsset.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelFilterAsset.setText(Lang.T("Filter By Asset and Balance"));
        headBGC.gridy = ++gridy;
        jPanelMain.add(jLabelFilterAsset, headBGC);

        jLabel2.setText(Lang.T("Asset"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel2, labelGBC);
        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxFilterAsset, fieldGBC);
        jComboBoxFilterAsset.setToolTipText(Lang.T("ExActionPanel.jComboBoxFilterAsset"));

        jPanelFilterBalance.setLayout(jPanelLayout);

        jLabelBalancePosition.setText(Lang.T("Balance Position"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelFilterBalance.add(jLabelBalancePosition, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelFilterBalance.add(jComboBoxFilterBalancePosition, gridBagConstraints);

        jLabel19.setText(Lang.T("Balance Side"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelFilterBalance.add(jLabel19, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelFilterBalance.add(jComboBoxFilterSideBalance, gridBagConstraints);

        jLabel8.setText(Lang.T("More or Equal"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelFilterBalance.add(jLabel8, gridBagConstraints);
        jTextFieldBQ.setToolTipText(Lang.T(""));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelFilterBalance.add(jTextFieldBQ, gridBagConstraints);

        jLabel9.setText(Lang.T("Less or Equal"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelFilterBalance.add(jLabel9, gridBagConstraints);
        jTextFieldLQ.setToolTipText(Lang.T(""));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelFilterBalance.add(jTextFieldLQ, gridBagConstraints);

        fieldGBC.gridy = ++gridy;
        jPanelMain.add(jPanelFilterBalance, fieldGBC);

        separateBGC.gridy = ++gridy;
        jPanelMain.add(jSeparator3, separateBGC);

        jLabel20.setFont(headFont); // NOI18N
        jLabel20.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel20.setText(Lang.T("Filter by Actions and Period"));
        headBGC.gridy = ++gridy;
        jPanelMain.add(jLabel20, headBGC);

        jLabel3.setText(Lang.T("Action"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel3, labelGBC);

        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxTXTypeFilter, fieldGBC);
        jComboBoxTXTypeFilter.setToolTipText(Lang.T("ExActionPanel.jComboBoxTXTypeFilter"));

        jPanelStartEndActions.setLayout(jPanelLayout);

        jLabelDataStart.setText(Lang.T("Data start"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelStartEndActions.add(jLabelDataStart, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelStartEndActions.add(jTextFieldDateStart, gridBagConstraints);
        //jTextFieldDateStart.setToolTipText(Lang.T(
        //        "Empty or %1 or as timestamp in seconds").replace("%1","yyyy-MM-dd hh:mm:00"));

        jLabelDateEnd.setText(Lang.T("Date end"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = labelGBC.insets;
        jPanelStartEndActions.add(jLabelDateEnd, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
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
        jLabel4.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel4.setText(Lang.T("Filter by Persons"));
        headBGC.gridy = ++gridy;
        jPanelMain.add(jLabel4, headBGC);

        jLabel1.setText(Lang.T("Filter"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel1, labelGBC);

        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBoxPersonFilter, fieldGBC);
        jComboBoxPersonFilter.setToolTipText(Lang.T("ExActionPanel.jComboBoxPersonFilter"));

        separateBGC.gridy = ++gridy;
        jPanelMain.add(jSeparator5, separateBGC);

        jCheckBoxSelfPay.setText(Lang.T("Accrual by creator account too"));
        jCheckBoxSelfPay.setSelected(true);
        fieldGBC.gridy = ++gridy;
        jPanelMain.add(jCheckBoxSelfPay, fieldGBC);

        /////////////////////// BUTTONS

        jPanel3.setLayout(jPanelLayout);

        jButtonCalcCompu.setText(Lang.T("Preview Fee"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 10;
        jPanel3.add(jButtonCalcCompu, gridBagConstraints);

        jButtonViewResult.setText(Lang.T("Preview Results"));
        gridBagConstraints.gridx = 1;
        jPanel3.add(jButtonViewResult, gridBagConstraints);

        headBGC.gridy = ++gridy;
        jPanel3.add(jLabel_FeesResult, headBGC);

        jTablePreviewAccruals = new MTable(new AccrualsModel(new ArrayList<>(), false));

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

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        fieldGBC.weightx = 0.1;
        add(jPanelMain, gridBagConstraints);

    }

    public Fun.Tuple2<ExAction, String> getResult() {

        if (!jPanelMain.isVisible())
            return new Fun.Tuple2<>(null, null);

        Account creator = (Account) parent.parentPanel.jComboBox_Account_Work.getSelectedItem();
        if (creator == null)
            return new Fun.Tuple2<>(null, Lang.T("Empty Creator account"));

        Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String> balancePosition
                = (Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String>) jComboBoxAccrualAction.getSelectedItem();

        if (asset.isUnTransferable(creator.equals(asset.getMaker()))
                || balancePosition == null)
            return new Fun.Tuple2<>(null, Lang.T("Empty actions for this asset"));


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
                asset.getKey(),
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

    private JButton jButtonCalcCompu;
    private JButton jButtonViewResult;
    private JLabel jLabelFilterAsset;
    public JCheckBox jCheckBoxAccrualsUse;
    private JLabel jLabel_Help = new JLabel();
    private JCheckBox jCheckBoxSelfPay;
    private JComboBox<Fun.Tuple2<Fun.Tuple2, String>> jComboBoxAccrualAction;
    public JComboBox<ItemCls> jComboBoxAccrualAsset;
    private JComboBox<String> jComboBoxMethodPaymentType;
    public JComboBox<ItemCls> jComboBoxFilterAsset;
    private JComboBox<Integer> jComboBoxTXTypeFilter;
    private JComboBox<String> jComboBoxPersonFilter;
    private JComboBox<String> jComboBoxFilterSideBalance;
    private JComboBox<Integer> jComboBoxFilterBalancePosition;
    private JLabel jLabel1;
    private JLabel jLabel13;
    private JLabel jLabel19;
    private JLabel jLabel2;
    private JLabel jLabel20;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabelMethod;
    private JLabel jLabelActionAssetTitle;
    private JLabel jLabelAmount;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JLabel jLabelAssetToPay;
    private JLabel jLabelAction;
    private JLabel jLabelDataStart;
    private JLabel jLabelDateEnd;
    private JLabel jLabelMethodPaymentDescription;
    private JLabel jLabelPaymentMax;
    private JLabel jLabelPaymentMin;
    private JLabel jLabelTitlemetod;
    private JLabel jLabel_FeesResult;
    private JLabel jLabelBalancePosition;
    private JPanel jPanelMinMaxAmounts;
    private JPanel jPanelFilterBalance;
    private JPanel jPanelStartEndActions;
    public JPanel jPanelMain;
    private JPanel jPanel3;
    private JSeparator jSeparator1;
    private JSeparator jSeparator2;
    private JSeparator jSeparator3;
    private JSeparator jSeparator4;
    private JSeparator jSeparator5;
    private JTextField jTextFieldAmount;
    private JTextField jTextFieldBQ;
    private JDateChooser jTextFieldDateStart;
    private JDateChooser jTextFieldDateEnd;

    private JTextField jTextFieldLQ;
    private JTextField jTextFieldPaymentMax;
    private JTextField jTextFieldPaymentMin;

    private MTable jTablePreviewAccruals;
    private JScrollPane jScrollPaneAccruals = new JScrollPane();
}
