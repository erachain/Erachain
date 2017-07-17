package gui.items.assets;

import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import lang.Lang;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

//import settings.Settings;
import utils.DateTimeFormat;
import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.item.assets.AssetCls;
import core.transaction.Transaction;

@SuppressWarnings("serial")
public class IssueAssetDialog extends JDialog //JFrame
{
	

	public IssueAssetDialog()
	{
		this.setTitle(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("Issue Asset"));
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setModal(true);
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		 IssueAssetPanel Issue_Assets_SplitPanel = new IssueAssetPanel();
		 Issue_Assets_SplitPanel.setName(Lang.getInstance().translate("Issue Asset"));
	}
}
