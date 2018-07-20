package gui.items.accounts;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import gui.AccountRenderer;
import gui.PasswordPane;
import gui.items.assets.Asset_Info;
import gui.items.assets.AssetsComboBoxModel;
import gui.library.M_DecimalFormatedTextField;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import lang.Lang;
import utils.Converter;
import utils.MenuPopupUtil;
import utils.NameUtils;
import utils.Pair;
import utils.NameUtils.NameResult;

public class AssetSendPanel extends javax.swing.JPanel {

 // TODO - "A" - &
    static String wrongFirstCharOfAddress = "A";
    public Account recipient;

    public String message;

    public byte[] messageBytes;

    public BigDecimal amount;

    public int feePow;

    public boolean isTextB;

    public Account sender;

    public AssetCls asset;

    public long key;

    public String head;

    public byte[] isTextByte;

    public byte[] encrypted;
    public Integer result;
    public Account account;
    BufferedImage image1;

    private int max_Height;

    private int max_Widht;
    private Image Im;
    private String defaultImagePath = "images/icons/coin.png";

    private PersonCls person_To;
/**
    * Creates new form AssetSendPanel
    */

    private AccountsComboBoxModel accountsModel;
   public AssetSendPanel(AssetCls asset2, Account account2,  Account account_To, PersonCls person) {

       account = account2;
       asset =asset2;
       recipient = account_To;
       person_To = person;

       initComponents();

       this.jTextField_Recive_Detail.setText("");
       this.jTextField_Mess_Title.setText("");
       this.jTextField_Ammount.setText("0");
       this.jLabel_Icon.setText("");
       // icon
       jLabel_Icon.setIcon(new ImageIcon(defaultImagePath));
       // account model
       this.accountsModel = new AccountsComboBoxModel();
       jComboBox_Account.setModel(accountsModel);
   
    // favorite combo box
      jComboBox_Asset.setModel(new AssetsComboBoxModel());
       if (asset != null) {
           this.jTextArea_Account_Description.setText(asset.getDescription());

           for (int i = 0; i < jComboBox_Asset.getItemCount(); i++) {
               AssetCls item = jComboBox_Asset.getItemAt(i);
               if (item.getKey() == asset.getKey()) {
                   // not worked jComboBox_Asset.setSelectedItem(asset);
                   jComboBox_Asset.setSelectedIndex(i);
               //    jComboBox_Asset.setEnabled(false);// .setEditable(false);
                   break;
               } else {
               //    jComboBox_Asset.setEnabled(true);
               }
           }
       }

       this.jComboBox_Fee.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }));
       // accoutn ComboBox
       this.accountsModel = new AccountsComboBoxModel();
       this.jComboBox_Account.setModel(accountsModel);
     //  this.jComboBox_Account.setRenderer(new AccountRenderer(0));
     //  ((AccountRenderer) jComboBox_Account.getRenderer()).setAsset(((AssetCls) jComboBox_Account.getSelectedItem()).getKey());
       if (account != null) jComboBox_Account.setSelectedItem(account);

       //ON FAVORITES CHANGE

       jComboBox_Account.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {

              asset = ((AssetCls) jComboBox_Account.getSelectedItem());

               if (asset != null) {
                   ((AccountRenderer) jComboBox_Account.getRenderer()).setAsset(asset.getKey());
                   jComboBox_Account.repaint();
                // set image
                   setImage();
                   jLabel_Icon.repaint();
                // set scale
                   int scale = 8;
                   if(asset!=null)scale = asset.getScale();
                   jTextField_Ammount.setScale(scale);
                 // set description
                 //  jTextArea_Account_Description.setText(asset.getDescription());
                   jScrollPane2.setViewportView(new Asset_Info(asset));
                   
               }

           }
       });

       
       // default set asset
       if (asset == null) asset = ((AssetCls) jComboBox_Asset.getSelectedItem());
       
       this.jComboBox_Asset.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {

              asset = ((AssetCls) jComboBox_Asset.getSelectedItem());

               if (asset != null) {
      //             ((AccountRenderer) jComboBox_Account.getRenderer()).setAsset(asset.getKey());
                   jComboBox_Account.repaint();
                // set image
                   setImage();
                  jLabel_Icon.repaint();
                // set scale
                   int scale = 8;
                   if(asset!=null)scale = asset.getScale();
                   jTextField_Ammount.setScale(scale);
                  // jTextArea_Account_Description.setText(asset.getDescription());
                   jScrollPane2.setViewportView(new Asset_Info(asset));
                   
               }

           }
       });
       
       jButton_ok.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               onSendClick();
           }
       });
       
      // set image asset
       setImage();

      // set acoount TO
      this.jTextField_To.getDocument().addDocumentListener(new DocumentListener() {

          @Override
          public void changedUpdate(DocumentEvent arg0) {
          }

          @Override
          public void insertUpdate(DocumentEvent arg0) {
              refreshReceiverDetails();
          }

          @Override
          public void removeUpdate(DocumentEvent arg0) {
              refreshReceiverDetails();
          }
      });

       if( recipient != null){
           jTextField_To.setText(recipient.getAddress());
           //refreshReceiverDetails()

       }

      this.jLabel_Title.setText(Lang.getInstance().translate("Title"));
      this.jLabel_Account.setText(Lang.getInstance().translate("Select Account") + ":");
      this.jLabel_To.setText(Lang.getInstance().translate("To: (address or name)"));
      this.jLabel_Recive_Detail.setText(Lang.getInstance().translate("Receiver details") + ":");
      this.jLabel_Mess_Title.setText(Lang.getInstance().translate("Title") + ":");
     this.jLabel_Mess.setText(Lang.getInstance().translate("Message") + ":");
      this.jCheckBox_Enscript.setText(Lang.getInstance().translate("Encrypt Message") + ":");
      this.jLabel_Asset.setText(Lang.getInstance().translate("Asset") + ":");
      this.jLabel_Ammount.setText(Lang.getInstance().translate("Amount") + ":");
      this.jLabel_Fee.setText(Lang.getInstance().translate("Fee Level") + ":");

      this.jButton_ok.setText(Lang.getInstance().translate("Send"));
   // CONTEXT MENU
      MenuPopupUtil.installContextMenu(this.jTextField_To);
      MenuPopupUtil.installContextMenu(this.jTextField_Ammount);
      MenuPopupUtil.installContextMenu(this.jTextArea_Description);
      MenuPopupUtil.installContextMenu(this.jTextField_Recive_Detail);
      jTextArea_Account_Description.setWrapStyleWord(true);
      jTextArea_Account_Description.setLineWrap(true);
      jScrollPane2.setViewportView(new Asset_Info(asset)); //jTextArea_Account_Description);
   }
   
   private void refreshReceiverDetails() {
       String toValue = jTextField_To.getText();
       AssetCls asset = ((AssetCls) jComboBox_Asset.getSelectedItem());

       this.jTextField_Recive_Detail.setText(Account.getDetails(toValue, asset));

       if (false && toValue != null && toValue.startsWith(wrongFirstCharOfAddress)) {
           this.jCheckBox_Enscript.setEnabled(false);
           this.jCheckBox_Enscript.setSelected(false);
          // isText.setSelected(false);
       } else {
           this.jCheckBox_Enscript.setEnabled(true);
       }
   }
    public boolean cheskError(){
        this.jButton_ok.setEnabled(false);
        //TODO TEST
        //CHECK IF NETWORK OK
        /*if(Controller.getInstance().getStatus() != Controller.STATUS_OKE)
        {
            //NETWORK NOT OK
            JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);

            //ENABLE
            this.jButton_ok.setEnabled(true);

            return;
        }*/

        //READ SENDER
        sender = (Account) jComboBox_Account.getSelectedItem();
        //CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            //ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (password.equals("")) {
                this.jButton_ok.setEnabled(true);
                return false;
            }
            if (!Controller.getInstance().unlockWallet(password)) {
                //WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.jButton_ok.setEnabled(true);
                return false;
            }
        }

        //READ RECIPIENT
        String recipientAddress = jTextField_To.getText();

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
                this.jButton_ok.setEnabled(true);
                return false;
            }
        }

        int parsing = 0;
       
       
        try {
            //READ AMOUNT
            parsing = 1;
            amount = new BigDecimal(jTextField_Ammount.getText());

            //READ FEE
            parsing = 2;
            feePow = Integer.parseInt((String)this.jComboBox_Fee.getSelectedItem());
        } catch (Exception e) {
            //CHECK WHERE PARSING ERROR HAPPENED
            switch (parsing) {
                case 1:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid amount!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case 2:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
            }
            //ENABLE
            this.jButton_ok.setEnabled(true);
            return false;
        }

        if (amount.equals(new BigDecimal("0.0"))) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Amount must be greater 0.0"), Lang.getInstance().translate("Error") + ":  " + Lang.getInstance().translate("Invalid amount!"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            this.jButton_ok.setEnabled(true);
            return false;
        }

        this.message = jTextArea_Description.getText();

       isTextB = true;

       

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
                        this.jButton_ok.setEnabled(true);
                        return false;
                    }
                }
            }
        }
        // if no TEXT - set null
        if (messageBytes != null && messageBytes.length == 0) messageBytes = null;
        // if amount = 0 - set null
        if (amount.compareTo(BigDecimal.ZERO) == 0) amount = null;

        boolean encryptMessage = this.jCheckBox_Enscript.isSelected();

        encrypted = (encryptMessage) ? new byte[]{1} : new byte[]{0};
        isTextByte = (isTextB) ? new byte[]{1} : new byte[]{0};

       
        if (amount != null) {
            //CHECK IF PAYMENT OR ASSET TRANSFER
            asset = (AssetCls) this.jComboBox_Asset.getSelectedItem();
            key = asset.getKey();
        }

        if (messageBytes != null) {
            if (messageBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message size exceeded!") + " <= MAX", Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.jButton_ok.setEnabled(true);
                return false;
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
                    this.jButton_ok.setEnabled(true);

                    return false;
                }

                messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
            }
        }
       head = this.jTextField_Mess_Title.getText();
        if (head == null)
            head = "";
        if (head.getBytes(StandardCharsets.UTF_8).length > 256) {

            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Title size exceeded!") + " <= 256", Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return false;

        }

        
        return true;
    }

    public void confirmaftecreatetransaction(){
        //CHECK VALIDATE MESSAGE
        if (result == Transaction.VALIDATE_OK) {
            //RESET FIELDS

            if (amount != null && amount.compareTo(BigDecimal.ZERO) == 1) //IF MORE THAN ZERO
            {
                this.jTextField_Ammount.setText("0");
            }

            // TODO "A" ??
            if (false && this.jTextField_To.getText().startsWith(wrongFirstCharOfAddress)) {
                this.jTextField_To.setText("");
            }

            this.jTextArea_Description.setText("");

            // TODO "A" ??
            if (true || this.jTextField_To.getText().startsWith(wrongFirstCharOfAddress)) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message and/or payment has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }
    
   
    }
    public void onSendClick(){
        
    }

   /**
    * This method is called from within the constructor to initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is always
    * regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
   private void initComponents() {
       java.awt.GridBagConstraints gridBagConstraints;

       jLabel_Recive_Detail = new javax.swing.JLabel();
       jTextField_To = new javax.swing.JTextField();
       jLabel_Account = new javax.swing.JLabel();
       jLabel_To = new javax.swing.JLabel();
       jComboBox_Account = new javax.swing.JComboBox<>();
       jLabel_Mess_Title = new javax.swing.JLabel();
       jTextField_Recive_Detail = new javax.swing.JTextField();
       jLabel_Title = new javax.swing.JLabel();
       jTextField_Mess_Title = new javax.swing.JTextField();
       jLabel_Mess = new javax.swing.JLabel();
       jScrollPane1 = new javax.swing.JScrollPane();
       jTextArea_Description = new javax.swing.JTextArea();
       jCheckBox_Enscript = new javax.swing.JCheckBox();
       jLabel_Asset = new javax.swing.JLabel();
       jComboBox_Asset = new javax.swing.JComboBox<>();
       jLabel_Ammount = new javax.swing.JLabel();
       jTextField_Ammount = new M_DecimalFormatedTextField();
       jLabel_Fee = new javax.swing.JLabel();
       jComboBox_Fee = new javax.swing.JComboBox<>();
       jButton_ok = new javax.swing.JButton();
       jLabel_Icon = new javax.swing.JLabel();
       jScrollPane2 = new javax.swing.JScrollPane();
       jTextArea_Account_Description = new javax.swing.JTextArea();

       java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
       layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
       layout.rowHeights = new int[] {0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0};
       setLayout(layout);

       jLabel_Recive_Detail.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       jLabel_Recive_Detail.setText("jLabel1");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 0;
       gridBagConstraints.gridy = 6;
       gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
       add(jLabel_Recive_Detail, gridBagConstraints);

       jTextField_To.setText("jTextField1");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 2;
       gridBagConstraints.gridy = 4;
       gridBagConstraints.gridwidth = 15;
       gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
       gridBagConstraints.weightx = 0.3;
       gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
       add(jTextField_To, gridBagConstraints);

       jLabel_Account.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       jLabel_Account.setText("jLabel2");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 0;
       gridBagConstraints.gridy = 2;
       gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
       add(jLabel_Account, gridBagConstraints);

       jLabel_To.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       jLabel_To.setText("jLabel3");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 0;
       gridBagConstraints.gridy = 4;
       gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
       add(jLabel_To, gridBagConstraints);

       
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 2;
       gridBagConstraints.gridy = 2;
       gridBagConstraints.gridwidth = 15;
       gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
       gridBagConstraints.weightx = 0.3;
       gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
       add(jComboBox_Account, gridBagConstraints);

       jLabel_Mess_Title.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       jLabel_Mess_Title.setText("jLabel4");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 0;
       gridBagConstraints.gridy = 8;
       gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
       add(jLabel_Mess_Title, gridBagConstraints);

       jTextField_Recive_Detail.setEditable(false);
       jTextField_Recive_Detail.setText("jTextField2");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 2;
       gridBagConstraints.gridy = 6;
       gridBagConstraints.gridwidth = 15;
       gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
       gridBagConstraints.weightx = 0.3;
       gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
       add(jTextField_Recive_Detail, gridBagConstraints);

       jLabel_Title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       jLabel_Title.setText("jLabel5");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 0;
       gridBagConstraints.gridy = 0;
       gridBagConstraints.gridwidth = 17;
       gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
       gridBagConstraints.insets = new java.awt.Insets(15, 15, 6, 15);
       add(jLabel_Title, gridBagConstraints);

       jTextField_Mess_Title.setText("jTextField3");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 2;
       gridBagConstraints.gridy = 8;
       gridBagConstraints.gridwidth = 15;
       gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
       gridBagConstraints.weightx = 0.3;
       gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
       add(jTextField_Mess_Title, gridBagConstraints);

       jLabel_Mess.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       jLabel_Mess.setText("jLabel6");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 0;
       gridBagConstraints.gridy = 10;
       gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
       add(jLabel_Mess, gridBagConstraints);

       jTextArea_Description.setColumns(20);
       jTextArea_Description.setRows(5);
       jScrollPane1.setViewportView(jTextArea_Description);

       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 2;
       gridBagConstraints.gridy = 10;
       gridBagConstraints.gridwidth = 15;
       gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
       gridBagConstraints.weightx = 0.3;
       gridBagConstraints.weighty = 0.1;
       gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
       add(jScrollPane1, gridBagConstraints);

       jCheckBox_Enscript.setText("jCheckBox1");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 2;
       gridBagConstraints.gridy = 12;
       gridBagConstraints.gridwidth = 7;
       gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
       gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
       add(jCheckBox_Enscript, gridBagConstraints);

       jLabel_Asset.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       jLabel_Asset.setText("jLabel7");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 0;
       gridBagConstraints.gridy = 14;
       gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
       add(jLabel_Asset, gridBagConstraints);

       
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 2;
       gridBagConstraints.gridy = 14;
       gridBagConstraints.gridwidth = 15;
       gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
       gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
       add(jComboBox_Asset, gridBagConstraints);

       jLabel_Ammount.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       jLabel_Ammount.setText("jLabel8");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 0;
       gridBagConstraints.gridy = 18;
       gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
       add(jLabel_Ammount, gridBagConstraints);

       jTextField_Ammount.setText("jTextField4");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 2;
       gridBagConstraints.gridy = 18;
       gridBagConstraints.gridwidth = 7;
       gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
       gridBagConstraints.weightx = 0.2;
       gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
       add(jTextField_Ammount, gridBagConstraints);

       jLabel_Fee.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       jLabel_Fee.setText("jLabel9");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 12;
       gridBagConstraints.gridy = 18;
       gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
       gridBagConstraints.weightx = 0.1;
       gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
       add(jLabel_Fee, gridBagConstraints);

       jComboBox_Fee.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 14;
       gridBagConstraints.gridy = 18;
       gridBagConstraints.gridwidth = 3;
       gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
       gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
       gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
       add(jComboBox_Fee, gridBagConstraints);

       jButton_ok.setText("jButton1");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 16;
       gridBagConstraints.gridy = 20;
       gridBagConstraints.insets = new java.awt.Insets(7, 0, 15, 15);
       add(jButton_ok, gridBagConstraints);

       jLabel_Icon.setText("jLabel10");
       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 2;
       gridBagConstraints.gridy = 16;
       gridBagConstraints.gridwidth = 1;
       gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
       gridBagConstraints.weightx = 0.4;
      // add(jLabel_Icon, gridBagConstraints);

       jTextArea_Account_Description.setEditable(false);
       jTextArea_Account_Description.setColumns(20);
       jTextArea_Account_Description.setRows(5);
       jTextArea_Account_Description.setEnabled(false);
       

       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 2;
       gridBagConstraints.gridy = 16;
       gridBagConstraints.gridwidth = 17;
       gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
       gridBagConstraints.weightx = 0.4;
       gridBagConstraints.weighty = 0.2;
       gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
       add(jScrollPane2, gridBagConstraints);
   }// </editor-fold>                        

   public void setImage(){
       // image view
              InputStream inputStream = null;
              if(asset == null) return;
              byte[] image_Byte = asset.getImage();
              if (image_Byte.length > 0) {
                  inputStream = new ByteArrayInputStream(asset.getImage());
              
                  try {
                      image1 = ImageIO.read(inputStream);

                      // jLabel2.setText("jLabel2");
                      ImageIcon image = new ImageIcon(image1);
                      int x = image.getIconWidth();
                      max_Height = image.getIconHeight();

                      max_Widht = 250;
                      double k = ((double) x / (double) max_Widht);
                      max_Height = (int) (max_Height / k);


                      if (max_Height != 0) {
                          Im = image.getImage().getScaledInstance(max_Widht, max_Height, 1);
                          ImageIcon ic = new ImageIcon(Im);
                          jLabel_Icon.setIcon(ic);
                          jLabel_Icon.setSize(ic.getIconWidth(), ic.getIconHeight());
                      }


                  } catch (IOException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                  }
                  return;
              }
              // if era then file system icon
             if(asset.getKey()== 1l){
                 jLabel_Icon.setIcon(new ImageIcon("images/icons/icon64.png"));
                  return;
             }
             jLabel_Icon.setIcon(new ImageIcon(defaultImagePath));

              
          }

   // Variables declaration - do not modify                     
   public javax.swing.JButton jButton_ok;
   private javax.swing.JCheckBox jCheckBox_Enscript;
   private javax.swing.JComboBox<Account> jComboBox_Account;
   public javax.swing.JComboBox<AssetCls> jComboBox_Asset;
   private javax.swing.JComboBox<String> jComboBox_Fee;
   private javax.swing.JLabel jLabel_Asset;
   private javax.swing.JLabel jLabel_Account;
   private javax.swing.JLabel jLabel_Ammount;
   private javax.swing.JLabel jLabel_Fee;
   private javax.swing.JLabel jLabel_Icon;
   private javax.swing.JLabel jLabel_Mess;
   private javax.swing.JLabel jLabel_Mess_Title;
   public javax.swing.JLabel jLabel_Recive_Detail;
   public javax.swing.JLabel jLabel_Title;
   public javax.swing.JLabel jLabel_To;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JTextArea jTextArea_Account_Description;
   public javax.swing.JTextArea jTextArea_Description;
   public M_DecimalFormatedTextField jTextField_Ammount;
   public javax.swing.JTextField jTextField_Mess_Title;
   private javax.swing.JTextField jTextField_Recive_Detail;
   public javax.swing.JTextField jTextField_To;
   // End of variables declaration                   
}
