import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClientTest implements Runnable{

	private BufferedReader in;
	private PrintWriter out;
	private BufferedReader userInput;
	private Socket socket;
	
	public ChatClientTest(){
		
		try{
		//initializes socket connection, transfer between server and client, and reader for user input 
			socket = new Socket("76.91.123.97", 22222);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			userInput = new BufferedReader(new InputStreamReader(System.in));
			out = new PrintWriter(socket.getOutputStream());
			System.out.println("*Connection established.*");
		}
		catch(Exception e){};
	}
	
	public void sendMessage() {			
		int counter = 0;
		String s;
		try {
			while (true){			
				if (counter >= 1){
					System.out.println("Enter your message: ");
					s = userInput.readLine();
				}else {
					System.out.println("Enter your name for chat.");
					s = userInput.readLine();
				}
				//send to server
				out.println(s);
				out.flush();
				
				
				//closes communication with the server
				if (s.equalsIgnoreCase("bye")){
					socket.close();
					break;
				}
				counter++;
			}
		}catch (UnknownHostException e) {
			System.out.println("Host is unknown, connection was unsuccesful.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Communication between server and client was lost or has experienced some errors.");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			while (true){
				//print server response
				System.out.println(in.readLine());
				if (in.readLine().equalsIgnoreCase("bye")){
					in.close();
					break;
				}
			}
		}catch (UnknownHostException e) {
			System.out.println("Host is unknown, connection was unsuccesful.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Communication between server and client was lost or has experienced some errors.");
			e.printStackTrace();
		}
	}
	

}
