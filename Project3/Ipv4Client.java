import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Random;

public class Ipv4Client implements Runnable{
	static DataInputStream in = null;
	static DataOutputStream out = null;
	static Socket socket;
	private static int data;
	public static byte[] byteArray;
	public static byte[] payLoad;
	static short totalLen;
	public static short[] shortArr;
	
	@SuppressWarnings("deprecation")
	public static void main(String[]args) throws UnknownHostException, IOException, InterruptedException{

    	
    	int counter=0;
    	
    	do{
    		//open socket connection
        	socket = new Socket("76.91.123.97", 22223);
        	//out/in streams
        	out = new DataOutputStream(socket.getOutputStream());
        	in = new DataInputStream(socket.getInputStream());
        	//make payload
        	payLoad();
        	int n = payLoad.length;
        	//pass in size of data
        	new Thread(new Ipv4Client(n)).start();
      
    		ByteArrayOutputStream m = new ByteArrayOutputStream();

    		m.writeTo(out);
    		out.write(byteArray);
    		out.flush();
    		counter++;
    	}while(counter<10);
		
	}
	public Ipv4Client(int data){
		//make room for header and data
		totalLen = (short) (20+data);
		//make total array sized apppropriately
		byteArray = new byte[totalLen];
		//add constant and unimplemented numbers
		addConstValues();
		//calculate checkSum
		checkSumHelper();		
	}

	public static void payLoad(){
		Random rd1 = new Random();
		//reasonable length
		int rand = rd1.nextInt(2000);
		//multiply by 16 so we don't have weird lengths
		rand = rand*16;
		//if negative, adjust
		if(rand<0){
			rand = rand*-1;
		}
		//leave bytes blank, 0s
		payLoad = new byte[rand];
	}
	public void addConstValues(){
		byteArray[0] = 69;
		byteArray[1] = 0;
		//get total length
		byte[] arr = splitShort(totalLen);
		byteArray[2] = arr[0];
		byteArray[3] = arr[1];	
		byteArray[4] = 0;
		byteArray[5] = 0;
		byteArray[6] = 64;
		byteArray[7] = 0;
		byteArray[8] = 50;
		byteArray[9] = 6;
		//checksum, 0 for now til finish rest of it
		byteArray[10] = 0;
		byteArray[11] = 0;
		
		//hard code dummy source code
		byteArray[12] = 76;
		byteArray[13] = 91;
		byteArray[14] = 123;
		byteArray[15] = 97;
		//hard code destination
		byteArray[16] = 76;
		byteArray[17] = 91;
		byteArray[18] = 123;
		byteArray[19] = 97;
		
	}
	public static short toShort(byte a, byte b){
		return (short) (((a<<8) | (b&0xFF) )&0xFFFF);
	}
	
	public static byte[] splitShort(short s){	
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.putShort(s);
		return buffer.array();
	}
	
	public byte toByte(int n){
		byte ret = (byte) (n & 0xFF);
		return ret;
	}
	public static void checkSumHelper(){
		//length of bytes in 16 bit chunks
		shortArr = new short[10];
		int shortCount = 0;
		//only checksum header
		for(int i = 0;i<20;i+=2){
			short s = toShort(byteArray[i],byteArray[i+1]);
			shortArr[shortCount] = s;
			shortCount++;
		}
		short finalSum = checkSum(shortArr);
		//split this short into 2 bytes
		byte[]arr = splitShort(finalSum);
		byteArray[10] = arr[0];
		byteArray[11] = arr[1];
	}
	public static short checkSum(short[]buf){
		long sum = 0;
		
		for(int i = 0;i<buf.length;i++){
			sum = sum + buf[i];
			if((sum & 0xFFFF0000) != 0){
				sum = sum & 0xFFFF;
				sum++;
			}	
		}
		return (short) ~(sum & 0xFFFF);
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