import java.io.*;

import java.io.IOException;
import java.net.UnknownHostException;

public class ChatClientDemo {

	public static void main(String[] args) {
		ChatClient chatter = new ChatClient();
		Thread responseThread = new Thread(chatter);
		responseThread.start();
	
	}
}
