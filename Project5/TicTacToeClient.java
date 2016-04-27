import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TicTacToeClient implements Runnable,java.io.Serializable{
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Socket socket;
	private static Message m;
	boolean gameOver = false;
	boolean Surrender = false;

	public static void main(String[]args) throws IOException, InterruptedException{
		
		Scanner kb = new Scanner(System.in);
		//make tictactoe client
		TicTacToeClient newClient = new TicTacToeClient();
	
		//start run
		new Thread(newClient).start();
		
		System.out.println("Welcome to the Tic Tac Toe Server!");
		System.out.print("Please enter a Username: ");
		String s = kb.nextLine();
		
		//start a connection, send username
		ConnectMessage connect = new ConnectMessage(s);
		newClient.out.writeObject(connect);
		newClient.out.flush();
		int num = 1;
		
			
		do{	
			System.out.println("To start a game with the Robot, enter 1.\n, enter 2 for a player list.");
			num = kb.nextInt();
			
			if(num==1){
				System.out.println("To make a move, enter a row number between 0 and 2,\nfollowed by a column number between 0 and 2");
				System.out.println("To quit at any point, enter 3 for both entries");
				//start game with computer
				StartGameMessage startGame = new StartGameMessage(null);
				newClient.out.writeObject(startGame);
				newClient.out.flush();	
			}else if(num==2){
				CommandMessage newComm = new CommandMessage(CommandMessage.Command.LIST_PLAYERS);
				newClient.out.writeObject(newComm);
				newClient.out.flush();
				num = 3;

			}
		}while(num!=1);
		
				do{
					int move1 = kb.nextInt();		
					int move2 = kb.nextInt();
					
					if(move1==3 && move2==3){
						newClient.Surrender = true;
					}			
					newClient.makeMove(move1, move2);
				}while(newClient.gameOver==false);
				
					
		System.out.println("Thank you for Playing!");
		newClient.in.close();
		newClient.out.close();
		newClient.socket.close();
	}
	public TicTacToeClient() throws IOException{
		socket = new Socket("76.91.123.97",22222);
		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());
	}

	private void changeToMessage(Message m) {		
		switch (m.getType()){
			case BOARD:{
				board(m);
				break;
			}
			case ERROR:{
				System.out.println("Error");
				break;
			}
			case PLAYER_LIST:{
				playerList(m);
				break;
			}
		default:
			break;
		}	
	}
	private void board(Message m) {
		BoardMessage board = (BoardMessage)m;
		byte[][]b = board.getBoard();
		System.out.println("<<<<Board Config>>>>");
		for(int i =0;i<3;i++){
			for(int j=0;j<3;j++){
				System.out.print(" " + b[j][i]);
			}
			System.out.println();
		}
		System.out.println();	
		
		if(Surrender==true){
			board = new BoardMessage(b,BoardMessage.Status.PLAYER1_SURRENDER,(byte) board.getTurn());
		}
		
		switch (board.getStatus()){
			case PLAYER1_SURRENDER:{
				gameOver = true;
				System.out.println("Player 1 has surrendered! Player 2 Wins.");
				break;
			}
			case PLAYER2_SURRENDER:{
				gameOver = true;
				System.out.println("Player 2 has surrendered! Player 1 Wins.");
				break;
			}
			case PLAYER1_VICTORY:{
				gameOver = true;
				System.out.println("Player 1 Wins!");
				break;
			}
			case PLAYER2_VICTORY:{
				gameOver = true;
				System.out.println("Player 2 Wins!");
				break;
			}
			case STALEMATE:{
				gameOver = true;
				System.out.println("StaleMate!");
				break;
			}
			case IN_PROGRESS:{
				break;
			}
			case ERROR:{
				System.out.println("Error: illegal move");
				break;
			}
			default:{
				break;
			}
		}
	}
	/**
	 * Handle PlayerListMessage Message type
	 * @param m
	 */
	private void playerList(Message m) {	
		PlayerListMessage plist = (PlayerListMessage) m;
		String[] strArr = plist.getPlayers();
		System.out.println(">>>>Player List<<<<");
		int i = 0;
		do{
			System.out.println(strArr[i]);
			i++;
		}while(i<strArr.length);		
	}
	private void makeMove(int num1, int num2) throws IOException{		
		byte i = (byte) num1;
		byte j = (byte) num2;
		MoveMessage aMove = new MoveMessage(i, j);
		out.writeObject(aMove);
		out.flush();
	}
	public void run() {
		//message for response
		Message m;
		try {
			m = (Message) in.readObject();
	    	//get server responses that aren't empty
			while (!(m.equals(null))) {	
	    		//read server response object
				changeToMessage(m);			
				m = (Message)in.readObject();
	    	}
	    } catch (Exception e) {
	    	System.out.println("Connection Closed");
	    }
	  }
}
