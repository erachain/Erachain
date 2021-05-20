package org.erachain.gui.items.mails;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.wallet.DWSet;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.TableMenuPopupUtil;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

@SuppressWarnings("serial")
public class MailsHTMLTableModel extends JTable implements Observer {

    public static boolean markIncome = Settings.getInstance().markIncome();
    public final static Color FORE_COLOR = Settings.getInstance().markColorObj();
    public final static Color FORE_COLOR_SELECTED = Settings.getInstance().markColorSelectedObj();

    private static final Logger LOGGER = LoggerFactory.getLogger(MailsHTMLTableModel.class);

    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();

    private boolean needUpdate;

    Comparator<MessageBuf> comparator = new Comparator<MessageBuf>() {
        public int compare(MessageBuf c1, MessageBuf c2) {
            long diff = c2.getTimestamp() - c1.getTimestamp();
            if (diff > 0)
                return 1;
            if (diff < 0)
                return -1;
            return 0;
        }
    };
    JMenuItem menuDecrypt;
    int width;
    int fontHeight;
    private ArrayList<MessageBuf> messageBufs;
    private DefaultTableModel messagesModel;

    DCSet dcSet = DCSet.getInstance();
    Wallet wallet = Controller.getInstance().getWallet();
    DWSet dwSet = wallet.database;
    WTransactionMap tableMap = dwSet.getTransactionMap();
    private Account myAccountFilter;
    private Account sideAccountFilter;

