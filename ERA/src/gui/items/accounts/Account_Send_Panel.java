package gui.items.accounts;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.R_Send;
import core.transaction.Transaction;
import gui.AccountRenderer;
import gui.PasswordPane;
import gui.items.assets.AssetsComboBoxModel;
import gui.library.Issue_Confirm_Dialog;
import gui.library.MButton;
import gui.library.My_JFileChooser;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import gui.transaction.Send_RecordDetailsFrame;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import utils.Converter;
import utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

//import settings.Settings;

@SuppressWarnings("serial")

public class Account_Send_Panel extends Class_Account_Transaction_Panel {
    
    private Account_Send_Panel th;
    public boolean noRecive;

    public Account_Send_Panel(AssetCls asset, Account account, Account account_To, PersonCls person) {
        super(asset,account,account_To, person);
        String a;
        th = this;
        if (asset == null) a = "";
        else a = asset.viewName();

        jTextArea_Title.setText(Lang.getInstance().translate("If You want to send asset %asset%, fill in this form").replace("%asset%", a));

        //  icon.setIcon(null);
        sendButton.setText(Lang.getInstance().translate("Send"));
        toLabel.setText(Lang.getInstance().translate("To: (address or name)") + ":");
        recDetailsLabel.setText(Lang.getInstance().translate("Receiver details") + ":");
        
        
        

    }

   
    @Override
    public void onSendClick() {
        
     // confirm params
        if (!cheskError()) return;

        // CREATE TX MESSAGE
        Transaction transaction = Controller.getInstance().r_Send(
                Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), feePow, recipient, key,
                amount, head, messageBytes, isTextByte, encrypted);
        // test result = new Pair<Transaction, Integer>(null,
        // Transaction.VALIDATE_OK);

        String Status_text = "<HTML>" + Lang.getInstance().translate("Size") + ":&nbsp;" + transaction.viewSize(false)
                + " Bytes, ";
        Status_text += "<b>" + Lang.getInstance().translate("Fee") + ":&nbsp;" + transaction.getFee().toString()
                + " COMPU</b><br></body></HTML>";

        Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(null, true, Lang.getInstance().translate("Send Mail"),
                (int) (this.getWidth() / 1.2), (int) (this.getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"));
        Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((R_Send) transaction);
        dd.jScrollPane1.setViewportView(ww);
        dd.pack();
        dd.setLocationRelativeTo(this);
        dd.setVisible(true);

        // JOptionPane.OK_OPTION
        if (dd.isConfirm) {

            if (noRecive) {

                // String raw = Base58.encode(transaction.toBytes(false, null));
                My_JFileChooser chooser = new My_JFileChooser();
                chooser.setDialogTitle(Lang.getInstance().translate("Save File"));
                // chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                // FileNameExtensionFilter filter = new
                // FileNameExtensionFilter("*.era","*.*");
                // chooser.setFileFilter(filter);

                // chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {

                    String pp = chooser.getSelectedFile().getPath();

                    File ff = new File(pp);
                    // if file
                    if (ff.exists() && ff.isFile()) {
                        int aaa = JOptionPane.showConfirmDialog(chooser,
                                Lang.getInstance().translate("File") + Lang.getInstance().translate("Exists") + "! "
                                        + Lang.getInstance().translate("Overwrite") + "?",
                                Lang.getInstance().translate("Message"), JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);
                        System.out.print("\n gggg " + aaa);
                        if (aaa != 0) {
                            return;
                        }
                        ff.delete();

                    }

                    try (FileWriter fw = new FileWriter(ff)) {
                        fw.write(transaction.toJson().toJSONString());
                    } catch (IOException e) {
                        System.out.println(e);
                    }

                    /*
                     * try(FileOutputStream fos=new FileOutputStream(pp)) { //
                     * перевод строки в байты // String ssst =
                     * model.getValueAt(row, 2).toString(); byte[] buffer
                     * =transaction.toBytes(false, null); // if ZIP
                     *
                     * fos.wri.write(buffer, 0, buffer.length);
                     *
                     * } catch(IOException ex){
                     *
                     * System.out.println(ex.getMessage()); }
                     */
                }

                // JOptionPane.showMessageDialog(new JFrame(),
                // Lang.getInstance().translate("File save"),
                // Lang.getInstance().translate("Success"),
                // JOptionPane.INFORMATION_MESSAGE);

            } else {

                result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, false);
                confirmaftecreatetransaction();
               
            }
        }
        // ENABLE
        this.sendButton.setEnabled(true);
    }

}
