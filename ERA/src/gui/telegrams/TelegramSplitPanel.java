package gui.telegrams;

import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import gui.MainFrame;
import gui.PasswordPane;
import gui.Split_Panel;
import gui.items.accounts.Account_Name_Add;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.accounts.Account_Set_Name_Dialog;
import gui.items.accounts.Accounts_Name_TableModel;
import gui.items.accounts.Accounts_Panel;
import gui.items.mails.Mail_Send_Dialog;
import gui.library.MTable;
import gui.models.AccountsComboBoxModel;
import lang.Lang;
import settings.Settings;
import utils.Converter;
import utils.NameUtils;
import utils.NameUtils.NameResult;
import utils.Pair;

/**
*
* @author Саша
*/
public class TelegramSplitPanel extends Split_Panel {

   /**
    * Creates new form TelegramSplitPanel
    */
    LeftTelegram leftTelegram;
    RightTelegramPanel rightTelegramPanel;
    private static final long serialVersionUID = 1L;
    public Accounts_Panel accountPanel;
  //  public AssetCls assetSelect;
    private Account selecArg;
 //   private RightTelegramPanel rightPanel;
    private AccountsComboBoxModel accountsModel;
    static TelegramSplitPanel th ;
    private Accounts_Name_TableModel accountModel;
    private MTable tableFavoriteAccounts;
    protected int row;
    private Account recipient;
    private Account sender;

    
   
    
    @SuppressWarnings("rawtypes")
    public TelegramSplitPanel() {
    super("TelegramSplitPanel");
   //th = this;
    this.jScrollPanel_LeftPanel.setVisible(false);
    this.searchToolBar_LeftPanel.setVisible(false);
    this.toolBar_LeftPanel.setVisible(false);
    this.setName(Lang.getInstance().translate("My Accounts"));
    this.jToolBar_RightPanel.setVisible(false);

    GridBagConstraints PanelGBC = new GridBagConstraints();
    PanelGBC.fill = GridBagConstraints.BOTH;
    PanelGBC.anchor = GridBagConstraints.NORTHWEST;
    PanelGBC.weightx = 1;
    PanelGBC.weighty = 1;
    PanelGBC.gridx = 0;
    PanelGBC.gridy = 0;
    accountPanel = new Accounts_Panel();
    leftTelegram = new LeftTelegram();
    rightTelegramPanel= new RightTelegramPanel();
    this.leftPanel.add(leftTelegram, PanelGBC);
    this.jSplitPanel.setLeftComponent(leftTelegram);
    this.jSplitPanel.setRightComponent(rightTelegramPanel);
    // EVENTS on CURSOR
   // accountPanel.table.getSelectionModel().addListSelectionListener(new Account_Tab_Listener());
   
    accountModel = new Accounts_Name_TableModel( Lang.getInstance().translate(new String[]{"No.", "Account", "Name"}));
   tableFavoriteAccounts = new MTable(this.accountModel);
   leftTelegram.jScrollPaneCenter.setViewportView(tableFavoriteAccounts);
   
 //   leftTelegram.jTableFavoriteAccounts.setModel(accountModel);
    leftTelegram.jButtonAddAccount.addActionListener(new ActionListener(){

        @Override
        public void actionPerformed(ActionEvent arg0) {
            // TODO Auto-generated method stub
           
                // TODO Auto-generated method stub
                new Account_Name_Add();
           
        }
        
    });
    
    // set position from table recievers
    if( Settings.getInstance().getTelegramDefaultReciever() != null){
        rightTelegramPanel.jLabelRaght.setText(Settings.getInstance().getTelegramDefaultReciever());
        rightTelegramPanel.walletTelegramsFilterTableModel.setReciever(Settings.getInstance().getTelegramDefaultReciever());
        int k = accountModel.getRowCount();
        for(int i = 0;i<k;i++){
           if(accountModel.getAccount(i).getA().equals(Settings.getInstance().getTelegramDefaultReciever())){
               tableFavoriteAccounts.setRowSelectionInterval(tableFavoriteAccounts.convertRowIndexToModel(i), tableFavoriteAccounts.convertRowIndexToModel(i)); 
           }
        }
        
    }
    else{
        
    }
    
    tableFavoriteAccounts.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

        @Override
        public void valueChanged(ListSelectionEvent e) {
            // TODO Auto-generated method stub
           if( tableFavoriteAccounts.getSelectedRow() == 0) return;;
            String account = (String) accountModel.getValueAt((tableFavoriteAccounts.convertRowIndexToModel(tableFavoriteAccounts.getSelectedRow())),accountModel.COLUMN_ADDRESS);
            rightTelegramPanel.jLabelRaght.setText(account);
            rightTelegramPanel.walletTelegramsFilterTableModel.setReciever(account);
            // set settings
            Settings.getInstance().setTelegramDefaultReciever(account);
        }
        
        
    });
   

