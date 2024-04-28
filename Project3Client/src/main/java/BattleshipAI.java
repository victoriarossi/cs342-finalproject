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
    private ArrayList<ShipInfo> shipInfos = new ArrayList<>(); // this stores my ship info
    ArrayList<ArrayList<Boolean>> enemyGrid; // this stores the hits to the enemy that the AI plays


    BattleshipAI(){
        placeShips();
        for(ShipInfo theShip : shipInfos){
            System.out.println(theShip.getPositions());
        }

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
        }
        enemyGrid.get(x).set(y, true);
        return new Point(x, y);
    }
}