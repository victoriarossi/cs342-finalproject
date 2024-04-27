import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class BattleshipAI {
    private int gridSize = 10; // assuming a 10x10 grid
    private boolean[][] grid = new boolean[gridSize][gridSize]; // track placed ships
    public GuiClient theGui = new GuiClient();
    private boolean isHorizontal = true;
    int size = 10;
    boolean[][] occupied = new boolean[size][size];
    private ShipInfo currentShip = null;
    private ArrayList<ShipInfo> shipInfos = new ArrayList<>(); // this stores my ship info
    //    ArrayList<ArrayList<Character>> gridForEnemy; // this stores my grid with the enemy's hits
    ArrayList<ArrayList<Boolean>> enemyGrid; // this stores the hits to the enemy that the AI plays
    ArrayList<ShipInfo> shipInfosEnemy; // this stores the ship info of the enemy


    BattleshipAI(){
        placeShips();
        for(ShipInfo theShip : shipInfos){
            System.out.println(theShip.getPositions());
        }
//        //initialize grid for enemy with just water
//        gridForEnemy = new ArrayList<>();
//        for (int i = 0; i < gridSize; i++) {
//            ArrayList<Character> row = new ArrayList<>();
//            for (int j = 0; j < gridSize; j++) {
//                row.add('W');
//            }
//            gridForEnemy.add(row);
//        }

        //initialize grid for enemy with just water
        enemyGrid = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            ArrayList<Boolean> row = new ArrayList<>();
            for (int j = 0; j < gridSize; j++) {
                row.add(false);
            }
            enemyGrid.add(row);
        }
    }

//    void setEnemyGrid(ArrayList<ArrayList<Boolean>> enemyGrid){
//        this.enemyGrid = enemyGrid;
//    }

    public void placeShips() {
        int[] shipSizes = {5, 4, 3, 3, 2}; // sizes of the ships
        Random random = new Random();

        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                int x = random.nextInt(gridSize);
                int y = random.nextInt(gridSize);
                boolean horizontal = random.nextBoolean();
                ShipInfo ship = new ShipInfo(size);
                if (canPlaceShip(x, y, size, horizontal)) { // Ensure to pass the correct parameters
                    for (int i = 0; i < size; i++) {
                        int posX = horizontal ? x : x + i;
                        int posY = horizontal ? y + i : y;
                        ship.addPosition(posY, posX); // Place horizontally or vertically
                        grid[posX][posY] = true; // Mark this grid position as occupied
                    }
                    shipInfos.add(ship);
                    placed = true;
                }
            }
        }
    }

//    public ArrayList<ArrayList<Character>> getGrid(){
//        return gridForEnemy;
//    }

    public ArrayList<ShipInfo> getShipInfo(){
        return shipInfos;
    }

    boolean canPlaceShip(int startX, int startY, int length, boolean horizontal) {
        if (horizontal) {
            if (startY + length > gridSize) {
                return false; // Check if the ship goes out of bounds
            }
            for (int i = 0; i < length; i++) {
                if (grid[startX][startY + i]) {
                    return false; // Check if the position is already occupied
                }
            }
        } else {
            if (startX + length > gridSize) {
                return false; // Check if the ship goes out of bounds
            }
            for (int i = 0; i < length; i++) {
                if (grid[startX + i][startY]) {
                    return false; // Check if the position is already occupied
                }
            }
        }
        return true;
    }

    public Point makeAMove(){
        // we want to hit on enemyGrid
        // Stores the move it made. Make sure not to make the same move with the Array<Array<Boolean>>
        // IE if false not guessed so make true and use move
        // if true do not make move redraw an x and y coordinates.
        Random random = new Random();
        int x = random.nextInt(gridSize);
        int y = random.nextInt(gridSize);
        while(enemyGrid.get(x).get(y)){
            x = random.nextInt(gridSize);
            y = random.nextInt(gridSize);
            System.out.println("Here");
        }
        System.out.println("AI MAKE MOVE CLASS");
        enemyGrid.get(x).set(y, true);
        //if there is a ship there put 'H'. Otherwise, 'M'
//        enemyGrid.get(x).set(y, 'H');
        return new Point(x, y);
    }
    // Randomly place ships on the grid
//    public void placeShips() {
//        int[] shipSizes = {5, 4, 3, 3, 2}; // sizes of the ships
//        Random random = new Random();
//
//        for (int size : shipSizes) {
//            boolean placed = false;
//            while (!placed) {
//                int x = random.nextInt(gridSize);
//                int y = random.nextInt(gridSize);
//                boolean horizontal = random.nextBoolean();
//                ShipInfo ship = new ShipInfo(size);
//                if (canPlaceShip(x, y, ship)) {
//                    for (int i = 0; i < size; i++) {
//                        if (horizontal) {
//                            ship.addPosition(x,y + i); // Place horizontally
//                        } else {
//                            ship.addPosition(x + i, y); // Place vertically
//                        }
//                    }
//                    //TODO: make sure the new Button is correct (my brain can't process rn :) *<)=0) )
//                    shipInfos.add(ship);
//                    placed = true;
//                }
//            }
//        }
//    }

//    boolean canPlaceShip(int startX, int startY, int size , ShipInfo ship) {
//        if (isHorizontal) {
//            for (int i = 0; i < ship.length; i++) {
//                if (startX + i >= size || grid[startX + i][startY])
//                    return false;  // Horizontal check
//            }
//        }
//        else {
//            for (int i = 0; i < ship.length; i++) {
//                if (startY + i >= size || grid[startX][startY + i]) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//
//    ArrayList<ShipInfo> getShipInfos() {
//        return shipInfos;
//    }
//
//    public boolean[][] getGrid() {
//        return grid;
//    }
}