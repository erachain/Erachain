package utils;
// 30/03
import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import controller.Controller;
import core.transaction.Transaction;
import database.wallet.TransactionMap;
import gui.ClosingDialog;
import gui.ConsolePanel;
import gui.Gui;
import gui.PasswordPane;
import gui.CoreRowSorter;
import gui.SendAssetPanel;
import gui.items.assets.AssetsPanel;
import gui.models.WalletTransactionsTableModel;
import gui.naming.NamingServicePanel;
import gui.settings.SettingsFrame;
import gui.transaction.TransactionDetailsFactory;
import gui.voting.VotingPanel;
import settings.Settings;

public class SysTray implements Observer{

	
	private static final Logger LOGGER = Logger.getLogger(SysTray.class);
	private static SysTray systray = null;
	private TrayIcon icon = null;
	private PopupMenu createPopupMenu;

	public static SysTray getInstance() {
		if (systray == null) {
			systray = new SysTray();
		}

		return systray;
	}
	
	public SysTray()
	{
		Controller.getInstance().addObserver(this);	
	}
	
	public void createTrayIcon() throws HeadlessException,
			MalformedURLException, AWTException, FileNotFoundException {
		if (icon == null) {
			if (!SystemTray.isSupported()) {
				LOGGER.info("SystemTray is not supported");
			} else {
				
				//String toolTipText = "DATACHAINS.world "	+ Controller.getInstance().getVersion();
				createPopupMenu = createPopupMenu();
				TrayIcon icon = new TrayIcon(createImage(
						"images/icons/icon32.png", "tray icon"), "DATACHAINS.world "
						+ Controller.getInstance().getVersion(),
						createPopupMenu);
				
				icon.setImageAutoSize(true);
				
				SystemTray.getSystemTray().add(icon);
				this.icon = icon;
			
				icon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							Gui.getInstance().bringtoFront();
						} catch (Exception e1) {
							LOGGER.error(e1.getMessage(),e1);
						}
					}
				});
				
				this.update(new Observable(), new ObserverMessage(ObserverMessage.NETWORK_STATUS, Controller.getInstance().getStatus()));
			}
		}
	}
	
	public void sendMessage(String caption, String text, TrayIcon.MessageType messagetype  )
	{
		if(icon != null)
		{
			icon.displayMessage(caption, text,
					messagetype);
		}
	}

	// Obtain the image URL
	private  Image createImage(String path, String description)
			throws MalformedURLException, FileNotFoundException {

		File file = new File(path);

		if (!file.exists()) {
			throw new FileNotFoundException("Iconfile not found: " + path);
		}

		URL imageURL = file.toURI().toURL();
		return (new ImageIcon(imageURL, description)).getImage();
	}

	private PopupMenu createPopupMenu() throws HeadlessException {
		PopupMenu menu = new PopupMenu();
		
		
		MenuItem decentralizedWeb = new MenuItem("DATACHAINS.world Web/Social Network");
		decentralizedWeb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
        			URLViewer.openWebpage(new URL("http://127.0.0.1:"+Settings.getInstance().getWebPort()));
				} catch (MalformedURLException e1) {
					LOGGER.error(e1.getMessage(),e1);
				}
			}
		});
		menu.add(decentralizedWeb);
		MenuItem walletStatus = new MenuItem("Wallet Status (web)");
		walletStatus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					URLViewer.openWebpage(new URL("http://127.0.0.1:"+Settings.getInstance().getWebPort() + "/index/status.html"));
				} catch (MalformedURLException e1) {
					LOGGER.error(e1.getMessage(),e1);
				}
			}
		});
		menu.add(walletStatus);
		
		
		
		MenuItem lock = new MenuItem("Lock/Unlock");
		lock.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(Controller.getInstance().isWalletUnlocked())
				{
					Controller.getInstance().lockWallet();
				}else
				{
					String password = PasswordPane.showUnlockWalletDialog(); 
					if(!password.equals("") && !Controller.getInstance().unlockWallet(password))
					{
						JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		menu.add(lock);
		
		
		MenuItem settings = new MenuItem("Settings");
		settings.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new SettingsFrame();
			}
		});
		menu.add(settings);
		
		
		MenuItem console = new MenuItem("Console");
		console.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
					JFrame frame = new JFrame("Console");

				    frame.setSize(800, 600);
				    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				    frame.setVisible(true);
					ConsolePanel ap = new ConsolePanel();
					frame.getContentPane().add(ap);
					
			}
		});
		menu.add(console);
		
		MenuItem transactions = new MenuItem("Transactions");
		transactions.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
					JFrame frame = new JFrame("Transactions");

				    frame.setSize(800, 600);
				    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				    frame.setVisible(true);
				 final   WalletTransactionsTableModel transactionsModel = new WalletTransactionsTableModel();
				final    JTable transactionsTable = new JTable(transactionsModel);
					
					//TRANSACTIONS SORTER
					Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
					indexes.put(WalletTransactionsTableModel.COLUMN_CONFIRMATIONS, TransactionMap.TIMESTAMP_INDEX);
					indexes.put(WalletTransactionsTableModel.COLUMN_TIMESTAMP, TransactionMap.TIMESTAMP_INDEX);
					indexes.put(WalletTransactionsTableModel.COLUMN_CREATOR, TransactionMap.ADDRESS_INDEX);
					indexes.put(WalletTransactionsTableModel.COLUMN_AMOUNT, TransactionMap.AMOUNT_INDEX);
					CoreRowSorter sorter = new CoreRowSorter(transactionsModel, indexes);
					transactionsTable.setRowSorter(sorter);
					
					//TRANSACTION DETAILS
					transactionsTable.addMouseListener(new MouseAdapter() 
					{
						public void mouseClicked(MouseEvent e) 
						{
							if(e.getClickCount() == 2) 
							{
								//GET ROW
						        int row = transactionsTable.getSelectedRow();
						        row = transactionsTable.convertRowIndexToModel(row);
						        
						        //GET TRANSACTION
						        Transaction transaction = transactionsModel.getTransaction(row);
						         
						        //SHOW DETAIL SCREEN OF TRANSACTION
						        TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
						    }
						}
					});			
					
					frame.getContentPane().add(new JScrollPane(transactionsTable)  );
			}
		});
		menu.add(transactions);
				
		MenuItem messages = new MenuItem("Sends");
		messages.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
					JFrame frame = new JFrame("Sends");

				    frame.setSize(800, 600);
				    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				    frame.setVisible(true);
					SendAssetPanel ap = new SendAssetPanel(null, null);
					frame.getContentPane().add(ap);
					
			}
		});
		menu.add(messages);

		MenuItem assets = new MenuItem("Assets");
		assets.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
					JFrame frame = new JFrame("Assets");

				    frame.setSize(800, 600);
				    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				    frame.setVisible(true);
					AssetsPanel ap = new AssetsPanel();
					frame.getContentPane().add(ap);
					
			}
		});
		menu.add(assets);
		
		MenuItem names = new MenuItem("Names");
		names.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
					JFrame frame = new JFrame("Names");

				    frame.setSize(800, 600);
				    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				    frame.setVisible(true);
					frame.getContentPane().add(
							new NamingServicePanel());
					
			}
		});
		menu.add(names);
		
		MenuItem voting = new MenuItem("Voting");
		voting.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
					JFrame frame = new JFrame("Voting");

				    frame.setSize(800, 600);
				    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				    frame.setVisible(true);
					frame.getContentPane().add(
							new VotingPanel());
					
			}
		});
		menu.add(voting);
		
		MenuItem exit = new MenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new ClosingDialog();
			}
		});
		menu.add(exit);
		
		return menu;
	}
	
	public void setToolTipText(String text)
	{
		this.icon.setToolTip(text);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		
		if(this.icon == null) {
			return;
		}
		
		ObserverMessage message = (ObserverMessage) arg1;
			
		int currentHeight;
		String networkStatus = "";
		String syncProcent = "";
		String toolTipText = "DATACHAINS.world " + Controller.getInstance().getVersion() + "\n";
		
		
		if(Controller.getInstance().getStatus() == Controller.STATUS_NO_CONNECTIONS) {
			networkStatus = "No connections";
			syncProcent = "";
		} else if(Controller.getInstance().getStatus() == Controller.STATUS_SYNCHRONIZING) {
			networkStatus = "Synchronizing";
		} else if(Controller.getInstance().getStatus() == Controller.STATUS_OK)	{
			networkStatus = "OK";
			syncProcent = "";
		}	

		if(message.getType() == ObserverMessage.WALLET_SYNC_STATUS)	{
			currentHeight = (int)message.getValue();
			if(currentHeight == -1)
			{
				this.update(null, new ObserverMessage(
						ObserverMessage.NETWORK_STATUS, Controller.getInstance().getStatus()));
				currentHeight = Controller.getInstance().getHeight();
				return;
			}
			networkStatus = "Wallet Synchronizing";
			
			syncProcent = 100 * currentHeight/Controller.getInstance().getHeight() + "%";
			
			toolTipText += networkStatus + " " + syncProcent;
			toolTipText += "\nHeight: " + currentHeight + "/" + Controller.getInstance().getHeight() + "/" + Controller.getInstance().getMaxPeerHeight();
			setToolTipText(toolTipText);
			
		} else if(message.getType() == ObserverMessage.BLOCKCHAIN_SYNC_STATUS) {
			currentHeight = (int)message.getValue(); 

			if(Controller.getInstance().getStatus() == Controller.STATUS_SYNCHRONIZING)
			{
				syncProcent = 100 * currentHeight/Controller.getInstance().getMaxPeerHeight() + "%";	
			}
			
			toolTipText += networkStatus + " " + syncProcent;
			toolTipText += "\nHeight: " + currentHeight + "/" + Controller.getInstance().getMaxPeerHeight();
			setToolTipText(toolTipText);
			
		} else {
			if(Controller.getInstance().getStatus() == Controller.STATUS_OK || Controller.getInstance().getStatus() == Controller.STATUS_NO_CONNECTIONS) {
				toolTipText += networkStatus + " " + syncProcent;
				toolTipText += "\nHeight: " + Controller.getInstance().getHeight();
				setToolTipText(toolTipText);
			}
		}
	}
	
	
}