package org.erachain.gui.models;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.datachain.DCSet;
import org.erachain.gui.PasswordPane;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.TableMenuPopupUtil;
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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

@SuppressWarnings("serial")
public class SendTableModel extends JTable implements Observer {


    private static final Logger LOGGER = LoggerFactory.getLogger(SendTableModel.class);

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

    public SendTableModel() {
        this.setShowGrid(false);

        fontHeight = this.getFontMetrics(this.getFont()).getHeight();

        messageBufs = new ArrayList<MessageBuf>();
        messagesModel = new DefaultTableModel();
        this.setModel(messagesModel);
        messagesModel.addColumn("");

        DefaultTableCellRenderer topRenderer = new DefaultTableCellRenderer();
        topRenderer.setVerticalAlignment(DefaultTableCellRenderer.TOP);
        this.getColumn("").setCellRenderer(topRenderer);

        List<Transaction> transactions = new ArrayList<Transaction>();

        for (Transaction transaction : Controller.getInstance().getUnconfirmedTransactions(1000, true)) {
            if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
                transactions.add(transaction);
            }
        }

        for (Account account : Controller.getInstance().getAccounts()) {
            transactions.addAll(DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressAndType(account.getShortAddressBytes(), Transaction.SEND_ASSET_TRANSACTION, 0, 0));
        }

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


        //MENU
        JPopupMenu menu = new JPopupMenu();

        JMenuItem copyMessage = new JMenuItem(Lang.getInstance().translate("Copy Message"));
        copyMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem menuItem = (JMenuItem) e.getSource();
                JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
                Component invoker = popupMenu.getInvoker(); //this is the JMenu (in my code)
                SendTableModel invokerAsJComponent = (SendTableModel) invoker;

