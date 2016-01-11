import java.io.*;
import java.net.*;

/**
 * @author otobraz
 * Listen for client messages and forward them to ServerDispatcher
 */
public class EventController extends Thread{
	
	private ServerDispatcher serverDispatcher;
	private ClientInfo clientInfo;
	private Socket socket;
	private BufferedReader mIn;
	private String message;
	private boolean b;
	
	public EventController(ClientInfo clientInfo, ServerDispatcher aServerDispatcher) throws IOException{
		this.clientInfo = clientInfo;
		serverDispatcher = aServerDispatcher;
		socket = clientInfo.getClientSocket();
		mIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public void requestTime() throws IOException{
		String message = "Let me know your time";
		serverDispatcher.setMessageSentAt(System.currentTimeMillis());
		for(ClientInfo clientInfo: serverDispatcher.getClients()){
			clientInfo.getClientHandler().sendMessageToClient(message);
		}
	}
	
	/**
	 * Until interrupted, reads messages from the client socket, forwards them
	 * to the server dispatcher's queue and notifies the server dispatcher.
	 */
	public void run(){
		while(!isInterrupted()){
			try {
				sleep(5000);
				if(!serverDispatcher.isElectionRunning()){
					b = false;
					if(clientInfo.isMaster()){
						requestTime();
					}
					while(!b){
						if((message = mIn.readLine()) != null){
							clientInfo.setTime(Long.parseLong(message));
							serverDispatcher.getMaster().getClientHandler().sendMessage(clientInfo, message);
							b = true;
						}
					}
				}
			}catch(Exception e){
				
			}
			
		}
		
		// Communication is broken. Interrupt both listener and sender threads
		clientInfo.getEventController().interrupt();
		clientInfo.getClientHandler().interrupt();
		serverDispatcher.deleteClient(clientInfo);
	}
}

