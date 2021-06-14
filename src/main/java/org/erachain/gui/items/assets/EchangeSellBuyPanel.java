package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MTable;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;

public class EchangeSellBuyPanel extends JTabbedPane {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public CreateOrderPanel buyOrderPanel;
    String action;
    String account;
    private AssetCls have;
    private AssetCls want;
    private SellOrdersTableModel sellOrdersTableModel;
    private BuyOrdersTableModel buyOrdersTableModel;
    private TradesTableModel tradesTableModel;
    private CreateOrderPanel sellOrderPanel;
    private JPopupMenu sellOrdersMenu = new JPopupMenu();
    private JPopupMenu buyOrdersMenu = new JPopupMenu();
    private JPanel jPanel_Trade;
    private JPanel jPanel_History;

    public EchangeSellBuyPanel(AssetCls have2, AssetCls want2, String action2, String account2) {
        // TODO Auto-generated constructor stub

        this.action = action2;
        this.have = have2;
        this.want = want2;
        this.account = account2;
        new javax.swing.JTabbedPane();
        jPanel_Trade = new javax.swing.JPanel();
        jPanel_History = new javax.swing.JPanel();

        // setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel_Trade.setLayout(new java.awt.GridBagLayout());

        // LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(0, 5, 5, 0);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 0;

        // ORDER GBC
        GridBagConstraints orderGBC = new GridBagConstraints();
        orderGBC.insets = new Insets(0, 5, 5, 0);
        orderGBC.fill = GridBagConstraints.BOTH;
        orderGBC.anchor = GridBagConstraints.NORTHWEST;
        orderGBC.weightx = 1;
        orderGBC.gridy = 2;

        // TABLE GBC
        GridBagConstraints tableGBC = new GridBagConstraints();
        tableGBC.insets = new Insets(0, 5, 5, 0);
        tableGBC.fill = GridBagConstraints.BOTH;
        tableGBC.anchor = GridBagConstraints.NORTHWEST;
        tableGBC.weightx = 1;
        tableGBC.weighty = 1;
        tableGBC.gridy = 4;

        // CREATE TITLE LABEL
        JLabel lblTitle = new JLabel(Lang.T("Buy %have%").replace("%have%", this.have.toString())
                .replace("%want%", this.want.toString()));// this.have.toString()
        // + " / " +
        // this.want.toString());

        // lblTitle.setFont(new Font("Serif", Font.PLAIN, 18));
        // jPanel_Trade.add(lblTitle, labelGBC);
        if (action != null && action.equals("To sell"))
            lblTitle.setVisible(false);

        // CREATE BUY LABEL
        labelGBC.gridy = 1;
        JLabel lblBuy = new JLabel(Lang.T("Sell %want%").replace("%have%", this.have.toString())
                .replace("%want%", this.want.toString()));
        // lblBuy.setFont(new Font("Serif", Font.PLAIN, 18));
        // jPanel_Trade.add(lblBuy, labelGBC);
        if (action != null && action.equals("To sell"))
            lblBuy.setVisible(false);

        // CREATE SELL LABEL

        labelGBC.gridy = 0;
        labelGBC.gridx = 1;
        if (action != null && action.equals("To sell"))
            labelGBC.gridx = 0;

        // CREATE TITLE LABEL
        JLabel lblTitle1 = new JLabel(Lang.T("Sell %have%")
                .replace("%have%", this.have.toString()).replace("%want%", this.want.toString()));

        // lblTitle1.setFont(new Font("Serif", Font.PLAIN, 18));
        // jPanel_Trade.add(lblTitle1, labelGBC);
        if (action != null && action.equals("Buy"))
            lblTitle1.setVisible(false);

        labelGBC.gridy = 1;

        JLabel lblSell = new JLabel(Lang.T("Buy %want%").replace("%have%", this.have.toString())
                .replace("%want%", this.want.toString()));

        // lblSell.setFont(new Font("Serif", Font.PLAIN, 18));
        // jPanel_Trade.add(lblSell, labelGBC);
        if (action != null && action.equals("Buy"))
            lblSell.setVisible(false);

        // CREATE BUY PANEL
        buyOrderPanel = new CreateOrderPanel(this.want, this.have, true, account);
        jPanel_Trade.add(buyOrderPanel, orderGBC);
        // buyOrderPanel.setBackground(Color.BLUE);
        if (action != null && action.equals("To sell"))
            buyOrderPanel.setVisible(false);

        // CREATE SELL PANEL
        orderGBC.gridx = 1;

        sellOrderPanel = new CreateOrderPanel(this.have, this.want, false, account);

        // sellOrderPanel.setBackground(Color.BLUE);

        orderGBC.fill = GridBagConstraints.BOTH;
        if (action != null && action.equals("To sell")) {
            orderGBC.gridx = 0;
            orderGBC.fill = GridBagConstraints.BOTH;

        }

        jPanel_Trade.add(sellOrderPanel, orderGBC);
        if (action != null && action.equals("Buy"))
            sellOrderPanel.setVisible(false);

        // sellOrderPanel.setPreferredSize(new
        // Dimension((int)sellOrderPanel.getPreferredSize().getWidth(),
        // (int)buyOrderPanel.getPreferredSize().getHeight()));
        // sellOrderPanel.setMinimumSize(new
        // Dimension((int)sellOrderPanel.getPreferredSize().getWidth(),
        // (int)buyOrderPanel.getPreferredSize().getHeight()));

        orderGBC.fill = GridBagConstraints.BOTH;

        // CREATE SELL ORDERS LABEL
        labelGBC.gridx = 0;
        labelGBC.gridy = 3;
        JLabel lblSellOrders = new JLabel(Lang.T("Sell Orders"));
        // lblSellOrders.setFont(new Font("Serif", Font.PLAIN, 18));
        jPanel_Trade.add(lblSellOrders, labelGBC);
        if (action != null && action.equals("To sell"))
            lblSellOrders.setVisible(false);

        // CREATE BUY ORDERS LABEL
        labelGBC.gridx = 1;
        if (action != null && action.equals("To sell"))
            labelGBC.gridx = 0;
        JLabel lblBuyOrders = new JLabel(Lang.T("Buy Orders"));
        // lblBuyOrders.setFont(new Font("Serif", Font.PLAIN, 18));
        jPanel_Trade.add(lblBuyOrders, labelGBC);
        if (action != null && action.equals("Buy"))
            lblBuyOrders.setVisible(false);

        // CREATE SELL ORDERS TABLE
        this.sellOrdersTableModel = new SellOrdersTableModel(this.have, this.want);
        final MTable sellOrdersTable = new MTable(this.sellOrdersTableModel);

        // ORDER INFO
        JMenuItem orderInfo = new JMenuItem(Lang.T("Order info"));
        orderInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sellOrdersTableModel.isEmpty())
                    return;

