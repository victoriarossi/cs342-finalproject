import javafx.scene.control.Button;

class ShipInfo {
    Button shipButton;
    int length;
    boolean isPlaced = false;

    public ShipInfo(Button shipButton, int length) {
        this.shipButton = shipButton;
        this.length = length;
    }
}