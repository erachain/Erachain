package network;

import java.net.ServerSocket;
import java.net.Socket;
 import org.apache.log4j.Logger;

import controller.Controller;
import core.transaction.Transaction;
import database.DBSet;
import database.PeerMap;
import datachain.DCSet;
import gui.status.WalletStatus;
import lang.Lang;
import ntp.NTP;
import settings.Settings;

public class ConnectionAcceptor extends Thread{

	private ConnectionCallback callback;
	
	private ServerSocket socket;
	
	private boolean isRun;
	
	static Logger LOGGER = Logger.getLogger(ConnectionAcceptor.class.getName());
	
	public ConnectionAcceptor(ConnectionCallback callback)
	{
		this.callback = callback;
	}
	
	public void run()
	{
		this.isRun = true;
		
		PeerMap map = Controller.getInstance().getDBSet().getPeerMap();
		while(isRun)
		{
			try{ // NEED
				Thread.sleep(100);	
			} catch(Exception es)
			{
			}

			try
			{	
				
				if(socket == null)
				{
					//START LISTENING
					socket = new ServerSocket(Controller.getInstance().getNetworkPort()); 
				}
				
				
				//CHECK IF WE HAVE MAX CONNECTIONS CONNECTIONS
				if(Settings.getInstance().getMaxConnections() <= callback.getActivePeersCounter(false))
				{
					//IF SOCKET IS OPEN CLOSE IT
					if(!socket.isClosed())
					{
						socket.close();
					}
					
					//Thread.sleep(50);
				}
				else
				{		
					//REOPEN SOCKET
					if(socket.isClosed())
					{
						socket = new ServerSocket(Controller.getInstance().getNetworkPort()); 
					}
					
					//ACCEPT CONNECTION
					Socket connectionSocket = socket.accept();
					
					//CHECK IF SOCKET IS NOT LOCALHOST || WE ARE ALREADY CONNECTED TO THAT SOCKET || BLACKLISTED
					if(
							/*connectionSocket.getInetAddress().isSiteLocalAddress() 
							 * || connectionSocket.getInetAddress().isAnyLocalAddress() 
							 * || connectionSocket.getInetAddress().isLoopbackAddress() 
							 *  */
							
							/*
							(
									(NTP.getTime() < Transaction.getPOWFIX_RELEASE() ) 
									&& 
									callback.isConnectedTo(connectionSocket.getInetAddress())
							)
							||
							*/
							map.isBanned(connectionSocket.getInetAddress().getAddress())
							)
						
					{
						//DO NOT CONNECT TO OURSELF/EXISTING CONNECTION
						// or BANNED
						connectionSocket.close();
					}
					else
					{
						
						if (!this.isRun)
							return;

						//CREATE PEER
						////new Peer(callback, connectionSocket);
						LOGGER.info("START ACCEPT CONNECT FROM " + connectionSocket.getInetAddress().getHostAddress()
								+ " isMy:" + Network.isMyself(connectionSocket.getInetAddress())
								+ " my:" + Network.getMyselfAddress());

						callback.startPeer(connectionSocket);
					}
				}
			}
			catch(Exception e)
			{

				try{ 
					Thread.sleep(1000);	
				} catch(Exception es)
				{
				}

				//LOGGER.info(e.getMessage(),e);
				//LOGGER.info(Lang.getInstance().translate("Error accepting new connection") + " - " + e.getMessage());			
			}
		}
	}
	
	public void halt()
	{
		this.isRun = false;
	}
}
