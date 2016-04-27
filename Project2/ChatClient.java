import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ChatClient implements Runnable {

	private static Socket socket;
	private static PrintStream out;
	private static DataInputStream in;
	private static BufferedReader userInput;
	private static boolean first = true;
	public static void main(String[] args) throws IOException {
    	try{
        	//initializing socket connection
    		socket = new Socket("127.0.0.1", 1337);
        	//socket = new Socket("76.91.123.97", 22222);
        	//input stream from server
        	in = new DataInputStream(socket.getInputStream());
        	//output to server
        	out = new PrintStream(socket.getOutputStream());
        	//initialize input stream from user
        	userInput = new BufferedReader(new InputStreamReader(System.in));
    	}
    	catch(UnknownHostException e){
    		System.out.println("Communication Error");
    	}
    	//Thread for server input
    	
    	new Thread(new ChatClient()).start();
    	boolean flag = false;
    	String s = null;
    	while (!flag) {
    		if(first == true){
    			System.out.println("Enter a User Name");
    			s = userInput.readLine();
    			out.println(s);
    			first = false;
    		}
    		else if(s.equalsIgnoreCase("exit")){
   				flag = true;
   				socket.close();
    			out.close();
    			in.close();
    		}
    		else{
    			System.out.println("Enter a message:");
    			s = userInput.readLine();
    			out.println(s);
    			}
    	}  
  }

@SuppressWarnings("deprecation")
public void run() {
	//string for response
	String s;
	try {
    	//get server responses that aren't empty
		while ((s = in.readLine()) != null) {	
    		//print server response
			System.out.println(s);
			if(s.equals("Name in use.")){
				out.close();
    			in.close();
    			socket.close();
			}
    	}
    } catch (Exception e) {
    	System.out.println("Connection Closed");
    }
  }
}