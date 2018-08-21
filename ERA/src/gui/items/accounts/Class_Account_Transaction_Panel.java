package gui.items.accounts;

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

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MaskFormatter;

import org.apache.commons.net.util.Base64;

import controller.Controller;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.ParseException;

@SuppressWarnings("serial")

public class Class_Account_Transaction_Panel extends JPanel {
    // private final MessagesTableModel messagesTableModel;

    // TODO - "A" - &
    static String wrongFirstCharOfAddress = "A";

    public JComboBox<Account> cbxFrom;
    public JTextField txtTo;
    public M_DecimalFormatedTextField txtAmount;
    public JComboBox<String> txtFeePow;
    public JTextArea txtMessage;
    public JCheckBox encryptedCHcKBox;
    public JCheckBox isText;
    public JButton sendButton;
    public AccountsComboBoxModel accountsModel;
    public JComboBox<AssetCls> cbxFavorites;
    public JTextField txtRecDetails;
    public JLabel messageLabel;
    public JLabel icon;
    public JTextArea jTextArea_Title;
    public JLabel toLabel;
    public JLabel recDetailsLabel;
    public JTextField txt_Title;
    int y;

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

    public Class_Account_Transaction_Panel(AssetCls asset2, Account account2,  Account account_To, PersonCls person) {
        account = account2;
        asset =asset2;
        recipient = account_To;
        y = 0;
        GridBagLayout gridBagLayout = new GridBagLayout();
        // gridBagLayout.columnWidths = new int[]{0, 112, 140, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.1, 0.1, 0.1, 0.1};
        this.setLayout(gridBagLayout);

        // PADDING
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        // icon
        GridBagConstraints iconlabelGBC = new GridBagConstraints();
        iconlabelGBC.insets = new Insets(5, 5, 5, 5);
        iconlabelGBC.fill = GridBagConstraints.BOTH;// .HORIZONTAL;
        iconlabelGBC.anchor = GridBagConstraints.NORTHWEST;
        iconlabelGBC.weightx = 1;
        // iconlabelGBC.weighty = 0.1;
        iconlabelGBC.gridx = 0;
        iconlabelGBC.gridy = y;
        iconlabelGBC.gridwidth = 1;

        icon = new JLabel();
        icon.setIcon(new ImageIcon(defaultImagePath));
        this.add(icon, iconlabelGBC);

        // title info
        GridBagConstraints titlelabelGBC = new GridBagConstraints();
        titlelabelGBC.insets = new Insets(5, 5, 5, 5);
        titlelabelGBC.fill = GridBagConstraints.BOTH;// .HORIZONTAL;
        titlelabelGBC.anchor = GridBagConstraints.NORTHWEST;
        titlelabelGBC.weightx = 0;
        titlelabelGBC.weighty = 0.3;
        titlelabelGBC.gridx = 1;
        titlelabelGBC.gridy = y;
        titlelabelGBC.gridwidth = 4;

        jTextArea_Title = new javax.swing.JTextArea();

        jTextArea_Title.setEditable(false);

        jTextArea_Title.setColumns(20);

        jTextArea_Title.setRows(1);
        jTextArea_Title.setLineWrap(true);
        jTextArea_Title.setBackground(this.getBackground());
        // jTextArea_Title.setEnabled(false);

        jTextArea_Title.setFocusCycleRoot(true);

        jTextArea_Title.setText(Lang.getInstance().translate("Title"));
        // jTextArea_Title.setFont(new java.awt.Font("Tahoma", 0, 14)); //
        // NOI18N

        this.add(jTextArea_Title, titlelabelGBC);

        // ASSET FAVORITES
        GridBagConstraints favoritesGBC = new GridBagConstraints();
        favoritesGBC.insets = new Insets(5, 5, 5, 0);
        favoritesGBC.fill = GridBagConstraints.BOTH;
        favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
        favoritesGBC.weightx = 1;
        favoritesGBC.gridwidth = 5;
        favoritesGBC.gridx = 0;
        favoritesGBC.gridy = ++y;

        cbxFavorites = new JComboBox();
        // this.add(cbxFavorites, favoritesGBC);

        this.accountsModel = new AccountsComboBoxModel();

        // LABEL FROM
        GridBagConstraints labelFromGBC = new GridBagConstraints();
        labelFromGBC.insets = new Insets(5, 5, 5, 5);
        labelFromGBC.fill = GridBagConstraints.HORIZONTAL;
        labelFromGBC.anchor = GridBagConstraints.NORTHWEST;
        labelFromGBC.weightx = 0;
        labelFromGBC.gridx = 0;
        labelFromGBC.gridy = ++y;
        JLabel fromLabel = new JLabel(Lang.getInstance().translate("Select account") + ":");
        this.add(fromLabel, labelFromGBC);
        // fontHeight =
        // fromLabel.getFontMetrics(fromLabel.getFont()).getHeight();

        // COMBOBOX FROM
        GridBagConstraints cbxFromGBC = new GridBagConstraints();
        cbxFromGBC.gridwidth = 4;
        cbxFromGBC.insets = new Insets(5, 5, 5, 0);
        cbxFromGBC.fill = GridBagConstraints.HORIZONTAL;
        cbxFromGBC.anchor = GridBagConstraints.NORTHWEST;
        cbxFromGBC.weightx = 0;
        cbxFromGBC.gridx = 1;
        cbxFromGBC.gridy = y;

        this.cbxFrom = new JComboBox();
        this.cbxFrom.setRenderer(new AccountRenderer(0));
        this.add(this.cbxFrom, cbxFromGBC);

        // ON FAVORITES CHANGE

        // LABEL TO
        GridBagConstraints labelToGBC = new GridBagConstraints();
        labelToGBC.gridy = ++y;
        labelToGBC.insets = new Insets(5, 5, 5, 5);
        labelToGBC.fill = GridBagConstraints.HORIZONTAL;
        labelToGBC.anchor = GridBagConstraints.NORTHWEST;
        labelToGBC.weightx = 0;
        labelToGBC.gridx = 0;
        toLabel = new JLabel(Lang.getInstance().translate("To: (address or name)"));
        this.add(toLabel, labelToGBC);

        // TXT TO
        GridBagConstraints txtToGBC = new GridBagConstraints();
        txtToGBC.gridwidth = 4;
        txtToGBC.insets = new Insets(5, 5, 5, 0);
        txtToGBC.fill = GridBagConstraints.HORIZONTAL;
        txtToGBC.anchor = GridBagConstraints.NORTHWEST;
        txtToGBC.weightx = 0;
        txtToGBC.gridx = 1;
        txtToGBC.gridy = y;

        txtTo = new JTextField();
        this.add(txtTo, txtToGBC);

        txtTo.getDocument().addDocumentListener(new DocumentListener() {

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

        // LABEL RECEIVER
        GridBagConstraints labelDetailsGBC = new GridBagConstraints();
        labelDetailsGBC.gridy = ++y;
        labelDetailsGBC.insets = new Insets(5, 5, 5, 5);
        labelDetailsGBC.fill = GridBagConstraints.HORIZONTAL;
        labelDetailsGBC.anchor = GridBagConstraints.NORTHWEST;
        labelDetailsGBC.weightx = 0;
        labelDetailsGBC.gridx = 0;
        recDetailsLabel = new JLabel(Lang.getInstance().translate("Receiver details") + ":");
        this.add(recDetailsLabel, labelDetailsGBC);

        // RECEIVER DETAILS
        GridBagConstraints txtReceiverGBC = new GridBagConstraints();
        txtReceiverGBC.gridwidth = 4;
        txtReceiverGBC.insets = new Insets(5, 5, 5, 0);
        txtReceiverGBC.fill = GridBagConstraints.HORIZONTAL;
        txtReceiverGBC.anchor = GridBagConstraints.NORTHWEST;
        txtReceiverGBC.weightx = 0;
        txtReceiverGBC.gridx = 1;
        txtReceiverGBC.gridy = y;

        txtRecDetails = new JTextField();
        txtRecDetails.setEditable(false);
        this.add(txtRecDetails, txtReceiverGBC);

        // LABEL TITLE
        GridBagConstraints labelMessageGBC = new GridBagConstraints();
        labelMessageGBC.insets = new Insets(5, 5, 5, 5);
        labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;
        labelMessageGBC.anchor = GridBagConstraints.NORTHWEST;
        labelMessageGBC.weightx = 0;
        labelMessageGBC.gridx = 0;
        labelMessageGBC.gridy = ++y;

        JLabel title_Label = new JLabel(Lang.getInstance().translate("Title") + ":");
        this.add(title_Label, labelMessageGBC);

        // TXT TITLE
        GridBagConstraints txtMessageGBC = new GridBagConstraints();
        txtMessageGBC.gridwidth = 4;
        txtMessageGBC.insets = new Insets(5, 5, 5, 0);
        txtMessageGBC.fill = GridBagConstraints.HORIZONTAL;
        txtMessageGBC.anchor = GridBagConstraints.NORTHWEST;
        txtMessageGBC.weightx = 0;
        txtMessageGBC.gridx = 1;
        txtMessageGBC.gridy = y;

        txt_Title = new JTextField();

        this.add(txt_Title, txtMessageGBC);

        // LABEL MESSAGE
        // GridBagConstraints labelMessageGBC = new GridBagConstraints();
        labelMessageGBC.insets = new Insets(5, 5, 5, 5);
        labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;
        labelMessageGBC.anchor = GridBagConstraints.NORTHWEST;
        labelMessageGBC.weightx = 0;
        labelMessageGBC.gridx = 0;
        labelMessageGBC.gridy = ++y;

        messageLabel = new JLabel(Lang.getInstance().translate("Message") + ":");

        // TXT MESSAGE
        // GridBagConstraints txtMessageGBC = new GridBagConstraints();
        txtMessageGBC.gridwidth = 4;
        txtMessageGBC.insets = new Insets(5, 5, 5, 0);
        txtMessageGBC.fill = GridBagConstraints.BOTH;
        txtMessageGBC.anchor = GridBagConstraints.NORTHWEST;
        txtMessageGBC.weightx = 0.1;
        txtMessageGBC.weightx = 0.1;
        txtMessageGBC.gridx = 1;
        txtMessageGBC.gridy = y;

        this.txtMessage = new JTextArea();
        this.txtMessage.setRows(4);
        this.txtMessage.setColumns(25);

        this.txtMessage.setBorder(this.txtTo.getBorder());

        JScrollPane messageScroll = new JScrollPane(this.txtMessage);
        messageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        messageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(messageScroll, txtMessageGBC);

        this.add(messageLabel, labelMessageGBC);

        // LABEL ISTEXT
        GridBagConstraints labelIsTextGBC = new GridBagConstraints();
        labelIsTextGBC.gridy = ++y;
        labelIsTextGBC.insets = new Insets(5, 5, 5, 5);
        labelIsTextGBC.fill = GridBagConstraints.HORIZONTAL;
        labelIsTextGBC.anchor = GridBagConstraints.NORTHWEST;
        labelIsTextGBC.weightx = 0;
        labelIsTextGBC.gridx = 0;

        final JLabel isTextLabel = new JLabel(Lang.getInstance().translate("Text Message") + ":");
        isTextLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        this.add(isTextLabel, labelIsTextGBC);

        // TEXT ISTEXT
        GridBagConstraints isChkTextGBC = new GridBagConstraints();
        isChkTextGBC.insets = new Insets(5, 5, 5, 5);
        isChkTextGBC.fill = GridBagConstraints.BOTH;
        isChkTextGBC.anchor = GridBagConstraints.NORTHWEST;
        isChkTextGBC.weightx = 0.2;
        isChkTextGBC.weighty = 0.2;
        isChkTextGBC.gridx = 1;
        isChkTextGBC.gridy = y;

        isText = new JCheckBox();
        isText.setSelected(true);
        this.add(isText, isChkTextGBC);

        // LABEL ENCRYPTED
        GridBagConstraints labelEncGBC = new GridBagConstraints();
        labelEncGBC.insets = new Insets(5, 5, 5, 5);
        labelEncGBC.fill = GridBagConstraints.HORIZONTAL;
        labelEncGBC.anchor = GridBagConstraints.NORTHWEST;
        labelEncGBC.weightx = 0;
        labelEncGBC.gridx = 4;
        labelEncGBC.gridx = 2;
        labelEncGBC.gridy = y;

        JLabel encLabel = new JLabel(Lang.getInstance().translate("Encrypt message") + ":");
        encLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        this.add(encLabel, labelEncGBC);

        // ENCRYPTED CHECKBOX
        GridBagConstraints ChkEncGBC = new GridBagConstraints();
        ChkEncGBC.insets = new Insets(5, 5, 5, 5);
        ChkEncGBC.fill = GridBagConstraints.HORIZONTAL;
        ChkEncGBC.anchor = GridBagConstraints.NORTHWEST;
        ChkEncGBC.weightx = 0;
        ChkEncGBC.gridx = 3;
        ChkEncGBC.gridy = y;

        encryptedCHcKBox = new JCheckBox();
        encryptedCHcKBox.setSelected(true);
        this.add(encryptedCHcKBox, ChkEncGBC);

        // coin TITLE
        GridBagConstraints labecoin = new GridBagConstraints();
        labecoin.insets = new Insets(5, 5, 5, 5);
        labecoin.fill = GridBagConstraints.HORIZONTAL;
        labecoin.anchor = GridBagConstraints.NORTHWEST;
        labecoin.weightx = 0;
        labecoin.gridx = 0;
        labecoin.gridy = ++y;

        JLabel coin_Label = new JLabel(Lang.getInstance().translate("Asset") + ":");
        coin_Label.setHorizontalAlignment(SwingConstants.RIGHT);
        this.add(coin_Label, labecoin);

        // TXT TITLE
        GridBagConstraints txtCoin = new GridBagConstraints();
        txtCoin.gridwidth = 4;
        txtCoin.insets = new Insets(5, 5, 5, 0);
        txtCoin.fill = GridBagConstraints.HORIZONTAL;
        txtCoin.anchor = GridBagConstraints.NORTHWEST;
        txtCoin.weightx = 0;
        txtCoin.gridx = 1;
        txtCoin.gridy = y;

        this.add(cbxFavorites, txtCoin);

        // LABEL AMOUNT
        GridBagConstraints amountlabelGBC = new GridBagConstraints();
        amountlabelGBC.insets = new Insets(5, 5, 5, 5);
        amountlabelGBC.fill = GridBagConstraints.HORIZONTAL;
        amountlabelGBC.anchor = GridBagConstraints.NORTHWEST;
        amountlabelGBC.weightx = 0;
        amountlabelGBC.gridx = 0;
        amountlabelGBC.gridy = ++y;

        final JLabel amountLabel = new JLabel(Lang.getInstance().translate("Amount") + ":");
        amountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        this.add(amountLabel, amountlabelGBC);

        // TXT AMOUNT
        GridBagConstraints txtAmountGBC = new GridBagConstraints();
        txtAmountGBC.insets = new Insets(5, 5, 5, 5);
        txtAmountGBC.fill = GridBagConstraints.HORIZONTAL;
        txtAmountGBC.anchor = GridBagConstraints.NORTHWEST;
        txtAmountGBC.weightx = 0.4;
        txtAmountGBC.gridx = 1;
        txtAmountGBC.gridy = y;

      
        
     //   format.setRoundingMode(RoundingMode.HALF_UP);
        
        
       
        txtAmount = new M_DecimalFormatedTextField();
        int scale = 8;
        if(asset!=null)scale = asset.getScale();
        txtAmount.setScale(scale);
        
     //   txtAmount = new JTextField("0.00000000");
        // txtAmount.setPreferredSize(new Dimension(130,22));
        this.add(txtAmount, txtAmountGBC);

        // LABEL GBC
        GridBagConstraints feelabelGBC = new GridBagConstraints();
        feelabelGBC.anchor = GridBagConstraints.NORTHWEST;
        feelabelGBC.gridy = y;
        feelabelGBC.insets = new Insets(5, 5, 5, 5);
        feelabelGBC.fill = GridBagConstraints.BOTH;
        feelabelGBC.weightx = 0;
        feelabelGBC.gridx = 2;
        final JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee level") + ":");
        feeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        feeLabel.setVerticalAlignment(SwingConstants.TOP);
        this.add(feeLabel, feelabelGBC);

        // FEE TXT
        GridBagConstraints feetxtGBC = new GridBagConstraints();
        feetxtGBC.fill = GridBagConstraints.BOTH;
        feetxtGBC.insets = new Insets(5, 5, 5, 5);
        feetxtGBC.anchor = GridBagConstraints.NORTH;
        feetxtGBC.gridx = 3;
        feetxtGBC.gridy = y;
        txtFeePow = new JComboBox();//new M_DecimalFormatedTextField();
        txtFeePow.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }));
        txtFeePow.setSelectedIndex(0);
        
        
        // txtFeePow.setPreferredSize(new Dimension(130,22));
        this.add(txtFeePow, feetxtGBC);

