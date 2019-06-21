package org.erachain.gui.telegrams;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.View;

import org.erachain.core.transaction.RSend;
import org.erachain.utils.Converter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.erachain.gui.library.Library;
import org.mapdb.Fun.Tuple3;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.DateTimeFormat;

public class RendererMessage extends JLabel implements TableCellRenderer {
    private static final long serialVersionUID = 1L;
    private static JLabel resizer;
  
   
    private static final Logger LOGGER = LoggerFactory.getLogger(RendererMessage.class);
    private String isScriptImage;
    
 //   JTextPane jtp;
 //   JTextArea ta;
    
    public RendererMessage() {
       //setContentType("text/html");
       // ta = new JTextArea();
     //   jtp = new JTextPane();
     //   setViewportView(jtp);
     //   setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
        resizer = new JLabel();
   
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        String background = null;
        String color;
        WalletTelegramsFilterTableModel model = (WalletTelegramsFilterTableModel) table.getModel();
       
        Color col = UIManager.getColor("Table.background");
            int red = col.getRed();
            int green = col.getGreen();
            int blue = col.getBlue();
            background = "rgb(" + red +"," + green + "," + blue + ")";
           
            Tuple3<Long, Long, Transaction> val = (Tuple3<Long, Long, Transaction>)value;
            if (model.getSender() == null ) return this;
            String image;
            if (model.getSender().equals(val.a)) {
                resizer.setHorizontalAlignment(LEFT);
                color = "Green";
                image = Settings.getInstance().getUserPath() + "images/messages/send.png";
            } else {
                color = "Blue";
                resizer.setHorizontalAlignment(RIGHT);
                image = Settings.getInstance().getUserPath() + "images/messages/receive.png";
                col = Color.gray;
                red = col.getRed();
                green = col.getGreen();
                blue = col.getBlue();
              //  resizer.setBackground(Color.MAGENTA);//.UIManager.getColor("Table.selectionBackground"));
                 background = "#DCDCDC";
            }
           
          String text = encrypt((RSend)val.c);
          text = Library.to_HTML(text);
          
          if (isSelected) {
          //    Color col = UIManager.getColor("Table.selectionBackground");
         //     int red = col.getRed();
          //    int green = col.getGreen();
         //     int blue = col.getBlue();
            //  resizer.setBackground(Color.MAGENTA);//.UIManager.getColor("Table.selectionBackground"));
         //      background = "rgb(" + red +"," + green + "," + blue + ")";
           } else {
             // resizer.setBackground(UIManager.getColor("Table.background"));
           //    Color col = UIManager.getColor("Table.background");
           //    int red = col.getRed();
           //    int green = col.getGreen();
           //    int blue = col.getBlue();
           //    background = "rgb(" + red +"," + green + "," + blue + ")";
          }
            
          value = "<HTML><body style='background:"+background+";'><p>&nbsp;&nbsp;<img src='file:"+ image +"'>";
          if (((RSend)val.c).isEncrypted()){
              
              value = value  +"&nbsp;&nbsp;<img src='file:"+ isScriptImage +"'>";
          }
                 
              value = value    + "&nbsp;<span style='font-size:10px;font-family:" + UIManager.getFont("Label.font").getFamily() + ";color:"+ color   + "'>"
                   + "&nbsp;&nbsp;" + Lang.getInstance().translate("Date") +": " + DateTimeFormat.timestamptoString(val.c.getTimestamp()) + "</span></p>"
                   + "<p style='font-size:10px;font-family:" + UIManager.getFont("Label.font").getFamily() + ";color:"+ color   + "'>&nbsp;&nbsp;"+Lang.getInstance().translate("Sender") +": " + val.a   + " &nbsp;&nbsp; "+Lang.getInstance().translate("Recipient") +": " + val.b + "</p>"
                  + "&nbsp;&nbsp;<p>" + "<span style='font-size:" + UIManager.getFont("Label.font").getSize() + "px;font-family:"
                    + UIManager.getFont("Label.font").getFamily() + "'>" +text + "</p></HTML>";
            
         
       

       

           if (hasFocus) {
               resizer.setBorder(new LineBorder(new Color(99, 130, 191)));
               //  setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
           } else {
               resizer.setBorder(new LineBorder(null, 0));
               // setBorder(noFocusBorder);
           }
          // calc height
           Dimension prefSize = getPreferredSize1((String) value, true, table.getWidth()-10);
         // set hight cell table
        table.setRowHeight((row), (int) (prefSize.getHeight()+30));
       
        return resizer;
    }
    
    
    public static java.awt.Dimension getPreferredSize1(String html, boolean width, int prefSize) {

        resizer.setText(html);
        View view = (View) resizer.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);

        view.setSize(width ? prefSize : 0, width ? 0 : prefSize);

        float w = view.getPreferredSpan(View.X_AXIS);
        float h = view.getPreferredSpan(View.Y_AXIS);

        return new java.awt.Dimension((int) Math.ceil(w), (int) Math.ceil(h));
    }
   
    
    
    private  String encrypt(RSend trans) {

        //  jTextArea_Messge.setContentType("text/html");
        //  if (trans.isText())  jTextArea_Messge.setContentType("text");
        if (!trans.isEncrypted())
            return trans.viewData();

        if (!Controller.getInstance().isWalletUnlocked()) {
            isScriptImage = Settings.getInstance().getUserPath() + "images/messages/locked.png";
            return "<span style='color:Navy'>" + Lang.getInstance().translate("Encrypted") + "</span>";
        }

        byte[] decryptedData = Controller.getInstance().decrypt(trans.getCreator(),
                trans.getRecipient(), trans.getData());

        if (decryptedData == null) {
            isScriptImage = Settings.getInstance().getUserPath() + "images/messages/locked.png";

            return "<span style='color:Red'>" + Lang.getInstance().translate("Error") + "</span>";
        } else {
            isScriptImage = Settings.getInstance().getUserPath() + "images/messages/unlockedred.png";

            return trans.isText() ?
                    new String(decryptedData, Charset.forName("UTF-8"))
                    : Converter.toHex(decryptedData);
        }

    }
    
}