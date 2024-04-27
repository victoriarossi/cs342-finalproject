
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
        import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

public class CodeToSave {
    private int gridSize = 10; // assuming a 10x10 grid
    private boolean[][] grid = new boolean[gridSize][gridSize]; // track placed ships
    public GuiClient theGui = new GuiClient();
    private boolean isHorizontal = true;
    int size = 10;
    boolean[][] occupied = new boolean[size][size];
    private ShipInfo currentShip = null;
    private ShipInfo ship = null;
    private ArrayList<ShipInfo> shipInfos = new ArrayList<>();

    CodeToSave(){
        placeShips();
        System.out.println();
    }

    // Randomly place ships on the grid
    public void placeShips() {
        int[] shipSizes = {5, 4, 3, 3, 2}; // sizes of the ships
        Random random = new Random();

        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                int x = random.nextInt(gridSize);
                int y = random.nextInt(gridSize);
                boolean horizontal = random.nextBoolean();

                if (canPlaceShip(x, y, ship)) {
                    for (int i = 0; i < size; i++) {
                        if (horizontal) {
                            grid[x][y + i] = true; // Place horizontally
                        } else {
                            grid[x + i][y] = true; // Place vertically
                        }
                    }
                    //TODO: make sure the new Button is correct (my brain can't process rn :) *<)=0) )
                    ShipInfo ship = new ShipInfo(new Button(), currentShip.length);
                    shipInfos.add(ship);
                    placed = true;
                }
            }
        }
    }

    boolean canPlaceShip(int startX, int startY, ShipInfo ship) {
        if (isHorizontal) {
            for (int i = 0; i < ship.length; i++) {
                if (startX + i >= size || grid[startX + i][startY])
                    return false;  // Horizontal check
            }
        }
        else {
            for (int i = 0; i < ship.length; i++) {
                if (startY + i >= size || grid[startX][startY + i]) {
                    return false;
                }
            }
        }
        return true;
    }


    ArrayList<ShipInfo> getShipInfos() {
        return shipInfos;
    }

    public boolean[][] getGrid() {
        return grid;
    }
}