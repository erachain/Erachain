

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PagePanel;
import com.sun.pdfview.PDFViewer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax.swing.*;

/**
 * An example of using the PagePanel class to show PDFs. For more advanced
 * usage including navigation and zooming, look ad the 
 * com.sun.pdfview.PDFViewer class.
 *
 * @author joshua.marinacci@sun.com
 */
public class Main {

    private static JScrollPane ss;
    private static int pages1;
    private static PDFPage page;
    private static PagePanel panel;
    private static PDFFile pdffile;
    private static JFrame frame;

    public static void setup() throws IOException {

        initComponents();
        okButton.setEnabled(false);
        okCheckBox.setEnabled(false);
        
        okCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                if(okCheckBox.isSelected()) okButton.setEnabled(true);
                else okButton.setEnabled(false);
            }
            
        });
        
        nextPageButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
               if( pages1 < pdffile.getNumPages()){
                pages1++;
                page = pdffile.getPage(pages1);
                panel.showPage(page);
               } else
                   okCheckBox.setEnabled(true);
            }
            
        });
        
        cancelButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
             frame.dispose();
            }
            
        });
        prevPageButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
               if( pages1 > 0){
                pages1--;
                page = pdffile.getPage(pages1);
                panel.showPage(page);
               }
            }
            
        });
        
        zoomAddutton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                zoomIndex+=0.1;
                int w = (int)(1500*zoomIndex);
                int h = (int)(2000*zoomIndex);
                panel.setPreferredSize(new Dimension((int)(1500*zoomIndex), (int)(2000*zoomIndex)));
                //panel.repaint();
                jScrollPane1.setViewportView(panel);
                
               }
            
        });
        
        zoomMinButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                if (zoomIndex> 0.3) zoomIndex-=0.1;
                
                int w = (int)(1500*zoomIndex);
                int h = (int)(2000*zoomIndex);
                panel.setPreferredSize(new Dimension((int)(1500*zoomIndex), (int)(2000*zoomIndex)));
                //panel.repaint();
                jScrollPane1.setViewportView(panel);
                
               }
            
        });
        
        //set up the frame and panel
         frame = new JFrame("PDF Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       panel = new PagePanel();
        jScrollPane1.setViewportView(panel);
        frame.setPreferredSize(new Dimension(1500,600));
        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);

        //load a pdf from a byte buffer
        File file = new File("License Erachain 0107.pdf");
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY,
            0, channel.size());
        pdffile = new PDFFile(buf);

      //  PDFViewer vv = new PDFViewer(true);
       
       pages1 = 1;
        // show the first page
        page = pdffile.getPage(pages1);
        zoomIndex = 1.0;
        panel.setPreferredSize(new Dimension(1500, 2000));
        panel.showPage(page);
        
    //    new PDFDisplay("C:\\Users\\Саша\\workspace\\ERA+Berkeley\\License Erachain 0107.pdf", 1);

    }
    private static void initComponents() {
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
        okCheckBox = new JCheckBox ();

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        layout.rowHeights = new int[] {0, 8, 0, 8, 0, 8, 0};
        mainPanel.setLayout(layout);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 8);
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
        PagejPanelLayout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        PagejPanelLayout.rowHeights = new int[] {0, 8, 0};
        PagejPanel.setLayout(PagejPanelLayout);

        prevPageButton.setText("<<");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        PagejPanel.add(prevPageButton, gridBagConstraints);

        nextPageButton.setText(">>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        PagejPanel.add(nextPageButton, gridBagConstraints);

        jnumPageTextField.setPreferredSize(new java.awt.Dimension(30, 20));
        jnumPageTextField.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        PagejPanel.add(jnumPageTextField, gridBagConstraints);

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

        okCheckBox.setText("I Igree");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        PagejPanel.add(okCheckBox, gridBagConstraints);
        
        okButton.setText("Ok");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 3;
        PagejPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 3;
        PagejPanel.add(cancelButton, gridBagConstraints);

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
    }// </editor-fold>                        
   
   

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    
                    Main.setup();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
 // Variables declaration - do not modify                     
    private static javax.swing.JPanel PagejPanel;
    private static javax.swing.JButton cancelButton;
    private static javax.swing.JLabel jLabel1;
    public static javax.swing.JScrollPane jScrollPane1;
    private static javax.swing.JTextField jnumPageTextField;
    private static javax.swing.JButton nextPageButton;
    private static javax.swing.JButton okButton;
    private static javax.swing.JButton prevPageButton;
    private static javax.swing.JButton zoomAddutton;
    private static javax.swing.JButton zoomMinButton;
    private static javax.swing.JPanel zoom_jPanel;
    private static JPanel mainPanel;
    static double zoomIndex;
    private static javax.swing.JCheckBox okCheckBox;
    // End of variables declaration    
}