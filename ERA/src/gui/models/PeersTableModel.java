package gui.models;
import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.validation.constraints.Null;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import database.PeerMap.PeerInfo;
import datachain.DCSet;
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
	
	String[] columnNames = new String[]{"IP", "Height", "Ping mc", "Reliable", "Initiator", "Finding ago", "Online Time", "Version"};
	// String[] columnNames = Lang.getInstance().translate(new String[]{"IP", "Height", "Ping mc", "Reliable", "Initiator", "Finding ago", "Online Time", "Version"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,false,false,false,false,false,false,false};
	
	public PeersTableModel()
	{
		Controller.getInstance().addActivePeersObserver(this);
		
		this.timer.cancel();
		this.timer = new Timer();
		
		TimerTask action = new TimerTask() {
	        public void run() {
	        	try {
	        		fireTableDataChanged();
	        	}
				catch(Exception e)
				{
					//LOGGER.error(e.getMessage(),e);
				}
	        }
		};
		
		this.timer.schedule(action, 
				//Settings.getInstance().getPingInterval()>>1,
				5000,
				//Settings.getInstance().getPingInterval()
				5000
				);
	}
	
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
    }
	
	// С‡РёС‚Р°РµРј РєРѕР»РѕРЅРєРё РєРѕС‚РѕСЂС‹Рµ РёР·РјРµРЅСЏРµРј РІС‹СЃРѕС‚Сѓ	   
		public Boolean[] get_Column_AutoHeight(){
			
			return this.column_AutuHeight;
		}
	// СѓСЃС‚Р°РЅР°РІР»РёРІР°РµРј РєРѕР»РѕРЅРєРё РєРѕС‚РѕСЂС‹Рј РёР·РјРµРЅРёС‚СЊ РІС‹СЃРѕС‚Сѓ	
		public void set_get_Column_AutoHeight( Boolean[] arg0){
			this.column_AutuHeight = arg0;	
		}
		
	
		 public Peer get_Peers(int row){
			 if (row<0) return null;
			return peers.get(row);
		 }
	@Override
	public int getColumnCount() 
	{
		return columnNames.length;
	}

	@Override
	public String getColumnName(int index) 
	{
		return Lang.getInstance().translate(columnNames[index]);
	}
	
	public String getColumnNameNO_Translate(int index) 
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
		if(peers == null || this.peers.size() -1 < row )
		{
			return null;
		}
		
		Peer peer = peers.get(row);
		
		if(peer == null || DCSet.getInstance().isStoped())
			return null;
			
		PeerInfo peerInfo = Controller.getInstance().getDBSet().getPeerMap().getInfo(peer.getAddress());
		if (peerInfo == null)
			return null;
		
		switch(column)
		{
			case COLUMN_ADDRESS:
				return peer.getAddress().getHostAddress();

			case COLUMN_HEIGHT:
				if(!peer.isUsed()) {
					int banMinutes = Controller.getInstance().getDBSet().getPeerMap().getBanMinutes(peer);
					if (banMinutes > 0) {
						return Lang.getInstance().translate("Banned") + " " + banMinutes + "m";
					} else {
						return Lang.getInstance().translate("Broken");
					}
				}
				Tuple2<Integer, Long> res = Controller.getInstance().getHWeightOfPeer(peer);
				if(res == null) {
					return Lang.getInstance().translate("Waiting...");
				} else {
					return res;
				}
			
			case COLUMN_PINGMC:
				if(!peer.isUsed()) {
					return Lang.getInstance().translate("Broken");
				} else if(peer.getPing() > 1000000) {
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
			
		} else if (message.getType() == ObserverMessage.UPDATE_PEER_TYPE) {
			Peer peer1 = (Peer) message.getValue();
			int n = 0;
			for(Peer peer2: this.peers)
			{
				if(Arrays.equals(peer1.getAddress().getAddress(),
						peer2.getAddress().getAddress()))
				{
					///this.peersStatus.set(n, true);
					break;
				}
				n++;
			}
			this.fireTableRowsUpdated(n, n);
			
		} else if (message.getType() == ObserverMessage.ADD_PEER_TYPE) {
			
				this.fireTableDataChanged();
				
		} else if (message.getType() == ObserverMessage.REMOVE_PEER_TYPE) {
			
				this.fireTableDataChanged();
		}
	}

	public void deleteObserver() 
	{
		Controller.getInstance().removeActivePeersObserver(this);
		
	}

	
}
