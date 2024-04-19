import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    private String player1; // ID of player1
    private String player2;  // ID of player2
    private boolean playingAI; // boolean to see if we are playing against the AI
    private String messageContent; // content of the message
//    private MessageType messageType; // type of message (BROADCAST = ALL USERS OR PRIVATE = SINGLE USER)
    private ArrayList<ArrayList<Character>> player1grid; // grid of boats of player 1. 'W' : water, 'B' : boat, 'H' : hit, 'M' : miss
    private ArrayList<ArrayList<Character>> player2grid; // grid of boats of player 2
    // move of the play
    private int x; // row
    private int y; // column
    private String userIDReceiver; // ID of user receiving a private message

    // Constructor to initialize message object with userID, message content, and type of message
    public Message(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.playingAI = false;
    }

    public Message(int x, int y){
        this.x = x;
        this.y = y;
    }
    // getter to retrieve userID of the message
    public String getPlayer1() {
        return player1;
    }

    // setter to set userID of the message
    public void setPlayer1(String userID) {
        this.player1 = player1;
    }

    // getter to retrieve userID of the message
    public String getPlayer2() {
        return player2;
    }


    // setter to set userID of the message
    public void setPlayer2(String userID) {
        this.player2 = player2;
    }

    // getter to retrieve message content of the message
    public String getMessageContent() {
        return messageContent;
    }

    public String toString(){
        return player1 + messageContent + player2;
    }

}
