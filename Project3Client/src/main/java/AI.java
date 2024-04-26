import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javafx.scene.control.Button;

public class AI {
    ArrayList<ArrayList<Character>> grid;
    String username;
    static int count = 0;
    private ArrayList<ShipInfo> shipInfos;

    AI() {
        this.username = String.valueOf(count);
        count++;
        // initialize grid with water
        grid = new ArrayList<>();
        for(int i=0; i < 10; i++){
            ArrayList row = new ArrayList<>();
            for(int j=0; j < 10; j++){
                row.add(j, 'W');
            }
            grid.add(row);
        }
        initializeGrid();
    }

    private void initializeGrid(){
        //initialize randomize grid
        int[] shipLengths = {5, 4, 3, 3, 2};
        for (int i = 0; i < shipLengths.length; i++) {
            Random random = new Random();
            int x = random.nextInt(10);
            int y = random.nextInt(10);
            boolean isHorizontal = random.nextInt(2) == 0 ? true : false;
            if(canPlaceShip(x, y, isHorizontal, shipLengths[i])){
                placeShip(x, y, isHorizontal, shipLengths[i]);
            }
        }
    }

    private void placeShip(int startX, int startY, boolean isHorizontal, int length) {
//        ShipInfo ship = new ShipInfo(new Button(String.valueOf(length)), length);
        if (isHorizontal) {
            for (int i = 0; i < length; i++) {
                int x = startX + i;
                int y = startY;
                grid.get(y).set(x, 'B');
//                ship.addPosition(y, x);
            }
        } else {
            // Place ship vertically
            for (int i = 0; i < length; i++) {
                int x = startX;
                int y = startY + i;
                grid.get(y).set(x, 'B');
//                ship.addPosition(y, x);
            }
        }
//        shipInfos.add(ship);
    }

    private boolean canPlaceShip(int startX, int startY, boolean isHorizontal, int length) {
        if (isHorizontal) {
            for (int i = 0; i < length; i++) {
                if (startX + i >= 10 || grid.get(startX + i).get(startY) == 'B') return false;  // Horizontal check
            }
        }
        else {
            for (int i = 0; i < length; i++) {
                if (startY + i >= 10 || grid.get(startX).get(startY + i) == 'B') {
                    return false;
                }
            }
        }
        return true;
    }

    // play move
    public Point playMove(ArrayList<ArrayList<Character>> grid){
        return new Point(0,0);
    }
}