                int row = invokerAsJComponent.getSelectedRow();
                row = invokerAsJComponent.convertRowIndexToModel(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(messageBufs.get(row).getDecrMessageTXT());
                clipboard.setContents(value, null);
            }
        });
        menu.add(copyMessage);

        JMenuItem copyAllMessages = new JMenuItem(Lang.getInstance().translate("Copy All Messages"));
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

        JMenuItem copySender = new JMenuItem(Lang.getInstance().translate("Copy Sender Account"));
        copySender.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem menuItem = (JMenuItem) e.getSource();
                JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
                Component invoker = popupMenu.getInvoker();
                SendTableModel invokerAsJComponent = (SendTableModel) invoker;

                int row = invokerAsJComponent.getSelectedRow();
                row = invokerAsJComponent.convertRowIndexToModel(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(messageBufs.get(row).getSender().getAddress());
                clipboard.setContents(value, null);
            }
        });
        menu.add(copySender);

        JMenuItem copyRecipient = new JMenuItem(Lang.getInstance().translate("Copy Recipient Account"));
        copyRecipient.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem menuItem = (JMenuItem) e.getSource();
                JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
                Component invoker = popupMenu.getInvoker();
                SendTableModel invokerAsJComponent = (SendTableModel) invoker;

                int row = invokerAsJComponent.getSelectedRow();
                row = invokerAsJComponent.convertRowIndexToModel(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(messageBufs.get(row).getRecipient().getAddress());
                clipboard.setContents(value, null);
            }
        });

        menu.add(copyRecipient);

        menuDecrypt = new JMenuItem(Lang.getInstance().translate("Decrypt"));
        menuDecrypt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem menuItem = (JMenuItem) e.getSource();
                JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
                Component invoker = popupMenu.getInvoker();
                SendTableModel invokerAsJComponent = (SendTableModel) invoker;

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
                    SendTableModel tableModelparent = (SendTableModel) e.getSource();

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
                        menuDecrypt.setText(Lang.getInstance().translate("Hide decrypted"));
                    } else {
                        menuDecrypt.setText(Lang.getInstance().translate("Decrypt"));
                    }
                }
            }
        });

        Controller.getInstance().addWalletObserver(this);
        Controller.getInstance().addObserver(this);
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
        return messageBufs.get(row).getDecrMessageHtml(this.getWidth(), (this.getSelectedRow() == row), true);
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {

        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.WALLET_STATUS) {
            int status = (int) message.getValue();

            if (status == Wallet.STATUS_LOCKED) {
                cryptoCloseAll();
            }
        }

        if (message.getType() == ObserverMessage.NETWORK_STATUS || (int) message.getValue() == Controller.STATUS_OK) {
            this.repaint();
        }

        if (message.getType() == ObserverMessage.WALLET_LIST_BLOCK_TYPE) {
            if (Controller.getInstance().getStatus() == Controller.STATUS_OK) {

                this.repaint();
            }
        }

        if (message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE) {
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
                    
                    Transaction messagetx = (Transaction)message.getValue();
                    messagetx.setDC(DCSet.getInstance(), Transaction.FOR_NETWORK, DCSet.getInstance().getBlockMap().size() + 1, 1, true);

                    addMessage(0, (RSend) messagetx);

                    messagesModel.setRowCount(messageBufs.size());

                    for (int j = messageBufs.size() - 1; j >= 0; j--) {
                        setHeight(j);
                    }

                    if (messageBufs.get(1).getOpend() && Controller.getInstance().isWalletUnlocked()) {
                        CryptoOpenBox(0, 1);
                    }

                    this.repaint();
                }
            }
        }
    }

    private void addMessage(int pos, RSend transaction) {
        messageBufs.add(pos, new MessageBuf(
                // TODO use viewData instead - use transaction instead buffer
                transaction.getData(),
                transaction.isEncrypted(),
                transaction.getCreator(), //.asPerson(),
                transaction.getRecipient(), //.asPerson(),
                transaction.getTimestamp(),
                transaction.getAmount(),
                transaction.getKey(),
                transaction.getFee(),
                transaction.getSignature(),
                transaction.getCreator().getPublicKey(),
                transaction.isText()
        ));
    }

    public void cryptoCloseAll() {
        for (int i = 0; i < messageBufs.size(); i++) {
            CryptoOpenBox(i, 2);
        }

        menuDecrypt.setText(Lang.getInstance().translate("Decrypt"));
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
                        JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                        return;
                    }
                }

                byte[] decryptedData = Controller.getInstance().decrypt(messageBufs.get(row).sender,
                        messageBufs.get(row).recipient, messageBufs.get(row).rawMessage);

                if (decryptedData == null) {
                    messageBufs.get(row).setDecryptedMessage(Lang.getInstance().translate("Decrypt Error!"));
                } else {
                    messageBufs.get(row).setDecryptedMessage((messageBufs.get(row).isText()) ? new String(decryptedData, StandardCharsets.UTF_8) :
                            Base58.encode(decryptedData)); //Converter.toHex(decryptedData));
                    messageBufs.get(row).setOpend(true);
                    menuDecrypt.setText(Lang.getInstance().translate("Hide decrypted"));
                }

            } else {
                if (toOpen != 1) {
                    messageBufs.get(row).setDecryptedMessage("");
                    messageBufs.get(row).setOpend(false);
                    menuDecrypt.setText(Lang.getInstance().translate("Decrypt"));
                }
            }


            setHeight(row);
        }
    }

    private void setHeight(int row) {
        int textHeight = (3 + lineCount(messageBufs.get(row).getDecrMessage())) * fontHeight;
        if (textHeight < 24 + 3 * fontHeight) {
            textHeight = 24 + 3 * fontHeight;
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
        private BigDecimal fee;
        private byte[] signature;

        public MessageBuf(byte[] rawMessage, boolean encrypted, PublicKeyAccount sender, Account recipient, long timestamp, BigDecimal amount, long assetKey, BigDecimal fee, byte[] signature, byte[] senderPublicKey, boolean isText) {
            this.rawMessage = rawMessage;
            this.encrypted = encrypted;
            this.decryptedMessage = "";
            this.opened = false;
            this.sender = sender;
            this.recipient = recipient;
            this.timestamp = timestamp;
            this.amount = amount;
            this.assetKey = assetKey;
            this.fee = fee;
            this.senderPublicKey = senderPublicKey;
            this.recipientPublicKey = null;
            this.signature = signature;
            this.isText = isText;
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
            Account account = this.sender;
            String imginout = "";
            if (account != null) {
                imginout = "<img src='file:images/messages/receive.png'>";
            } else {
                imginout = "<img src='file:images/messages/send.png'>";
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

            String colorHeader = "F0F0F0";
            String colorTextHeader = "000000";
            String colorTextMessage = "000000";
            String colorTextBackground = "FFFFFF";


            if (selected) {
                colorHeader = "C4DAEF";
                colorTextBackground = "D1E8FF";
            }

            if (this.encrypted) {
                if (this.opened) {
                    colorTextMessage = "0000FF";
                } else {
                    colorTextMessage = "FF0000";
                }
            }

            String decrMessage = this.getDecrMessage();
            decrMessage = decrMessage.replace("<", "&lt;");
            decrMessage = decrMessage.replace(">", "&gt;");
            decrMessage = decrMessage.replace("\n", "<br>");

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

                String send_type;
                if (key < 0) {
                    send_type = Lang.getInstance().translate("DEBT");
                } else {
                    if (amo_sign < 0) {
                        send_type = Lang.getInstance().translate("HOLD");
                    } else {
                        send_type = Lang.getInstance().translate("PAY");
                    }
                }
                amountStr = "<font" + fontSize + ">" + send_type + " "
                        //+ Lang.getInstance().translate("Amount") + ": "
                        + NumberAsString.formatAsString(this.amount) + "</font>"
                        + " " + Controller.getInstance().getAsset(this.getAbsAssetKey()).getShort(DCSet.getInstance());
            }


            return "<html>\n"
                    + "<body width='" + width + "'>\n"
                    + "<table border='0' cellpadding='3' cellspacing='0'><tr>\n<td bgcolor='" + colorHeader + "' width='" + (width / 2 - 1) + "'>\n"
                    + "<font size='2' color='" + colorTextHeader + "'>\n" + Lang.getInstance().translate("From") + ":" + this.sender
                    + "\n<br>\n" + Lang.getInstance().translate("To") + ": "
                    + this.recipient + "\n</font></td>\n"
                    + "<td bgcolor='" + colorHeader + "' align='right' width='" + (width / 2 - 1) + "'>\n"
                    + "<font size='2' color='" + colorTextHeader + "'>\n" + strconfirmations + " . "
                    + DateTimeFormat.timestamptoString(this.timestamp)
                    + " " + Lang.getInstance().translate("Fee") + ": "
                    + NumberAsString.formatAsString(fee)
                    + "<br></font>\n"
                    + amountStr
                    + "</td></tr></table>"
                    + "<table border='0' cellpadding='3' cellspacing='0'>\n<tr bgcolor='" + colorTextBackground + "'><td width='25'>" + imginout
                    + "<td width='" + width + "'>\n"
                    + "<font size='2.5' color='" + colorTextMessage + "'>\n"
                    + decrMessage
                    + "\n</font>"
                    + "<td width='30'>" + imgLock
                    + "</td></tr>\n</table>\n"
                    + "</body></html>\n";
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
                    imgLock = Lang.getInstance().translate("Decrypted");
                } else {
                    imgLock = Lang.getInstance().translate("Encrypted");
                }
            } else {
                imgLock = Lang.getInstance().translate("Unencrypted");
            }

            int confirmations = this.getConfirmations();

            String strConfirmations = Integer.toString(confirmations);

            if (confirmations < 1) {
                strConfirmations = strConfirmations + " !";
            }

            int amo_sign = this.amount.compareTo(BigDecimal.ZERO);

            String send_type;
            if (this.getAssetKey() < 0 && amo_sign > 0) {
                send_type = Lang.getInstance().translate("debt");
            } else if (this.getAssetKey() > 0 && amo_sign < 0) {
                send_type = Lang.getInstance().translate("hold");
            } else if (this.getAssetKey() < 0 && amo_sign < 0) {
                send_type = Lang.getInstance().translate("spend");
            } else {
                send_type = Lang.getInstance().translate("pay");
            }

            String strAsset = Controller.getInstance().getAsset(this.getAbsAssetKey()).getShort();

            return Lang.getInstance().translate("Date") + ": " + DateTimeFormat.timestamptoString(this.timestamp) + "\n"
                    + send_type + "\n"
                    + Lang.getInstance().translate("Sender") + ": " + this.sender + "\n"
                    + Lang.getInstance().translate("Recipient") + ": " + this.recipient + "\n"
                    + Lang.getInstance().translate("Amount") + ": " + NumberAsString.formatAsString(this.amount) + " " + strAsset + " . " + Lang.getInstance().translate("Fee") + ": " + NumberAsString.formatAsString(this.fee) + "\n"
                    + Lang.getInstance().translate("Type") + ": " + imginout + ". " + imgLock + "\n"
                    + Lang.getInstance().translate("Confirmations") + ": " + strConfirmations + "\n"
                    + Lang.getInstance().translate("[MESSAGE START]\n")
                    + getDecrMessage() + "\n"
                    + Lang.getInstance().translate("[MESSAGE END]\n");
        }
    }

}

