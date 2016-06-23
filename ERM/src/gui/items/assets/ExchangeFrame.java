package gui.items.assets;

import lang.Lang;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import controller.Controller;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import core.item.assets.Trade;
import database.DBSet;


public class ExchangeFrame extends JFrame
{
	private static final long serialVersionUID = -7052380905136603354L;
	
	private AssetCls have;
	private AssetCls want;
	
	private SellOrdersTableModel sellOrdersTableModel;
	private BuyOrdersTableModel buyOrdersTableModel;
	private TradesTableModel tradesTableModel;
	private OrderPanel sellOrderPanel;
	public OrderPanel buyOrderPanel;
	
	private JPopupMenu sellOrdersMenu = new JPopupMenu();
	private JPopupMenu buyOrdersMenu = new JPopupMenu();

	public ExchangeFrame(AssetCls have, AssetCls want, String action, String account) 
	{
		super(Lang.getInstance().translate("DATACHAINS.world") + " - " + Lang.getInstance().translate("Check Exchange"));
		
		this.have = have;
		this.want = want;
		
	//	this.setTitle(Lang.getInstance().translate("DATACHAINS.world") + " - " + Lang.getInstance().translate("Check Exchange")+" - " + this.have.toString() + " / " + this.want.toString());
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		
		//ORDER GBC
		GridBagConstraints orderGBC = new GridBagConstraints();
		orderGBC.insets = new Insets(0, 5, 5, 0);
		orderGBC.fill = GridBagConstraints.BOTH;  
		orderGBC.anchor = GridBagConstraints.NORTHWEST;
		orderGBC.weightx = 1;
		orderGBC.gridy = 2;	
		
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.insets = new Insets(0, 5, 5, 0);
		tableGBC.fill = GridBagConstraints.BOTH;  
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;
		tableGBC.weighty = 1;
		tableGBC.gridy = 4;	
		
		//CREATE TITLE LABEL
		JLabel lblTitle = new JLabel(Lang.getInstance().translate("Buy %have%").replace("%have%", this.have.getName().toString()).replace("%want%", this.want.getName().toString()));//this.have.toString() + " / " + this.want.toString());
				
		lblTitle.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblTitle, labelGBC);
		if(action == "Buy" || action =="To sell") lblTitle.setVisible(false);
		
				
		//CREATE BUY LABEL
		labelGBC.gridy = 1;
		JLabel lblBuy = new JLabel( Lang.getInstance().translate("Sell %want%").replace("%have%", this.have.getName().toString()).replace("%want%", this.want.getName().toString()));
		lblBuy.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblBuy, labelGBC);
		if (action == "To sell")lblBuy.setVisible(false);
		
		//CREATE SELL LABEL
		
		
		
		labelGBC.gridy = 0;
		labelGBC.gridx = 1;
		if (action == "To sell")labelGBC.gridx = 0;
		
		//CREATE TITLE LABEL
				JLabel lblTitle1 = new JLabel(Lang.getInstance().translate("Sell %have%" ).replace("%have%", this.have.getName().toString()).replace("%want%", this.want.getName().toString()));
						
				lblTitle1.setFont(new Font("Serif", Font.PLAIN, 18));
				this.add(lblTitle1, labelGBC);
				if(action == "Buy" || action =="To sell") lblTitle1.setVisible(false);
				
				labelGBC.gridy = 1;
		
		JLabel lblSell = new JLabel( Lang.getInstance().translate(" Buy %want%").replace("%have%", this.have.getName().toString()).replace("%want%", this.want.getName().toString()));

		lblSell.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblSell, labelGBC);
		if (action == "Buy")lblSell.setVisible(false);
		
		//CREATE BUY PANEL
		buyOrderPanel = new OrderPanel(this.want, this.have, true, account);
		this.add(buyOrderPanel, orderGBC);
		//buyOrderPanel.setBackground(Color.BLUE);
		if (action == "To sell")buyOrderPanel.setVisible(false);
		
		//CREATE SELL PANEL
		orderGBC.gridx = 1;
		
		sellOrderPanel = new OrderPanel(this.have, this.want, false, account);
		
		
		//sellOrderPanel.setBackground(Color.BLUE);
		
		orderGBC.fill = GridBagConstraints.NORTH;  
		if (action == "To sell"){
			orderGBC.gridx = 0;
			orderGBC.fill = GridBagConstraints.BOTH;
			
		}
		
		this.add(sellOrderPanel, orderGBC);
		if (action == "Buy")sellOrderPanel.setVisible(false);
		
		
		
		sellOrderPanel.setPreferredSize(new Dimension((int)sellOrderPanel.getPreferredSize().getWidth(), (int)buyOrderPanel.getPreferredSize().getHeight()));
		sellOrderPanel.setMinimumSize(new Dimension((int)sellOrderPanel.getPreferredSize().getWidth(), (int)buyOrderPanel.getPreferredSize().getHeight()));
		
		orderGBC.fill = GridBagConstraints.BOTH;  
		
		//CREATE SELL ORDERS LABEL
		labelGBC.gridx = 0;
		labelGBC.gridy = 3;
		JLabel lblSellOrders = new JLabel(Lang.getInstance().translate("Sell orders"));
		lblSellOrders.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblSellOrders, labelGBC);
		if (action == "To sell")lblSellOrders.setVisible(false);
		
		//CREATE BUY ORDERS LABEL
		labelGBC.gridx = 1;
		if (action == "To sell")labelGBC.gridx = 0;
		JLabel lblBuyOrders = new JLabel(Lang.getInstance().translate("Buy orders"));
		lblBuyOrders.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblBuyOrders, labelGBC);
		if (action == "Buy")lblBuyOrders.setVisible(false);
						
		//CREATE SELL ORDERS TABLE
		this.sellOrdersTableModel = new SellOrdersTableModel(this.have, this.want);
		final JTable sellOrdersTable = new JTable(this.sellOrdersTableModel);
		
		// MENU on MY ORDERS
		JMenuItem trades = new JMenuItem(Lang.getInstance().translate("Trades"));
		trades.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = sellOrdersTable.getSelectedRow();
				row = sellOrdersTable.convertRowIndexToModel(row);

				Order order = sellOrdersTableModel.getOrder(row);
				new TradesFrame(order);
			}
		});
		sellOrdersMenu.add(trades);
		JMenuItem cancel = new JMenuItem(Lang.getInstance().translate("Cancel"));
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = sellOrdersTable.getSelectedRow();
				row = sellOrdersTable.convertRowIndexToModel(row);

				Order order = sellOrdersTableModel.getOrder(row);
				new CancelOrderFrame(order);
			}
		});
		sellOrdersMenu.add(cancel);
		sellOrdersTable.setComponentPopupMenu(sellOrdersMenu);

		sellOrdersTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				
				JTable target = (JTable)e.getSource();
				int row = target.getSelectedRow();

				if(row > sellOrdersTableModel.orders.size())
					return;
				
				Order order = sellOrdersTableModel.orders.get(row).getB();
				if (order == null)
					return;

				if (Controller.getInstance().isAddressIsMine(order.getCreator().getAddress()))
					sellOrdersMenu.getComponent(1).setEnabled(true);
				else
					sellOrdersMenu.getComponent(1).setEnabled(false);
				
				if (e.getClickCount() == 2) {

					if(row < sellOrdersTableModel.orders.size())
					{
						buyOrderPanel.txtAmount.setText(order.getAmountHaveLeft().toPlainString());
						buyOrderPanel.txtPrice.setText(order.getPriceCalc().toPlainString());
					}
				}
			}
		});
		
		JScrollPane sellScrollPane = new JScrollPane(sellOrdersTable);
		
		this.add(sellScrollPane, tableGBC);
		
		if (action == "To sell")sellScrollPane.setVisible(false);
		
		//CREATE BUY ORDERS TABLE
		tableGBC.gridx = 1;
		if (action == "To sell")tableGBC.gridx = 0;
		this.buyOrdersTableModel = new BuyOrdersTableModel(this.want, this.have);
		final JTable buyOrdersTable = new JTable(this.buyOrdersTableModel);
		
		buyOrdersTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				JTable target = (JTable)e.getSource();
				int row = target.getSelectedRow();

				if(row > buyOrdersTableModel.orders.size())
					return;
				
				Order order = buyOrdersTableModel.orders.get(row).getB();
				if (order == null)
					return;
				
				if (Controller.getInstance().isAddressIsMine(order.getCreator().getAddress()))
					buyOrdersMenu.getComponent(1).setEnabled(true);
				else
					buyOrdersMenu.getComponent(1).setEnabled(false);
				
				if (e.getClickCount() == 2) {
					//if (buying)
					sellOrderPanel.txtAmount.setText(buyOrdersTableModel.orders.get(row).getB().getAmountWantLeft().toPlainString());
					sellOrderPanel.txtPrice.setText(buyOrdersTableModel.orders.get(row).getB().getPriceCalcReverse().toPlainString());
				}
			}
		});
		// MENU on MY ORDERS
		JMenuItem buyTrades = new JMenuItem(Lang.getInstance().translate("Trades"));
		buyTrades.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = buyOrdersTable.getSelectedRow();
				row = buyOrdersTable.convertRowIndexToModel(row);

				Order order = buyOrdersTableModel.getOrder(row);
				new TradesFrame(order);
			}
		});
		buyOrdersMenu.add(buyTrades);
		JMenuItem buyCancel = new JMenuItem(Lang.getInstance().translate("Cancel"));
		buyCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = buyOrdersTable.getSelectedRow();
				row = buyOrdersTable.convertRowIndexToModel(row);

				Order order = buyOrdersTableModel.getOrder(row);
				new CancelOrderFrame(order);
			}
		});
		buyOrdersMenu.add(buyCancel);
		buyOrdersTable.setComponentPopupMenu(buyOrdersMenu);

		JScrollPane buyScrollPane = new JScrollPane(buyOrdersTable);
		this.add(buyScrollPane, tableGBC);
		
		if (action == "Buy")buyScrollPane.setVisible(false);
		
		//CREATE TRADE HISTORY LABEL
		labelGBC.gridx = 0;
		labelGBC.gridy = 5;
		JLabel lblTradeHistory = new JLabel(Lang.getInstance().translate("Trade History"));
		lblTradeHistory.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblTradeHistory, labelGBC);
		
		//CREATE TRADE HISTORY TABLE
		tableGBC.gridx = 0;
		tableGBC.gridy = 6;
		tableGBC.gridwidth = 2;
		this.tradesTableModel = new TradesTableModel(this.have, this.want);
		final JTable tradesTable = new JTable(this.tradesTableModel);
		
		////
		tradesTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				
				JTable target = (JTable)e.getSource();
				int row = target.getSelectedRow();

				if(row > tradesTableModel.getSortableList().size())
					return;
				
				Trade trade = tradesTableModel.getTrade(row);
				if (trade == null)
					return;
				
				boolean type = trade.getInitiatorOrder(DBSet.getInstance()).getHave() == have.getKey();
				
				if (e.getClickCount() == 2) {

					if (type) {
						BigDecimal price = trade.getAmountHave().divide(trade.getAmountWant(), 8, RoundingMode.HALF_DOWN);
						sellOrderPanel.txtAmount.setText(trade.getAmountWant().toPlainString());
						sellOrderPanel.txtPrice.setText(price.toPlainString());
	
						buyOrderPanel.txtAmount.setText(trade.getAmountWant().toPlainString());
						buyOrderPanel.txtPrice.setText(price.toPlainString());
					} else {
						BigDecimal price = trade.getAmountWant().divide(trade.getAmountHave(), 8, RoundingMode.HALF_DOWN);
						sellOrderPanel.txtAmount.setText(trade.getAmountHave().toPlainString());
						sellOrderPanel.txtPrice.setText(price.toPlainString());
	
						buyOrderPanel.txtAmount.setText(trade.getAmountHave().toPlainString());
						buyOrderPanel.txtPrice.setText(price.toPlainString());						
					}
				}
			}
		});

		////
		this.add(new JScrollPane(tradesTable), tableGBC);
		
		//PACK
		this.pack();
		this.setResizable(true);
		if(action == "Buy" || action =="To sell") this.setSize(900,800);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}
