package gui.status;
// 16/03

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.ToolTipManager;

import utils.GUIUtils;
import utils.ObserverMessage;
import controller.Controller;
import core.BlockChain;
import core.BlockGenerator;
import core.account.Account;
import core.block.Block;
import core.block.GenesisBlock;
import core.transaction.Transaction;
import database.DBSet;
import lang.Lang;

@SuppressWarnings("serial")
public class ForgingStatus extends JLabel implements Observer {

	private ImageIcon forgingDisabledIcon;
	private ImageIcon forgingEnabledIcon;
	private ImageIcon forgingIcon;
	private ImageIcon forgingWaitIcon;
	
	public ForgingStatus()
	{
		super();
		
		//CREATE ICONS
		this.forgingDisabledIcon = this.createIcon(Color.RED);
		this.forgingEnabledIcon = this.createIcon(Color.ORANGE);
		this.forgingWaitIcon = this.createIcon(Color.MAGENTA);
		this.forgingIcon = this.createIcon(Color.GREEN);

		//TOOLTIP
		ToolTipManager.sharedInstance().setDismissDelay( (int) TimeUnit.SECONDS.toMillis(5));
		this.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent mEvt) {
				
				long winBalance = 0;
				Account winAccount = null;
				BlockChain bchain = Controller.getInstance().getBlockChain();
				List<Block> lastBlocksForTarget = bchain.getLastBlocksForTarget(DBSet.getInstance());
				int newHeight = bchain.getHeight(DBSet.getInstance()) + 1;
				long target = bchain.getTarget(DBSet.getInstance());
				if (target == 0l)
					target = 1000l;

				DBSet dbSet = DBSet.getInstance();
	            for(Account account: Controller.getInstance().getAccounts())
		        {
	            	long win_value = account.calcWinValue(dbSet, bchain, lastBlocksForTarget, newHeight, target);
	            	if (win_value > Math.abs(winBalance)) {
	            		winBalance = win_value;
	            		winAccount = account;
	            	}
		        }
	            
		        String timeForge = "";
		        if(winAccount != null)
		        {
		        	//timeForge = getTimeToGoodView((60*5+19)*Controller.getInstance().getLastBlock().getGeneratingBalance()/totalBalanceInt);
		        	timeForge = (BlockChain.BASE_TARGET * winBalance / target) + " " + winAccount.getAddress();
		        	timeForge = Lang.getInstance().translate("Won data for forging: %timeForge%.").replace("%timeForge%", timeForge);
		        }
		        else
		        {
		        	timeForge = Lang.getInstance().translate("infinity");
		        }
		        

				if(Controller.getInstance().getForgingStatus() == BlockGenerator.ForgingStatus.FORGING)
				{
		            setToolTipText(timeForge);
				}
				else if (Controller.getInstance().getForgingStatus() == BlockGenerator.ForgingStatus.FORGING_DISABLED && Controller.getInstance().getStatus() == Controller.STATUS_OK) 
				{
					setToolTipText(Lang.getInstance().translate("To start forging you need to unlock the wallet."
							+ " " + timeForge));
				}
				else if (Controller.getInstance().getForgingStatus() == BlockGenerator.ForgingStatus.FORGING_WAIT && Controller.getInstance().getStatus() == Controller.STATUS_OK) 
				{
					setToolTipText(Lang.getInstance().translate("To start forging need await SYNC peer."
							+ " " + timeForge));
				}
				else
				{
					setToolTipText(Lang.getInstance().translate("For forging wallet must be online and fully synchronized."
							+ " " + timeForge));
				}
				
	    }});

		//LISTEN ON STATUS
		Controller.getInstance().addObserver(this);	
		setIconAndText(Controller.getInstance().getForgingStatus());
	}
	
	public static String getTimeToGoodView(long intdif) {
		String result = "+ ";
		long diff = intdif * 1000;
		final int ONE_DAY = 1000 * 60 * 60 * 24;
		final int ONE_HOUR = ONE_DAY / 24;
		final int ONE_MINUTE = ONE_HOUR / 60;
		final int ONE_SECOND = ONE_MINUTE / 60;
		
		long d = diff / ONE_DAY;
		diff %= ONE_DAY;
		
		long h = diff / ONE_HOUR;
		diff %= ONE_HOUR;
		
		long m = diff / ONE_MINUTE;
		diff %= ONE_MINUTE;
		
		long s = diff / ONE_SECOND;
		//long ms = diff % ONE_SECOND;
		
		if(d>0)
		{
			result += d > 1 ? d + " " + Lang.getInstance().translate("days") + " " : d + " " + Lang.getInstance().translate("day") + " ";	
		}
		
		if(h>0 && d<5)
		{
			result += h > 1 ? h + " " + Lang.getInstance().translate("hours") + " " : h + " " + Lang.getInstance().translate("hour") + " ";	
		}	
		
		if(m>0 && d == 0 && h<10 )
		{
			result += m > 1 ? m + " " + Lang.getInstance().translate("mins") + " " : m + " " + Lang.getInstance().translate("min") + " ";	
		}
		
		if(s>0 && d == 0 && h == 0 && m<15)
		{
			result += s > 1 ? s + " " + Lang.getInstance().translate("secs") + " " : s + " " + Lang.getInstance().translate("sec") + " ";
		}
		
		return result.substring(0, result.length() - 1);
	}

	private ImageIcon createIcon(Color color)
	{
		return GUIUtils.createIcon(color, this.getBackground());
	}

	@Override
	public void update(Observable arg0, Object arg1) 
	{
		ObserverMessage message = (ObserverMessage) arg1;
		
		if(message.getType() == ObserverMessage.FORGING_STATUS)
		{
			BlockGenerator.ForgingStatus status = (BlockGenerator.ForgingStatus) message.getValue();
			
			setIconAndText(status);
		}		
	}

	private void setIconAndText(BlockGenerator.ForgingStatus status) {
		if(status == BlockGenerator.ForgingStatus.FORGING_DISABLED)
		{
			forgingDisabled();
		} else if (status == BlockGenerator.ForgingStatus.FORGING_ENABLED)
		{
			this.setIcon(forgingEnabledIcon);
			this.setText(status.getName());
		} else if (status == BlockGenerator.ForgingStatus.FORGING_WAIT)
		{
			this.setIcon(forgingWaitIcon);
			this.setText(status.getName());
		} else if (status == BlockGenerator.ForgingStatus.FORGING)
		{
			this.setIcon(forgingIcon);
			this.setText(status.getName());
		}
	}

	public void forgingDisabled() {
		this.setIcon(forgingDisabledIcon);
		this.setText(BlockGenerator.ForgingStatus.FORGING_DISABLED.getName());
	}

 
}
