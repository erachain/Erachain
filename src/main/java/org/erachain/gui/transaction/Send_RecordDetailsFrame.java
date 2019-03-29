package org.erachain.gui.transaction;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.transaction.RSend;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.library.MTextPane;
import org.erachain.gui.library.MAccoutnTextField;
import org.erachain.lang.Lang;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;

@SuppressWarnings("serial")
public class Send_RecordDetailsFrame extends RecDetailsFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(Send_RecordDetailsFrame.class);
    private JTextField messageText;
    private JScrollPane jScrollPane1;
    private MTextPane jTextArea_Messge;
    private Send_RecordDetailsFrame th;

    public Send_RecordDetailsFrame(final RSend r_Send) {
        super(r_Send);
        th = this;
        //LABEL RECIPIENT
        ++labelGBC.gridy;
        JLabel recipientLabel = new JLabel(Lang.getInstance().translate("Recipient") + ":");
        this.add(recipientLabel, labelGBC);

        //RECIPIENT
        ++detailGBC.gridy;
        MAccoutnTextField recipient = new MAccoutnTextField(r_Send.getRecipient());
        //	JTextField recipient = new JTextField(r_Send.getRecipient().getAddress());
        recipient.setEditable(false);
        //	MenuPopupUtil.installContextMenu(recipient);
        this.add(recipient, detailGBC);
		
	/*	String personStr = r_Send.getRecipient().viewPerson();
		if (personStr.length()>0) {
			++labelGBC.gridy;
			++detailGBC.gridy;
			this.add(new JLabel(personStr), detailGBC);
		}
		*/

        if (r_Send.getHead() != null) {
            //LABEL MESSAGE
            ++labelGBC.gridy;
            JLabel title_Label = new JLabel(Lang.getInstance().translate("Title") + ":");
            this.add(title_Label, labelGBC);

            // ISTEXT
            ++detailGBC.gridy;
            detailGBC.gridwidth = 2;
            JTextField head_Text = new JTextField(r_Send.getHead());
            head_Text.setEditable(false);
            MenuPopupUtil.installContextMenu(head_Text);
            this.add(head_Text, detailGBC);
        }


        byte[] r_data = r_Send.getData();
        if (r_data != null && r_data.length > 0) {
            //LABEL MESSAGE
            ++labelGBC.gridy;
            JLabel serviceLabel = new JLabel(Lang.getInstance().translate("Message") + ":");
            this.add(serviceLabel, labelGBC);

            jScrollPane1 = new javax.swing.JScrollPane();
            //jTextArea_Messge = new javax.swing.JTextArea();
            jTextArea_Messge = new MTextPane();


            jTextArea_Messge.text_pane.setEditable(false);
            jTextArea_Messge.text_pane.setContentType("text/html");

            jTextArea_Messge.set_text(r_Send.viewData());

            MenuPopupUtil.installContextMenu(jTextArea_Messge.text_pane);
            //jTextArea_Messge.setText();

            jTextArea_Messge.setPreferredSize(new Dimension(300, 200));
            //   jScrollPane1.setMaximumSize(new Dimension(600,800));

            jScrollPane1.setViewportView(jTextArea_Messge);

            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = detailGBC.gridy + 1;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.weighty = 0.6;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 9);
            add(jScrollPane1, gridBagConstraints);

            labelGBC.gridy = labelGBC.gridy + 4;

            if (r_Send.isEncrypted()) {
                //ENCRYPTED CHECKBOX

                //ENCRYPTED
                GridBagConstraints chcGBC = new GridBagConstraints();
                chcGBC.fill = GridBagConstraints.HORIZONTAL;
                chcGBC.anchor = GridBagConstraints.NORTHWEST;
                chcGBC.gridy = ++labelGBC.gridy;
                chcGBC.gridx = 2;
                chcGBC.gridwidth = 1;
                final JCheckBox encrypted = new JCheckBox(Lang.getInstance().translate("Encrypted"));

                encrypted.setSelected(r_Send.isEncrypted());

                this.add(encrypted, chcGBC);

                encrypted.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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

                        //	encrypted.setEnabled(false);
                        if (!encrypted.isSelected()) {
                            Account account = Controller.getInstance().getAccountByAddress(r_Send.getCreator().getAddress());

                            byte[] privateKey = null;
                            byte[] publicKey = null;
                            //IF SENDER ANOTHER
                            if (account == null) {
                                PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(r_Send.getRecipient().getAddress());
                                privateKey = accountRecipient.getPrivateKey();

                                publicKey = r_Send.getCreator().getPublicKey();
                            }
                            //IF SENDER ME
                            else {
                                PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(account.getAddress());
                                privateKey = accountRecipient.getPrivateKey();

                                publicKey = Controller.getInstance().getPublicKeyByAddress(r_Send.getRecipient().getAddress());
                            }

                            try {
                                byte[] ddd = AEScrypto.dataDecrypt(r_data, privateKey, publicKey);
                                String sss = new String(ddd, "UTF-8");
                                String str = (new String(AEScrypto.dataDecrypt(r_data, privateKey, publicKey), "UTF-8"));
                                jTextArea_Messge.set_text(str); //"{{" +  str.substring(0,RSend.MAX_DATA_VIEW) + "...}}");
                            } catch (UnsupportedEncodingException | InvalidCipherTextException e1) {
                                jTextArea_Messge.set_text("unknown password");
                                LOGGER.error(e1.getMessage(), e1);
                            }
                        } else {
                            jTextArea_Messge.set_text(r_Send.viewData());

                        }
                    }
                });
            }
        }

        if (r_Send.getAmount() != null) {

            String sendType = Lang.getInstance().translate(r_Send.viewFullTypeName());
            //LABEL AMOUNT
            ++labelGBC.gridy;
            JLabel amountLabel = new JLabel(sendType + ":");
            this.add(amountLabel, labelGBC);

            //AMOUNT
            detailGBC.gridy = labelGBC.gridy;
            detailGBC.gridwidth = 2;
            JTextField amount = new JTextField(r_Send.getAmount().toPlainString());
            amount.setEditable(false);
            MenuPopupUtil.installContextMenu(amount);
            this.add(amount, detailGBC);

            //ASSET
            //detailGBC.gridy;
            detailGBC.gridx = 3;
            detailGBC.gridwidth = 1;
            JTextField asset = new JTextField(Controller.getInstance().getAsset(r_Send.getAbsKey()).toString());
            asset.setEditable(false);
            MenuPopupUtil.installContextMenu(asset);
            this.add(asset, detailGBC);
            detailGBC.gridx = 1;
            detailGBC.gridwidth = 3;
        }

        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //    this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
