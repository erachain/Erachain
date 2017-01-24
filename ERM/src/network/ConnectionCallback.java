package network;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import network.message.Message;

public interface ConnectionCallback {

	void onConnect(Peer peer, boolean asNew);
	void onDisconnect(Peer peer);
	void onError(Peer peer, String error);
	boolean isKnownAddress(InetAddress address, boolean andUsed);
	boolean isKnownPeer(Peer peer, boolean andUsed);
	List<Peer> getActivePeers(boolean onlyWhite);
	Peer getKnownPeer(Peer peer);
	void onMessage(Message message);
	Peer startPeer(Socket socket);
	
}
