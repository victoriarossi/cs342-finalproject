import javafx.scene.control.Button;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.function.Consumer;

public class Server{

	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	ArrayList<AIThread> ais = new ArrayList<>();
	static ArrayList<String> clientID = new ArrayList<>();
	TheServer server;

	private Consumer<Serializable> callback;
	private Stack<UserInfo> userStack = new Stack<>();
	private ArrayList<UserInfo> userInfos = new ArrayList<>();
	int count = 0;

	Server(Consumer<Serializable> call){
		callback = call;
		server = new TheServer();
		server.start();
	}
	
	
	public class TheServer extends Thread {

		public void run() {

			try (ServerSocket mysocket = new ServerSocket(5555);) {
				System.out.println("Server is waiting for a client!");

				// keeps accepting new client connections
				while (true) {
					ClientThread c = new ClientThread(mysocket.accept());
					clients.add(c);
					c.start();
				}
			}//end of try
			catch (Exception e) {
				callback.accept("Server socket did not launch");
			}

		}
	}

	class AIThread extends Thread {
		Socket connection;
		ObjectInputStream in;
		ObjectOutputStream out;

		String aiName = "";
		ArrayList<ArrayList<Character>> grid;
		ArrayList<ShipInfo> shipInfos;

		AIThread(Socket socket) {
			this.connection = socket;
			this.aiName = String.valueOf(count);
			count++;

			// initialize grid with water
			grid = new ArrayList<>();
			for(int i=0; i < 10; i++){
				ArrayList row = new ArrayList<>();
				for(int j=0; j < 10; j++){
					row.add(j, 'W');
				}
				grid.add(row);
			}

			initializeGrid();
		}

		private void initializeGrid(){
			//initialize randomize grid
			int[] shipLengths = {5, 4, 3, 3, 2};
			for (int i = 0; i < shipLengths.length; i++) {
				Random random = new Random();
				int x = random.nextInt(10);
				int y = random.nextInt(10);
				boolean isHorizontal = random.nextInt(2) == 0 ? true : false;
				if(canPlaceShip(x, y, isHorizontal, shipLengths[i])){
					placeShip(x, y, isHorizontal, shipLengths[i]);
				}
			}
		}

		private void placeShip(int startX, int startY, boolean isHorizontal, int length) {
			ShipInfo ship = new ShipInfo(new Button(String.valueOf(length)), length);
			if (isHorizontal) {
				for (int i = 0; i < length; i++) {
					int x = startX + i;
					int y = startY;
					grid.get(y).set(x, 'B');
					ship.addPosition(y, x);
				}
			} else {
				// Place ship vertically
				for (int i = 0; i < length; i++) {
					int x = startX;
					int y = startY + i;
					grid.get(y).set(x, 'B');
					ship.addPosition(y, x);
				}
			}
			shipInfos.add(ship);
		}

		private boolean canPlaceShip(int startX, int startY, boolean isHorizontal, int length) {
			if (isHorizontal) {
				for (int i = 0; i < length; i++) {
					if (startX + i >= 10 || grid.get(startX + i).get(startY) == 'B') return false;  // Horizontal check
				}
			}
			else {
				for (int i = 0; i < length; i++) {
					if (startY + i >= 10 || grid.get(startX).get(startY + i) == 'B') {
						return false;
					}
				}
			}
			return true;
		}

		public void run() {
			try {

				// processes incoming messages from client
				while (true) {
					Message message = (Message) in.readObject(); // reads next message object from client

					if ("Move".equals(message.getMessageContent())){
						// do something
					}
				}

			} catch (Exception e) {

			}
		}
	}

	class ClientThread extends Thread{

		Socket connection;
		ObjectInputStream in;
		ObjectOutputStream out;

		String clientName = "";
		boolean paired = false;
		boolean myTurn = false;
		ArrayList<ArrayList<Character>> grid = new ArrayList<>();


		ClientThread(Socket s){
			this.connection = s; // stores client's socket connection
		}


		public void run(){

			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);