        // BUTTON SEND
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(5, 5, 5, 5);
        buttonGBC.fill = GridBagConstraints.BOTH;
        buttonGBC.anchor = GridBagConstraints.PAGE_START;
        buttonGBC.gridx = 3;
        buttonGBC.gridy = ++y;

        sendButton = new JButton(Lang.getInstance().translate("Send"));
        // sendButton.setPreferredSize(new Dimension(80, 25));

        this.add(sendButton, buttonGBC);

        // CONTEXT MENU
        MenuPopupUtil.installContextMenu(txtTo);
        MenuPopupUtil.installContextMenu(txtAmount);
        MenuPopupUtil.installContextMenu(txtMessage);
        MenuPopupUtil.installContextMenu(txtRecDetails);
        /*
         * ScheduledExecutorService service =
         * Executors.newSingleThreadScheduledExecutor();
         * service.scheduleWithFixedDelay( new Runnable() { public void run() {
         *
         * messageLabel.setText("<html>" +
         * Lang.getInstance().translate("Message") + ":<br>("+
         * txtMessage.getText().length()+")</html>");
         *
         * }}, 0, 500, TimeUnit.MILLISECONDS);
         *
         */
        /*
         * this.pack(); this.setLocationRelativeTo(null);
         * this.setMaximizable(true);
         * this.setTitle(Lang.getInstance().translate("Persons"));
         * this.setClosable(true); this.setResizable(true);
         */