    public MailsHTMLTableModel(MailSendPanel parent, Account myAccountFilter) {
        this.setShowGrid(false);

        fontHeight = this.getFontMetrics(this.getFont()).getHeight();

        messagesModel = new DefaultTableModel();
        this.myAccountFilter = myAccountFilter;

        this.setModel(messagesModel);
        messagesModel.addColumn("");

        DefaultTableCellRenderer topRenderer = new DefaultTableCellRenderer();
        topRenderer.setVerticalAlignment(DefaultTableCellRenderer.TOP);
        this.getColumn("").setCellRenderer(topRenderer);

        //MENU
        JPopupMenu menu = new JPopupMenu();

        JMenuItem seeDetails = new JMenuItem(Lang.T("See Details"));
        seeDetails.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem menuItem = (JMenuItem) e.getSource();
                JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
                Component invoker = popupMenu.getInvoker(); //this is the JMenu (in my code)
                MailsHTMLTableModel invokerAsJComponent = (MailsHTMLTableModel) invoker;

                int row = invokerAsJComponent.getSelectedRow();
                row = invokerAsJComponent.convertRowIndexToModel(row);

                IssueConfirmDialog dd = new IssueConfirmDialog(MainFrame.getInstance(), true, messageBufs.get(row).tx,
                        (int) (parent.getWidth() / 1.2), (int) (parent.getHeight() / 1.2), Lang.T("Transaction"));
                dd.setLocationRelativeTo(parent);
                dd.setVisible(true);

            }
        });
        menu.add(seeDetails);

        JMenuItem copyMessage = new JMenuItem(Lang.T("Copy Message"));
        copyMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem menuItem = (JMenuItem) e.getSource();
                JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
                Component invoker = popupMenu.getInvoker(); //this is the JMenu (in my code)
                MailsHTMLTableModel invokerAsJComponent = (MailsHTMLTableModel) invoker;

                int row = invokerAsJComponent.getSelectedRow();
                row = invokerAsJComponent.convertRowIndexToModel(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(messageBufs.get(row).getDecrMessageTXT());
                clipboard.setContents(value, null);
            }
        });
        menu.add(copyMessage);

        JMenuItem copyAllMessages = new JMenuItem(Lang.T("Copy All Messages"));
        copyAllMessages.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String strvalue = "";

                for (int i = 0; i < messageBufs.size(); i++) {
                    strvalue += messageBufs.get(i).getDecrMessageTXT() + "\n";
                }

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(strvalue);
                clipboard.setContents(value, null);

            }
        });
        menu.add(copyAllMessages);

        JMenuItem copySender = new JMenuItem(Lang.T("Copy Sender Account"));
        copySender.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem menuItem = (JMenuItem) e.getSource();
                JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
                Component invoker = popupMenu.getInvoker();
                MailsHTMLTableModel invokerAsJComponent = (MailsHTMLTableModel) invoker;

                int row = invokerAsJComponent.getSelectedRow();
                row = invokerAsJComponent.convertRowIndexToModel(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(messageBufs.get(row).getSender().getAddress());
                clipboard.setContents(value, null);
            }
        });
        menu.add(copySender);

        JMenuItem copyRecipient = new JMenuItem(Lang.T("Copy Recipient Account"));
        copyRecipient.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem menuItem = (JMenuItem) e.getSource();
                JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
                Component invoker = popupMenu.getInvoker();
                MailsHTMLTableModel invokerAsJComponent = (MailsHTMLTableModel) invoker;

                int row = invokerAsJComponent.getSelectedRow();
                row = invokerAsJComponent.convertRowIndexToModel(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(messageBufs.get(row).getRecipient().getAddress());
                clipboard.setContents(value, null);
            }
        });

        menu.add(copyRecipient);

        menuDecrypt = new JMenuItem(Lang.T("Decrypt"));
        menuDecrypt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem menuItem = (JMenuItem) e.getSource();
                JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
                Component invoker = popupMenu.getInvoker();
                MailsHTMLTableModel invokerAsJComponent = (MailsHTMLTableModel) invoker;

                int row = invokerAsJComponent.getSelectedRow();
                row = invokerAsJComponent.convertRowIndexToModel(row);

                CryptoOpenBox(row, 0);

                invokerAsJComponent.repaint();
            }

        });

        menu.add(menuDecrypt);

        TableMenuPopupUtil.installContextMenu(this, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON

        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    MailsHTMLTableModel tableModelparent = (MailsHTMLTableModel) e.getSource();

                    Point p = e.getPoint();
                    int row = tableModelparent.rowAtPoint(p);

                    CryptoOpenBox(row, 0);
                    tableModelparent.repaint();
                }
            }
        });


        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                int row = lsm.getMinSelectionIndex();

                if (row > -1) {
                    if (!messageBufs.get(row).getEncrypted()) {
                        menuDecrypt.setVisible(false);
                    } else {
                        menuDecrypt.setVisible(true);
                    }
                    if (messageBufs.get(row).getOpend()) {
                        menuDecrypt.setText(Lang.T("Hide decrypted"));
                    } else {
                        menuDecrypt.setText(Lang.T("Decrypt"));
                    }
                }
            }
        });

        resetItems();

        //LISTEN ON STATUS
        Controller.getInstance().addObserver(this);
        Controller.getInstance().addWalletObserver(this);
        tableMap.addObserver(this);
        Controller.getInstance().guiTimer.addObserver(this); // обработка repaintGUI
        dcSet.getBlockMap().addObserver(this); // for new blocks

    }


    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Object value = getValueAt(row, column);

        boolean isSelected = false;
        boolean hasFocus = false;

        // Only indicate the selection and focused cell if not printing
        if (!isPaintingForPrint()) {
            isSelected = isCellSelected(row, column);

            boolean rowIsLead = (selectionModel.getLeadSelectionIndex() == row);
            boolean colIsLead = (columnModel.getSelectionModel().getLeadSelectionIndex() == column);

            hasFocus = (rowIsLead && colIsLead) && isFocusOwner();
        }
        JComponent cellRenderer = (JComponent) renderer.getTableCellRendererComponent(this, value, isSelected,
                hasFocus, row, column);

        if (isSelected && hasFocus) {
            cellRenderer.setBorder(BorderFactory.createLineBorder(new Color(102, 167, 232, 255), 1));
        } else {
            cellRenderer.setBorder(BorderFactory.createLineBorder(new Color(205, 205, 205, 255), 1));
        }
        return cellRenderer;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        //all cells false
        return false;
    }


    @Override
    public Object getValueAt(int row, int column) {
        if (row < messageBufs.size()) {
            return messageBufs.get(row).getDecrMessageHtml(this.getWidth(), (this.getSelectedRow() == row), true);
        }
        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {

        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.WALLET_STATUS) {
            int status = (int) message.getValue();

            if (status == Wallet.STATUS_LOCKED) {
                cryptoCloseAll();
            }
        } else if (message.getType() == ObserverMessage.WALLET_SYNC_STATUS) {
            needUpdate = true;

        } else if (message.getType() == ObserverMessage.CHAIN_ADD_BLOCK_TYPE && Controller.getInstance().isStatusOK()) {
            repaintConfirms();

        } else if (message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK) {
            needUpdate = true;

        } else if (message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE) {
            boolean is;
            if (((Transaction) message.getValue()).getType() == Transaction.SEND_ASSET_TRANSACTION) {
                is = false;
                for (int i = messageBufs.size() - 1; i >= 0; i--)
                    for (MessageBuf messageBuf : messageBufs) {
                        if (Arrays.equals(((RSend) message.getValue()).getSignature(), messageBuf.getSignature())) {
                            is = true;
                            break;
                        }
                    }
                if (!is) {

                    Transaction messagetx = (Transaction) message.getValue();
                    messagetx.setDC(DCSet.getInstance(), false);

                    addMessage(0, (RSend) messagetx);

                    messagesModel.setRowCount(messageBufs.size());

                    for (int j = messageBufs.size() - 1; j >= 0; j--) {
                        setHeight(j);
                    }

                    if (!messageBufs.isEmpty() && messageBufs.get(0).getOpend() && Controller.getInstance().isWalletUnlocked()) {
                        CryptoOpenBox(0, 1);
                    }

                    this.repaint();
                }
            }
        } else if (message.getType() == ObserverMessage.GUI_REPAINT && needUpdate) {
            needUpdate = false;
            resetItems();
        }
    }

    private void repaintConfirms() {
        repaint();
    }

    private void addMessage(int pos, RSend transaction) {
        if (true || !transaction.hasAmount()) {
            messageBufs.add(pos, new MessageBuf(
                    // TODO use viewData instead - use transaction instead buffer
                    transaction.getTitle(),
                    transaction.getData(),
                    transaction.isEncrypted(),
                    transaction.getCreator(), //.asPerson(),
                    transaction.getRecipient(), //.asPerson(),
                    transaction.getTimestamp(),
                    transaction.getAmount(),
                    transaction.getKey(),
                    transaction.isBackward(),
                    transaction.getFee(),
                    transaction.getSignature(),
                    transaction.getCreator().getPublicKey(),
                    transaction.isText(),
                    transaction));
        }
    }

    public void cryptoCloseAll() {
        for (int i = 0; i < messageBufs.size(); i++) {
            CryptoOpenBox(i, 2);
        }

        menuDecrypt.setText(Lang.T("Decrypt"));
        this.repaint();
    }


    public void CryptoOpenBoxAll() {
        int toOpen = 0;
        if (messageBufs.size() > 0 && messageBufs.get(0).getOpend()) {
            toOpen = 2;
        } else {
            toOpen = 1;
        }

        for (int i = 0; i < messageBufs.size(); i++) {
            CryptoOpenBox(i, toOpen);
        }
    }

    private void CryptoOpenBox(int row, int toOpen) {
        // toOpen 0 - switch, 1 - open, 2 - close

        if (messageBufs.get(row).getEncrypted()) {
            if (toOpen != 2 && !messageBufs.get(row).getOpend()) {
                if (!Controller.getInstance().isWalletUnlocked()) {
                    //ASK FOR PASSWORD
                    String password = PasswordPane.showUnlockWalletDialog(this);
                    if (password.equals("")) {
                        return;
                    }
                    if (!Controller.getInstance().unlockWallet(password)) {
                        //WRONG PASSWORD
                        JOptionPane.showMessageDialog(null, Lang.T("Invalid password"), Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                        return;
                    }
                }

                byte[] decryptedData = Controller.getInstance().decrypt(messageBufs.get(row).sender,
                        messageBufs.get(row).recipient, messageBufs.get(row).rawMessage);

                if (decryptedData == null) {
                    messageBufs.get(row).setDecryptedMessage(Lang.T("Decrypt Error!"));
                } else {
                    messageBufs.get(row).setDecryptedMessage((messageBufs.get(row).isText()) ? new String(decryptedData, StandardCharsets.UTF_8) :
                            Base58.encode(decryptedData)); //Converter.toHex(decryptedData));
                    messageBufs.get(row).setOpend(true);
                    menuDecrypt.setText(Lang.T("Hide decrypted"));
                }

            } else {
                if (toOpen != 1) {
                    messageBufs.get(row).setDecryptedMessage("");
                    messageBufs.get(row).setOpend(false);
                    menuDecrypt.setText(Lang.T("Decrypt"));
                }
            }


            setHeight(row);
        }
    }

    private void setHeight(int row) {
        int lines = lineCount(messageBufs.get(row).getDecrMessage());
        if (lines > 5)
            lines = 5;
        int textHeight = (4 + lines) * fontHeight;
        if (textHeight < fontHeight + 4 * fontHeight) {
            textHeight = 24 + 4 * fontHeight;
        }
        this.setRowHeight(row, textHeight);
    }

    int lineCount(String text) {

        if (text == null) return 0;
        int lineCount = 1;

        for (int k = 0; k < text.length(); k++) {
            if (text.charAt(k) == '\n') {
                lineCount++;
            }
        }
        return lineCount;
    }

    public class MessageBuf {
        private String title;
        private byte[] rawMessage;
        private String decryptedMessage;
        private boolean encrypted;
        private boolean opened;
        private boolean isText;
        private PublicKeyAccount sender;
        private byte[] senderPublicKey;
        private Account recipient;
        private byte[] recipientPublicKey;
        private long timestamp;
        private BigDecimal amount;
        private long assetKey;
        private boolean backward;
        private BigDecimal fee;
        private byte[] signature;
        public final Transaction tx;
        private String img_Local_URL;
        private Image cachedImage;
        ImageIcon image = null;
        private int max_Widht;
        private int max_Height;


        public MessageBuf(String title, byte[] rawMessage, boolean encrypted, PublicKeyAccount sender, Account recipient, long timestamp, BigDecimal amount, long assetKey, boolean backward, BigDecimal fee, byte[] signature, byte[] senderPublicKey, boolean isText, Transaction transaction) {
            this.title = title;
            this.rawMessage = rawMessage;
            this.encrypted = encrypted;
            this.decryptedMessage = "";
            this.opened = false;
            this.sender = sender;
            this.recipient = recipient;
            this.timestamp = timestamp;
            this.amount = amount;
            this.assetKey = assetKey;
            this.backward = backward;
            this.fee = fee;
            this.senderPublicKey = senderPublicKey;
            this.recipientPublicKey = null;
            this.signature = signature;
            this.isText = isText;
            tx = transaction;
        }

        public byte[] getMessage() {
            return this.rawMessage;
        }

        public boolean getEncrypted() {
            return this.encrypted;
        }

        public String getDecrMessage() {
            if (this.rawMessage == null) return "";

            if (decryptedMessage.equals("")) {
                if (this.encrypted && !this.opened) {
                    this.decryptedMessage = "Encrypted";
                }
                if (!this.encrypted) {
                    this.decryptedMessage = (isText) ? new String(this.rawMessage, StandardCharsets.UTF_8) :
                            Base58.encode(this.rawMessage); //Converter.toHex(this.rawMessage);
                }
            }
            return this.decryptedMessage;
        }

        public Account getSender() {
            return this.sender;
        }

        public Account getRecipient() {
            return this.recipient;
        }

        public BigDecimal getFee() {
            return this.fee;
        }

        public BigDecimal getAmount() {
            return this.amount;
        }

        public long getAssetKey() {
            return this.assetKey;
        }

        public long getAbsAssetKey() {
            if (this.assetKey < 0)
                return -this.assetKey;

            return this.assetKey;
        }

        public byte[] getSignature() {
            return this.signature;
        }

        public byte[] getSenderPublicKey() {
            return this.senderPublicKey;
        }

        public byte[] getToPublicKey() {
            return this.recipientPublicKey;
        }

        public void setRecipientPublicKey(byte[] recipientPublicKey) {
            this.recipientPublicKey = recipientPublicKey;
        }

        public long getTimestamp() {
            return this.timestamp;
        }

        public boolean getOpend() {
            return this.opened;
        }

        public void setOpend(boolean opened) {
            this.opened = opened;
        }

        public void setDecryptedMessage(String decryptedMessage) {
            this.decryptedMessage = decryptedMessage;
        }

        public int getConfirmations() {

            if (DCSet.getInstance().getTransactionTab().contains(this.signature)) {
                return 0;
            } else {
                Transaction tx = Controller.getInstance().getTransaction(this.signature);
                if (tx != null) {
                    return tx.getConfirmations(DCSet.getInstance());
                } else {
                    return 0;
                }
            }

        }

        public boolean isText() {
            return isText;
        }

        public String getDecrMessageHtml(int width, boolean selected, boolean images) {

            Account sideAccount;
            String imginout = "";
            String sidePreff;
            String colorTextHeader;
            if (myAccountFilter != null) {
                if (this.sender != null && this.sender.equals(myAccountFilter)) {
                    imginout = "<img src='file:images/messages/send.png'>";
                    sideAccount = recipient;
                    sidePreff = "To";
                    colorTextHeader = markIncome ? Settings.colorToHex(Settings.getInstance().markColorObj()) : "";
                } else {
                    imginout = "<img src='file:images/messages/receive.png'>";
                    sideAccount = sender;
                    sidePreff = "From";
                    colorTextHeader = !markIncome ? Settings.colorToHex(Settings.getInstance().markColorObj()) : "";
                }
            } else {
                imginout = "";
                sidePreff = "To";
                sideAccount = recipient;
                colorTextHeader = "";
            }

            String imgLock = "";

            if (this.encrypted) {
                if (this.opened) {
                    imgLock = "<img src='file:images/messages/unlocked.png'>";
                } else {
                    imgLock = "<img src='file:images/messages/locked.png'>";
                }
            } else {
                imgLock = "<img src='file:images/messages/unlockedred.png'>";
            }

            int confirmations = this.getConfirmations();

            String strconfirmations = Integer.toString(confirmations);

            if (confirmations < 1) {
                strconfirmations = "<font color=red>" + strconfirmations + "</font>";
            }

            //String colorHeader = "F0F0F0";
            //String colorTextHeader = "000000";
            String colorTextMessage = "000000";
            //String colorTextBackground = "FFFFFF";


            if (selected) {
                //colorHeader = "C4DAEF";
                //colorHeader = Settings.getInstance().markColorSelectedObj().toString();
                //colorTextBackground = "D1E8FF";
                //colorTextBackground = Settings.getInstance().markColorSelectedObj().toString();
            }

            if (this.encrypted) {
                if (this.opened) {
                    colorTextMessage = "0000FF";
                } else {
                    colorTextMessage = "FF0000";
                }
            }

            String decrMessage = this.getDecrMessage();
            decrMessage = Library.to_HTML(decrMessage);

            String amountStr = "";

            if (this.amount != null) {

                int amo_big = this.amount.abs().intValue();
                String fontSize = "";
                if (amo_big > 1000) {
                    fontSize = " size='4'";
                } else if (amo_big > 10) {
                    fontSize = " size='3'";
                } else {
                    fontSize = " size='2'";
                }
                int amo_sign = this.amount.compareTo(BigDecimal.ZERO);
                long key = this.getAssetKey();

                AssetCls asset = Controller.getInstance().getAsset(this.getAbsAssetKey());
                byte[] iconBytes = asset.getIcon();
                if (false && iconBytes != null && iconBytes.length > 1) {
                    //if (asset.getKey() == 1l) image = new ImageIcon("images/icons/icon32.png");
                    image = new ImageIcon(iconBytes);
                    cachedImage = image.getImage().getScaledInstance(fontHeight, fontHeight, 1);
                    img_Local_URL = "http:\\img_" + assetKey;
                    // TODO нужно еще КЭШ картинок сделать как тут org.erachain.gui.items.assets.AssetInfo.HTML_Add_Local_Images

                }

                String actionName = tx.viewFullTypeName();
                amountStr = "<b><font size='3'>" + Lang.T(actionName) + " "
                        //+ Lang.T("Amount") + ": "
                        + NumberAsString.formatAsString(this.amount) + "</font> "
                        // TODO ошибка открытия
                        + (cachedImage == null ? "" : "<img src='" + img_Local_URL + "'>")
                        + " " + asset.toString()
                        + "</b>";
            }

            return "<html>"
                    + "<body width='" + width + "'>"
                    + "<table border='0' cellpadding='3' cellspacing='0'><tr><td" // bgcolor='" + colorHeader
                    + " width='" + (width / 2 - 1) + "'>"
                    + "<font size='2.5'" // color='" + colorTextHeader
                    + ">"
                    + imginout + " " + Lang.T(sidePreff) + ": " + sideAccount.viewPerson()
                    //+ imginout + " " + Lang.T("From") + ": " + sender.viewPerson()
                    //+ "<br>" + Lang.T("To") + ": " + recipient.viewPerson()
                    + "</font><br>"
                    + "<font size=1.5em color='" + colorTextHeader + "'><b>" + title
                    + "</b></font></td>"
                    + "<td" // bgcolor='" + colorHeader
                    + " align='right' width='" + (width / 2 - 1) + "'>"
                    + "<font size='2.5'" // color='" + colorTextHeader
                    + ">" + strconfirmations + " . "
                    + DateTimeFormat.timestamptoString(this.timestamp)
                    + " " + Lang.T("Fee") + ": "
                    + NumberAsString.formatAsString(fee)
                    + "<br></font>"
                    + amountStr
                    + "</td></tr></table>"
                    + "<table border='0' cellpadding='3' cellspacing='0'><tr" // bgcolor='" + colorTextBackground
                    + "><td width='25'>"
                    + "<td width='" + width + "'>"
                    + "<font size='2.5' color='" + colorTextMessage + "'>"
                    + decrMessage
                    + "</font>"
                    + "<td width='30'>" + imgLock
                    + "</td></tr></table>"
                    + "</body></html>";
        }

        public String getDecrMessageTXT() {
            Account account = this.sender;

            String imginout = "";
            if (account != null) {
                imginout = "Receive";
            } else {
                imginout = "Send'>";
            }

            String imgLock = "";

            if (this.encrypted) {
                if (this.opened) {
                    imgLock = Lang.T("Decrypted");
                } else {
                    imgLock = Lang.T("Encrypted");
                }
            } else {
                imgLock = Lang.T("Unencrypted");
            }

            int confirmations = this.getConfirmations();

            String strConfirmations = Integer.toString(confirmations);

            if (confirmations < 1) {
                strConfirmations = strConfirmations + " !";
            }

            int amo_sign = this.amount.compareTo(BigDecimal.ZERO);

            String send_type;
            if (this.getAssetKey() < 0 && amo_sign > 0) {
                send_type = Lang.T("debt");
            } else if (this.getAssetKey() > 0 && amo_sign < 0) {
                send_type = Lang.T("hold");
            } else if (this.getAssetKey() < 0 && amo_sign < 0) {
                send_type = Lang.T("spend");
            } else {
                send_type = Lang.T("pay");
            }

            String strAsset = Controller.getInstance().getAsset(this.getAbsAssetKey()).getShort();

            return Lang.T("Date") + ": " + DateTimeFormat.timestamptoString(this.timestamp) + "\n"
                    + send_type + "\n"
                    + Lang.T("Sender") + ": " + this.sender + "\n"
                    + Lang.T("Recipient") + ": " + this.recipient + "\n"
                    + Lang.T("Amount") + ": " + NumberAsString.formatAsString(this.amount) + " " + strAsset + " . " + Lang.T("Fee") + ": " + NumberAsString.formatAsString(this.fee) + "\n"
                    + Lang.T("Type") + ": " + imginout + ". " + imgLock + "\n"
                    + Lang.T("Confirmations") + ": " + strConfirmations + "\n"
                    + Lang.T("[MESSAGE START]\n")
                    + getDecrMessage() + "\n"
                    + Lang.T("[MESSAGE END]\n");
        }
    }

    /**
     * из кошелька только берем - там же и неподтвержденные
     */
    private void resetItems() {

        List<Transaction> transactions = new ArrayList<Transaction>();

        if (myAccountFilter == null) {
            // IN WALLET - UNCONFIRMED TOO HERE - ALL
            try (IteratorCloseable<Fun.Tuple2<Long, Integer>> iterator =
                         tableMap.getTypeIterator((byte) Transaction.SEND_ASSET_TRANSACTION, true)) {
                while (iterator.hasNext()) {
                    transactions.add(tableMap.get(iterator.next()));
                }
            } catch (IOException e) {
            }
        } else {
            if (this.sideAccountFilter == null) {
                // только на этот счет и любая сторона
                try (IteratorCloseable<Fun.Tuple2<Long, Integer>> iterator =
                             tableMap.getAddressTypeIterator(myAccountFilter, Transaction.SEND_ASSET_TRANSACTION, true)) {
                    while (iterator.hasNext()) {
                        transactions.add(tableMap.get(iterator.next()));
                    }
                } catch (IOException e) {
                }
            } else {
                // BOTH ACCOUNTS
                try (IteratorCloseable<Fun.Tuple2<Long, Integer>> iterator =
                             tableMap.getAddressTypeIterator(myAccountFilter, Transaction.SEND_ASSET_TRANSACTION, true)) {
                    while (iterator.hasNext()) {
                        Transaction transaction = tableMap.get(iterator.next());
                        if (transaction.isInvolved(sideAccountFilter))
                            transactions.add(transaction);
                    }
                } catch (IOException e) {
                }
            }
        }

        if (messageBufs == null)
            messageBufs = new ArrayList<MessageBuf>();
        else
            messageBufs.clear();

        for (Transaction messagetx : transactions) {
            messagetx.setDC(DCSet.getInstance(), true);

            boolean is = false;
            for (MessageBuf message : messageBufs) {
                if (Arrays.equals(messagetx.getSignature(), message.getSignature())) {
                    is = true;
                    break;
                }
            }
            if (!is) {
                addMessage(messageBufs.size(), (RSend) messagetx);
            }
        }

        Collections.sort(messageBufs, comparator);

        messagesModel.setRowCount(messageBufs.size());
        for (int j = messageBufs.size() - 1; j >= 0; j--) {
            setHeight(j);
        }

        this.repaint();

    }

    public synchronized void setMyAccount(Account accountFilter) {
        this.myAccountFilter = accountFilter;
        resetItems();
    }

    public synchronized void setSideAccount(Account accountFilter) {
        this.sideAccountFilter = accountFilter;
        resetItems();
    }
}

