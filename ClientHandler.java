import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientHandler extends Thread{

	private ArrayList<String> mMessageQueue = new ArrayList<String>();
	private long[] rTTS = new long[10];
	private long[] times = new long[10];
	private ServerDispatcher serverDispatcher;
	private ClientInfo clientInfo;
	private PrintWriter mOut;
	private long sumOfTimes = 0;
	private int numberOfMessages = 0;
	
	public ClientHandler(ClientInfo clientInfo, ServerDispatcher serverDispatcher) throws IOException{
		this.clientInfo = clientInfo;
		this.serverDispatcher = serverDispatcher;
		Socket socket = clientInfo.getClientSocket();
		mOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	}
	
	/**
	 * Adds given message to the message queue and notifies this thread
	 * (actually getNextMessageFromQueue method) that a message is arrived.
	 * sendMessage is called by other thread(ServerDispatcher)	
	 */
	public synchronized void sendMessage(ClientInfo from, String message){
		rTTS[serverDispatcher.getClients().indexOf(clientInfo)] = 
				System.currentTimeMillis() - serverDispatcher.getMessageSentAt();
		mMessageQueue.add(message);
		notify();
	}
	
	/**
	 * @return and deletes the next message from the message queue. If the queue is
	 * empty, falls in sleep until notified for message arrival by sendMessage method
	 */
	private synchronized String getNetMessageFromQueue() throws InterruptedException{	
		while(mMessageQueue.size() == 0){
			if(!clientInfo.isMaster()){
				wait((serverDispatcher.getClients().indexOf(clientInfo)*100) + 5250);
				if(mMessageQueue.size() == 0){
					return "Timeout";
				}
			}else{
				wait();
			}
		}
		String message = (String) mMessageQueue.get(0);
		mMessageQueue.remove(0);
		return message;
	}
	
	/**
	 * Sends given message to the client's socket
	 */
	public void sendMessageToClient(String message){
		mOut.println(message);
		mOut.flush();
	}
	
	private void berkeleyAlgorithm(String message) throws InterruptedException, IOException{
		if(clientInfo.isMaster()){
			numberOfMessages++;
			sumOfTimes += Long.parseLong(message);
			if(numberOfMessages == serverDispatcher.getClients().size()){
				Long timeToAdjust = Math.abs(sumOfTimes) / serverDispatcher.getClients().size();							
				for(int i = 0; i < numberOfMessages; i++){
					long t = -clientInfo.getTime() + timeToAdjust - rTTS[i]/2;
					serverDispatcher.getClients().get(i).getClientHandler().sendMessage(clientInfo, ""+t);
				}
				sumOfTimes = 0;
				numberOfMessages = 0;
				message = getNetMessageFromQueue();
				long newTime = clientInfo.getTime() + Long.parseLong(message);
				Date date = new Date(newTime);
				sendMessageToClient("*date -s " + new SimpleDateFormat("hh:mm:ss").format(date));
			}	
		}else{
			long newTime = clientInfo.getTime() + Long.parseLong(message);
			Date date = new Date(newTime);
			sendMessageToClient("*date -s " + new SimpleDateFormat("hh:mm:ss").format(date));
		}
	}
	public void startElection(){
		serverDispatcher.setOrigin(clientInfo.getClientId());
		serverDispatcher.setElectionRunning(true);
	}
	
	public void ringAlgorithm(int origin, String message) throws InterruptedException{
		sendMessageToClient("Election in process...");
		ArrayList<ClientInfo> clients = serverDispatcher.getClients();
		if(clients.indexOf(clientInfo) == clients.size()-1){
			clientInfo.setMaster(false);
			clients.get(0).getClientHandler().
			sendMessage(clientInfo, message + "|" + clientInfo.getClientId());
		}else{
			clientInfo.setMaster(false);
			clients.get(clients.indexOf(clientInfo)+1).getClientHandler().
			sendMessage(clientInfo, message + "|" + clientInfo.getClientId());
		}
		sendMessageToClient("Waiting");
		message = getNetMessageFromQueue();
		if(clientInfo.getClientId() == origin && serverDispatcher.isElectionRunning()){
			int max = -1;
			sendMessageToClient("List received: " + message);
			for(char n: message.toCharArray()){
				if(n != '|' && Integer.parseInt(""+n) > max){
					max = Integer.parseInt(""+n);
				}
			}
			ClientInfo newMaster = serverDispatcher.getClientyById(max);				
			serverDispatcher.setMaster(newMaster);	
			newMaster.setMaster(true);
			serverDispatcher.setOrigin(-1);
			serverDispatcher.setElectionRunning(false);
			serverDispatcher.broadcastMessage("New Master: " + newMaster.getClientId());
			message = getNetMessageFromQueue();
		}
		sendMessageToClient(message);
	}
	/**
	 * Until interrupted, reads messages from the message queue
	 * and sends them to the client's socket
	 */
	public void run(){
		try{
			while(!isInterrupted()){
				ClientInfo c1 = null;
				String message = getNetMessageFromQueue();
				for(ClientInfo c: serverDispatcher.getClients()){
					if(c != null){
						try {
							c.getClientSocket().getOutputStream().write(" ".getBytes());
						} catch (IOException e) {
							c1 = c;
							c.getClientHandler().interrupt();
						} 
					}
				}
				serverDispatcher.deleteClient(c1);
				if(message.trim().equals("Timeout") && !serverDispatcher.isElectionRunning()){
					startElection();
				}
				if(serverDispatcher.isElectionRunning()){
					if(clientInfo.getClientId() == serverDispatcher.getOrigin()){
						ringAlgorithm(clientInfo.getClientId(),"");
					}
					else{
						ringAlgorithm(serverDispatcher.getOrigin(),message);
					}
				}else if(!serverDispatcher.isElectionRunning()){
					berkeleyAlgorithm(message);
				}
			}
		}catch(Exception e){
			// Communication problem
		}
		
		// Communication is broken. Interrupt both listener and sender threads
		clientInfo.getEventController().interrupt();
		clientInfo.getClientHandler().interrupt();
		serverDispatcher.deleteClient(clientInfo);
	}
}
