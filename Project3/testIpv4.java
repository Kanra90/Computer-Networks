import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class testIpv4{
	static DataInputStream in = null;
	static DataOutputStream out = null;
	static Socket socket;
	private static int data;
	public static byte[] byteArray;
	public static byte[] payLoad;
	static short totalLen;
	public static short[] shortArr;
	public static int counter = 0;
	public static int udpSize;
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
        	testIpv4 udp = new testIpv4(0,24,0);
        	
    		out.write(udp.getByteArr());
    		int port = in.readUnsignedShort();
    		//System.out.println(port);
    		//loop through subsequent packets
    		int count = 2;
    		
			testIpv4 udp1 = new testIpv4(count, 28 + count, port);
			out.write(udp1.getByteArr());

			int firstHex = in.readUnsignedShort();
			int secondHex = in.readUnsignedShort();
    		System.out.println(printer.toHexString(firstHex) + "" + printer.toHexString(secondHex));
			count = 2;

	}

	public testIpv4(int udpCount, int size, int dport){
		//make room for header and data
		byteArray = new byte[size];
		udpSize = udpCount;
		destinationPort = dport;
		//add constant and unimplemented numbers
		
		if(udpSize==0){
			ipv4Packet();
			checkSumHelper();
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
		short num2 = (short)(28 + udpSize);
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
		
		//length will be 8 + dataSize
		short num1 = (short) (8 + udpSize);
		byte [] arr1 = splitShort(num1);
		byteArray[24] = arr[0];
		byteArray[25] = arr[1];	
		
		udpHelper();
		//26 and 27 gets checkSum
	}
	public static void udpHelper(){
		
		//construct pseudo header in 16 bit shorts
		short[] udpArr = new short[10];
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
		udpArr[5] = (short) (8 + udpSize);
		//source port
		udpArr[6] = 0;
		//destination port
		udpArr[7] = (short) (destinationPort & 0xFFFF);
		//udp length
		udpArr[8] = (short) (8 + udpSize);
		//checkSum belongs in 9
		udpArr[9] = 0;
		
		short udpCheckSum = checkSum(udpArr);
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
			sum = sum + buf[i];
			if((sum & 0xFFFF0000) != 0){
				sum = sum & 0xFFFF;
				sum++;
			}	
		}
		return (short) ~(sum & 0xFFFF);
	}
	public static short udpCheckSum(byte[]buf){
		short sum = 0;
		
		for(int i = 0;i<buf.length;i++){
			sum = (short) (sum + buf[i]);
			if((sum & 0xFF00) !=0){
				sum = (short) (sum & 0xFF);
				sum++;
			}
		}
		return (short) ~(sum & 0xFF);
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