// set position sender
   if(Settings.getInstance().getTelegramDefaultSender()!=null){
       
       try {
           leftTelegram.jComboAccount
                    .setSelectedItem(new Account(Settings.getInstance().getTelegramDefaultSender()));
           leftTelegram.jComboAccount.repaint();
           rightTelegramPanel.jLabelLeft.setText(Settings.getInstance().getTelegramDefaultSender());
           rightTelegramPanel.walletTelegramsFilterTableModel.setSender(Settings.getInstance().getTelegramDefaultSender());
       } catch (Exception e) {
           // TODO: handle exception
       }
   } 
   else{
       leftTelegram.jComboAccount
       .setSelectedIndex(0);
       rightTelegramPanel.walletTelegramsFilterTableModel.setSender(leftTelegram.jComboAccount.getItemAt(0).getAddress());
       rightTelegramPanel.jLabelLeft.setText(leftTelegram.jComboAccount.getItemAt(0).getAddress());
       leftTelegram.jComboAccount.repaint();
   }
   
    leftTelegram.jComboAccount.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

           Account asset = ((Account) leftTelegram.jComboAccount.getSelectedItem());

            if (asset != null) {
                rightTelegramPanel.jLabelLeft.setText(asset.getAddress()); 
                rightTelegramPanel.walletTelegramsFilterTableModel.setSender(asset.getAddress());
                Settings.getInstance().setTelegramDefaultSender(asset.getAddress());
                
            }

        }
    });
    
    rightTelegramPanel.jLabelCenter.setText(" ->");
    rightTelegramPanel.jButtonSendTelegram.setText(Lang.getInstance().translate("Send"));
    rightTelegramPanel.jButtonSendTelegram.addActionListener(new ActionListener(){

        @Override
        public void actionPerformed(ActionEvent arg0) {
            // TODO Auto-generated method stub
            onSendClick();
        }
        
        
        
        
    });
    
    // menu

    JPopupMenu menu = new JPopupMenu();

    menu.addPopupMenuListener(new PopupMenuListener() {

        @Override
        public void popupMenuCanceled(PopupMenuEvent arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
            // TODO Auto-generated method stub
            int row1 = tableFavoriteAccounts.getSelectedRow();
            if (row1 < 0)
                return;

            row = tableFavoriteAccounts.convertRowIndexToModel(row1);

        }
    });

    JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Copy Account"));
    copyAddress.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Pair<String, Tuple2<String, String>> account = accountModel.getAccount(row);
            StringSelection value = new StringSelection(account.getA());
            clipboard.setContents(value, null);
        }
    });
    menu.add(copyAddress);

    JMenuItem menu_copyPublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
    menu_copyPublicKey.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            Pair<String, Tuple2<String, String>> account = accountModel.getAccount(row);
            byte[] publick_Key = Controller.getInstance().getPublicKeyByAddress(account.getA());
            PublicKeyAccount public_Account = new PublicKeyAccount(publick_Key);
            StringSelection value = new StringSelection(public_Account.getBase58());
            clipboard.setContents(value, null);
        }
    });
    menu.add(menu_copyPublicKey);

    JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.getInstance().translate("Send asset"));
    Send_Coins_item_Menu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Pair<String, Tuple2<String, String>> account1 = accountModel.getAccount(row);
            Account account = new Account(account1.getA());
            new Account_Send_Dialog(null, null, account, null);

        }
    });
    menu.add(Send_Coins_item_Menu);

    JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("Send mail"));
    Send_Mail_item_Menu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Pair<String, Tuple2<String, String>> account1 = accountModel.getAccount(row);
            Account account = new Account(account1.getA());
            new Mail_Send_Dialog(null, null, account, null);

        }
    });
    menu.add(Send_Mail_item_Menu);

    JMenuItem setName = new JMenuItem(Lang.getInstance().translate("Edit name"));
    setName.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Pair<String, Tuple2<String, String>> account1 = accountModel.getAccount(row);

            new Account_Set_Name_Dialog(account1.getA());
            tableFavoriteAccounts.repaint();

        }
    });
    menu.add(setName);

    JMenuItem menuItemDelete = new JMenuItem(Lang.getInstance().translate("Remove Favorite"));
    menuItemDelete.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (!Controller.getInstance().isWalletUnlocked()) {
                // ASK FOR PASSWORD
                String password = PasswordPane.showUnlockWalletDialog(MainFrame.getInstance());
                if (password.equals("")) {

                    return;
                }
                if (!Controller.getInstance().unlockWallet(password)) {
                    // WRONG PASSWORD
                    JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
                            Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                    // ENABLE

                    return;
                }
            }

            int row = tableFavoriteAccounts.getSelectedRow();
            try {
                row = tableFavoriteAccounts.convertRowIndexToModel(row);
                Pair<String, Tuple2<String, String>> ac = accountModel.getAccount(row);
                Controller.getInstance().wallet.database.getAccountsPropertisMap().delete(ac.getA());
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
    });
    menu.add(menuItemDelete);
    
    tableFavoriteAccounts.setComponentPopupMenu(menu); 
    
   
}

