import java.net.Socket;

public class ClientInfo {
	
	private Socket clientSocket = null;
	private EventController eventController = null;
	private ClientHandler clientHandler = null;
	private long time;
	
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	private int clientId;
	private boolean isMaster;
	
	public Socket getClientSocket() {
		return clientSocket;
	}
	
	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public EventController getEventController() {
		return eventController;
	}
	
	public void setEventController(EventController setEventController) {
		this.eventController = setEventController;
	}
	
	public ClientHandler getClientHandler() {
		return clientHandler;
	}
	
	public void setClientHandler(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}
	
	public int getClientId() {
		return clientId;
	}
	
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	
	public boolean isMaster() {
		return isMaster;
	}
	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}
}
