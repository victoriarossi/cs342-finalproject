import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Server{

	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	static ArrayList<String> clientID = new ArrayList<>();
	TheServer server;
//	ArrayList<ArrayList<>>
	private Consumer<Serializable> callback;


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
						if (message.getMessageType() == Message.MessageType.BROADCAST && "checkUser".equals(message.getMessageContent())) {
							String initialName = message.getUserID();
							if (!clientID.contains(initialName)) { // checks if username is not already taken
								clientID.add(initialName);
								clientName = initialName;
								callback.accept(clientName + " has connected to server.");

								// notifies all clients of the new user
								updateClients(new Message("Server", "New User", Message.MessageType.BROADCAST, new ArrayList<>(clientID)));

								// sends confirmation to new user that their username is valid
								out.writeObject(new Message("Server", "Ok Username", Message.MessageType.PRIVATE,new ArrayList<>(clientID)));
							} else {
								// informs client that username is taken
								out.writeObject(new Message("Server", "Taken Username", Message.MessageType.PRIVATE));
							}
						}
						else {
							// forwards any other type of message to all clients
							updateClients(message);
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
						updateClients(new Message("Server", clientName + " has left the chat.", Message.MessageType.BROADCAST, new ArrayList<>(clientID)));
					}
					synchronized (clients) {
						clients.remove(this);
					}
				}
			}//end of run

			// method to send a message to all clients or specific client
			public void updateClients(Message message) {

				//  broadcast message to all connected clients
				if(message.getMessageType() == Message.MessageType.BROADCAST) {
					for(ClientThread t : clients) {
						if(t.clientName != "") {
							try {
								t.out.writeObject(message);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}

				// sends a private message to a specific user
				else if (message.getMessageType() == Message.MessageType.PRIVATE) {
					String recipient = message.getUserIDReceiver();
					for(ClientThread t : clients) {
						if(t.clientName.equals(recipient)) {
							try {
								t.out.writeObject(message);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}//end of client thread
}

	
