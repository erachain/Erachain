package org.erachain.gui.items.assets;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.library.HoldersLibraryPanel;
import org.erachain.gui.library.HyperLinkAccount;
import org.erachain.gui.library.VoushLibraryPanel;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;


public class AssetInfo extends JTextPane {

    //private BalancesTableModel balancesTableModel;
    private static String img_Local_URL = "http:\\img";
    BufferedImage image1;
    private AssetCls asset;
    private Transaction transaction;
    private AssetInfo th;
    private PublicKeyAccount owner;
    private JLabel image_Label;
    private int max_Widht;
    private int max_Height;
    private Image Im;
    ImageIcon image = null;

    /**
     * Creates new form Asset_Info003
     */

    public AssetInfo(AssetCls asset, boolean fullView) {
        //   initComponents();
        th = this;
        this.asset = asset;
        owner = asset.getOwner();
        HyperLinkAccount hl_Owner = new HyperLinkAccount(owner);


        byte[] recordReference = asset.getReference();
        transaction = Transaction.findByDBRef(DCSet.getInstance(), recordReference);
        this.setMinimumSize(new Dimension(0, 0));
        image_Label = new JLabel("");

        byte[] imageByte = asset.getImage();
        if (imageByte != null && imageByte.length > 0) {
            //   img_HTML = "<img src='data:image/gif;base64," + a + "' width = '350' /></td><td style ='padding-left:20px'>";
            // label
            image = new ImageIcon(imageByte);

            int x = image.getIconWidth();
            max_Height = image.getIconHeight();

            max_Widht = 200;
            double k = ((double) x / (double) max_Widht);
            max_Height = (int) (max_Height / k);

            if (max_Height != 0) {
                Im = image.getImage().getScaledInstance(max_Widht, max_Height, 1);
                image = new ImageIcon(Im);
                image_Label.setIcon(image);
                //image_Label.setSize(image.getIconWidth(), image.getIconHeight());
            }

        } else {
            imageByte = asset.getIcon();
            if (imageByte != null && imageByte.length > 1) {
                //if (asset.getKey() == 1l) image = new ImageIcon("images/icons/icon32.png");
                image = new ImageIcon(imageByte);
                Im = image.getImage().getScaledInstance(45, 45, 1);
                image = new ImageIcon(Im);
                image_Label.setIcon(image);
            }
        }

        //   img_HTML = "<img src='data:image/gif;base64," + a + "' width = '350' /></td><td style ='padding-left:20px'>";

        String color = "#" + Integer.toHexString(UIManager.getColor("Panel.background").getRGB()).substring(2);

        String text = "<body style= 'font-family:"
                + UIManager.getFont("Label.font").getFamily() + "; font-size: " + UIManager.getFont("Label.font").getSize() + "pt;'>";

        text += "<table><tr valign='top' align = 'left'><td>";
        text += "<DIV  style='float:left'><b>" + Lang.getInstance().translate("Key") + ": </b>" + asset.getKey() + "</DIV>";
        if (image != null)
            text += "<div><a href ='!!img'  style='color: " + color + "' ><img src=\"" + img_Local_URL + "\"></a></div>";
        Transaction record = Transaction.findByDBRef(DCSet.getInstance(), asset.getReference());
        if (record != null)
            text += "<td><div  style='float:left'><div><b>" + Lang.getInstance().translate("Block-SeqNo") + ": </b>" + record.viewHeightSeq() + "</div>";
        text += "<div><b>" + Lang.getInstance().translate("Name") + ": </b>" + asset.viewName() + "</div>";
        text += "<div   style='word-wrap: break-word; '>"
                + Library.to_HTML(Lang.getInstance().translate(asset.viewDescription())) + "</div>";
        text += "<div>" + Lang.getInstance().translate("Owner") + ": <a href = '!!Owner'><b>" + hl_Owner.get_Text() + "</b></a></div>";
        text += "<div>" + Lang.getInstance().translate("TYPE") + ": <b>" + Lang.getInstance().translate(asset.viewAssetType()) + "</b>,";
        text += " " + Lang.getInstance().translate("accuracy") + ": <b>" + asset.getScale() + "</b>,";
        text += " " + Lang.getInstance().translate("quantity") + ": <b>" + asset.getQuantity() + "</b></div><<BR></td></tr></table>";
        text += "<div>";

        this.setContentType("text/html");
        this.setText(text);
        HTML_Add_Local_Images();
        this.setEditable(false);
        MenuPopupUtil.installContextMenu(this);
        if (fullView) {
            add_comp();
        }
        setCaretPosition(0);

        this.addHyperlinkListener(new HyperlinkListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void hyperlinkUpdate(HyperlinkEvent arg0) {
                // TODO Auto-generated method stub
                if (arg0.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
                if (arg0.getDescription().toString().equals("!!Owner")) {
                    Point location = MouseInfo.getPointerInfo().getLocation();
                    int x = location.x - th.getLocationOnScreen().x;
                    int y = location.y - th.getLocationOnScreen().y;
                    hl_Owner.get_PopupMenu().show(th, x, y);
                    return;
                }
                if (arg0.getDescription().toString().equals("!!img")) {
                }
            }
        });
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                //		System.out.print("\nMouse" + getComponentAt(e.getPoint()) +"\n");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
            }

        });

        this.setOpaque(false);
    }


    private void add_comp()
    // TODO add component SWING to JTEXTPANE
    {
        try {
            javax.swing.JTabbedPane jTabbedPane1 = new javax.swing.JTabbedPane();
            // vouches
            jTabbedPane1.add(new VoushLibraryPanel(transaction));
            // holders
            jTabbedPane1.add(new HoldersLibraryPanel(asset, -1));
//            jTabbedPane1.add(new HoldersLibraryPanel(asset, 2));
            //          jTabbedPane1.add(new HoldersLibraryPanel(asset, 3));
            //         jTabbedPane1.add(new HoldersLibraryPanel(asset, 4));

            // Get the text pane's document
            // JTextPane textPane = new JTextPane();
            StyledDocument doc = (StyledDocument) this.getDocument();
            // The component must first be wrapped in a style
            Style style = doc.addStyle("StyleName", null);
            StyleConstants.setComponent(style, jTabbedPane1);
            // Insert the component at the end of the text
            doc.insertString(doc.getLength(), "ignored text", style);
        } catch (BadLocationException e) {
        }
    }

    public void HTML_Add_Local_Images() {
        // TODO ADD image into URL
        try {
            Dictionary cache = (Dictionary) this.getDocument().getProperty("imageCache");
            if (cache == null) {
                cache = new Hashtable();
                this.getDocument().putProperty("imageCache", cache);
            }

            URL u = new URL(img_Local_URL);
            if (Im != null) cache.put(u, Im);


        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


    }

    public void delay_on_Close() {
        // 	balancesTableModel.deleteObservers();

    }
    // Variables declaration - do not modify
    //  private javax.swing.JPanel jPanelHead;
    // private javax.swing.JScrollPane scrollPaneDescription;
    //  private javax.swing.JScrollPane jScrollPane3;

    // End of variables declaration
}
