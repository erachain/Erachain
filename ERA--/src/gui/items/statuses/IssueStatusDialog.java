package gui.items.statuses;

import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;
import lang.Lang;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.transaction.Transaction;
import gui.transaction.OnDealClick;

@SuppressWarnings("serial")
public class IssueStatusDialog extends JDialog //JFrame
{

	//@SuppressWarnings({ "unchecked", "rawtypes" })
	public IssueStatusDialog()
	{
		
	    javax.swing.JTabbedPane jTabbedPane = new javax.swing.JTabbedPane();
        // INSERT PANEL
		JPanel issuePanel = new IssueStatusPanel();
		issuePanel.setName(Lang.getInstance().translate("Create Status"));	
		jTabbedPane.add(issuePanel);
		 

	}
		
}
