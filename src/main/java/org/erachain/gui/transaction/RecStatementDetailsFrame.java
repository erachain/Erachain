package org.erachain.gui.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.RSignNote;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("serial")
public class RecStatementDetailsFrame extends RecDetailsFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(Send_RecordDetailsFrame.class);
    private JTextPane messageText;
    private RecStatementDetailsFrame th;

    public RecStatementDetailsFrame(final RSignNote r_Statement) {
        super(r_Statement);
        th = this;
        if (r_Statement.getKey() > 0) {
            ++labelGBC.gridy;
            ++detailGBC.gridy;
            detailGBC.gridx = 1;
            detailGBC.gridwidth = 3;
            JTextField template = new JTextField(Controller.getInstance().getTemplate(r_Statement.getKey()).toString());
            template.setEditable(false);
            MenuPopupUtil.installContextMenu(template);
            this.add(template, detailGBC);
        }

        if (r_Statement.getData() != null) {
            //LABEL MESSAGE
            ++labelGBC.gridy;
            JLabel serviceLabel = new JLabel(Lang.getInstance().translate("Message") + ":");
            this.add(serviceLabel, labelGBC);

            // ISTEXT
            ++detailGBC.gridy;
            detailGBC.gridwidth = 2;
            messageText = new JTextPane();
            messageText.setContentType("text/html");
            String ss = ((r_Statement.isText()) ? Library.viewDescriptionHTML(new String(r_Statement.getData(), StandardCharsets.UTF_8)) :
                    Base58.encode(r_Statement.getData())); //Converter.toHex(r_Statement.getData()));
            messageText.setEditable(false);
            //messageText.setSize(200, 300);
            //messageText.setPreferredSize(new Dimension(800,200));
            MenuPopupUtil.installContextMenu(messageText);


            ss = "<div  style='word-wrap: break-word;'>" + ss;

            messageText.setText(ss);

            JScrollPane scrol = new JScrollPane();


            //	scrol.setPreferredSize(new Dimension(800,300));
            int rr = (int) (getFontMetrics(UIManager.getFont("Table.font")).stringWidth(this.signature.getText()));

            scrol.setPreferredSize(new Dimension(rr, 300));
            scrol.setViewportView(messageText);
            detailGBC.fill = GridBagConstraints.NONE;


            this.add(scrol, detailGBC);


            detailGBC.gridwidth = 3;

            //ENCRYPTED CHECKBOX

            //ENCRYPTED
            GridBagConstraints chcGBC = new GridBagConstraints();
            chcGBC.fill = GridBagConstraints.HORIZONTAL;
            chcGBC.anchor = GridBagConstraints.NORTHWEST;
            chcGBC.gridy = ++labelGBC.gridy;
            chcGBC.gridx = 2;
            chcGBC.gridwidth = 1;
            final JCheckBox encrypted = new JCheckBox(Lang.getInstance().translate("Encrypted"));

            encrypted.setSelected(r_Statement.isEncrypted());
            encrypted.setEnabled(r_Statement.isEncrypted());

            this.add(encrypted, chcGBC);

            encrypted.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!encrypted.isSelected()) {
                        if (!Controller.getInstance().isWalletUnlocked()) {
                            //ASK FOR PASSWORD
                            String password = PasswordPane.showUnlockWalletDialog(th);
                            if (!Controller.getInstance().unlockWallet(password)) {
                                //WRONG PASSWORD
                                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                                encrypted.setSelected(!encrypted.isSelected());

                                return;
                            }
                        }

                        byte[] decryptedData = Controller.getInstance().decrypt(r_Statement.getCreator(),
                                (Account) r_Statement.getRecipientAccounts().toArray()[0], r_Statement.getData());

                        if (decryptedData == null) {
                            messageText.setText(Lang.getInstance().translate("Decrypt Error!"));
                        } else {
                            messageText.setText(r_Statement.isText() ?
                                    new String(decryptedData, StandardCharsets.UTF_8)
                                    : Base58.encode(decryptedData)); //Converter.toHex(decryptedData));

                        }

                    }
                    //encrypted.isSelected();

                }
            });
        }

        //PACK

        //    this.setResizable(false);
        //    this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
