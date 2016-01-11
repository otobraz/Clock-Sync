import java.util.*;

/**
 * @author otobraz
 * ServerDispatcher class is purposed to listen for messages received
 * from clients and to dispatch them to all the clients connected to the
 * chat server.
 */
public class ServerDispatcher{
	
	private ArrayList<ClientInfo> clients = new ArrayList<ClientInfo>();
	private ClientInfo master;
	private int origin;
	private boolean isElectionRunning = false;
	private long messageSentAt;
	
	public long getMessageSentAt() {
		return messageSentAt;
	}

	public void setMessageSentAt(long messageSentAt) {
		this.messageSentAt = messageSentAt;
	}

	public synchronized ArrayList<ClientInfo> getClients() {
		return clients;
	}

	public synchronized void setClients(ArrayList<ClientInfo> clients) {
		this.clients = clients;
	}
	
	public ClientInfo getMaster() {
		return master;
	}

	public void setMaster(ClientInfo master) {
		this.master = master;
	}
	
	public int getOrigin() {
		return origin;
	}

	public void setOrigin(int origin) {
		this.origin = origin;
	}

	public boolean isElectionRunning() {
		return isElectionRunning;
	}

	public void setElectionRunning(boolean isElectionRunning) {
		this.isElectionRunning = isElectionRunning;
	}

	/**
	 * Adds given client to the server's client list
	 */
	public synchronized void addClient(ClientInfo aClientInfo){
		clients.add(aClientInfo);
	}
	
	/**
	 * Deletes given client from the server's client list
	 * if the client is in the list
	 */
	public synchronized void deleteClient(ClientInfo clientInfo){
		int clientIndex = clients.indexOf(clientInfo);
		if(clientIndex != -1){
			clients.remove(clientIndex);
		}
	}
	
	public synchronized ClientInfo getClientyById(int id){
		for(ClientInfo clientInfo: clients){
			if(clientInfo.getClientId() == id)
				return clientInfo;
		}
		return null;
	}

	public synchronized void broadcastMessage(String message){
		for(ClientInfo clientInfo: clients){
			clientInfo.getClientHandler().sendMessage(master,message);
		}
	}
}

