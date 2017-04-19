package gui.models;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

import settings.Settings;
import utils.DateTimeFormat;
import utils.ObserverMessage;
import controller.Controller;
import database.DBSet;
import database.PeerMap.PeerInfo;
import lang.Lang;
import network.Peer;

@SuppressWarnings("serial")
public class KnownPeersTableModel extends AbstractTableModel implements Observer{

//	private static final int COLUMN_ADDRESS = 0;
//	public static final int COLUMN_CONNECTED = 1;
	private static final int COLUMN_ADDRESS = 0;
	private static final int COLUMN_HEIGHT = 1;
	private static final int COLUMN_PINGMC = 2;
	private static final int COLUMN_REILABLE = 3;
	private static final int COLUMN_INITIATOR = 4;
	private static final int COLUMN_FINDING_AGO = 5;
	private static final int COLUMN_ONLINE_TIME = 6;
	private static final int COLUMN_VERSION = 7;
	
	private List<Peer> peers;
	private ArrayList<Boolean> peersStatus = new ArrayList<Boolean>();
	
//	private String[] columnNames = {"IP", Lang.getInstance().translate("Connected now")};
//	String[] columnNames = new String[]{"IP", "Height", "Ping mc", "Reliable", "Initiator", "Finding ago", "Online Time", "Version"};
	 String[] columnNames = Lang.getInstance().translate(new String[]{"IP", "Height", "Ping mc", "Reliable", "Initiator", "Finding ago", "Online Time", "Version"});
	
	static Logger LOGGER = Logger.getLogger(KnownPeersTableModel.class.getName());

	public List<String> getPeers()
	{
		List<String> peersstr= new ArrayList<String>(); 
		for(Peer peer: peers)
		{
			peersstr.add(peer.getAddress().getHostAddress().toString());
		}
		return peersstr;
	}
	
	public void addAddress(String address) 
	{
		InetAddress address1;
		try {
			address1 = InetAddress.getByName(address);
			Peer peer = new Peer(address1);
			peers.add(peer);
			peersStatus.add(false);
			this.fireTableDataChanged();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			LOGGER.error(e.getMessage(),e);
		}
	}
	
	
	public void deleteAddress(int row) 
	{
		String address = this.getValueAt(row, 0).toString();
		int n = JOptionPane.showConfirmDialog(
				new JFrame(), Lang.getInstance().translate("Do you want to remove address %address%?").replace("%address%", address),
				Lang.getInstance().translate("Confirmation"),
                JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			peers.remove(row);
			peersStatus.remove(row);
			this.fireTableDataChanged();
		} 
	}
	
	@SuppressWarnings("unused")
	public KnownPeersTableModel()
	{
		peers = Settings.getInstance().getKnownPeers();
		
		for(Peer peer: peers)
		{
			peersStatus.add(false);
		}
		Controller.getInstance().addActivePeersObserver(this);
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
	/*	if(peers == null)
		{
			return null;
		}
		
		Peer peer = peers.get(row);
		if  (peer == null)
			return null;

		Boolean peerStatus = peersStatus.get(row);
		if  (peerStatus == null)
			return null;

		switch(column)
		{
			case COLUMN_ADDRESS:
				return peer.getAddress().getHostAddress().toString();
			
			case COLUMN_CONNECTED:
				return peersStatus;
		}
		
		return null;
		*/
		
		if(peers == null || this.peers.size() -1 < row )
		{
			return null;
		}
		
		Peer peer = peers.get(row);
		
		if(peer == null || DBSet.getInstance().isStoped())
			return null;
			
		PeerInfo peerInfo = DBSet.getInstance().getPeerMap().getInfo(peer.getAddress());
		if (peerInfo == null)
			return null;
		
		switch(column)
		{
			case COLUMN_ADDRESS:
				return peer.getAddress().getHostAddress();

			case COLUMN_HEIGHT:
				if(!peer.isUsed()) {
					int banMinutes = DBSet.getInstance().getPeerMap().getBanMinutes(peer);
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
		    //(JTable)this.get
		    //component.setRowSelectionInterval(row, row);
		    
			List<Peer> peersBuff = (List<Peer>) message.getValue();
			int n = 0;
			
			for(Peer peer1: this.peers)
			{
				boolean connected = false;
				
				for(Peer peer2: peersBuff)
				{
					if(peer1.getAddress().toString().equals(peer2.getAddress().toString()))
					{
						this.peersStatus.set(n, true);
						connected = true;
					}
				}
				
				if(!connected)
				{
					this.peersStatus.set(n, false);
					this.fireTableRowsUpdated(n, n);
				}
				
				n++;
			}	
		}
	}

	public void removeObservers() 
	{
		Controller.getInstance().removeActivePeersObserver(this);
		
	}
}
