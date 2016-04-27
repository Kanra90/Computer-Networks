import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;

public class Ipv6Client implements Runnable{
	public static byte[] byteArray;
	static Socket socket;
	static DataOutputStream out;
	static DataInputStream in;
	
	public static void main(String[]args) throws IOException, InterruptedException{
	   	int count = 0;

		do{
			socket = new Socket("76.91.123.97", 22222);
	       	//out/in streams
	       	out = new DataOutputStream(socket.getOutputStream());
	       	in = new DataInputStream(socket.getInputStream());
	       	
	       	//get a random length size
	    	int n = randomizeLength();
	    	//System.out.println(n);
		   	new Thread(new Ipv6Client(n)).start();
	    	//randomize size
	    	//Ipv6Client client = new Ipv6Client(n);
	    	out.write(byteArray);
	    	out.flush();
	    	count++;
	    	Thread.sleep(1000);
		}while(count<=10);
    	socket.close();
	}
	
	public Ipv6Client(int size) throws IOException{
		//allocate buffer to size
		//data length+payload size
		byteArray = new byte[40 + size];
		
		//version
		byteArray[0] = 0x60;
		//size = number of bits
		byteArray[4] = (byte) ((size>>8) & 0xFF);	
		byteArray[5] = (byte) (size& 0xFF);
		byteArray[6] = 17;
		byteArray[7] = 20;
		//byte 8 - 17 filled with 0
		//2 bytes of all 1's
		
		//we half the size because the count is in octets
		byteArray[18] = (byte) 0xFF;
		byteArray[19] = (byte) 0xFF;
		//2 bytes of all 1s
		//ipv4 address
		byteArray[20] = 76;
		byteArray[21] = 91;
		byteArray[22] = 123;
		byteArray[23] = 97;

		//bytes 24 - 33 full of 0s
		//2 bytes of all 1s
		byteArray[34] = (byte) 0xFF;
		byteArray[35] = (byte) 0xFF;
		byteArray[36] = 76;
		byteArray[37] = 91;
		byteArray[38] = 123;
		byteArray[39] = 97;
		//rest of data has 0s
	}
	public static int randomizeLength(){
		
		Random rd = new Random();
		//reasonable size in OCTETS 100 octets
		int n = rd.nextInt(31);
		//make sure divisible by 8
		n = n*8;
		if(n<0){
			n = n*-1;
		}
		//make payload size in bytes
		return n;
		
	}
	public void run() {
		//string for response
		String s;
		try {
	    	//get server responses that aren't empty
			while ((s = in.readLine()) != null) {	
	    		//print server response
				System.out.println(s);
	    	}
	    } catch (Exception e) {
	    	System.out.println("Connection Closed");
	    }
	  }
}
