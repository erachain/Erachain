package gui.create;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import core.BlockChain;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple4;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PagePanel;

import controller.Controller;
import core.exdata.ExData;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import datachain.DCSet;
import lang.Lang;
import settings.Settings;
import utils.Zip_Bytes;

/**
 * An example of using the PagePanel class to show PDFs. For more advanced usage
 * including navigation and zooming, look ad the com.sun.pdfview.PDFViewer
 * class.
 *
 * @author joshua.marinacci@sun.com
 */
public class License_JFrame extends JDialog {
    
    private JScrollPane ss;
    private int pages1;
    private PDFPage page;
    private PagePanel panel;
    private PDFFile pdffile;
    private License_JFrame th = this;
    // private static JFrame frame;
    private boolean needAccept;
    private int goCreateWallet;
    int height;
    int width;
    
    JFrame parent;
    
    public License_JFrame(boolean needAccept, JFrame parent, int goCreateWallet) {
        
        this.needAccept = needAccept;
        this.parent = parent;
        this.goCreateWallet = goCreateWallet;
                
        try {
            setup();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public License_JFrame() {
        // this.license = "<html>" + Processor.process(getLicenseText()) +
        // "</html>";
        needAccept = false;
        
        try {
            setup();
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void setup() throws IOException {
        
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screens = kit.getScreenSize();
        
        width = screens.width;
        height = (int) (width * 1.4);
        initComponents();
        setTitle(Lang.getInstance().translate("License"));
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);
        //
        okButton.setEnabled(BlockChain.DEVELOP_USE);
        okCheckBox.setEnabled(BlockChain.DEVELOP_USE);
        
        okCheckBox.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                if (okCheckBox.isSelected())
                    okButton.setEnabled(true);
                else
                    okButton.setEnabled(false);
            }
            
        });
        
        nextPageButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                if (pages1 < pdffile.getNumPages()) {
                    pages1++;
                    page = pdffile.getPage(pages1);
                    panel.showPage(page);
                } else
                    okCheckBox.setEnabled(true);
            }
            
        });
        
        cancelButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                th.dispose();
            }
            
        });
        prevPageButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                if (pages1 > 0) {
                    pages1--;
                    page = pdffile.getPage(pages1);
                    panel.showPage(page);
                }
            }
            
        });
        
        zoomAddutton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                zoomIndex += 0.1;
                th.height = (int) (th.height * zoomIndex);
                th.width = (int) (th.width * zoomIndex);
                panel.setPreferredSize(new Dimension(th.width, th.height));
                // panel.repaint();
                jScrollPane1.setViewportView(panel);
                
            }
            
        });
        
        zoomMinButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                if (zoomIndex > 0.3)
                    zoomIndex -= 0.1;
                
                th.height = (int) (th.height * zoomIndex);
                th.width = (int) (th.width * zoomIndex);
                panel.setPreferredSize(new Dimension(th.width, th.height));
                // panel.repaint();
                jScrollPane1.setViewportView(panel);
                
            }
            
        });
        
        // set up the frame and panel
        // th.setTitle("");
        // th.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new PagePanel();
        jScrollPane1.setViewportView(panel);
        th.add(mainPanel);
                
        Tuple2<Integer, Integer> langRef = Controller.LICENSE_LANG_REFS.get(Settings.getInstance().getLang());
        if (langRef == null)
            langRef = Controller.LICENSE_LANG_REFS.get("en");

        ByteBuffer buf = null;

        Transaction record = DCSet.getInstance().getTransactionFinalMap().get(langRef);
        if (record != null) {
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
                                Entry<String, Tuple2<Boolean, byte[]>> fileData = it_Files.next();
                                boolean zip = new Boolean(fileData.getValue().a);
                                String name_File = (String) fileData.getKey();
                                setTitle(getTitle() + " - " + name_File);

                                byte[] file_byte = (byte[]) fileData.getValue().b;
                                if (zip) {
                                    try {
                                        file_byte = Zip_Bytes.decompress(file_byte);
                                    } catch (DataFormatException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                }

                                buf = ByteBuffer.wrap(file_byte);
                            }
                        }
                    }
                }
            }
        }
        
        if (buf == null) {
            // load a pdf from a byte buffer
            File file = new File("Erachain Licence Agreement.pdf");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel channel = raf.getChannel();
            buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
        
        pdffile = new PDFFile(buf);
        zoomIndex = 1.0;
        // PDFViewer vv = new PDFViewer(true);
        th.setPreferredSize(new Dimension());
        pages1 = 1;
        // show the first page
        
        this.addComponentListener(new ComponentListener() {
            
            @Override
            public void componentHidden(ComponentEvent arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void componentMoved(ComponentEvent arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void componentResized(ComponentEvent arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void componentShown(ComponentEvent arg0) {
                // TODO Auto-generated method stub
                page = pdffile.getPage(pages1);
                panel.setPreferredSize(new Dimension(th.width, th.height));
                panel.showPage(page);
            }
            
        });
        // Toolkit kit = Toolkit.getDefaultToolkit();
        // Dimension screens = kit.getScreenSize();
        int h = (int) (screens.height * 0.9);
        int w = (int) (screens.width * 0.9);
        this.setModal(true);
        th.setPreferredSize(new Dimension(w, h));
        
        th.pack();
        this.setLocationRelativeTo(null);
        th.setVisible(true);
        
        // new PDFDisplay("C:\\Users\\Саша\\workspace\\ERA+Berkeley\\License
        // Erachain 0107.pdf", 1);
        
    }
    
    private void initComponents() {
        mainPanel = new JPanel();
        java.awt.GridBagConstraints gridBagConstraints;
        
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        PagejPanel = new javax.swing.JPanel();
        prevPageButton = new javax.swing.JButton();
        nextPageButton = new javax.swing.JButton();
        jnumPageTextField = new javax.swing.JTextField();
        zoomMinButton = new javax.swing.JButton();
        zoomAddutton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        zoom_jPanel = new javax.swing.JPanel();
        okCheckBox = new JCheckBox();
        
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] { 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0 };
        layout.rowHeights = new int[] { 0, 8, 0, 8, 0, 8, 0 };
        mainPanel.setLayout(layout);
        
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(Lang.getInstance().translate("Read carefully") + "!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 8);
        
        if (needAccept)
            mainPanel.add(jLabel1, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        mainPanel.add(jScrollPane1, gridBagConstraints);
        
        java.awt.GridBagLayout PagejPanelLayout = new java.awt.GridBagLayout();
        PagejPanelLayout.columnWidths = new int[] { 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0 };
        PagejPanelLayout.rowHeights = new int[] { 0, 8, 0 };
        PagejPanel.setLayout(PagejPanelLayout);
        
        prevPageButton.setText("<<");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        PagejPanel.add(prevPageButton, gridBagConstraints);
        
        nextPageButton.setText(">>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        PagejPanel.add(nextPageButton, gridBagConstraints);
        
        jnumPageTextField.setPreferredSize(new java.awt.Dimension(30, 20));
        jnumPageTextField.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        // PagejPanel.add(jnumPageTextField, gridBagConstraints);
        
        zoomMinButton.setText("Zoom -");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 0;
        PagejPanel.add(zoomMinButton, gridBagConstraints);
        
        zoomAddutton.setText("Zoom +");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 0;
        PagejPanel.add(zoomAddutton, gridBagConstraints);
        
        okCheckBox.setText(Lang.getInstance().translate("I accept"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        // PagejPanel.add(okCheckBox, gridBagConstraints);
        if (needAccept)
            PagejPanel.add(okCheckBox, gridBagConstraints);
        
        okButton.setText(Lang.getInstance().translate("Next"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 3;
        // PagejPanel.add(okButton, gridBagConstraints);
        
        if (needAccept)
            PagejPanel.add(okButton, gridBagConstraints);
        
        okButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                setVisible(false);
                
                if (parent != null)
                    ((NoWalletFrame) parent).goAfterLicence(goCreateWallet);
            }
            
        });
        cancelButton.setText(Lang.getInstance().translate(parent == null ? "Not Accept" : "Back"));
        cancelButton.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 3;
        // PagejPanel.add(cancelButton, gridBagConstraints);
        if (needAccept)
            PagejPanel.add(cancelButton, gridBagConstraints);
        
        cancelButton.addActionListener(new ActionListener() {
            
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
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 10, 8);
        mainPanel.add(PagejPanel, gridBagConstraints);
        
        zoom_jPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        mainPanel.add(zoom_jPanel, gridBagConstraints);
        
        // CLOSE NICELY
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                
                if (!needAccept)
                    return;
                
                Controller.getInstance().stopAll(0);
                
            }
        });
        
    }// </editor-fold>
    
    // Variables declaration - do not modify
    private javax.swing.JPanel PagejPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    public javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jnumPageTextField;
    private javax.swing.JButton nextPageButton;
    private javax.swing.JButton okButton;
    private javax.swing.JButton prevPageButton;
    private javax.swing.JButton zoomAddutton;
    private javax.swing.JButton zoomMinButton;
    private javax.swing.JPanel zoom_jPanel;
    private JPanel mainPanel;
    static double zoomIndex;
    private javax.swing.JCheckBox okCheckBox;
    // End of variables declaration
}