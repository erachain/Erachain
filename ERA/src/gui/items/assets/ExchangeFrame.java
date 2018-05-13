package gui.items.assets;

import lang.Lang;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import core.item.assets.AssetCls;
import gui.MainFrame;


public class ExchangeFrame extends JDialog
{
	private static final long serialVersionUID = -7052380905136603354L;
	
	private AssetCls have;
	private AssetCls want;
	//Echange_Sell_Buy_Panel tt;
	
	public CreateOrderPanel buyOrderPanel;
	
	
	String action;
	String account;

	java.awt.GridBagConstraints gridBagConstraints;

	public ExchangeFrame(AssetCls have, AssetCls want, String action, String account) 
	{

		this.account= account;
		this.action = action;
		this.have = have;
		this.want = want;
		
		
		
	//	this.setTitle(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("Check Exchange")+" - " + this.have.toString() + " / " + this.want.toString());
		initComponents();
	}
	
	public ExchangeFrame(AssetCls have, String account) 
	{

		this.account= account;
		this.action = "";
		this.have = have;
		this.want = have;
	
		initComponents();
		
	}
	
	   private void initComponents() {
	       
		   if (this.want== null) {
				
				AssetPairSelect ss = new AssetPairSelect(have.getKey(),  action,  account);	
			this.want = ss.pairAsset;
			ss.assetPairSelectTableModel.removeObservers();
			}
		   if(this.want == null) {
			   this.dispose();
			   return;
		   }
		   
	        this.setModal(true);
	//		this.setAlwaysOnTop(true); 
	        
	//		super(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("Check Exchange"));
				this.setTitle(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("Check Exchange"));      
	        
	      //ICON
			List<Image> icons = new ArrayList<Image>();
			icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
			icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
			icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
			icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
	//		this.setFrameIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png")));
			this.setIconImages(icons);
			//CLOSE
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			//LAYOUT
			
			 getContentPane().add(new Exchange_Panel(have, want, action, account), java.awt.BorderLayout.CENTER);
			
	      //PACK
			this.pack();
			this.setResizable(true);
	//		if(action == "Buy" || action =="To sell") this.setSize(900,800);
	//		 int wH = (MainFrame.getInstance().desktopPane.getWidth()- MainFrame.getInstance().desktopPane.getWidth()*15/100);
	//		 int hG = (MainFrame.getInstance().desktopPane.getHeight()- MainFrame.getInstance().desktopPane.getHeight()*15/100);
	//		 this.setMinimumSize(new Dimension(100,100));
	//		this.setSize(new Dimension(wH,hG));
	//		this.setPreferredSize(new Dimension(wH,hG));
			this.setSize(MainFrame.getInstance().getWidth()-100, MainFrame.getInstance().getHeight()-100);
			this.setMaximumSize(new Dimension(MainFrame.getInstance().getWidth()-10, MainFrame.getInstance().getHeight()-10));
			this.setLocationRelativeTo(MainFrame.getInstance());
			this.setVisible(true);
	        
	        
	        
	        
	        
	        
	        
	        
	        
	        
	        
	        
	        
	        
	    }
	
}
	   