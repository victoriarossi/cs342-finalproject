import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    private String userID; // ID of user sending the message
    private String messageContent; // content of the message
    private MessageType messageType; // type of message (BROADCAST = ALL USERS OR PRIVATE = SINGLE USER)
    private ArrayList<String> listOfUsers; // list of users ID
    private String userIDReceiver; // ID of user receiving a private message

    // Constructor to initialize message object with userID, message content, and type of message
    public Message(String userID, String messageContent, MessageType messageType) {
        this.userID = userID;
        this.messageContent = messageContent;
        this.messageType = messageType;
    }

    // Constructor to initialize message object with userID, message content, type of message, and list of users
    public Message(String userID, String messageContent, MessageType messageType, ArrayList listOfUsers) {
        this.userID = userID;
        this.messageContent = messageContent;
        this.messageType = messageType;
        this.listOfUsers = listOfUsers;
    }

    // Constructor to initialize message object with userID, message content, user to send message to
    public Message(String userIDSender, String messageContent, String userIDReceiver) {
        this.userID = userIDSender;
        this.messageContent = messageContent;
        this.messageType = MessageType.PRIVATE;
        this.userIDReceiver = userIDReceiver;
    }

    // getter to retrieve userID of the message
    public String getUserID() {
        return userID;
    }

    // setter to set userID of the message
    public void setUserID(String userID) {
        this.userID = userID;
    }

    // getter to retrieve message content of the message
    public String getMessageContent() {
        return messageContent;
    }

    // getter to retrieve message type of the message
    public MessageType getMessageType() {
        return messageType;
    }

    // getter to retrieve clients usernames
    public ArrayList<String> getListOfUsers(){
        return listOfUsers;
    }

    // getter to retrieve the private receiver of message
    public String getUserIDReceiver() {
        return userIDReceiver;
    }

    // enum defining possible types of messages
    enum MessageType {
        BROADCAST, // Message for all clients
        PRIVATE // Message for 1 specific client
    }

    public String toString(){
        String message = userID + " sent: " + messageContent;
        if(messageType == MessageType.BROADCAST){
            message += " to everyone.";
        } else if(messageType == MessageType.PRIVATE){
            message += " to you.";
        }
        return message;
    }

}