        // Container parent = this.getParent();
        // this.setSize(new Dimension(
        // (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
        // this.setLocation(20, 20);
        // this.setIconImages(icons);

        // CLOSE
        // setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        // this.setResizable(true);
        // splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
        // this.setVisible(true);

        // favorite combo box
        cbxFavorites.setModel(new AssetsComboBoxModel());
        if (asset != null) {
            for (int i = 0; i < cbxFavorites.getItemCount(); i++) {
                AssetCls item = cbxFavorites.getItemAt(i);
                if (item.getKey() == asset.getKey()) {
                    // not worked cbxFavorites.setSelectedItem(asset);
                    cbxFavorites.setSelectedIndex(i);
                    cbxFavorites.setEnabled(false);// .setEditable(false);
                    break;
                } else {
                    cbxFavorites.setEnabled(true);
                }
            }
        }

        // accoutn ComboBox
        this.accountsModel = new AccountsComboBoxModel();
        this.cbxFrom.setModel(accountsModel);
        this.cbxFrom.setRenderer(new AccountRenderer(0));
        ((AccountRenderer) cbxFrom.getRenderer()).setAsset(((AssetCls) cbxFavorites.getSelectedItem()).getKey());
        if (account != null) cbxFrom.setSelectedItem(account);

        //ON FAVORITES CHANGE

