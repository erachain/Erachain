package gui.items.assets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.net.util.Base64;

import core.account.PublicKeyAccount;
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import database.DBSet;
import gui.items.persons.Person_Work_Dialog;
import gui.library.HyperLinkAccount;
import gui.library.MTable;
import gui.library.M_Accoutn_Text_Field;
import gui.library.Voush_Library_Panel;
import gui.library.library;
import gui.models.BalancesTableModel;
import lang.Lang;
import utils.MenuPopupUtil;


public class Asset_Detail_Panel_003 extends JTextPane {

   private AssetCls asset;
private Transaction transaction;
private Asset_Detail_Panel_003 th;
private PublicKeyAccount owner;
private JLabel image_Label;
private BalancesTableModel balancesTableModel;
/**
    * Creates new form Asset_Info003
    */
	
   public Asset_Detail_Panel_003(AssetCls asset) {
    //   initComponents();
	   th=this;
	   this.asset = asset;
	   owner = asset.getOwner();
	   HyperLinkAccount hl_Owner = new HyperLinkAccount(owner);
		
	
	   byte[] recordReference = asset.getReference();
       transaction = Transaction.findByDBRef(DBSet.getInstance(), recordReference);
	   this.setMinimumSize(new Dimension(0, 0)); 	
	   // image +
	   String img_HTML = "";
	   image_Label = new JLabel("");
	   byte[] image_Byte = asset.getImage();
	   if (image_Byte.length > 0){
		 //base 64  
	   String a = Base64.encodeBase64String(image_Byte);
	//   img_HTML = "<img src='data:image/gif;base64," + a + "' width = '350' /></td><td style ='padding-left:20px'>";
	   // label
	   InputStream inputStream = new ByteArrayInputStream(asset.getImage());
       try {
			BufferedImage image1 = ImageIO.read(inputStream);
			
			// jLabel2.setText("jLabel2");
			ImageIcon image = new ImageIcon(image1);
			int x = image.getIconWidth();
			int y = image.getIconHeight();

			int x1 = 250;
			double k = ((double) x / (double) x1);
			y = (int) ((double) y / k);
			

			if (y != 0) {
				Image Im = image.getImage().getScaledInstance(x1, y, 1);
				ImageIcon ic = new ImageIcon(Im);
				image_Label.setIcon(ic);
				image_Label.setSize(ic.getIconWidth(), ic.getIconHeight());
			}
			
			
			
			
			
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	   }
	   
	   
       String text ="<body style= 'font-family:"
				+ UIManager.getFont("Label.font").getFamily() + "; font-size: "+UIManager.getFont("Label.font").getSize() +"pt;'>";
      text +="<div>" + img_HTML + "</div>";
       text += "<DIV><b>" + Lang.getInstance().translate("Key")+ ": </b>" + asset.getKey() + "<DIV>";
	    Transaction record = Transaction.findByDBRef(DBSet.getInstance(), asset.getReference());
       
	    text += "<div><b>"+ Lang.getInstance().translate("Block-SeqNo") + ": </b>" + record.viewHeightSeq(DBSet.getInstance()) +"</div>";
	    text += "<div><b>"+ Lang.getInstance().translate("Name") + ": </b>" + asset.getName() + "</div>";
	    text += "<div   style='word-wrap: break-word; '>" + library.to_HTML(asset.getDescription()) + "</div>";
	    text += "<div><b>" + Lang.getInstance().translate("Owner") + ": </b><a href = '!!Owner'>" + hl_Owner.get_Text() + "</a></div>";
	    text += "<div><b>" + Lang.getInstance().translate("Divisible") + ": </b>" + Lang.getInstance().translate(asset.isDivisible()+"") +"</div>";
	    text += "<div></b>" + Lang.getInstance().translate("Quantity") + ": </b>" + asset.getQuantity() +"</div><<BR>"; 
	   
	   
       	   
	    this.setContentType("text/html");
	    this.setText(text);
	    this.setEditable(false);
	    MenuPopupUtil.installContextMenu(this);
	    add_comp();
	    setCaretPosition(0);
	    
	    
	    
	    this.addHyperlinkListener(new HyperlinkListener(){ 

			@SuppressWarnings("deprecation")
			@Override
			public void hyperlinkUpdate(HyperlinkEvent arg0) {
				// TODO Auto-generated method stub
				if (arg0.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
				 if(arg0.getDescription().toString().equals("!!Owner")) {
					 Point location = MouseInfo.getPointerInfo().getLocation();
				        int x = (int) location.x - th.getLocationOnScreen().x;
				        int y = (int) location.y - th.getLocationOnScreen().y;
					 hl_Owner.get_PopupMenu().show(th, x, y);
				}
			
			
			
			}
	    	
	    	
	    });	    
	    this.addMouseListener(new MouseListener(){

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

   /**
    * This method is called from within the constructor to initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is always
    * regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
   private void initComponents() {
       java.awt.GridBagConstraints gridBagConstraints;

       jScrollPane1 = new javax.swing.JScrollPane();
       jPanel1 = new javax.swing.JPanel();
       jScrollPane3 = new javax.swing.JScrollPane();
     

       setLayout(new java.awt.BorderLayout());

       jScrollPane1.setBorder(null);

       jPanel1.setLayout(new java.awt.GridBagLayout());

   

       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
       gridBagConstraints.weightx = 0.1;
       gridBagConstraints.weighty = 0.1;
       jPanel1.add(jScrollPane3, gridBagConstraints);

       jScrollPane1.setViewportView(jPanel1);

   
   }// </editor-fold>                        

   private void add_comp()
   {    
   try {
       // Get the text pane's document
      // JTextPane textPane = new JTextPane();
       StyledDocument doc = (StyledDocument)this.getDocument();

      
      // JButton BB = new JButton("sdfsd");
    
       javax.swing.JTabbedPane jTabbedPane1 = new javax.swing.JTabbedPane();
       
       GridBagConstraints gridBagConstraints;
	
   
       
       
       
     
    // vouches
       jTabbedPane1.add(new Voush_Library_Panel(transaction));
       
       JPanel jPanel_Tab_Holders = new javax.swing.JPanel();
       JScrollPane jScrollPane_Tab_Holders = new javax.swing.JScrollPane();
    
       jPanel_Tab_Holders.setLayout(new java.awt.GridBagLayout());
       

        balancesTableModel = new BalancesTableModel(asset.getKey());
 		MTable  jTable1 = new MTable(balancesTableModel);
 		
       //  jTable1.setMinimumSize(new Dimension(0,0));
 	
     //    Dimension d = jTable1.getPreferredSize();
     //    d.height = 300;
    //     jTable1.setPreferredScrollableViewportSize(d);
 		
 		
       
       jScrollPane_Tab_Holders.setViewportView(jTable1);

       gridBagConstraints = new java.awt.GridBagConstraints();
       gridBagConstraints.gridx = 0;
       gridBagConstraints.gridy = 1;
               gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
       gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
       gridBagConstraints.weightx = 0.1;
       gridBagConstraints.weighty = 0.1;
       gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
       jPanel_Tab_Holders.add(jScrollPane_Tab_Holders, gridBagConstraints);

       
      
      

       jTabbedPane1.addTab(Lang.getInstance().translate("Holders"), jPanel_Tab_Holders);
       
    
       // The component must first be wrapped in a style
       Style style = doc.addStyle("StyleName", null);
    
       StyleConstants.setComponent(style,jTabbedPane1);

       // Insert the component at the end of the text
       doc.insertString(doc.getLength(), "ignored text", style);
       
       Style style1 = doc.addStyle("StyleName1", null);
       M_Accoutn_Text_Field pane = new M_Accoutn_Text_Field(asset.getOwner() );
     //  pane.setMinimumSize(new Dimension(0,0));
       StyleConstants.setComponent(style1,pane);
     //  doc.insertString(10, "ignored text", style1);
       
       // image
       Style imageStyle = doc.addStyle("StyleImage", null);
       
       StyleConstants.setComponent(imageStyle,image_Label);
       image_Label.setVisible(true);
       image_Label.setSize(300, 300);
       doc.insertString(1, "ignored text", imageStyle);
    
       
       
       
   } catch (BadLocationException e) {
   }
   }

   public void  delay_on_Close(){
   	balancesTableModel.removeObservers();
			
	}
   // Variables declaration - do not modify                     
   private javax.swing.JPanel jPanel1;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JScrollPane jScrollPane3;
  
   // End of variables declaration                   
}
