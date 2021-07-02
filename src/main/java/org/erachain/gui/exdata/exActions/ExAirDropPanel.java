package org.erachain.gui.exdata.exActions;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.exdata.exActions.ExAction;
import org.erachain.core.exdata.exActions.ExAirDrop;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.gui.IconPanel;
import org.erachain.gui.exdata.AccrualsModel;
import org.erachain.gui.exdata.AirDropsModel;
import org.erachain.gui.exdata.ExDataPanel;
import org.erachain.gui.items.assets.ComboBoxAssetsModel;
import org.erachain.gui.library.MTable;
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


public class ExAirDropPanel extends IconPanel implements ExActionPanelInt {

    public static String NAME = "ExActionPanel";
    public static String TITLE = "Accruals";

    private ExDataPanel parent;
    public ComboBoxAssetsModel assetsModel;
    private Boolean lock = new Boolean(false);

    public ExAirDropPanel(ExDataPanel parent) {
        super(NAME, TITLE);
        this.parent = parent;
        initComponents();

        assetsModel = new ComboBoxAssetsModel();
        this.jComboBoxAccrualAsset.setModel(assetsModel);
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

        jComboBoxAccrualAction.setRenderer(new RenderComboBoxAssetActions());
        jComboBoxAccrualAsset.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateAction();
            }
        });
        updateAction();

        jComboBoxAccrualAsset.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateAction();
            }
        });
        updateAction();

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
                            jLabel_FeesResult.setText(exActionRes.a == null ? Lang.T(exActionRes.b) :
                                    Lang.T(exActionRes.b) + (exActionRes.a.errorValue == null ? "" : Lang.T(exActionRes.a.errorValue)));
                            jButtonViewResult.setEnabled(true);
                            return;
                        }

                        ExAirDrop airDrop = (ExAirDrop) exActionRes.a;
                        airDrop.setDC(DCSet.getInstance());
                        airDrop.preProcess(Controller.getInstance().getMyHeight(), (Account) parent.parentPanel.jComboBox_Account_Work.getSelectedItem(), false);
                        List<Fun.Tuple3<Account, BigDecimal, Fun.Tuple2<Integer, String>>> accruals = airDrop.getResults();
                        jLabel_FeesResult.setText("<html>" + Lang.T("Count # кол-во") + ": <b>" + accruals.size()
                                + "</b>, " + Lang.T("Additional Fee") + ": <b>" + BlockChain.feeBG(airDrop.getTotalFeeBytes())
                                + "</b>, " + Lang.T("Total") + ": <b>" + airDrop.getTotalPay());
                    } finally {
                        jButtonCalcCompu.setEnabled(true);
                        jButtonViewResult.setEnabled(true);

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
                            jLabel_FeesResult.setText(exActionRes.a == null ? Lang.T(exActionRes.b) :
                                    Lang.T(exActionRes.b) + (exActionRes.a.errorValue == null ? "" : Lang.T(exActionRes.a.errorValue)));
                            jButtonViewResult.setEnabled(true);
                            return;
                        }

                        ExAirDrop airDrop = (ExAirDrop) exActionRes.a;
                        airDrop.setDC(DCSet.getInstance());
                        airDrop.preProcess(Controller.getInstance().getMyHeight(), (Account) parent.parentPanel.jComboBox_Account_Work.getSelectedItem(), true);
                        List<Fun.Tuple3<Account, BigDecimal, Fun.Tuple2<Integer, String>>> results = airDrop.getResults();

                        String result = "<html>";
                        if (airDrop.resultCode != Transaction.VALIDATE_OK) {
                            result += "<b>" + Lang.T("Error") + "!<b> " + Lang.T(OnDealClick.resultMess(airDrop.resultCode)) + "<br>";
                            result += Lang.T("Found errors") + ":<b> " + airDrop.getAddressesCount() + "<br>";
                        } else {
                            result += Lang.T("Count # кол-во") + ": <b>" + airDrop.getAddressesCount()
                                    + "</b>, " + Lang.T("Additional Fee") + ": <b>" + BlockChain.feeBG(airDrop.getAddressesCount())
                                    + "</b>, " + Lang.T("Total") + ": <b>" + airDrop.getTotalPay();
                        }
                        jLabel_FeesResult.setText(result);

                        AirDropsModel model = new AirDropsModel(results, airDrop.resultCode != Transaction.VALIDATE_OK);
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


    private void initComponents() {

        jPanelMain = new JPanel();
        jComboBoxAccrualAsset = new JComboBox<>();
        jComboBoxAccrualAction = new JComboBox<>();
        jLabelBalancePosition = new JLabel();
        jComboBoxFilterBalancePosition = new JComboBox<>();
        jLabel_FeesResult = new JLabel();
        jComboBoxFilterSideBalance = new JComboBox<>();
        jPanelMain = new JPanel();
        jPanel3 = new JPanel();
        jButtonViewResult = new JButton();
        jCheckBoxAccrualsUse = new JCheckBox();
        jLabelAmount = new JLabel();
        jSeparator1 = new JSeparator();
        jSeparator2 = new JSeparator();
        jSeparator3 = new JSeparator();
        jSeparator4 = new JSeparator();
        jSeparator5 = new JSeparator();
        jTextFieldAmount = new JTextField();
        jButtonCalcCompu = new JButton();

        java.awt.GridBagLayout jPanelLayout = new java.awt.GridBagLayout();
        jPanelLayout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0};
        jPanelLayout.rowHeights = new int[]{0};

        Font ff = (Font) UIManager.get("Label.font");
        Font headFont = new Font(ff.getFontName(), Font.BOLD, ff.getSize() + 1);

        GridBagLayout layout = new GridBagLayout();
        //layout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        //layout.rowHeights = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        setLayout(layout);

        GridBagConstraints gridBagConstraints;

        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.anchor = GridBagConstraints.LINE_END;
        labelGBC.insets = new Insets(0, 20, 10, 0);

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
        jLabel_Help.setPreferredSize(new Dimension(400, 200));

        jPanelMain.setLayout(layout);
        jPanelMain.setVisible(false);

        ImageIcon helpIcon = new ImageIcon("images/icons/tip.png");
        int x = helpIcon.getIconWidth();
        int y = helpIcon.getIconHeight();
        int x1 = headFont.getSize() * 2;
        double k = ((double) x / (double) x1);
        y = (int) ((double) y / k);
        helpIcon = new ImageIcon(helpIcon.getImage().getScaledInstance(x1, y, Image.SCALE_SMOOTH));

        JLabel jLabelAssetToPay = new JLabel(Lang.T("Asset"));
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelAssetToPay, labelGBC);

        fieldGBC.gridy = gridy;
        jComboBoxAccrualAsset.setToolTipText(Lang.T("ExActionPanel.jComboBoxAccrualAsset"));
        jPanelMain.add(jComboBoxAccrualAsset, fieldGBC);


        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabelAmount, labelGBC);
        fieldGBC.gridy = gridy;
        jPanelMain.add(jTextFieldAmount, fieldGBC);
        jTextFieldAmount.setToolTipText(Lang.T("ExActionPanel.jTextFieldAmount"));

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
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        fieldGBC.weightx = 0.1;
        add(jPanelMain, gridBagConstraints);

    }

    public Fun.Tuple2<ExAction, String> getResult() {

        if (!jPanelMain.isVisible())
            return new Fun.Tuple2<>(null, null);

        Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String> balancePosition
                = (Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String>) jComboBoxAccrualAction.getSelectedItem();

        return ExAirDrop.make(
                ((AssetCls) jComboBoxAccrualAsset.getSelectedItem()).getKey(),
                jTextFieldAmount.getText(),
                balancePosition.a.a, balancePosition.a.b,
                null);
    }

    private JButton jButtonCalcCompu;
    private JButton jButtonViewResult;
    public JCheckBox jCheckBoxAccrualsUse;
    private JLabel jLabel_Help = new JLabel();
    public JComboBox<ItemCls> jComboBoxAccrualAsset;
    private javax.swing.JComboBox<Integer> jComboBoxFilterBalancePosition;
    private javax.swing.JComboBox<String> jComboBoxFilterSideBalance;
    private javax.swing.JComboBox<Fun.Tuple2<Fun.Tuple2, String>> jComboBoxAccrualAction;
    private JLabel jLabelAmount;
    private JLabel jLabel_FeesResult;
    private JLabel jLabelBalancePosition;
    public JPanel jPanelMain;
    private JPanel jPanel3;
    private JSeparator jSeparator1;
    private JSeparator jSeparator2;
    private JSeparator jSeparator3;
    private JSeparator jSeparator4;
    private JSeparator jSeparator5;
    private JTextField jTextFieldAmount;

    private MTable jTablePreviewAccruals;
    private JScrollPane jScrollPaneAccruals = new JScrollPane();
}
