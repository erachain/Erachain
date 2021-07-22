package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.exdata.ExData;
import org.erachain.core.exdata.exActions.ExAction;
import org.erachain.core.exdata.exLink.ExLinkAuthor;
import org.erachain.core.exdata.exLink.ExLinkSource;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.library.FileChooser;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MAttachedFilesPanel;
import org.erachain.gui.library.SignLibraryPanel;
import org.erachain.gui.transaction.RecDetailsFrame;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;
import org.erachain.utils.ZipBytes;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.DataFormatException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Саша
 */
@SuppressWarnings("serial")
public class RNoteInfo extends RecDetailsFrame {

    RSignNote statement;
    ExData exData;
    RSignNote statementEncrypted;
    private MAttachedFilesPanel file_Panel;
    private SignLibraryPanel voush_Library_Panel;
    private javax.swing.JLabel jLabel_Title;
    private JTextPane jTextArea_Body;

    Controller cntr;

    public RNoteInfo(Transaction transaction) {

        super(transaction, true);

        cntr = Controller.getInstance();

        statement = (RSignNote) transaction;
        statement.parseDataFull();
        statement.calcFee(false);

        initComponents();

        viewInfo();
    }

    private void initComponents() {

        ++labelGBC.gridy;
        jLabel_Title = new JLabel(Lang.T("Title") + ":");
        add(jLabel_Title, labelGBC);

        fieldGBC.gridy = labelGBC.gridy;
        add(new JLabel(statement.getTitle()), fieldGBC);

        if (statement.isEncrypted()) {
            JCheckBox encrypted = new JCheckBox(Lang.T("Encrypted"));
            encrypted.setSelected(true);
            fieldGBC.gridy = ++labelGBC.gridy;
            add(encrypted, fieldGBC);

            encrypted.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!encrypted.isSelected()) {
                        if (!cntr.isWalletUnlocked()) {
                            //ASK FOR PASSWORD
                            String password = PasswordPane.showUnlockWalletDialog(null);
                            if (!cntr.unlockWallet(password)) {
                                //WRONG PASSWORD
                                JOptionPane.showMessageDialog(null, Lang.T("Invalid password"), Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                                encrypted.setSelected(!encrypted.isSelected());

                                return;
                            }
                        }

                        statementEncrypted = statement;

                        Account account = cntr.getInvolvedAccount(statement);
                        Fun.Tuple3<Integer, String, RSignNote> result = statement.decrypt(account);
                        if (result.a < 0) {
                            JOptionPane.showMessageDialog(null,
                                    Lang.T(result.b == null ? "Not exists Account access" : result.b),
                                    Lang.T("Not decrypted"), JOptionPane.ERROR_MESSAGE);
                            encrypted.setSelected(!encrypted.isSelected());

                            return;

                        } else if (result.b != null) {
                            JOptionPane.showMessageDialog(null,
                                    Lang.T(" In pos: " + result.a + " - " + result.b),
                                    Lang.T("Not decrypted"), JOptionPane.ERROR_MESSAGE);
                            encrypted.setSelected(!encrypted.isSelected());

                            return;

                        }

                        statement = result.c;
                        statement.parseDataFull();
                        viewInfo();

                    } else if (statementEncrypted != null) {
                        // закроем доступ
                        statement = statementEncrypted;
                        viewInfo();
                    }
                }
            });
        }

        /////////////
        JScrollPane jScrollPane_Message_TextPane = new JScrollPane();
        jTextArea_Body = new JTextPane();
        jTextArea_Body.setContentType("text/html");
        jTextArea_Body.setEditable(false);
        MenuPopupUtil.installContextMenu(jTextArea_Body);
        jScrollPane_Message_TextPane.setViewportView(jTextArea_Body);
        jScrollPane_Message_TextPane.setPreferredSize(new Dimension(0, 500));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = ++labelGBC.gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.gridwidth = 3;
        add(jScrollPane_Message_TextPane, gridBagConstraints);
        ///////////////////

        jTextArea_Body.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent arg0) {
                // TODO Auto-generated method stub
                if (arg0.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;

                String fileName = arg0.getDescription();
                if (fileName.startsWith("#T#")) {
                    // TEMPLATE
                    fileName = "doc" + statement.viewHeightSeq();
                    String valuedText = exData.getValuedText();
                    valuedText = Library.to_HTML(valuedText);
                    fileName += Library.will_HTML(valuedText) ? ".html" : ".html";

                    FileChooser chooser = new FileChooser();
                    chooser.setDialogTitle(Lang.T("Save File") + ": " + fileName);
                    //chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    //chooser.setAcceptAllFileFilterUsed(false);

                    if (chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {

                        String pp = chooser.getSelectedFile().getPath() + File.separatorChar + fileName;

                        File ff = new File(pp);
                        // if file
                        if (ff.exists() && ff.isFile()) {
                            int aaa = JOptionPane.showConfirmDialog(chooser,
                                    Lang.T("File") + " " + fileName
                                            + " " + Lang.T("Exists") + "! "
                                            + Lang.T("Overwrite") + "?", Lang.T("Message"),
                                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                            if (aaa != 0) {
                                return;
                            }
                            ff.delete();

                        }

                        try (FileOutputStream fos = new FileOutputStream(pp)) {
                            byte[] buffer = valuedText.getBytes(StandardCharsets.UTF_8);
                            fos.write(buffer, 0, buffer.length);
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }

                        return;
                    }
                }

                FileChooser chooser = new FileChooser();
                chooser.setDialogTitle(Lang.T("Save File") + ": " + fileName);
                //chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                //chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {

                    String pp = chooser.getSelectedFile().getPath() + File.separatorChar + fileName;

                    File ff = new File(pp);
                    // if file
                    if (ff.exists() && ff.isFile()) {
                        int aaa = JOptionPane.showConfirmDialog(chooser,
                                Lang.T("File") + " " + fileName
                                        + " " + Lang.T("Exists") + "! "
                                        + Lang.T("Overwrite") + "?", Lang.T("Message"),
                                JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if (aaa != 0) {
                            return;
                        }
                        ff.delete();

                    }

                    try (FileOutputStream fos = new FileOutputStream(pp)) {
                        ExData exData = statement.getExData();
                        HashMap<String, Tuple3<byte[], Boolean, byte[]>> items = exData.getFiles();
                        Tuple3<byte[], Boolean, byte[]> fileItem = items.get(fileName);
                        byte[] buffer = fileItem.c;
                        // if ZIP
                        if (fileItem.b) {
                            byte[] buffer1 = null;
                            try {
                                buffer1 = ZipBytes.decompress(buffer);
                            } catch (DataFormatException e1) {
                                System.out.println(e1.getMessage());
                            }
                            fos.write(buffer1, 0, buffer1.length);
                        } else {
                            fos.write(buffer, 0, buffer.length);
                        }

                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }

                }

            }
        });

        if (false) {
            // старая версия - теперь все в HTML поле скопом со ссылками
            ++fieldGBC.gridy;
            file_Panel = new MAttachedFilesPanel();
            add(file_Panel, fieldGBC);

            voush_Library_Panel = new SignLibraryPanel(transaction);
            ++fieldGBC.gridy;
            add(voush_Library_Panel, fieldGBC);
        }

    }

    @SuppressWarnings("unchecked")
    private void viewInfo() {

        String resultStr = "";

        exData = statement.getExData();
        exData.setDC(DCSet.getInstance());

        ExAction exAction = exData.getExAction();
        if (exAction != null) {
            exAction.preProcess(statement);
            resultStr += exAction.getInfoHTML();
        }

        if (exData.isCanSignOnlyRecipients()) {
            resultStr += "<br><b>" + Lang.T("To sign can only Recipients") + "<b><br>";
        }

        // recipients
        if (exData.hasRecipients()) {
            resultStr += "<h2>" + Lang.T("Recipients") + "</h2>";
            Account[] recipients = exData.getRecipients();
            int size = recipients.length;
            for (int i = 1; i <= size; ++i) {
                if (i > 7 && size > 10) {
                    resultStr += "... <br>";
                    i = size;
                }
                resultStr += i + " " + recipients[i - 1].getAddress() + "<br>";
            }
            resultStr += "<br>";
        }

        // AUTHORS
        if (false && // in JTree
                exData.hasAuthors()) {
            resultStr += "<h2>" + Lang.T("Authors") + "</h2>";
            ExLinkAuthor[] authors = exData.getAuthors();
            int size = authors.length;
            for (int i = 1; i <= size; ++i) {
                if (i > 7 && size > 10) {
                    resultStr += "... <br>";
                    i = size;
                }

                PersonCls person = cntr.getPerson(authors[i - 1].getRef());
                String memo = authors[i - 1].getMemo();

                resultStr += i + ". " + authors[i - 1].getValue() + " x " + person.toString(cntr.getDCSet()) + (memo == null ? "" : " - " + memo) + "<br>";
            }
            resultStr += "<br>";
        }

        if (exData.isEncrypted()) {
            resultStr += "<h3>" + Lang.T("Encrypted") + "</h3><br>";
        }

        long templateKey = exData.getTemplateKey();
        if (templateKey > 0) {
            TemplateCls template = exData.getTemplate();
            resultStr += "<a href=#T#" + template.getKey() + "><h2>" + template.toString(DCSet.getInstance()) + "</h2></a>";

            String valuedText = exData.getValuedText();
            if (valuedText != null) {
                resultStr += Library.to_HTML(valuedText);
            }
            resultStr += "<hr><br>";

            JSONObject params = exData.getTemplateValues();
            if (params != null) {
                resultStr += " <h3>" + Lang.T("Template Values") + "</h3>";
                Set<String> keys = params.keySet();
                for (String key : keys) {
                    resultStr += key + ": " + params.get(key) + "<br>";
                }
            }
        }

        String message = exData.getMessage();
        if (message != null) {
            resultStr += Library.to_HTML(message) + "<br><br>";
        }

        if (exData.hasHashes()) {
            // hashes
            JSONObject hashes = exData.getHashes();
            resultStr += "<h3>" + Lang.T("Hashes") + "</h3>";
            int i = 1;
            for (Object s : hashes.keySet()) {
                resultStr += i + " " + s + " " + hashes.get(s) + "<br>";
            }
            resultStr += "<br";
        }

        if (exData.hasFiles()) {
            HashMap<String, Tuple3<byte[], Boolean, byte[]>> files = exData.getFiles();
            Iterator<Entry<String, Tuple3<byte[], Boolean, byte[]>>> it_Files = files.entrySet().iterator();
            resultStr += "<h3>" + Lang.T("Files") + "</h3>";
            if (true) {
                int i = 1;
                while (it_Files.hasNext()) {
                    Entry<String, Tuple3<byte[], Boolean, byte[]>> file = it_Files.next();
                    boolean zip = new Boolean(file.getValue().b);
                    String fileName = file.getKey();
                    resultStr += i++ + ". <a href=" + fileName + ">"
                            + fileName + (zip ? " (" + Lang.T("Zipped") + ")" : "")
                            + "</a>" + " - "
                            + (file.getValue().c.length > 20000 ? (file.getValue().c.length >> 10) + "kB" : file.getValue().c.length + "B") + "<br>";
                }
                resultStr += "<br";
            } else {
                while (it_Files.hasNext()) {
                    Entry<String, Tuple3<byte[], Boolean, byte[]>> file = it_Files.next();
                    boolean zip = new Boolean(file.getValue().b);
                    String name_File = file.getKey();
                    byte[] file_byte = file.getValue().c;
                    file_Panel.addRow(name_File, zip, file_byte);
                }
                //file_Panel.fireTableDataChanged();
            }

        } else if (statementEncrypted != null) {
            //file_Panel.clear();
        }

        // AUTHORS
        if (false && // in JTree
                exData.hasSources()) {
            resultStr += "<h2>" + Lang.T("Sources") + "</h2>";
            ExLinkSource[] sources = exData.getSources();
            int size = sources.length;
            for (int i = 1; i <= size; ++i) {
                if (i > 7 && size > 10) {
                    resultStr += "... <br>";
                    i = size;
                }

                Transaction sourceTx = cntr.getTransaction(sources[i - 1].getRef());
                String memo = sources[i - 1].getMemo();

                resultStr += i + ". " + sources[i - 1].getValue() + " x " + sourceTx.toStringFullAndCreatorLang() + (memo == null ? "" : " - " + memo) + "<br>";
            }
            resultStr += "<br>";
        }

        if (exData.hasTags()) {
            resultStr += "<h4>" + Lang.T("Tags") + "</h4>";
            resultStr += statement.getExTags();

        }

        int fontSize = UIManager.getFont("Label.font").getSize();

        resultStr = "<head><style>"
                + " h1{ font-size: " + (fontSize + 5) + "px;  } "
                + " h2{ font-size: " + (fontSize + 3) + "px;  }"
                + " h3{ font-size: " + (fontSize + 1) + "px;  }"
                + " h4{ font-size: " + fontSize + "px;  }"
                + " h5{ font-size: " + (fontSize - 1) + "px;  }"
                + " body{ font-family:"
                + UIManager.getFont("Label.font").getFamily() + "; font-size:" + fontSize + "px;"
                //+ "word-wrap:break-word;}"
                + "word-wrap:normal;}"
                + "</style> </head><body>" + resultStr
                + "</body>";

        jTextArea_Body.setText(resultStr);

    }
}