        cbxFavorites.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

               asset = ((AssetCls) cbxFavorites.getSelectedItem());

                if (asset != null) {
                    ((AccountRenderer) cbxFrom.getRenderer()).setAsset(asset.getKey());
                    cbxFrom.repaint();
                 // set image
                    setImage();
                    icon.repaint();
                 // set scale
                    int scale = 8;
                    if(asset!=null)scale = asset.getScale();
                    txtAmount.setScale(scale);
                    
                }

            }
        });

        
        // default set asset
        if (asset == null) asset = ((AssetCls) cbxFavorites.getSelectedItem());
        
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSendClick();
            }
        });
        
       // set image asset
        setImage();
       // set acoount TO
       if( recipient != null){
           txtTo.setText(recipient.getAddress());
           
       }
      //  person_To = person;
    }
    
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

                max_Widht = 350;
                double k = ((double) x / (double) max_Widht);
                max_Height = (int) (max_Height / k);


                if (max_Height != 0) {
                    Im = image.getImage().getScaledInstance(max_Widht, max_Height, 1);
                    ImageIcon ic = new ImageIcon(Im);
                    icon.setIcon(ic);
                    icon.setSize(ic.getIconWidth(), ic.getIconHeight());
                }


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }
        // if era then file system icon
       if(asset.getKey()== 1l){
            icon.setIcon(new ImageIcon("images/icons/icon64.png"));
            return;
       }
       icon.setIcon(new ImageIcon(defaultImagePath));

        
    }

    private void refreshReceiverDetails() {
        String toValue = txtTo.getText();
        AssetCls asset = ((AssetCls) cbxFavorites.getSelectedItem());

        txtRecDetails.setText(Account.getDetails(toValue, asset));

        if (false && toValue != null && toValue.startsWith(wrongFirstCharOfAddress)) {
            encryptedCHcKBox.setEnabled(false);
            encryptedCHcKBox.setSelected(false);
            isText.setSelected(false);
        } else {
            encryptedCHcKBox.setEnabled(true);
        }
    }
     public boolean cheskError(){
         this.sendButton.setEnabled(false);
         //TODO TEST
         //CHECK IF NETWORK OK
         /*if(Controller.getInstance().getStatus() != Controller.STATUS_OKE)
         {
             //NETWORK NOT OK
             JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);

             //ENABLE
             this.sendButton.setEnabled(true);

             return;
         }*/

         //READ SENDER
         sender = (Account) cbxFrom.getSelectedItem();
         //CHECK IF WALLET UNLOCKED
         if (!Controller.getInstance().isWalletUnlocked()) {
             //ASK FOR PASSWORD
             String password = PasswordPane.showUnlockWalletDialog(this);
             if (password.equals("")) {
                 this.sendButton.setEnabled(true);
                 return false;
             }
             if (!Controller.getInstance().unlockWallet(password)) {
                 //WRONG PASSWORD
                 JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                 //ENABLE
                 this.sendButton.setEnabled(true);
                 return false;
             }
         }

         //READ RECIPIENT
         String recipientAddress = txtTo.getText();

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
                 this.sendButton.setEnabled(true);
                 return false;
             }
         }

         int parsing = 0;
        
        
         try {
             //READ AMOUNT
             parsing = 1;
             amount = new BigDecimal(txtAmount.getText());

             //READ FEE
             parsing = 2;
             feePow = Integer.parseInt((String)txtFeePow.getSelectedItem());
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
             this.sendButton.setEnabled(true);
             return false;
         }

         if (amount.equals(new BigDecimal("0.0"))) {
             JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Amount must be greater 0.0"), Lang.getInstance().translate("Error") + ":  " + Lang.getInstance().translate("Invalid amount!"), JOptionPane.ERROR_MESSAGE);

             //ENABLE
             this.sendButton.setEnabled(true);
             return false;
         }

         this.message = txtMessage.getText();

        isTextB = isText.isSelected();

        

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
                         this.sendButton.setEnabled(true);
                         return false;
                     }
                 }
             }
         }
         // if no TEXT - set null
         if (messageBytes != null && messageBytes.length == 0) messageBytes = null;
         // if amount = 0 - set null
         if (amount.compareTo(BigDecimal.ZERO) == 0) amount = null;

         boolean encryptMessage = encryptedCHcKBox.isSelected();

         encrypted = (encryptMessage) ? new byte[]{1} : new byte[]{0};
         isTextByte = (isTextB) ? new byte[]{1} : new byte[]{0};

        
         if (amount != null) {
             //CHECK IF PAYMENT OR ASSET TRANSFER
             asset = (AssetCls) this.cbxFavorites.getSelectedItem();
             key = asset.getKey();
         }

         if (messageBytes != null) {
             if (messageBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
                 JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message size exceeded!") + " <= MAX", Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

                 //ENABLE
                 this.sendButton.setEnabled(true);
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
                     this.sendButton.setEnabled(true);

                     return false;
                 }

                 messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
             }
         }
        head = this.txt_Title.getText();
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
                 this.txtAmount.setText("0");
             }

             // TODO "A" ??
             if (false && this.txtTo.getText().startsWith(wrongFirstCharOfAddress)) {
                 this.txtTo.setText("");
             }

             this.txtMessage.setText("");

             // TODO "A" ??
             if (true || this.txtTo.getText().startsWith(wrongFirstCharOfAddress)) {
                 JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message and/or payment has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
             }
         } else {
             JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
         }
     
    
     }
     public void onSendClick(){
         
     }
}
