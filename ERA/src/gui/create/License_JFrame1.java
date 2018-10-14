package gui.create;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JDialog;
import javax.swing.JTextPane;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import com.github.rjeschke.txtmark.Processor;

import controller.Controller;
import core.exdata.ExData;
import core.item.templates.TemplateCls;
import core.transaction.IssueTemplateRecord;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import datachain.DCSet;
import lang.Lang;
import settings.Settings;

public class License_JFrame1 extends JDialog {
    
    static Logger LOGGER = Logger.getLogger(License_JFrame1.class.getName());
    boolean needAccept;
    NoWalletFrame parent;
    int goCreateWallet;
    String license;
    
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private JTextPane messageText;
    
    public License_JFrame1(boolean needAccept, NoWalletFrame parent, int goCreateWallet) {
        
        this.needAccept = needAccept;
        this.parent = parent;
        this.goCreateWallet = goCreateWallet;
        
        this.license = "<html>" + Processor.process(getLicenseText()) + "</html>";
        
        initComponents();
    }
    
    public License_JFrame1() {
        this.license = getLicenseText();
        
        needAccept = false;
        initComponents();
    }
    
    public String getLicenseText() {
        
        Long langRef = Controller.LICENSE_LANG_REFS.get(Settings.getInstance().getLang());
        if (langRef == null)
            langRef = Controller.LICENSE_LANG_REFS.get("en");
        
        String message;
        Transaction record = DCSet.getInstance().getTransactionFinalMap().get(langRef);
        if (record == null) {
            TemplateCls template = (TemplateCls) DCSet.getInstance().getItemTemplateMap().get(2l);
            message = Processor.process(template.getDescription());
        } else {
            if (record.getType() == Transaction.SIGN_NOTE_TRANSACTION) {
                
                R_SignNote note = (R_SignNote) record;
                if (record.getVersion() == 2) {
                    byte[] data = note.getData();

                    Tuple4<String, String, JSONObject, HashMap<String, Tuple2<Boolean, byte[]>>> map;
                    try {
                         map = ExData.parse_Data_V2(data);
                    } catch (Exception e) {
                        map = null;
                    }
                        
                    if (map != null) {
                        HashMap<String, Tuple2<Boolean, byte[]>> files = map.d;
                        if (files != null) {
                            Iterator<Entry<String, Tuple2<Boolean, byte[]>>> it_Files = files.entrySet().iterator();
                            while (it_Files.hasNext()) {
                                Entry<String, Tuple2<Boolean, byte[]>> file = it_Files.next();
                                boolean zip = new Boolean(file.getValue().a);
                                String name_File = (String) file.getKey();
                                byte[] file_byte = (byte[]) file.getValue().b;
                                return name_File;
                            }
                        }
                    }

                    try {
                        Tuple3<String, String, JSONObject> a = note.parse_Data_V2_Without_Files();
                        message = (String) a.c.get("MS");
                    } catch (Exception e) {
                        message = new String(note.getData(), Charset.forName("UTF-8"));
                    }
                    
                } else {
                    
                    try {
                        JSONObject dataJSON = (JSONObject) JSONValue
                                .parseWithException(new String(note.getData(), Charset.forName("UTF-8")));
                        message = (String) dataJSON.get("Message");
                    } catch (Exception e) {
                        message = new String(note.getData(), Charset.forName("UTF-8"));
                    }
                }
            } else if (record.getType() == Transaction.ISSUE_TEMPLATE_TRANSACTION) {
                IssueTemplateRecord template = (IssueTemplateRecord) record;
                message = template.getItem().getDescription();
            } else {
                TemplateCls template = (TemplateCls) DCSet.getInstance().getItemTemplateMap().get(2l);
                message = Processor.process(template.getDescription());
            }
        }
        
        return message;
        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);
        this.setModal(true);
        
        java.awt.GridBagConstraints gridBagConstraints;
        
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        // jTextArea1 = new javax.swing.JTextArea();
        
        messageText = new JTextPane();
        messageText.setContentType("text/html");
        
        jLabel1 = new javax.swing.JLabel();
        
        // setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(Lang.getInstance().translate("License"));
        setMinimumSize(new java.awt.Dimension(500, 350));
        getContentPane().setLayout(new java.awt.GridBagLayout());
        
        jCheckBox1.setText(Lang.getInstance().translate("I accept"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 6, 0);
        if (needAccept)
            getContentPane().add(jCheckBox1, gridBagConstraints);
        
        jCheckBox1.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                // TODO Auto-generated method stub
                jButton1.setEnabled(!jButton1.isEnabled());
            }
        });
        
        jButton1.setEnabled(false);
        jButton1.setText(Lang.getInstance().translate("Next"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 8, 0);
        
        if (needAccept)
            getContentPane().add(jButton1, gridBagConstraints);
        
        jButton1.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                setVisible(false);
                
                if (parent != null)
                    parent.goAfterLicence(goCreateWallet);
            }
            
        });
        
        jButton2.setText(Lang.getInstance().translate(parent == null ? "Not Accept" : "Back"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 8, 8);
        if (needAccept)
            getContentPane().add(jButton2, gridBagConstraints);
        
        jButton2.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                
                if (parent != null) {
                    parent.setVisible(true);
                    dispose();
                } else {
                    Controller.getInstance().stopAll(0);
                    // System.exit(0);
                }
            }
        });
        
        // CLOSE NICELY
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                
                if (!needAccept)
                    return;
                
                Controller.getInstance().stopAll(0);
                
            }
        });
        
        messageText.setText(this.license);
        jScrollPane1.setViewportView(messageText);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        getContentPane().add(jScrollPane1, gridBagConstraints);
        
        jLabel1.setText(Lang.getInstance().translate("Read carefully") + "!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        if (needAccept)
            getContentPane().add(jLabel1, gridBagConstraints);
        this.setAlwaysOnTop(true);
        this.setUndecorated(false);
        // if (needAccept) this.setUndecorated(true);
        // Current size of the default screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        pack();
        // this.setLocationRelativeTo(null);
        this.setSize(dim);
        this.setVisible(true);
    }
    // </editor-fold>
    // End of variables declaration
    
}
