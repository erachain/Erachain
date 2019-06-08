package org.erachain.gui.items.assets;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.*;
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
import org.slf4j.LoggerFactory;


public class AssetInfo extends JTextPane {

    // in pack toByte and Parse - reference not included
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Transaction.class.getName());

    //private BalancesTableModel balancesTableModel;
    private static String img_Local_URL = "http:\\img";
    BufferedImage image1;
    private AssetCls asset;
    private Transaction transaction;
    private AssetInfo th;
    private PublicKeyAccount owner;
    private int max_Widht;
    private int max_Height;
    private Image cachedImage;
    ImageIcon image = null;

    /**
     * Creates new form Asset_Info003
     */

    public AssetInfo(AssetCls asset, boolean fullView) {
        super();

        //   initComponents();
        th = this;
        this.asset = asset;
        owner = asset.getOwner();
        HyperLinkAccount hl_Owner = new HyperLinkAccount(owner);

        byte[] recordReference = asset.getReference();
        transaction = Transaction.findByDBRef(DCSet.getInstance(), recordReference);
        this.setMinimumSize(new Dimension(0, 0));

        image = null;
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

            if (max_Height > 1 ) {
                cachedImage = image.getImage().getScaledInstance(max_Widht, max_Height, 1);
                image = new ImageIcon(cachedImage);
            } else {
                image = null;
            }

        }

        if (image == null){
            imageByte = asset.getIcon();
            if (imageByte != null && imageByte.length > 1) {
                //if (asset.getKey() == 1l) image = new ImageIcon("images/icons/icon32.png");
                image = new ImageIcon(imageByte);
                cachedImage = image.getImage().getScaledInstance(40, 40, 1);
                image = new ImageIcon(cachedImage);
            }
        }

        //   img_HTML = "<img src='data:image/gif;base64," + a + "' width = '350' /></td><td style ='padding-left:20px'>";

        String color = "#" + Integer.toHexString(UIManager.getColor("Panel.background").getRGB()).substring(2);

        String text = "<body style= 'font-family:"
                + UIManager.getFont("Label.font").getFamily() + "; font-size: " + UIManager.getFont("Label.font").getSize() + "pt;'>";

        text += "<table><tr valign='top' align = 'left'><td>";
        text += "<DIV  style='float:left'><b>" + Lang.getInstance().translate("Key") + ": </b>" + asset.getKey() + "</DIV>";

        // ADD IMAGE to THML
        if (image != null) {
            text += "<div><a href ='!!img'  style='color: " + color + "' ><img src=\"" + img_Local_URL + "\"></a></div>";
        }

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

        setContentType("text/html");
        setText(text);

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
            if (cachedImage != null) cache.put(u, cachedImage);


        } catch (MalformedURLException e) {
            LOGGER.error(e.getMessage(), e);
        }


    }

    public void delay_on_Close() {
        // 	balancesTableModel.deleteObservers();

    }
}
