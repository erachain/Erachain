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
import gui.library.M_PDFView;
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
    
  private PagePanel panel;
   
    private License_JFrame th = this;
    private boolean needAccept;
    private int goCreateWallet;
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
      //  okCheckBox.setEnabled(BlockChain.DEVELOP_USE);
        
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
  
        cancelButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                th.dispose();
            }
            
        });
   
        panel = new PagePanel();
        jScrollPane1.setViewportView(panel);
        th.add(mainPanel);
        
        int h = (int) (screens.height * 0.9);
        int w = (int) (screens.width * 0.9);
        this.setModal(true);
        th.setPreferredSize(new Dimension(w, h));
        
        th.pack();
        this.setLocationRelativeTo(null);
        th.setVisible(true);
        }
    
    private void initComponents() {
        mainPanel = new JPanel();
        java.awt.GridBagConstraints gridBagConstraints;
        
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        PagejPanel = new javax.swing.JPanel();
        jnumPageTextField = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
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
    //    mainPanel.add(jScrollPane1, gridBagConstraints);
        M_PDFView pp = new M_PDFView();
        
        mainPanel.add( pp, gridBagConstraints);
        pp.show();
        java.awt.GridBagLayout PagejPanelLayout = new java.awt.GridBagLayout();
        PagejPanelLayout.columnWidths = new int[] { 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0 };
        PagejPanelLayout.rowHeights = new int[] { 0, 8, 0 };
        PagejPanel.setLayout(PagejPanelLayout);
        
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
    private javax.swing.JButton okButton;
    private JPanel mainPanel;
    static double zoomIndex;
    private javax.swing.JCheckBox okCheckBox;
    // End of variables declaration
}