				// processes incoming messages from client
				while (true) {
					Message message = (Message) in.readObject(); // reads next message object from client

					// checks if the message is a username check request
					if ("checkUser".equals(message.getMessageContent())) {
						String initialName = message.getPlayer1();
						if (!clientID.contains(initialName)) { // checks if username is not already taken
							clientID.add(initialName);
							clientName = initialName;
							callback.accept(clientName + " has connected to server.");

							// notifies all clients of the new user
							updateClients(new Message("Server", "New User", ""));

							// sends confirmation to new user that their username is valid
							out.writeObject(new Message("Server", "Ok Username",  ""));
						} else {
							// informs client that username is taken
							out.writeObject(new Message("Server", "Taken Username", ""));
						}
					}
					else {
						// forwards any other type of message to all clients
//							updateClients(message);
						if("Pair".equals(message.getMessageContent())){
//								System.out.println(message.getPlayer1grid());
							if(message.getPlayingAI()){
								System.out.println("PLAYING AI");
								System.out.println(message.getPlayer1());
								// add thread to the list
								AIThread ai = new AIThread(connection);
								System.out.println(message.getPlayer1());
								ais.add(ai);

								userInfos.add(new UserInfo(ai.aiName, ai.grid, ai.shipInfos));
								userInfos.add(new UserInfo(message.getPlayer1(),message.getPlayer1grid(), message.getShipInfo()));

								//return to the user that the ai is connected with ai as player 2
								updateClients(new Message(message.getPlayer1(), "AIConnected", ai.aiName, true, true, ai.shipInfos));
								System.out.println(clients);
//								for(ClientThread t : clients){
//									System.out.println(t.clientName.equals(message.getPlayer1()));
//									if(t.clientName.equals(message.getPlayer1())){
//										System.out.println("CONNECTED");
//
//									}
//								}
								ai.start();
							} else {
								pairPlayers(message);
							}
						} else if("Move".equals(message.getMessageContent())){
							// a player is playing a move
//							if(message.getPlayingAI()){
//								//playAIMove(message);
//							} else {
//								playMove(message);
//							}
							playMove(message);
						}
						else if ("Ship Sunk".equals(message.getMessageContent())) {
//							playMove(message);
							updateClients(new Message(message.getPlayer1(), "Win", message.getPlayer2()));
							updateClients(new Message(message.getPlayer2(), "Lose", message.getPlayer1()));
						}
					}
				}
			}
			catch (Exception e) {

				// checks if client had set a username
				if (clientName != null && !clientName.isEmpty()) {
					// handles disconnection
					callback.accept(clientName + " has left the chat.");

					synchronized (clientID) {
						clientID.remove(clientName); // removes client from active users list
					}

					// notifies all clients that the user left
					updateClients(new Message("Server", clientName + " has left the chat.", ""));
				}
				synchronized (clients) {
					clients.remove(this);
				}
			}
		}//end of run

		// method to send a message to all clients or specific client
		public void updateClients(Message message) {
			System.out.println("UPDATING CLIENT: " + message.getPlayer1());
			for(ClientThread t : clients) {
				try {
					if(t.clientName.equals(message.getPlayer1())) {
						System.out.println("CONNECTED");
						t.out.writeObject(message);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void pairPlayers(Message message){
			if(!userStack.empty()) {
				UserInfo enemy = userStack.pop();
				userInfos.add(enemy);
				userInfos.add(new UserInfo(message.getPlayer1(),message.getPlayer1grid(), message.getShipInfo()));
//					userInfos.add(new UserInfo(message.getPlayer1(),message.getPlayer1grid()));
//					System.out.println("user: " + message.getPlayer1() + " enemy: " + enemy.getUsername());
				if (!enemy.getUsername().equals(message.getPlayer1())) {
					// go over the threads and look for the enemy's thread
					for (ClientThread t : clients) {
						if (enemy.getUsername().equals(t.clientName)) {
							t.paired = true;
							t.myTurn = true;
//								System.out.println(t.clientName + "'s Grid on thread " +  enemy.getGrid());
//							System.out.println("Sending " + enemy.getUsername() + " with " + message.getPlayer1() + " as enemy");
							Message msg = new Message(enemy.getUsername(), "Paired", message.getPlayer1(), enemy.getGrid(), true);
							try {
								t.out.writeObject(msg);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (message.getPlayer1().equals(t.clientName)) { // look for my thread
							try {
//								System.out.println("Sending " + message.getPlayer1() + " (should be bob) with " + enemy.getUsername() + " as enemy");
								t.out.writeObject(new Message(message.getPlayer1(), "Paired", enemy.getUsername(), message.getPlayer1grid(), false));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			} else {
				userStack.push(new UserInfo(message.getPlayer1(), message.getPlayer1grid(), message.getShipInfo()));
//					userStack.push(new UserInfo(message.getPlayer1(), message.getPlayer1grid()));

				for(ClientThread t : clients) {
					if (t.clientName.equals(message.getPlayer1())) {
						try {
							updateClients(new Message(message.getPlayer1(), "Waiting", ""));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		public void playMove(Message message){
			for(ClientThread t : clients) {
				if(t.clientName.equals(message.getPlayer2())){
					//get enemy's grid
					ArrayList<ArrayList<Character>> grid = new ArrayList<>();
					ArrayList<ShipInfo> shipInfoList = new ArrayList<>();
					boolean allShipsSunk = false;

					for(UserInfo userInfo : userInfos) {
						if(userInfo.getUsername().equals(message.getPlayer2())){
							grid = userInfo.getGrid();
							shipInfoList.addAll(userInfo.getShipInfoList());
							allShipsSunk = userInfo.areAllShipsSunk();
						}
					}


					System.out.println("Updating enemy's grid: " + grid);
					if(grid.get(message.getX()).get(message.getY()) == 'B'){
						System.out.println("Set grid cell to B");
						grid.get(message.getX()).set(message.getY(), 'H');
						System.out.println("Checking if All Ships are Sunk");
						updateClients(new Message("Hit", message.getPlayer1(),message.getPlayer2(), message.getX(), message.getY(), false, shipInfoList));
						updateClients(new Message("Hit", message.getPlayer2(),message.getPlayer1(), message.getX(), message.getY(), true));
						if (allShipsSunk) {
							updateClients(new Message("Win", message.getPlayer1(), message.getPlayer2(), message.getX(), message.getY(), false, shipInfoList));
							updateClients(new Message("Win", message.getPlayer2(),message.getPlayer1(), message.getX(), message.getY(), true));
						}

					} else if(grid.get(message.getX()).get(message.getY()) == 'W'){
						grid.get(message.getX()).set(message.getY(), 'M');
						updateClients(new Message("Miss", message.getPlayer1(),message.getPlayer2(), message.getX(), message.getY(), false, shipInfoList));
						updateClients(new Message("Miss",message.getPlayer2(), message.getPlayer1(), message.getX(), message.getY(), true));
					}

				}
			}
		}

	}//end of client thread
}

