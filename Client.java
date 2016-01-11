import java.io.*;
import java.net.*;

/**
 * @author otobraz
 * Connect to Chat Server and prints all the messages received from the server. 
 * Allows user to send messages to the server.
 */
public class Client{
	
	public static final String SERVER_HOSTNAME = "192.168.1.15";
	public static final int SERVER_PORT = 2002;
	public static Socket socket;
	
	public static void main(String[] args){
		BufferedReader in = null;
		PrintWriter out = null;
		try{
			//Connect to Chat Server
			socket = new Socket(SERVER_HOSTNAME, SERVER_PORT);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			System.out.println("Connected to server " + SERVER_HOSTNAME + ":" + SERVER_PORT);
		}catch (IOException e){
			System.err.println("Can not establish connection to " + SERVER_HOSTNAME + ":" + SERVER_PORT);
			e.printStackTrace();
			System.exit(-1);
		}
		
		try{
			String message;
			while((message = in.readLine()) != null){
				int i = 0;
				for(char c: message.toCharArray()){
					if(c == ' '){
						i++;
					}else{ 
						message = message.substring(i,message.length());
						if(message.equals("Let me know your time")){
							out.println(""+System.currentTimeMillis());
							out.flush();
						}else{
							System.out.println(message);
						}
						break;
					}
				}
			}
		}catch (IOException e){
			System.err.println("Connection to server broken");
			e.printStackTrace();
		}
	}
}


