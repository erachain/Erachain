package gui;

import gui.items.assets.AssetsComboBoxModel;
import gui.models.AccountsTableModel;
import lang.Lang;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import utils.BigDecimalStringComparator;
import utils.NumberAsString;
import utils.TableMenuPopupUtil;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.item.assets.AssetCls;
import core.transaction.Transaction;

import gui.Send_Frame;
@SuppressWarnings("serial")
public class AccountsPanel extends JPanel implements ItemListener


//JInternalFrame
{
	//private JFrame parent;

	private static JComboBox<AssetCls> cbxFavorites;
	private AccountsTableModel tableModel;

	@SuppressWarnings("unchecked")
	public AccountsPanel(JFrame parent)
	{
		//this.parent = parent;
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		//this.setSize(500, 500);
		//this.setLocation(20, 20);
		//this.setMaximizable(true);
		//this.setTitle(Lang.getInstance().translate("Accounts"));
		//this.setClosable(true);
		//this.setResizable(true);
		//this.setBorder(true);
		
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.fill = GridBagConstraints.BOTH; 
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;
		tableGBC.weighty = 1;
		tableGBC.gridx = 1;	
		tableGBC.gridy= 1;	
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(10, 0, 0, 0);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridx = 1;	
		buttonGBC.gridy = 2;	
		
		//FAVORITES GBC
		GridBagConstraints favoritesGBC = new GridBagConstraints();
		favoritesGBC.insets = new Insets(10, 0, 10, 0);
		favoritesGBC.fill = GridBagConstraints.BOTH;  
		favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
		favoritesGBC.weightx = 1;
		favoritesGBC.gridx = 1;	
		favoritesGBC.gridy = 0;	
		
		//ASSET FAVORITES
		cbxFavorites = new JComboBox<AssetCls>(new AssetsComboBoxModel());
		this.add(cbxFavorites, favoritesGBC);
		
		//TABLE
		tableModel = new AccountsTableModel();
		final JTable table = Gui.createSortableTable(tableModel, 1);
		
		TableRowSorter<AccountsTableModel> sorter =  (TableRowSorter<AccountsTableModel>) table.getRowSorter();
		sorter.setComparator(AccountsTableModel.COLUMN_CONFIRMED_BALANCE, new BigDecimalStringComparator());
		sorter.setComparator(AccountsTableModel.COLUMN_WAINTING_BALANCE, new BigDecimalStringComparator());
		sorter.setComparator(AccountsTableModel.COLUMN_FEE_BALANCE, new BigDecimalStringComparator());
		
		//ON FAVORITES CHANGE
		cbxFavorites.addItemListener(this);
		
		//MENU
		JPopupMenu menu = new JPopupMenu();	
		
		JMenuItem sendAsset = new JMenuItem(Lang.getInstance().translate("Send Asset"));
		sendAsset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				AssetCls asset = getAsset();
				Account account = tableModel.getAccount(row);
        		//Menu.selectOrAdd( new SendMessageFrame(asset, account), MainFrame.desktopPane.getAllFrames());
				Menu.selectOrAdd( new Send_Frame(asset, account), null);

				/*
				JInternalFrame frame = new JInternalFrame();
				frame.getContentPane().add(new SendMessagePanel(asset, account));
			       //SHOW FRAME
				frame.pack();
				frame.setMaximizable(true);
				frame.setTitle(Lang.getInstance().translate("Accounts"));
				frame.setClosable(true);
				frame.setResizable(true);
				//frame.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
				frame.setLocation(20, 20);
				//CLOSE
				frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
				frame.setResizable(true);
				frame.setVisible(true);
				
        		Menu.selectOrAdd( frame, MainFrame.desktopPane.getAllFrames());
        		//MainFrame.desktopPane.add(frame);
        		 */
				
			}
		});
		menu.add(sendAsset);
		menu.addSeparator();

		JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Copy Address"));
		copyAddress.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Account account = tableModel.getAccount(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getAddress());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyAddress);
				
		JMenuItem copyBalance = new JMenuItem(Lang.getInstance().translate("Copy Balance"));
		copyBalance.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Account account = tableModel.getAccount(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				long key = cbxFavorites.getItemAt(cbxFavorites.getSelectedIndex()).getKey();
				StringSelection value = new StringSelection(account.getConfirmedBalance(key).toPlainString());
			    clipboard.setContents(value, null);
			}
		});
		
		menu.add(copyBalance);
		
		JMenuItem copyConfirmedBalance = new JMenuItem(Lang.getInstance().translate("Copy Confirmed Balance"));
		copyConfirmedBalance.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Account account = tableModel.getAccount(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getConfirmedBalance(Transaction.FEE_KEY).toPlainString());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyConfirmedBalance);
		
		JMenuItem copyGeneratingBalance = new JMenuItem(Lang.getInstance().translate("Copy Generating Balance"));
		copyGeneratingBalance.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Account account = tableModel.getAccount(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getGeneratingBalance().toPlainString());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyGeneratingBalance);

		menu.addSeparator();
		JMenuItem copyPublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
		copyPublicKey.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				PublicKeyAccount publicKeyAccount = tableModel.getPublicKeyAccount(row);
				//PublicKeyAccount publicKeyAccount = Controller.getInstance().getPublicKeyAccountByAddress(
				//		account.getAddress());
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(publicKeyAccount.getBase58());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyPublicKey);

		////////////////////
		TableMenuPopupUtil.installContextMenu(table, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON
		
		table.addMouseListener(new MouseAdapter() 
		{
		     @Override
		     public void mousePressed(MouseEvent e) 
		     {
		        Point p = e.getPoint();
		        int row = table.rowAtPoint(p);
		        table.setRowSelectionInterval(row, row);
		     }
		});
				
		//ADD TOTAL BALANCE
		final JLabel totalBalance = new JLabel(Lang.getInstance().translate("Confirmed Balance") + ": " + tableModel.getTotalBalance().toPlainString());
		this.add(totalBalance, buttonGBC);
		
		//ON TABLE CHANGE
		table.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent arg0) {
				totalBalance.setText(Lang.getInstance().translate("Confirmed Balance") + ": " + NumberAsString.getInstance().numberAsString(tableModel.getTotalBalance()));				
			}		
		});
		
		//ADD ACCOUNTS TABLE
		this.add(new JScrollPane(table), tableGBC);

		/*
		//ADD NEW ACCOUNT BUTTON
		buttonGBC.gridy++;
		JButton newButton = new JButton(Lang.getInstance().translate("New account"));
		newButton.setPreferredSize(new Dimension(150, 25));
		newButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onNewClick();
		    }
		});	
		this.add(newButton, buttonGBC);
		*/
		
	}
	
	public static AssetCls getAsset()
	{
		return (AssetCls) cbxFavorites.getSelectedItem();
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) 
	{
		if(e.getStateChange() == ItemEvent.SELECTED) 
		{		
			AssetCls asset = (AssetCls) cbxFavorites.getSelectedItem();
        	tableModel.setAsset(asset);  
		} 
	}
	
	// set select in Favorites to FEE asset
	public void setSelectionFavoriteItem() 
	{		
		for (int i=0; i < cbxFavorites.getItemCount(); i++)
		{
			AssetCls asset  = cbxFavorites.getItemAt(i+1);
			if (asset.getKey() == AssetCls.FEE_KEY)
			{
				cbxFavorites.setSelectedIndex(i);
		    	tableModel.setAsset(asset);
		    	return;
			}
		}
	}
}
