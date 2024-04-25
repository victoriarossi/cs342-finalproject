import javafx.scene.control.Button;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class ShipInfo implements Serializable {
    transient Button shipButton;
    int length;
    boolean isPlaced = false;

    List<Point> positions;
    int hits;

    public ShipInfo(Button shipButton, int length) {
        this.shipButton = shipButton;
        this.length = length;
        this.positions = new ArrayList<>();
        this.hits = 0;
    }

    // Method to add position to the ship
    public void addPosition(int x, int y) {
        positions.add(new Point(x, y));
    }

    // Method to record a hit and check if the ship is sunk
    public void recordHit() {
        hits++;
//        return isSunk();
    }

    public boolean isSunk() {
        return hits == length;
    }

    // Getter for positions
    public List<Point> getPositions() {
        return positions;
    }

}