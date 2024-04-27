import java.util.ArrayList;
import java.util.List;

public class UserInfo {
	String username;
	ArrayList<ArrayList<Character>> grid;
	ArrayList<ShipInfo> shipInfoList;

	public UserInfo(String username, ArrayList<ArrayList<Character>> grid, ArrayList<ShipInfo> shipInfoList) {
		this.username = username;
		this.grid = grid;
		this.shipInfoList = shipInfoList;
	}

	public String getUsername() {
		return username;
	}

	public ArrayList<ArrayList<Character>> getGrid() {
		return grid;
	}

	public ArrayList<ShipInfo> getShipInfoList(){
		return shipInfoList;
	}

	public boolean areAllShipsSunk() {
		for (ShipInfo ship : shipInfoList) {
			if (!ship.isSunk()) {
				return false;
			}
		}
		return true;
	}
}