@Override
public void delay_on_close() {
  //  rightPanel.table_Model.deleteObserver();
    accountPanel.tableModel.deleteObserver();
    Controller.getInstance().deleteObserver(accountPanel.reload_Button);
    Controller.getInstance().deleteObserver(accountPanel.newAccount_Button);
}

class Account_Tab_Listener implements ListSelectionListener {

    @Override
    public void valueChanged(ListSelectionEvent arg0) {

        AssetCls asset = (AssetCls) accountPanel.cbxFavorites.getSelectedItem();
        Account account = null;
  //      if (accountPanel.table.getSelectedRow() >= 0)
 //           account = accountPanel.tableModel.getAccount(accountPanel.table.convertRowIndexToModel(accountPanel.table.getSelectedRow()));
        if (account == null) return;
        if(asset ==null)return;
 //       if (account.equals(selecArg) && asset.equals(assetSelect)) return;
        selecArg = account;
//        assetSelect = asset;
 //       rightPanel.table_Model.set_Account(account);
 //       rightPanel.table_Model.fireTableDataChanged();
 //       rightPanel.set_Asset(asset);
   //     jScrollPane_jPanel_RightPanel.setViewportView(rightPanel);
        
    }

}


public void onSendClick() {
    
    this.rightTelegramPanel.jButtonSendTelegram.setEnabled(false);
  //CHECK IF WALLET UNLOCKED
    if (!Controller.getInstance().isWalletUnlocked()) {
        //ASK FOR PASSWORD
        String password = PasswordPane.showUnlockWalletDialog(this);
        if (password.equals("")) {
            this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
            return ;
        }
        if (!Controller.getInstance().unlockWallet(password)) {
            //WRONG PASSWORD
            JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
            return;
        }
    }
    
    //READ SENDER
     sender = (Account) this.leftTelegram.jComboAccount.getSelectedItem();
    
    //READ RECIPIENT
    String recipientAddress = rightTelegramPanel.jLabelRaght.getText();

     //ORDINARY RECIPIENT
    if (Crypto.getInstance().isValidAddress(recipientAddress)) {
       this.recipient = new Account(recipientAddress);
    } else {
        //IS IS NAME of RECIPIENT - resolve ADDRESS
        Pair<Account, NameResult> result = NameUtils.nameToAdress(recipientAddress);

        if (result.getB() == NameResult.OK) {
            recipient = result.getA();
        } else {
            JOptionPane.showMessageDialog(null, result.getB().getShortStatusMessage(), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
            return ;
        }
    }

    BigDecimal amount = null;
    int feePow =0;
    boolean isTextB = true;

    byte[] messageBytes = null; 
    String message = rightTelegramPanel.jTextPaneText.getText();
   
    if (message != null && message.length() > 0) {
        if (isTextB) {
            messageBytes = message.getBytes(Charset.forName("UTF-8"));
        } else {
            try {
                messageBytes = Converter.parseHexString(message);
            } catch (Exception g) {
                try {
                    messageBytes = Base58.decode(message);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message format is not base58 or hex!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

                    //ENABLE
                    this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
                    return;
                }
            }
        }
    }
    // if no TEXT - set null
    if (messageBytes != null && messageBytes.length == 0) messageBytes = null;
    // if amount = 0 - set null
   

    boolean encryptMessage = false;

    byte[] encrypted = (encryptMessage)?  new byte[]{1} : new byte[]{0};
    byte[] isTextByte =  new byte[]{1} ;

    Long key = 1l;
    

    if (messageBytes != null) {
        if (messageBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message size exceeded!") + " <= MAX", Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
            return;
        }

        if (encryptMessage) {
            //sender
            PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress().toString());
            byte[] privateKey = account.getPrivateKey();

            //recipient
            byte[] publicKey = Controller.getInstance().getPublicKeyByAddress(recipient.getAddress());
            if (publicKey == null) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("The recipient has not yet performed any action in the blockchain.\nYou can't send an encrypted message to him."), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);

                return;
            }

            messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
        }
    }
   String head = "Send Telegram";
    if (head == null) head = "";
    if (head.getBytes(StandardCharsets.UTF_8).length > 256) {

        JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Title size exceeded!") + " <= 256", Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        return;

    }

   // CREATE TX MESSAGE
    Transaction transaction = Controller.getInstance().r_Send(
            Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), feePow, recipient, key,
            amount, head, messageBytes, isTextByte, encrypted);
    
    Controller.getInstance().broadcastTelegram(transaction, true);
       
    
    // ENABLE
    rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
}
public boolean cheskError(){
   
    return true;
}


}
