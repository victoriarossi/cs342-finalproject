import javafx.scene.control.Button;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private boolean myTurn;
    private ArrayList<ShipInfo> shipInfos;

    // Constructor to initialize message object with userID, message content, and type of message
//    public Message(String player1, String player2) {
//        this.player1 = player1;
//        this.player2 = player2;
//        this.playingAI = false;
//    }

    public Message(String player1, String messageContent, String player2){
        this.player1 = player1;
        this.messageContent = messageContent;
        this.playingAI = false;
        this.player2 = player2;
    }

    public Message(String player1, String messageContent, ArrayList<ArrayList<Character>> grid, ArrayList<ShipInfo> shipInfos){
        this.player1 = player1;
        this.messageContent = messageContent;
        this.playingAI = false;
        this.player1grid = grid;
        this.shipInfos = shipInfos;
    }

    public Message(String player1, String messageContent, String player2, ArrayList<ArrayList<Character>> grid, Boolean myTurn, ArrayList<ShipInfo> shipInfos){
        this.player1 = player1;
        this.messageContent = messageContent;
        this.playingAI = false;
        this.player2 = player2;
        this.player1grid = grid;
        this.myTurn = myTurn;
        this.shipInfos = shipInfos;
    }

    public Message(String player1, String messageContent, String player2, ArrayList<ArrayList<Character>> grid, Boolean myTurn){
        this.player1 = player1;
        this.messageContent = messageContent;
        this.playingAI = false;
        this.player2 = player2;
        this.player1grid = grid;
        this.myTurn = myTurn;
    }

    public Message(int x, int y){
        this.x = x;
        this.y = y;
    }


    public Message(String messageContent, String player1, String player2, int x, int y, Boolean myTurn){
        this.player1 = player1;
        this.messageContent = messageContent;
        this.playingAI = false;
        this.player2 = player2;
        this.x = x;
        this.y = y;
        this.myTurn = myTurn;
    }

    public Message(String messageContent, String player1, String player2, int x, int y, Boolean myTurn, ArrayList<ShipInfo> shipInfos){
        this.player1 = player1;
        this.messageContent = messageContent;
        this.playingAI = false;
        this.player2 = player2;
        this.x = x;
        this.y = y;
        this.myTurn = myTurn;
        this.shipInfos = shipInfos;
    }


    public Message(String player1, String messageContent, String player2, Boolean playingAI, Boolean myTurn, ArrayList<ShipInfo> shipInfos){
        this.player1 = player1;
        this.messageContent = messageContent;
        this.playingAI = playingAI;
        this.player2 = player2;
        this.myTurn = myTurn;
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

    // setter to set the first turn
    public void setMyTurn(boolean myTurn){
        this.myTurn = myTurn;
    }

    // getter to set the first turn
    public boolean getMyTurn(){
        return myTurn;
    }

    // getter to get player 1's grid
    public ArrayList<ArrayList<Character>> getPlayer1grid() {
        return player1grid;
    }

    // getter to get player 2's grid
    public ArrayList<ArrayList<Character>> getPlayer2grid() {
        return player2grid;
    }

    // getter to get x position of players move
    public int getX(){
        return x;
    }

    // getter to get y position of players move
    public int getY(){
        return y;
    }

    // getter to get the ShipInfo list
    public ArrayList<ShipInfo> getShipInfo(){
        return shipInfos;
    }

    // getter to get if we are playing the AI or not
    public boolean getPlayingAI(){
        return playingAI;
    }

    public String toString(){
        return player1 + messageContent + player2;
    }


}
