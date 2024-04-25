import java.util.ArrayList;

public class UserInfo {
	String username;
	ArrayList<ArrayList<Character>> grid;
	List<ShipInfo> shipInfos = new ArrayList<>();

	public UserInfo(String username, ArrayList<ArrayList<Character>> grid, ArrayList<ShipInfo> ships) {
		this.username = username;
		this.grid = grid;
		this.shipInfos = ships;
	}

	public String getUsername() {
		return username;
	}

	public ArrayList<ArrayList<Character>> getGrid() {
		return grid;
	}

	public List<ShipInfo> getShipInfos() {
		return shipInfos;
	}
}
