package org.erachain.gui.items.assets;

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.ImageCropDialog;
import org.erachain.gui.items.TypeOfImage;
import org.erachain.gui.library.HoldersLibraryPanel;
import org.erachain.gui.library.HyperLinkAccount;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.SignLibraryPanel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;
import org.erachain.utils.NumberAsString;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;


public class AssetInfo extends JTextPane {

    // in pack toByte and Parse - reference not included
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Transaction.class);

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

        try {
            //   initComponents();
            th = this;
            this.asset = asset;
            owner = asset.getOwner();
            HyperLinkAccount hl_Owner = new HyperLinkAccount(owner);

            byte[] recordReference = asset.getReference();
            transaction = Transaction.findByDBRef(DCSet.getInstance(), recordReference);
            this.setMinimumSize(new Dimension(0, 0));

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

                if (max_Height > 1) {
                    cachedImage = image.getImage().getScaledInstance(max_Widht, max_Height, 1);
                } else {
                    cachedImage = null;
                }
            } else {
                cachedImage = null;
            }

            if (cachedImage == null) {
                imageByte = asset.getIcon();
                if (imageByte != null && imageByte.length > 1) {
                    //if (asset.getKey() == 1l) image = new ImageIcon("images/icons/icon32.png");
                    image = new ImageIcon(imageByte);
                    cachedImage = image.getImage().getScaledInstance(40, 40, 1);
                }
            }

            //   img_HTML = "<img src='data:image/gif;base64," + a + "' width = '350' /></td><td style ='padding-left:20px'>";

            String color = "#" + Integer.toHexString(UIManager.getColor("Panel.background").getRGB()).substring(2);

            String text = "<body style= 'font-family:"
                    + UIManager.getFont("Label.font").getFamily() + "; font-size: " + UIManager.getFont("Label.font").getSize() + "pt;'>";

            text += "<table><tr valign='top' align = 'left'><td>";
            text += "<DIV  style='float:left'><b>" + Lang.T("Key") + ": </b>" + asset.getKey() + "</DIV>";

            // ADD IMAGE to THML
            if (cachedImage != null) {
                text += "<div><a href ='!!img'  style='color: " + color + "' ><img src=\"" + img_Local_URL + "\"></a></div>";
            }

            Transaction record = Transaction.findByDBRef(DCSet.getInstance(), asset.getReference());
            if (record != null)
                text += "<td><div  style='float:left'><div><b>" + Lang.T("Block-SeqNo") + ": </b>" + record.viewHeightSeq() + "</div>";
            text += "<div><b>" + Lang.T("Name") + ": </b>" + asset.viewName() + "</div>";
            text += "<div   style='word-wrap: break-word; '>";

            if (asset.getKey() > 0 && asset.getKey() < 1000) {
                text += Library.to_HTML(Lang.T(asset.viewDescription())) + "</div>";
            } else {
                text += Library.to_HTML(asset.viewDescription()) + "</div>";
            }
            text += "<div>" + Lang.T("Owner") + ": <a href = '!!Owner'><b>" + hl_Owner.get_Text() + "</b></a></div>";
            text += "<div>" + Lang.T("Class") + ": <b>" + Lang.T(asset.getItemSubType()) + "</b>,";
            text += " " + Lang.T("Type") + ": <a href='!!Type'><b>" +
                    asset.charAssetType() + asset.viewAssetTypeAbbrev() + "</b>:"
                    + Lang.T(asset.viewAssetTypeFull()) + "</a>,";
            text += " " + Lang.T("Accuracy") + ": <b>" + asset.getScale() + "</b>,";
            text += " " + Lang.T("Quantity") + ": <b>" + NumberAsString.formatAsString(asset.getQuantity()) + "</b>";
            text += " " + Lang.T("Released") + ": <b>" + NumberAsString.formatAsString(asset.getReleased()) + "</b>";

            text += "</div><<BR></td></tr></table>";
            text += "<div>";

            setContentType("text/html");
            setText(text);

            if (true)
                HTML_Add_Local_Images();
            else
                iii();

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
                    if (arg0.getDescription().equals("!!Owner")) {
                        Point location = MouseInfo.getPointerInfo().getLocation();
                        int x = location.x - th.getLocationOnScreen().x;
                        int y = location.y - th.getLocationOnScreen().y;
                        hl_Owner.get_PopupMenu().show(th, x, y);
                        return;
                    } else if (arg0.getDescription().equals("!!img")) {
                        new ImageCropDialog(new ImageIcon(asset.getImage())) {
                            @Override
                            public void onFinish(BufferedImage image, TypeOfImage typeOfImage, boolean useOriginal) {
                            }
                        };
                    } else if (arg0.getDescription().equals("!!Type")) {
                        String find = asset.viewAssetTypeAbbrev();
                        SearchAssetsSplitPanel panel = new SearchAssetsSplitPanel(false);
                        panel.searchTextFieldSearchToolBarLeftPanelDocument.setText(":" + find);
                        panel.startSearchName();
                        MainPanel.getInstance().insertNewTab(Lang.T("Search") + " :" + find, panel);
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

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }


    private void add_comp()
    // TODO add component SWING to JTEXTPANE
    {
        try {
            javax.swing.JTabbedPane jTabbedPane1 = new javax.swing.JTabbedPane();
            // vouches
            jTabbedPane1.add(new SignLibraryPanel(transaction));
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

    // работает - добавляет в конец после всего текста
    public void iii() {
        ///ImageIcon currentIcon = new ImageIcon(manaSymbolImages.get(currentMatch));

        SimpleAttributeSet iconAtts = new SimpleAttributeSet();
        JLabel iconLabel = new JLabel(image);
        StyleConstants.setComponent(iconAtts, iconLabel);

        insertIcon(image);
    }

    public void HTML_Add_Local_Images() {
        // ADD image into URL
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
