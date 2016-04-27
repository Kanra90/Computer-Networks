import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Random;

public class UDPClient{
	static DataInputStream in = null;
	static DataOutputStream out = null;
	static Socket socket;
	public static byte[] byteArray;
	public static short[] shortArr;
	public static int udpPayLoad;
	public static int totalLen;
	public static int destinationPort;
	public static short finalcheckSum;
	
	@SuppressWarnings("deprecation")
	public static void main(String[]args) throws UnknownHostException, IOException, InterruptedException{
    	
    		//open socket connection
        	socket = new Socket("76.91.123.97", 22222);
        	//out/in streams
        	out = new DataOutputStream(socket.getOutputStream());
        	in = new DataInputStream(socket.getInputStream());
        	Integer printer = null;
        	//make first packet, with deadbeef
        	UDPClient udp = new UDPClient(0,24,0);
        	
        	
    		//long startTime = System.nanoTime();
    		out.write(udp.getByteArr());

    		//long endTime = System.nanoTime();
    		//long duration = (endTime - startTime);
    		//System.out.println("Time: " + duration/1000000);
    		//get back port number
    		int port = in.readUnsignedShort();
    		//System.out.println(port);
    		//loop through subsequent packets
    		int count = 2;
    		
    		long avgTime = 0;
    		//System.out.println("Destination port: " + port);
    		for(int i = 2;i<1025;i=i*2){  			
    			UDPClient udp1 = new UDPClient(i, 28+i, port);
    			long startTime = System.currentTimeMillis();
    			out.write(udp1.getByteArr());

    			int firstHex = in.readUnsignedShort();
    			int secondHex = in.readUnsignedShort();
    			long endTime = System.currentTimeMillis();
    			
    			long time = endTime-startTime;
    			avgTime = avgTime + time;
    			
        		System.out.println("Response: 0x" + printer.toHexString(firstHex) + "" + printer.toHexString(secondHex));  
    			System.out.println(time + "ms");

    		}
    		long averageTime = avgTime/10;
    		System.out.println("Average Time: " + averageTime + "ms");
	}

	public UDPClient(int udpCount, int size, int dport){
		//make room for header and data
		byteArray = new byte[size];
		totalLen = size;
		udpPayLoad = udpCount;
		destinationPort = dport;
		//add constant and unimplemented numbers
		//catches first deadbeef
		if(udpPayLoad==0){
			ipv4Packet();
			checkSumHelper();
		//UDP packets
		}else{
			ipv4UDPHeader();
			checkSumHelper();
		}
		//calculate checkSum
	}
	public void ipv4Packet(){
		byteArray[0] = 69;
		byteArray[1] = 0;
		//get total length as 24, 110000
		byteArray[3] = 24;	

		byteArray[6] = 64;
		byteArray[7] = 0;
		byteArray[8] = 50;
		byteArray[9] = 17;
		//checksum, 0 for now til finish rest of it

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
		
		byteArray[20] = (byte) 0xDE;
		byteArray[21] = (byte) 0xAD;
		byteArray[22] = (byte) 0xBE;
		byteArray[23] = (byte) 0xEF;
		
	}
	public void ipv4UDPHeader(){
		//ipv4 packet stuff
		byteArray[0] = 69;
		byteArray[1] = 0;
		
		//make sure to add new data size
		//include size of both headers and data
		short num2 = (short)(totalLen & 0xFFFF);
		byte[] arr2 = splitShort(num2);
		byteArray[2] = arr2[0];
		byteArray[3] = arr2[1];

		//hard coded junk
		byteArray[6] = 64;
		byteArray[7] = 0;
		byteArray[8] = 50;
		byteArray[9] = 17;
		
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

		//start from 20, beginning of udp packet
		//construct source port
		byteArray[20] = 0;
		byteArray[21] = 0;
		
		//construct destination port
		short num = (short) (destinationPort & 0xFFFF);
		byte [] arr = splitShort(num);
		byteArray[22] = arr[0];
		byteArray[23] = arr[1];
		
		//length will be 8 + dataSize, 
		short num1 = (short) (8 + udpPayLoad);
		byte [] arr1 = splitShort(num1);
		byteArray[24] = arr1[0];
		byteArray[25] = arr1[1];	

		//create random array size from udpSize
		Random rd = new Random();
		byte[] randBytes = new byte[udpPayLoad];
		rd.nextBytes(randBytes);
		int j = 0;
		for(int i = 28;i<28+udpPayLoad;i++){
			byteArray[i] = randBytes[j];
			j++;		
		}			
		udpHelper();
		//26 and 27 gets checkSum
		//leave 0 for now
	}
	public static void udpHelper(){
		
		//construct pseudo header in 16 bit shorts
		short[] udpArr = new short[10 + udpPayLoad/2];
		//put in the values from the pseudo header first:
		//source
		udpArr[0] = toShort(byteArray[12],byteArray[13]);
		udpArr[1] = toShort(byteArray[14],byteArray[15]);
		//destination
		udpArr[2] = toShort(byteArray[16],byteArray[17]);
		udpArr[3] = toShort(byteArray[18],byteArray[19]);
		//zeros and protocol
		udpArr[4] = 17;
		//udp length
		udpArr[5] = (short) (8 + udpPayLoad);
		//source port
		udpArr[6] = 0;
		//destination port
		udpArr[7] = (short) (destinationPort & 0xFFFF);
		//udp length
		udpArr[8] = (short) (8 + udpPayLoad);
		//checkSum belongs in 9
		udpArr[9] = 0;
		
		//construct random parts
		int j = 28;		
		for(int i = 10;i<udpArr.length;i++){
			//combine the 2 bytes of each to make shorts
			udpArr[i] = toShort(byteArray[j],byteArray[j+1]);
			j= j+2;		
		}	

		short udpCheckSum = checkSum(udpArr);
		udpArr[9] = udpCheckSum;
		byte[]arr = splitShort(udpCheckSum);
		byteArray[26] = arr[0];
		byteArray[27] = arr[1];
		
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
		finalcheckSum = finalSum;
		byte[]arr = splitShort(finalSum);
		byteArray[10] = arr[0];
		byteArray[11] = arr[1];
	}
	public byte[] getByteArr(){
		return byteArray;
	}
	public static short checkSum(short[]buf){
		long sum = 0;
		
		for(int i = 0;i<buf.length;i++){
			sum = sum + (buf[i] & 0xFFFF);
			if((sum & 0xFFFF0000) != 0){
				sum = sum & 0xFFFF;
				sum++;
			}	
		}
		return (short) ~(sum & 0xFFFF);
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
}
