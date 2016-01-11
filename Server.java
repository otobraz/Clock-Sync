import java.net.*;
import java.io.*;

public class Server {
	
	public static final int PORT = 2002;
	private static int id = 0;
	public static void main(String[] args){
		
		// Open server socket for listening
		ServerSocket serverSocket = null;
		try{
			serverSocket = new ServerSocket(PORT);
			System.out.println("Server started on port " + PORT);
		}catch (IOException e){
			System.out.println("Can not start listening on port " + PORT);
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Start ServerDispatcher thread
		ServerDispatcher serverDispatcher = new ServerDispatcher();
			 
		// Accept and handle client connections
		while(true){
			try{
				Socket socket = serverSocket.accept();
				ClientInfo clientInfo = new ClientInfo();
				clientInfo.setClientSocket(socket);
				EventController eventController = new EventController(clientInfo, serverDispatcher);
				ClientHandler clientHandler = new ClientHandler(clientInfo, serverDispatcher);
				clientInfo.setEventController(eventController);
				clientInfo.setClientHandler(clientHandler);
				clientInfo.setClientId(id); 
				id++;
				clientInfo.setMaster(false);
				serverDispatcher.addClient(clientInfo);
				clientHandler.startElection();
				eventController.start();
				clientHandler.start();
				clientHandler.sendMessage(clientInfo,"Election started by Client " + clientInfo.getClientId());
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}
