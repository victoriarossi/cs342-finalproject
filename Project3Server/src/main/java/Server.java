import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Stack;
import java.util.function.Consumer;

public class Server{

	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	static ArrayList<String> clientID = new ArrayList<>();
	TheServer server;
//	ArrayList<ArrayList<>>
	private Consumer<Serializable> callback;
	private Stack<String> userStack = new Stack<>();

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
			boolean firstTurn = false;


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
								updateClients(new Message("Server", "New User", null));

								// sends confirmation to new user that their username is valid
								out.writeObject(new Message("Server", "Ok Username",  null));
							} else {
								// informs client that username is taken
								out.writeObject(new Message("Server", "Taken Username", null));
							}
						}
						else {
							// forwards any other type of message to all clients
//							updateClients(message);
							if("Pair".equals(message.getMessageContent())){
								pairPlayers(message);
							} else if("grid".equals(message.getMessageContent())){
								// a player is playing a move

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
						updateClients(new Message("Server", clientName + " has left the chat.", null));
					}
					synchronized (clients) {
						clients.remove(this);
					}
				}
			}//end of run

			// method to send a message to all clients or specific client
			public void updateClients(Message message) {

				// sends a private message to a specific user
					String user = message.getPlayer1();
					String enemy = message.getPlayer2();
					for(ClientThread t : clients) {
						try {
							if(t.clientName.equals(user)) {
								message.setFirstTurn(true);
								t.out.writeObject(message);
							} else if (t.clientName.equals(enemy)) {
								Message msg = new Message(enemy, message.getMessageContent(), user);
								msg.setFirstTurn(false);
								t.out.writeObject(msg);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
			}

			public void pairPlayers(Message message){
				if(!userStack.empty()) {
					String enemy = userStack.pop();
					if (!enemy.equals(message.getPlayer1())) {
						for (ClientThread t : clients) {
							if (enemy.equals(t.clientName)) {
								t.paired = true;
								t.firstTurn = true;
								updateClients(new Message(message.getPlayer1(), "Paired", t.clientName));
								//t.pair = message.getPlayer1();
							}
						}
					}
				} else {
					userStack.push(message.getPlayer1());
					for(ClientThread t : clients) {
						if (t.clientName.equals(message.getPlayer1())) {
							try {
								updateClients(new Message(message.getPlayer1(), "Waiting", null));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}//end of client thread
}

	
