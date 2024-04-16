import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

public class Client extends Thread{

	private String username; // stores clients username

	Socket socketClient;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	private Consumer<Serializable> callback;
	
	Client(Consumer<Serializable> call){
		callback = call;
	}
	
	public void run() {

		try {
			socketClient= new Socket("127.0.0.1",5555);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
			socketClient.setTcpNoDelay(true);

			while (true) { // keeps listening for messages from the server
				try {
					Serializable data = (Serializable) in.readObject(); // reads incoming message object from server
					callback.accept(data); // passes received Message object to callback for processing
				}
				catch (Exception e) {
					e.printStackTrace();
					break; // exits loop of an error occurs
				}
			}

		}
		catch(Exception e) {
			e.printStackTrace();
		}
    }

	// Method to send a Message object to the server
	public void send(Message message) {
		
		try {
			out.writeObject(message); // writes Message object to the ObjectOutputStream
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// sets username for this client and sends a username check request to the server
	public void setUsername(String username) {
		this.username = username;
		Message usernameMsg = new Message(username, "checkUser", Message.MessageType.BROADCAST);
		send(usernameMsg);
	}

	// getter for client's username
	public String getUsername() {
		return this.username;
	}

}
