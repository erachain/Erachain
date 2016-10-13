package gui.models;
import java.awt.Component;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import controller.Controller;
import database.DBSet;
import database.PeerMap.PeerInfo;
import lang.Lang;
import network.Peer;
import settings.Settings;
import utils.DateTimeFormat;
import utils.ObserverMessage;

@SuppressWarnings("serial")
public class PeersTableModel extends AbstractTableModel implements Observer{

	private static final int COLUMN_ADDRESS = 0;
	private static final int COLUMN_HEIGHT = 1;
	private static final int COLUMN_PINGMC = 2;
	private static final int COLUMN_REILABLE = 3;
	private static final int COLUMN_INITIATOR = 4;
	private static final int COLUMN_FINDING_AGO = 5;
	private static final int COLUMN_ONLINE_TIME = 6;
	private static final int COLUMN_VERSION = 7;
	
	private Timer timer = new Timer();
	
	private List<Peer> peers;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"IP", "Height", "Ping mc", "Reliable", "Initiator", "Finding ago", "Online Time", "Version"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,false,false,false,false,false,false,false};
	
	public PeersTableModel()
	{
		Controller.getInstance().addActivePeersObserver(this);
		
		this.timer.cancel();
		this.timer = new Timer();
		
		TimerTask action = new TimerTask() {
	        public void run() {
	        		fireTableDataChanged();	        	
	        }
		};
		
		this.timer.schedule(action, 
				Settings.getInstance().getPingInterval(),
				Settings.getInstance().getPingInterval()
				);
	}
	
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object item = getValueAt(0, c);
		return item==null? null : item.getClass();
    }
	
	// читаем колонки которые изменяем высоту	   
		public Boolean[] get_Column_AutoHeight(){
			
			return this.column_AutuHeight;
		}
	// устанавливаем колонки которым изменить высоту	
		public void set_get_Column_AutoHeight( Boolean[] arg0){
			this.column_AutuHeight = arg0;	
		}
		
	
	@Override
	public int getColumnCount() 
	{
		return columnNames.length;
	}

	@Override
	public String getColumnName(int index) 
	{
		return columnNames[index];
	}

	@Override
	public int getRowCount() 
	{
		if(peers == null)
		{
			return 0;
		}
		
		return peers.size();
	}

	@Override
	public Object getValueAt(int row, int column)
	{
		if(peers == null || this.peers.size() -1 < row)
		{
			return null;
		}
		
		Peer peer = peers.get(row);
		
		if(peer == null || DBSet.getInstance().isStoped())
			return null;
			
		PeerInfo peerInfo = DBSet.getInstance().getPeerMap().getInfo(peer.getAddress());
		switch(column)
		{
			case COLUMN_ADDRESS:
				return peer.getAddress().getHostAddress();

			case COLUMN_HEIGHT:
				return Controller.getInstance().getHWeightOfPeer(peer);
			
			case COLUMN_PINGMC:
				if(peer.getPing() > 1000000) {
					return Lang.getInstance().translate("Waiting...");
				} else {
					return peer.getPing();
				}
			
			case COLUMN_REILABLE:
				return peerInfo.getWhitePingCouner();
			
			case COLUMN_INITIATOR:
				if(peer.isWhite()) {
					return Lang.getInstance().translate("You");
				} else {
					return Lang.getInstance().translate("Remote");
				}
			
			case COLUMN_FINDING_AGO:
				return DateTimeFormat.timeAgo(peerInfo.getFindingTime());
				
			case COLUMN_ONLINE_TIME:
				return DateTimeFormat.timeAgo(peer.getConnectionTime());

			case COLUMN_VERSION:
				return Controller.getInstance().getVersionOfPeer(peer).getA();
				
		}
		
		return null;
	}

	@Override
	public void update(Observable o, Object arg) 
	{	
		try
		{
			this.syncUpdate(o, arg);
		}
		catch(Exception e)
		{
			//GUI ERROR
		}
	}
	
	@SuppressWarnings("unchecked")	
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		if(message.getType() == ObserverMessage.LIST_PEER_TYPE)
		{
			this.peers = (List<Peer>) message.getValue();
		
			this.fireTableDataChanged();
		}
	}

	public void removeObservers() 
	{
		Controller.getInstance().removeActivePeersObserver(this);
		
	}
	
	 

	
	
	
}
