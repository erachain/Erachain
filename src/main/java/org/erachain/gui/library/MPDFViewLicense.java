package org.erachain.gui.library;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import org.erachain.controller.Controller;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;
import org.erachain.utils.ZipBytes;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Саша
 */
public class MPDFViewLicense extends javax.swing.JPanel {

    protected Logger logger;

    private PDFFile pdffile;
    /**
     * Creates new form MPDFViewLicense
     */
    private int pages;
    ByteBuffer buf = null;
    private JPanel jPanelBottomTutton;
    private JButton jButtonZoomAdd;
    private JButton jButtonZoomDec;
    private MPDFViewLicense th;
    protected int height;
    protected int width;
    protected double zoomIndex;
    private int width1;
    private int height1;
    int pageNum = 0;

    public MPDFViewLicense(String fileName) {
        super();

        logger = LoggerFactory.getLogger(getClass());

        //  new PDFViewer(true);
        th = this;
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screens = kit.getScreenSize();

        width1 = width = (int) (screens.width / 1.4);
        height1 = height = (int) (width * 1.4);
        zoomIndex = 1.0;
        initComponents();

        try {
            pdffile = new PDFFile(readPDFLicenseOrExit(fileName));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pages = pdffile.getNumPages();

        // view smal pages
        for (int i = 1; i <= pages; i++) {
            jPanel1.add(new PDFPageViewSmall(pdffile.getPage(i, true), i));
        }
        // button Zoom +
        jButtonZoomAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                zoomIndex = zoomIndex + 0.1;
                height1 = (int) (height * zoomIndex);
                width1 = (int) (width * zoomIndex);
                setImage(pageNum);

            }

        });
        // button zoom -
        jButtonZoomDec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                zoomIndex = zoomIndex - 0.1;
                height1 = (int) (height * zoomIndex);
                width1 = (int) (width * zoomIndex);
                setImage(pageNum);

            }

        });

        // show page
        setImage(pageNum);
        jSplitPane_Main.setDividerLocation(200);
        jScrollPane1.setViewportView(image);
    }

    private void setImage(int pageNum) {

        PDFPage pp = pdffile.getPage(pageNum, true);
        Rectangle2D r2d = pp.getBBox();
        Image pp1 = pp.getImage(
                width1, height1,
                r2d, // clip rect
                null, // null for the ImageObserver
                true, // fill background with white
                true // block until drawing is done
        );

        image.setPreferredSize(new Dimension(width1, height1));
        image.setIcon(new ImageIcon(pp1));
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelTop = new javax.swing.JPanel();
        jLabel_Top = new javax.swing.JLabel();
        jPanelCenter = new javax.swing.JPanel();
        jSplitPane_Main = new javax.swing.JSplitPane();
        jPanel_Left = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanelBottom = new javax.swing.JPanel();
        jPanelBottomTutton = new javax.swing.JPanel();
        jButtonZoomAdd = new javax.swing.JButton();
        jButtonZoomDec = new javax.swing.JButton();
        image = new JLabel();

        setLayout(new java.awt.GridBagLayout());

        jPanelTop.setLayout(new java.awt.GridBagLayout());

        jLabel_Top.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.3;
        add(jPanelTop, gridBagConstraints);

        jPanelCenter.setLayout(new java.awt.GridBagLayout());

        jPanel_Left.setLayout(new javax.swing.BoxLayout(jPanel_Left, javax.swing.BoxLayout.Y_AXIS));

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        jScrollPane2.setViewportView(jPanel1);

        jPanel_Left.add(jScrollPane2);

        jSplitPane_Main.setLeftComponent(jPanel_Left);
        jSplitPane_Main.setRightComponent(jScrollPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanelCenter.add(jSplitPane_Main, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 0.5;
        add(jPanelCenter, gridBagConstraints);

        jPanelBottom.setLayout(new java.awt.GridBagLayout());

        jPanelBottomTutton.setLayout(new java.awt.GridBagLayout());

        jButtonZoomAdd.setText("ZOOM+");
        jPanelBottomTutton.add(jButtonZoomAdd, new java.awt.GridBagConstraints());

        jButtonZoomDec.setText("ZOOM-");
        jPanelBottomTutton.add(jButtonZoomDec, new java.awt.GridBagConstraints());

        jPanelBottom.add(jPanelBottomTutton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        add(jPanelBottom, gridBagConstraints);
    }

    private ByteBuffer readPDFLicenseOrExit() {

        Long langRef = Controller.LICENSE_LANG_REFS.get(Settings.getInstance().getLang());
        if (langRef == null)
            langRef = Controller.LICENSE_LANG_REFS.get("en");
        Transaction record = DCSet.getInstance().getTransactionFinalMap().get(langRef);
        if (record != null) {
            if (record.getType() == Transaction.SIGN_NOTE_TRANSACTION) {

                RSignNote note = (RSignNote) record;
                note.parseDataFull();
                ExData exData = note.getExData();

                HashMap<String, Tuple3<byte[], Boolean, byte[]>> files = exData.getFiles();
                if (files != null && !files.isEmpty()) {
                    Iterator<Entry<String, Tuple3<byte[], Boolean, byte[]>>> it_Files = files.entrySet().iterator();
                    while (it_Files.hasNext()) {
                        Entry<String, Tuple3<byte[], Boolean, byte[]>> fileData = it_Files.next();
                        boolean zip = new Boolean(fileData.getValue().b);
                        // String name_File = (String) fileData.getKey();
                        // setTitle(getTitle() + " - " + name_File);

                        byte[] file_byte = (byte[]) fileData.getValue().c;
                        if (zip) {
                            try {
                                try {
                                    file_byte = ZipBytes.decompress(file_byte);
                                } catch (IOException e) {
                                    logger.error(e.getMessage(), e);
                                }
                            } catch (DataFormatException e1) {
                                logger.error(e1.getMessage(), e1);
                            }
                        }

                        buf = ByteBuffer.wrap(file_byte);
                    }
                }
            }
        }

        if (buf == null) {
            // load a pdf from a byte buffer

            File file;
            if (Settings.getInstance().getLang().equals("ru"))
                file = new File("Erachain Licence Agreement (ru).pdf");
            else
                file = new File("Erachain Licence Agreement.pdf");

            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(file, "r");
            } catch (FileNotFoundException e) {
                file = new File("Erachain Licence Agreement.pdf");
                try {
                    raf = new RandomAccessFile(file, "r");
                } catch (FileNotFoundException e1) {
                    logger.error(e1.getMessage(), e1);
                    return null;
                }
            }
            FileChannel channel = raf.getChannel();
            try {
                buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return buf;
    }

    public ByteBuffer readPDFLicenseOrExit(String fileName) {

        if (fileName == null)
            return readPDFLicenseOrExit();

        File file;
        file = new File(fileName);

        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel channel = raf.getChannel();
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        } catch (IOException e) {
            logger.error(e.getMessage() + " - " + fileName, e);
            Controller.getInstance().stopAndExit(3);
        }

        return null;

    }

    class PDFPageViewSmall extends JButton {
        PDFPage page1;
        private int pp3;

        PDFPageViewSmall(PDFPage pdfPage, int i) {
            super();
            pp3 = i;
            page1 = pdfPage;
            Rectangle2D r2d = pdfPage.getBBox();
            //  Image pp2 = pdfPage.getImage(120, 200, r2d, null);
            Image pp2 = pdfPage.getImage(
                    120, 180, //width & height
                    r2d, // clip rect
                    null, // null for the ImageObserver
                    true, // fill background with white
                    true // block until drawing is done
            );

            this.setIcon(new ImageIcon(pp2));


            this.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    pageNum = pp3;
                    Image pp1 = pdfPage.getImage(
                            width1, height1,
                            r2d, // clip rect
                            null, // null for the ImageObserver
                            true, // fill background with white
                            true // block until drawing is done
                    );

                    image.setPreferredSize(new Dimension(width1, height1));
                    image.setIcon(new ImageIcon(pp1));
                }
            });

        }
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel_Bottom;
    private javax.swing.JLabel jLabel_Top;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelBottom;
    private javax.swing.JPanel jPanelCenter;
    private javax.swing.JPanel jPanelRight;
    private javax.swing.JPanel jPanelTop;
    private javax.swing.JPanel jPanel_Left;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane_Main;
    private JLabel image;
    // End of variables declaration                   
}