                int row = sellOrdersTable.getSelectedRow();
                if (row < 0)
                    return;
                row = sellOrdersTable.convertRowIndexToModel(row);

                Order order = sellOrdersTableModel.getItem(row);

                Transaction orderAction = DCSet.getInstance().getTransactionFinalMap().get(order.getId());

                IssueConfirmDialog dialog = new IssueConfirmDialog(MainFrame.getInstance(), true, orderAction,
                        (int) (MainFrame.getInstance().getWidth() / 1.2),
                        (int) (MainFrame.getInstance().getHeight() / 1.2),
                        "");

                JPanel panel = TransactionDetailsFactory.getInstance().createTransactionDetail(orderAction);
                dialog.jScrollPane1.setViewportView(panel);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);

            }
        });
        sellOrdersMenu.add(orderInfo);

        // MENU on MY ORDERS
        JMenuItem trades = new JMenuItem(Lang.T("Trades"));
        trades.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sellOrdersTableModel.isEmpty())
                    return;

                int row = sellOrdersTable.getSelectedRow();
                if (row < 0)
                    return;
                row = sellOrdersTable.convertRowIndexToModel(row);

                Order order = sellOrdersTableModel.getItem(row);
                new TradesFrame(order, true);
            }
        });
        sellOrdersMenu.add(trades);

        JMenuItem sellChange = new JMenuItem(Lang.T("Change"));
        sellChange.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (sellOrdersTableModel.isEmpty())
                    return;
                int row = sellOrdersTable.getSelectedRow();
                if (row < 0)
                    return;
                int row1 = sellOrdersTable.convertRowIndexToModel(row);

                Order order = sellOrdersTableModel.getItem(row1);
                new ChangeOrderFrame(order, false);
            }
        });
        sellOrdersMenu.add(sellChange);

        JMenuItem cancel = new JMenuItem(Lang.T("Cancel"));
        cancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (sellOrdersTableModel.isEmpty())
                    return;
                int row = sellOrdersTable.getSelectedRow();
                if (row < 0)
                    return;
                int row1 = sellOrdersTable.convertRowIndexToModel(row);

                Order order = sellOrdersTableModel.getItem(row1);
                new CancelOrderFrame(order);
            }
        });
        sellOrdersMenu.add(cancel);

    //    sellOrdersTable.setComponentPopupMenu(sellOrdersMenu);
        TableMenuPopupUtil.installContextMenu(sellOrdersTable, sellOrdersMenu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON

        sellOrdersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if (false) {
                    super.mousePressed(e);

                    MTable target = (MTable) e.getSource();
                    int row = target.getSelectedRow();

                    if (row > sellOrdersTableModel.getRowCount())
                        return;

                    Order order = sellOrdersTableModel.getItem(row);
                    if (order == null)
                        return;
                }

            }

            @Override
            public void mouseClicked(MouseEvent e) {

                // иначе прии вызове правой мышки не происходит выбора текущей записи
                // и меню для старого выбора срабатывает

                super.mouseClicked(e);

                MTable target = (MTable) e.getSource();
                int row = target.getSelectedRow();

                if (row > sellOrdersTableModel.getRowCount())
                    return;

                Order order = sellOrdersTableModel.getItem(row);
                if (order == null)
                    return;

                if (Controller.getInstance().isAddressIsMine(order.getCreator().getAddress()))
                    sellOrdersMenu.getComponent(2).setEnabled(true);
                else
                    sellOrdersMenu.getComponent(2).setEnabled(false);

                if (e.getClickCount() == 2) {

                    if (row < sellOrdersTableModel.getRowCount()) {
                        ///buyOrderPanel.calculateWant(order.getAmountHaveLeft(), order.calcLeftPrice(), true);
                        buyOrderPanel.setFields(order.getAmountHaveLeft(), order.calcLeftPrice(), order.getAmountWantLeft());
                        sellOrderPanel.setFields(order.getAmountHaveLeft(), order.calcLeftPrice(), order.getAmountWantLeft());
                    }
                }
            }
        });

        JScrollPane sellScrollPane = new JScrollPane(sellOrdersTable);

        jPanel_Trade.add(sellScrollPane, tableGBC);

        if (action != null && action.equals("To sell"))
            sellScrollPane.setVisible(false);

        // CREATE BUY ORDERS TABLE
        tableGBC.gridx = 1;
        if (action != null && action.equals("To sell"))
            tableGBC.gridx = 0;
        this.buyOrdersTableModel = new BuyOrdersTableModel(this.want, this.have);
        final MTable buyOrdersTable = new MTable(this.buyOrdersTableModel);

        buyOrdersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                MTable target = (MTable) e.getSource();
                int row = target.getSelectedRow();

                if (row > buyOrdersTableModel.getRowCount())
                    return;

                Order order = buyOrdersTableModel.getItem(row);
                if (order == null)
                    return;

                if (Controller.getInstance().isAddressIsMine(order.getCreator().getAddress()))
                    buyOrdersMenu.getComponent(2).setEnabled(true);
                else
                    buyOrdersMenu.getComponent(2).setEnabled(false);

                if (e.getClickCount() == 2) {
                    ///sellOrderPanel.calculateWant(order.getAmountWantLeft(), order.calcLeftPriceReverse(), false);
                    sellOrderPanel.setFields(order.getAmountWantLeft(), order.calcLeftPriceReverse(), order.getAmountHaveLeft());
                    //buyOrderPanel.calculateWant(trade.getAmountWant(), price, type);
                    buyOrderPanel.setFields(order.getAmountWantLeft(), order.calcLeftPriceReverse(), order.getAmountHaveLeft());
                }
            }
        });
        
        // ORDER INFO
        JMenuItem buyOrderInfo = new JMenuItem(Lang.T("Order info"));
        buyOrderInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buyOrdersTableModel.isEmpty())
                    return;

                int row = buyOrdersTable.getSelectedRow();
                if (row < 0)
                    return;
                row = buyOrdersTable.convertRowIndexToModel(row);

                Order order = buyOrdersTableModel.getItem(row);
                Transaction orderAction = DCSet.getInstance().getTransactionFinalMap().get(order.getId());

                IssueConfirmDialog dialog = new IssueConfirmDialog(MainFrame.getInstance(), true, orderAction,
                        (int) (MainFrame.getInstance().getWidth() / 1.2),
                        (int) (MainFrame.getInstance().getHeight() / 1.2),
                        "");

                JPanel panel = TransactionDetailsFactory.getInstance().createTransactionDetail(orderAction);
                dialog.jScrollPane1.setViewportView(panel);
                dialog.setLocationRelativeTo(null);
                dialog.pack();
                dialog.setVisible(true);

            }
        });
        buyOrdersMenu.add(buyOrderInfo);

        // MENU on MY ORDERS
        JMenuItem buyTrades = new JMenuItem(Lang.T("Trades"));
        buyTrades.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buyOrdersTableModel.isEmpty())
                    return;

                int row = buyOrdersTable.getSelectedRow();
                if (row < 0)
                    return;

                row = buyOrdersTable.convertRowIndexToModel(row);

                Order order = buyOrdersTableModel.getItem(row);
                if (order != null)
                    new TradesFrame(order, false);
            }
        });
        buyOrdersMenu.add(buyTrades);

        JMenuItem buyChange = new JMenuItem(Lang.T("Change"));
        buyChange.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (buyOrdersTableModel.isEmpty())
                    return;
                int row = buyOrdersTable.getSelectedRow();
                if (row < 0)
                    return;
                int row1 = buyOrdersTable.convertRowIndexToModel(row);

                Order order = buyOrdersTableModel.getItem(row1);
                new ChangeOrderFrame(order, true);
            }
        });
        buyOrdersMenu.add(buyChange);

        JMenuItem buyCancel = new JMenuItem(Lang.T("Cancel"));
        buyCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buyOrdersTableModel.isEmpty())
                    return;

                int row = buyOrdersTable.getSelectedRow();
                if (row < 0)
                    return;

                row = buyOrdersTable.convertRowIndexToModel(row);

                Order order = buyOrdersTableModel.getItem(row);
                new CancelOrderFrame(order);
            }
        });
        buyOrdersMenu.add(buyCancel);

        //      buyOrdersTable.setComponentPopupMenu(buyOrdersMenu);
        TableMenuPopupUtil.installContextMenu(buyOrdersTable, buyOrdersMenu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON


        JScrollPane buyScrollPane = new JScrollPane(buyOrdersTable);
        jPanel_Trade.add(buyScrollPane, tableGBC);

        if (action != null && action.equals("Buy"))
            buyScrollPane.setVisible(false);

        addTab(Lang.T("Trade"), jPanel_Trade);

        jPanel_History.setLayout(new java.awt.GridBagLayout());

        // CREATE TRADE HISTORY LABEL
        labelGBC.gridx = 0;
        labelGBC.gridy = 0;
        JLabel lblTradeHistory = new JLabel(Lang.T("Trade History"));
        // lblTradeHistory.setFont(new Font("Serif", Font.PLAIN, 18));
        jPanel_History.add(lblTradeHistory, labelGBC);

        // CREATE TRADE HISTORY TABLE
        tableGBC.gridx = 0;
        tableGBC.gridy = 1;
        tableGBC.gridwidth = 2;
        this.tradesTableModel = new TradesTableModel(this.have, this.want);
        final MTable tradesTable = new MTable(this.tradesTableModel);

        ////
        tradesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                MTable target = (MTable) e.getSource();
                int row = target.getSelectedRow();

                if (row > tradesTableModel.getRowCount())
                    return;

                Trade trade = tradesTableModel.getItem(row);
                if (trade == null)
                    return;

                DCSet db = DCSet.getInstance();
                Order initiator = Order.getOrder(db, trade.getInitiator());
                boolean type = initiator.getHaveAssetKey() == have.getKey();

                if (e.getClickCount() == 2) {

                    if (type) {
                        BigDecimal price = trade.calcPriceRevers();
                        ///sellOrderPanel.calculateWant(trade.getAmountWant(), price, type);
                        sellOrderPanel.setFields(trade.getAmountWant(), price, trade.getAmountHave());

                        //buyOrderPanel.calculateWant(trade.getAmountWant(), price, type);
                        buyOrderPanel.setFields(trade.getAmountWant(), price, trade.getAmountHave());
                    } else {
                        BigDecimal price = trade.calcPrice();
                        //sellOrderPanel.calculateWant(trade.getAmountHave(), price, type);
                        sellOrderPanel.setFields(trade.getAmountHave(), price, trade.getAmountWant());

                        //buyOrderPanel.calculateWant(trade.getAmountHave(), price, type);
                        buyOrderPanel.setFields(trade.getAmountHave(), price, trade.getAmountWant());
                    }
                }
            }
        });

        ////
        jPanel_History.add(new JScrollPane(tradesTable), tableGBC);
        addTab(Lang.T("Trade History"), jPanel_History);

    }

}
