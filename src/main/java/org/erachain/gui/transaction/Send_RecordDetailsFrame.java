package org.erachain.gui.transaction;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSend;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.items.accounts.PacketSendPanel;
import org.erachain.gui.library.MAccoutnTextField;
import org.erachain.gui.library.MTextPane;
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
public class Send_RecordDetailsFrame extends RecDetailsFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(Send_RecordDetailsFrame.class);
    private JTextField messageText;
    private JScrollPane jScrollPane1;
    private MTextPane jTextArea_Messge;
    private Send_RecordDetailsFrame th;

    public Send_RecordDetailsFrame(final RSend r_Send) {
        super(r_Send, true);
        th = this;
        //LABEL RECIPIENT
        ++labelGBC.gridy;
        JLabel recipientLabel = new JLabel(Lang.T("Recipient") + ":");
        this.add(recipientLabel, labelGBC);

        //RECIPIENT
        ++fieldGBC.gridy;
        MAccoutnTextField recipient = new MAccoutnTextField(r_Send.getRecipient());
        recipient.setEditable(false);
        this.add(recipient, fieldGBC);

        if (r_Send.getTitle() != null) {
            //LABEL MESSAGE
            ++labelGBC.gridy;
            JLabel title_Label = new JLabel(Lang.T("Title") + ":");
            this.add(title_Label, labelGBC);

            // ISTEXT
            ++fieldGBC.gridy;
            JTextField head_Text = new JTextField(r_Send.getTitle());
            head_Text.setEditable(false);
            MenuPopupUtil.installContextMenu(head_Text);
            this.add(head_Text, fieldGBC);
        }
        if (r_Send.hasPacket()) {
            PacketSendPanel packagePanel = new PacketSendPanel(null, r_Send);
            packagePanel.assetsTableModel.setRows(r_Send.getPacket());
            fieldGBC.gridy = ++labelGBC.gridy;
            fieldGBC.gridwidth = 2;
            this.add(packagePanel, fieldGBC);

        } else if (r_Send.getAmount() != null) {

            AssetCls asset = r_Send.getAsset();

            String sendType;
            if (asset != null) {
                sendType = Lang.T(asset.viewAssetTypeAction(r_Send.isBackward(), r_Send.balancePosition(),
                        r_Send.getCreator() != null && r_Send.getAsset().getMaker().equals(r_Send.getCreator())));
            } else {
                sendType = "???";
            }
            fieldGBC.gridy = ++labelGBC.gridy;

            JLabel amountLabel = new JLabel(sendType);
            this.add(amountLabel, fieldGBC);

            //AMOUNT
            fieldGBC.gridy = ++labelGBC.gridy;
            fieldGBC.gridwidth = 2;
            JTextField amount = new JTextField(r_Send.getAmount().toPlainString());
            amount.setEditable(false);
            MenuPopupUtil.installContextMenu(amount);
            this.add(amount, fieldGBC);

            //ASSET
            fieldGBC.gridx = 3;
            fieldGBC.gridwidth = 1;
            JTextField assetFld = new JTextField();
            assetFld.setEditable(false);
            if (asset != null) {
                assetFld.setText(asset.toString());
                MenuPopupUtil.installContextMenu(assetFld);
            } else {
                assetFld.setText("???");
            }
            this.add(assetFld, fieldGBC);
            fieldGBC.gridx = 1;
            fieldGBC.gridwidth = 3;
        }

        byte[] r_data = r_Send.getData();
        if (r_data != null && r_data.length > 0) {
            //LABEL MESSAGE
            ++labelGBC.gridy;
            JLabel serviceLabel = new JLabel(Lang.T("Message") + ":");
            this.add(serviceLabel, labelGBC);

            jScrollPane1 = new javax.swing.JScrollPane();
            jTextArea_Messge = new MTextPane();


            jTextArea_Messge.text_pane.setEditable(false);
            jTextArea_Messge.text_pane.setContentType("text/html");

            jTextArea_Messge.setText(r_Send.viewData());

            MenuPopupUtil.installContextMenu(jTextArea_Messge.text_pane);

            jTextArea_Messge.setPreferredSize(new Dimension(300, 200));

            jScrollPane1.setViewportView(jTextArea_Messge);

            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = fieldGBC.gridy + 1;
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
                final JCheckBox encrypted = new JCheckBox(Lang.T("Encrypted"));

                encrypted.setSelected(r_Send.isEncrypted());

                this.add(encrypted, chcGBC);

                encrypted.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (!Controller.getInstance().isWalletUnlocked()) {
                            //ASK FOR PASSWORD
                            String password = PasswordPane.showUnlockWalletDialog(th);
                            if (!Controller.getInstance().unlockWallet(password)) {
                                //WRONG PASSWORD
                                JOptionPane.showMessageDialog(null, Lang.T("Invalid password"), Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                                encrypted.setSelected(!encrypted.isSelected());

                                return;
                            }
                        }

                        if (!encrypted.isSelected()) {

                            byte[] decryptedData = Controller.getInstance().decrypt(r_Send.getCreator(),
                                    r_Send.getRecipient(), r_Send.getData());

                            if (decryptedData == null) {
                                jTextArea_Messge.setText(Lang.T("Decrypt Error!"));
                            } else {
                                jTextArea_Messge.setText(r_Send.isText() ?
                                        new String(decryptedData, StandardCharsets.UTF_8)
                                        : Base58.encode(decryptedData)); //Converter.toHex(decryptedData));

                                encrypted.setSelected(!encrypted.isSelected());
                            }

                        } else {
                            jTextArea_Messge.setText(r_Send.viewData());
                        }
                    }
                });
            }
        }

        this.setVisible(true);
    }
}
