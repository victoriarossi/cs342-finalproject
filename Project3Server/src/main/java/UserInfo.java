import java.util.ArrayList;

public class UserInfo {
	String username;
	ArrayList<ArrayList<Character>> grid;

	public UserInfo(String username, ArrayList<ArrayList<Character>> grid) {
		this.username = username;
		this.grid = grid;
	}

	public String getUsername() {
		return username;
	}

	public ArrayList<ArrayList<Character>> getGrid() {
		return grid;
	}
}
