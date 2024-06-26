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
//	ArrayList<AIThread> ais = new ArrayList<>();
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
						if("Pair".equals(message.getMessageContent())){
								pairPlayers(message);
						} else if("Move".equals(message.getMessageContent())){
							playMove(message);
						}
						else if ("Win".equals(message.getMessageContent())) {
//							playMove(message);
							updateClients(new Message(message.getPlayer1(), "Win", message.getPlayer2()));
							updateClients(new Message(message.getPlayer2(), "Lose", message.getPlayer1()));
						}
//						else if ("Lose".equals(message.getMessageContent())) {
////							playMove(message);
//							updateClients(new Message(message.getPlayer1(), "Win", message.getPlayer2()));
//							updateClients(new Message(message.getPlayer2(), "Lose", message.getPlayer1()));
//						}
					}
				}
			}
			catch (Exception e) {
				System.out.println("EXCEPTION: " + e.getMessage());

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
			for(ClientThread t : clients) {
				try {
					if(t.clientName.equals(message.getPlayer1())) {
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
				if (!enemy.getUsername().equals(message.getPlayer1())) {
					// go over the threads and look for the enemy's thread
					for (ClientThread t : clients) {
						if (enemy.getUsername().equals(t.clientName)) {
							t.paired = true;
							t.myTurn = true;
							Message msg = new Message(enemy.getUsername(), "Paired", message.getPlayer1(), enemy.getGrid(), true);
							try {
								t.out.writeObject(msg);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (message.getPlayer1().equals(t.clientName)) { // look for my thread
							try {
								t.out.writeObject(new Message(message.getPlayer1(), "Paired", enemy.getUsername(), message.getPlayer1grid(), false));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			} else {
				userStack.push(new UserInfo(message.getPlayer1(), message.getPlayer1grid(), message.getShipInfo()));

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
					if(grid.get(message.getX()).get(message.getY()) == 'B'){
						grid.get(message.getX()).set(message.getY(), 'H');
						updateClients(new Message("Hit", message.getPlayer1(),message.getPlayer2(), message.getX(), message.getY(), false, shipInfoList));
						updateClients(new Message("Hit", message.getPlayer2(),message.getPlayer1(), message.getX(), message.getY(), true));
						if (allShipsSunk) {
							updateClients(new Message("Win", message.getPlayer1(), message.getPlayer2(), message.getX(), message.getY(), false, shipInfoList));
							updateClients(new Message("Lose", message.getPlayer2(),message.getPlayer1(), message.getX(), message.getY(), true));
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

