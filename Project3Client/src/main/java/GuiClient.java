import java.awt.*;
import java.util.HashMap;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class GuiClient extends Application{
	TextField usernameField = new TextField();

	Button connectBtn = new Button("Connect");

	private TextField messageTextField;

	private String selectedUser = "";

	private boolean gameStarted = false;

	TextField waiting;
	Button b1;
	HashMap<String, Scene> sceneMap;
	Client clientConnection;

	private String messageContent;
	ListView<String> listItems2;
	ListView<String> displayListUsers;

	private ArrayList<ShipInfo> shipInfos = new ArrayList<>();
	private ArrayList<ShipInfo> shipEnemyInfos = new ArrayList<>();
	ListView<String> displayListItems;
	ObservableList<String> storeUsersInListView;
	private ArrayList<ArrayList<Character>> grid;
	private ArrayList<ArrayList<Character>> enemyGrid;
	private int xMove;
	private int yMove;

	private final int size = 10;  // Size of the game board

	ShipInfo currentSelectedShip = null;
	boolean[][] occupied = new boolean[size][size];
	private Button[][] buttons = new Button[size][size];  // Buttons array representing the board
	private Button[][] buttons2 = new Button[size][size];  // Buttons array representing the board
	private Button[][] buttons2Enemy = new Button[size][size];
	String currUsername;
	private boolean isHorizontal = true;
	private boolean directionClicked = false;

	GridPane gridPaneEnemy = new GridPane();
	Button horizontalBtn;
	Button verticalBtn;
	Button start;

	int buttonsClickedCount = 0;
	private int placedShipsCounter = 0;
	String enemy;
	Boolean myTurn = false;
	Button hit;
	Button flipButton = new Button("Flip");
	Button NUKE;
	Button deselect;



	// styling strings for different UI
	String btnStyle = "-fx-background-color: #259EE8; -fx-text-fill: black; -fx-background-radius: 25px; -fx-padding: 14; -fx-cursor: hand; -fx-font-size: 18";
	String titleStyle = "-fx-font-size: 26; -fx-font-weight: bold; -fx-font-family: 'serif'; -fx-text-fill: #000000";
	String subtitleStyle = "-fx-font-size: 18; -fx-font-weight: bold";
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// initialize client connection and setup receiving messages
		clientConnection = new Client(data -> {
			Message msg = (Message) data;
			Platform.runLater(() -> {
				// checks if message indicates a valid username
				if ("Ok Username".equals(msg.getMessageContent())) {
					// updates the user list and goes to options screen
//					updateUserList(msg);
					primaryStage.setScene(sceneMap.get("options"));
				}

				// handles case where the username is already taken
				else if ("Taken Username".equals(msg.getMessageContent())) {
					showAlert("Be original, Professor McCarty dislikes copycats!", primaryStage);
				}
				else {
					if("Paired".equals(msg.getMessageContent())){
						enemy = msg.getPlayer2();
						myTurn = msg.getMyTurn();
						if(myTurn){
							NUKE.setDisable(false);
						} else {
							NUKE.setDisable(true);
						}

						createUserVSUserScene(primaryStage, grid);
					}
					if ("Waiting".equals(msg.getMessageContent()) && msg.getPlayer1().equals(currUsername)){
//							System.out.println(grid);
						primaryStage.setScene(sceneMap.get("waitingScene"));
					}

					if("Hit".equals(msg.getMessageContent())){
						//update enemy's grid
						enemyGrid.get(msg.getX()).set(msg.getY(), 'H');
						myTurn = msg.getMyTurn();
						if(myTurn){
							NUKE.setDisable(false);
						} else {
							shipEnemyInfos.addAll(msg.getShipInfo());
							NUKE.setDisable(true);
						}
						updateGridCell(msg.getX(), msg.getY(), msg.getMessageContent());


					} else if("Miss".equals(msg.getMessageContent())){
						//update enemy's grid
						enemyGrid.get(msg.getX()).set(msg.getY(), 'M');
						myTurn = msg.getMyTurn();
						if(myTurn){
							NUKE.setDisable(false);
						} else {
							shipEnemyInfos.addAll(msg.getShipInfo());
							NUKE.setDisable(true);
						}
						updateGridCell(msg.getX(), msg.getY(), msg.getMessageContent());
					}
				}
			});
		});

		clientConnection.start();
		// initialize lists view
		listItems2 = new ListView<String>();
		storeUsersInListView = FXCollections.observableArrayList();
		displayListUsers = new ListView<>();
		displayListItems = new ListView<>();

		// initialize user's grid and enemy's grid with only water
		grid = new ArrayList<>(size);
		enemyGrid = new ArrayList<>(size);
		for(int i = 0; i < size; i++){
			grid.add(new ArrayList<>());
			enemyGrid.add(new ArrayList<>());
			for(int j = 0; j < size; j++){
				grid.get(i).add('W');
				enemyGrid.get(i).add('W');
			}
		}

		deselect = new Button("Deselect");
		deselect.setStyle(btnStyle);
		deselect.setOnAction( e -> {

		});

		NUKE = new Button("Hit");
		NUKE.setStyle(btnStyle);
		NUKE.setOnAction( e -> {
			buttonsClickedCount = 0;
			NUKE.setDisable(true);
			clientConnection.send(new Message("Move", currUsername, enemy, xMove, yMove, false));

		});

		// scene map for different scenes
		sceneMap = new HashMap<String, Scene>();
		sceneMap.put("startScene", createIntroScene(primaryStage));
		sceneMap.put("rules", createRulesScene(primaryStage));
//		sceneMap.put("client",  createClientGui(primaryStage));
		sceneMap.put("clientLogin", createLoginScene(primaryStage)); // adds login screen to scene map
		sceneMap.put("options", createOptionsScene(primaryStage)); // adds the options screen to scene map
		sceneMap.put("setUpShipScene", createSetUpShipScene(primaryStage));
		sceneMap.put("users", createViewUsersScene(primaryStage)); // adds the view users screen to scene map
//		sceneMap.put("userVSUser", createuserVSUserScene(primaryStage)); // add the main game screen to scene map
		sceneMap.put("waitingScene", createWaitingScene(primaryStage));

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setScene(sceneMap.get("startScene")); // starts the scene in the login scene
		primaryStage.setTitle("Client");
		primaryStage.show();
	}

	private void updateGridCell(int x, int y, String result) {
		Platform.runLater(() -> {
			// Determine if the update is for the user's grid or enemy's grid
			if (result.equals("Hit") || result.equals("Miss")) {
				char status = result.equals("Hit") ? 'H' : 'M';

				//TODO: if is not my turn then set a new shipinfo for enemy

				// Update the user's grid if the opponent hits
				if (myTurn) {
					grid.get(x).set(y, status);
					Button btn = buttons2[x][y];
					btn.setText(result.equals("Hit") ? "H" : "M");
					btn.setStyle(result.equals("Hit") ? "-fx-background-color: red;" : "-fx-background-color: grey;");
				}
				else {
					enemyGrid.get(x).set(y, status);
					Button btn = buttons2Enemy[x][y];
					btn.setText(result.equals("Hit") ? "H" : "M");
					btn.setStyle(result.equals("Hit") ? "-fx-background-color: red;" : "-fx-background-color: grey;");
				}

				// Update the button on the user's grid UI to reflect the hit or miss

				ShipInfo ship = findShipAt(x, y);
				System.out.println("STATUS: " + status);
				System.out.println("SHIP: " + ship);
				if (ship != null && status == 'H') {
					System.out.println("HITS: " + ship.hits);
					ship.recordHit();
					System.out.println("HITS: " + ship.hits);
					if (ship.isSunk()) {
						System.out.println("RECORDED AND SUNK");
						highlightSunkShip(ship, myTurn);
					}
				}
			}
		});
	}

	private ShipInfo findShipAt(int x, int y) {
		for (ShipInfo ship : shipEnemyInfos) {  // Assuming shipInfos holds all ships
			System.out.println(ship.length + " positions: " + ship.positions);
			for (Point pos : ship.getPositions()) {
				if (pos.x == x && pos.y == y) {
					System.out.println("FOUND SHIP");
					System.out.println("POS: " + pos.x + ", " + pos.y);
					return ship;
				}
			}
		}
		return null;
	}

	private void highlightSunkShip(ShipInfo ship, boolean myTurn) {
		Button[][] targetButtons = myTurn ? buttons2: buttons2Enemy;
		for (Point part : ship.getPositions()) {
			Button btn = targetButtons[part.x][part.y];  // Adjust if you need to update the enemy's grid instead
			btn.setStyle("-fx-background-color: darkred;");
		}
	}



	public Button getBackBtn(String scene, Stage primaryStage){
		// Create back button
		Image home = new Image("back_arrow.png");
		ImageView homeView = new ImageView(home);
		homeView.setFitHeight(15);
		homeView.setFitWidth(15);
		Button backBtn = new Button();

		// brings you back to home screen on click
		backBtn.setOnAction( e -> {
			resetGrid();
			primaryStage.setScene(sceneMap.get(scene));
		});
		backBtn.setGraphic(homeView);
		backBtn.setStyle(btnStyle.concat("-fx-font-size: 14; -fx-padding: 10; -fx-background-radius: 25px; -fx-cursor: hand"));

		return backBtn;
	}


	// creates the initial login scene
	private Scene createLoginScene(Stage primaryStage) {

		BorderPane pane =  new BorderPane();

		Button backBtn = getBackBtn("startScene", primaryStage);

		// creates the title label and styles it
		Label title = new Label("Enter username:");
		title.setStyle(titleStyle);

		usernameField.setMaxWidth(200);
		usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 25px");

		// handles connect button action. Does not allow taken username or empty username
		connectBtn.setOnAction(e -> {
			String usernameAttempt = usernameField.getText();
			if (!usernameAttempt.isEmpty()) {
				clientConnection.setUsername(usernameAttempt);
				currUsername = usernameAttempt;
			}
			else {
				showAlert("Professor McCarty can't grade invisible students!", primaryStage);
			}
		});

		connectBtn.setStyle(btnStyle);
		VBox root = new VBox(40, title, usernameField, connectBtn);
		root.setStyle("-fx-background-color: #C7FBFF; ");
		root.setAlignment(Pos.CENTER);

		BorderPane.setAlignment(backBtn, Pos.TOP_LEFT);
		pane.setTop(backBtn);
		Color backgroundColor = Color.web("#C7FBFF");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		pane.setCenter(root); // sets the VBox as the central content of the BorderPane

		// returns login scene
		return new Scene(pane,800, 600);
	}


	// shows popup for invalid usernames
	private void showAlert(String message, Stage primaryStage) {

		VBox root = new VBox(20);
		root.setAlignment(Pos.CENTER);
		root.setStyle("-fx-background-color: #C7FBFF");

		// sets the header label and styles it
		Label header = new Label(message);
		header.setFont(new Font("Arial", 16));
		header.setStyle("-fx-text-fill: red");

		// creates the return button and handles the on click event
		Button returnBtn = new Button("I understand");
		returnBtn.setStyle(btnStyle);
		returnBtn.setOnAction(e -> {
			primaryStage.setScene(sceneMap.get("clientLogin"));
		});

		root.getChildren().addAll(header, returnBtn);

		// shows the error scene if an invalid username is entered
		Scene errorScene = new Scene(root, 800, 600);
		primaryStage.setScene(errorScene);
		primaryStage.show();
	}

	public Scene createSetUpShipScene(Stage primaryStage) {
		
		GridPane gridPane = new GridPane();
		gridPane.setAlignment(Pos.TOP_CENTER);
		gridPane.setHgap(0);
		gridPane.setVgap(0);


		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				Button button = new Button();
				button.setStyle("-fx-background-color: #77BAFC;");
				button.setPrefSize(40, 40);  // Set preferred size of each button
				int finalI = x;
				int finalJ = y;
				button.setOnAction(e -> {
					handleButtonAction(finalI, finalJ);
					directionClicked = false;
					horizontalBtn.setDisable(true);
					verticalBtn.setDisable(true);
				});
				buttons[x][y] = button;
				gridPane.add(button, x, y);
			}
		}

//		BorderPane pane = new BorderPane();
		Button backBtn = getBackBtn("options", primaryStage);
//		VBox root = new VBox(10);
		Pane shipContainer = new Pane(); // container to hold all ships
		shipContainer.setPrefSize(200, 60);

		start = new Button("Start");
		start.setStyle(btnStyle);
		start.setOnAction(e -> {
			gameStarted = true;
			System.out.println("Start clicked");
			clientConnection.send(new Message(currUsername, "Pair", grid, shipInfos));
		});

		if(placedShipsCounter != 5){
			start.setDisable(true);
		}

		horizontalBtn = new Button("Place Horizontally");
		horizontalBtn.setStyle("-fx-background-color: #259EE8; -fx-text-fill: black; -fx-background-radius: 25px; -fx-padding: 14; -fx-cursor: hand; -fx-font-size: 18");
		verticalBtn = new Button("Place Vertically");
		verticalBtn.setStyle(btnStyle);

		int xOffset = 125; // initial horizontal offset for first ship
		int[] shipLengths = {5, 4, 3, 3, 2}; // all ship lengths
		for (int i = 0; i < shipLengths.length; i++) {
			int length = shipLengths[i];
			Button shipButton = new Button(String.valueOf(length));
			shipButton.setStyle("-fx-background-color: #7B3F00; -fx-text-fill: white");
			shipButton.setPrefSize(length * 30, 20);
			shipButton.setLayoutX(xOffset);
			shipButton.setLayoutY(20);
			ShipInfo shipInfo = new ShipInfo(shipButton, length);
			shipInfos.add(shipInfo);
			shipContainer.getChildren().add(shipButton); // adds ship to the container

			shipButton.setOnAction(e -> {
				if (!shipInfo.isPlaced) {
					currentSelectedShip = shipInfo;
					directionClicked = true;
					horizontalBtn.setDisable(false);
					verticalBtn.setDisable(false);
				}
			});

			xOffset += (length * 30) + 10;
		}

		horizontalBtn.setOnAction(e -> {
			isHorizontal = true;  // Set placement to horizontal
		});

		verticalBtn.setOnAction(e -> {
			isHorizontal = false;  // Set placement to vertical
		});

		if(!directionClicked){
			horizontalBtn.setDisable(true);
			verticalBtn.setDisable(true);
		}

//		HBox.setMargin(horizontalBtn, new Insets(0, 0, 0, 10)); // Adds 10 pixels of margin to the left of horizontalBtn
//		HBox.setMargin(verticalBtn, new Insets(0, 0, 0, 10)); // Adds 10 pixels of margin to the left of verticalBtn
//		HBox layout1 = new HBox(20, horizontalBtn, verticalBtn);
//
//		Label placeShips = new Label("Place your ships:");
//		placeShips.setStyle(subtitleStyle);
//
//		VBox.setMargin(start, new Insets(0, 0, 0, 10));
//		VBox.setMargin(placeShips, new Insets(0, 0, 0, 10));
//
//		root.getChildren().addAll(placeShips, shipContainer, layout1, start);
//		BorderPane newPane = new BorderPane();
//		newPane.setCenter(gridPane);
//		newPane.setBottom(root);
//		newPane.setTop(backBtn);
//
//		Scene scene = new Scene(newPane, 800, 600);
//		primaryStage.setTitle("Battleship Game");
//		primaryStage.setScene(scene);
//		primaryStage.show();
////		return new Scene(pane, 800, 600);
//		return scene;
		horizontalBtn.setAlignment(Pos.CENTER);
		verticalBtn.setAlignment(Pos.CENTER);
		start.setAlignment(Pos.CENTER_RIGHT);
		HBox placementButtons = new HBox(10, horizontalBtn, verticalBtn, start);
		placementButtons.alignmentProperty().set(Pos.CENTER);

		Label title = new Label("Prepare Your Ships");
		title.setStyle(titleStyle);


		VBox root = new VBox(20);
		root.getChildren().addAll(title, gridPane, shipContainer, placementButtons);
		BorderPane newPane = new BorderPane();
		newPane.setCenter(root);
		newPane.setTop(backBtn);
		newPane.setStyle("-fx-background-color: #C7FBFF; -fx-font-family: 'serif'");
		root.setAlignment(Pos.CENTER);


		Scene scene = new Scene(newPane, 800, 600);
		primaryStage.setTitle("Battleship Game");
		primaryStage.setScene(scene);
		primaryStage.show();
		return scene;
	}


	private void handleButtonAction(int x, int y) {
		// This is where you will handle the logic for what happens when a button is clicked
		// For example, attack at (x, y) or place a ship
		System.out.println("Button clicked at: (" + x + "," + y + ")");
		if (gameStarted) {
			buttons[x][y].setText("X");  // Mark the button as clicked or attacked
			Message newMsg = new Message(x, y);
			clientConnection.send(newMsg);
		}
		else if (currentSelectedShip != null && !currentSelectedShip.isPlaced) {
			if (canPlaceShip(x, y, currentSelectedShip)) {
				placeShipOnGrid(x, y, currentSelectedShip);
				currentSelectedShip.isPlaced = true;
				currentSelectedShip.shipButton.setDisable(true);
				placedShipsCounter++;
				if(placedShipsCounter == 5){
					start.setDisable(false);
				}
			}
		}
	}


	private boolean canPlaceShip(int startX, int startY, ShipInfo ship) {
		if (isHorizontal) {
			for (int i = 0; i < ship.length; i++) {
				if (startX + i >= size || occupied[startX + i][startY]) return false;  // Horizontal check
			}
		}
		else {
			for (int i = 0; i < ship.length; i++) {
				if (startY + i >= size || occupied[startX][startY + i]) {
					return false;
				}
			}
		}
		return true;
	}

	private void placeShipOnGrid(int startX, int startY, ShipInfo ship) {
		String shipLength = String.valueOf(ship.length);
		if (isHorizontal) {
			for (int i = 0; i < ship.length; i++) {
				int x = startX + i;
				int y = startY;
				buttons[x][y].setText(shipLength);  // Mark the button as part of a ship
				occupied[x][y] = true;  // Mark cells as occupied
				buttons[x][y].setStyle("-fx-background-color: navy; -fx-text-fill: white");
				grid.get(y).set(x, 'B');
				ship.addPosition(y, x);
			}
//			System.out.println("ADDED SHIP: " + shipLength + " AT POSITION " + ship.positions);
		}
		else {
			// Place ship vertically
			for (int i = 0; i < ship.length; i++) {
				int x = startX;
				int y = startY + i;
				buttons[x][y].setText(shipLength);  // Mark the grid cell as occupied
				occupied[x][y] = true;  // Mark the cell as occupied
				buttons[x][y].setStyle("-fx-background-color: navy; -fx-text-fill: white");
				grid.get(y).set(x, 'B');
				ship.addPosition(y, x);
			}
		}

//		ship.isPlaced = true;
	}

	private void resetGrid() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				buttons[i][j].setText("");  // Clear any text
				buttons[i][j].setStyle("");  // Reset styles, if any
				occupied[i][j] = false;  // Reset the occupancy state
			}
		}

		for (ShipInfo shipInfo : shipInfos) {
			shipInfo.shipButton.setDisable(false);
			shipInfo.shipButton.setStyle("");
			shipInfo.isPlaced = false;
		}
		gameStarted = false;  // Reset game start flag
	}

	private void handleGridClick(int x, int y){
		xMove = x;
		yMove = y;
	}


	private void createUserVSUserScene(Stage primaryStage, ArrayList<ArrayList<Character>> grid) {
		GridPane gridPane = new GridPane();
		gridPane.setAlignment(Pos.TOP_CENTER);
		gridPane.setHgap(0);
		gridPane.setVgap(0);

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				Button button = new Button();

//				System.out.println(grid.get(x).get(y));
				if(grid.get(x).get(y).equals('B')) {
					button.setStyle("-fx-background-color: #000000;");
				} else {
					button.setStyle("-fx-background-color: #77BAFC;");
				}
				button.setPrefSize(40, 40);  // Set preferred size of each button
				button.setDisable(true);
				int finalI = x;
				int finalJ = y;
				button.setOnAction(e -> {
					System.out.println("Counter Before Click: " + buttonsClickedCount);
					if (buttonsClickedCount < 1) {
						handleGridClick(finalI, finalJ);
						button.setDisable(true);
						buttonsClickedCount++;
					}
				});
				buttons2[x][y] = button;
				gridPane.add(button, y,  x);
			}
		}

		gridPaneEnemy.setAlignment(Pos.TOP_CENTER);
		gridPaneEnemy.setHgap(0);
		gridPaneEnemy.setVgap(0);

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				Button button = new Button();
//				System.out.println(grid.get(x).get(y));
				if(enemyGrid.get(x).get(y).equals('W')) { // water
					button.setStyle("-fx-background-color: #77BAFC;");
				} else if(enemyGrid.get(x).get(y).equals('H')){ // successful hit
					button.setStyle("-fx-background-color: #FF4B33;");
				} else if(enemyGrid.get(x).get(y).equals('M')){
					button.setStyle("-fx-background-color: #DEEBFF;");
				}
				button.setPrefSize(40, 40);  // Set preferred size of each button
				int finalI = x;
				int finalJ = y;

				button.setOnAction(e -> {
					System.out.println("Counter Enemy: " + buttonsClickedCount);
					if (buttonsClickedCount < 1) {
						handleGridClick(finalI, finalJ);
						button.setDisable(true);
						buttonsClickedCount++;
					}
				});
				buttons2Enemy[x][y] = button;
				gridPaneEnemy.add(button, y,  x);
			}
		}


		BorderPane pane = new BorderPane();
		Color backgroundColor = Color.web("#C7FBFF");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		Label title = new Label("HIT YOUR MARK!");
		title.setStyle(titleStyle);

		HBox GridLayer = new HBox(20);
		GridLayer.getChildren().addAll(gridPane, gridPaneEnemy);
		GridLayer.setAlignment(Pos.CENTER);
		GridLayer.setPadding(new Insets(10));
		pane.setCenter(GridLayer);


		HBox ButtonLayer = new HBox(10);
		NUKE.setPrefWidth(100);
		deselect.setPrefWidth(100);
		ButtonLayer.setAlignment(Pos.CENTER);


		ButtonLayer.getChildren().addAll(NUKE, deselect);
		pane.setCenter(ButtonLayer);


		VBox root = new VBox(15);
		root.getChildren().addAll(title, GridLayer, ButtonLayer);
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(30));



		root.setStyle("-fx-background-color: #C7FBFF; -fx-font-family: 'serif'");
		pane.setCenter(root);
		primaryStage.setScene(new Scene(pane, 800, 600));
	}


	// creates options scene
	public Scene createOptionsScene(Stage primaryStage){
		Button playUserBtn = new Button("Play User");
		playUserBtn.setStyle(btnStyle);
		playUserBtn.setPrefSize(100, 50);
		Button playAIBtn = new Button("Play AI");
		playAIBtn.setStyle(btnStyle);
		playAIBtn.setPrefSize(100, 50);

		playUserBtn.setOnAction(e -> {
			primaryStage.setScene(sceneMap.get("setUpShipScene"));
		});
		playAIBtn.setOnAction(e -> primaryStage.setScene(sceneMap.get("setUpShipScene")));

		VBox root = new VBox(40, playUserBtn, playAIBtn);
		root.setStyle("-fx-background-color: #C7FBFF; -fx-font-family: 'serif'");
		root.setAlignment(Pos.CENTER);
		return new Scene(root, 800, 600);
	}

	public Scene createViewUsersScene(Stage primaryStage){
		BorderPane pane = new BorderPane();

		// Create back button
		Image home = new Image("back_arrow.png");
		ImageView homeView = new ImageView(home);
		homeView.setFitHeight(15);
		homeView.setFitWidth(15);
		Button backBtn = new Button();

		// changes scene back to options screen on click
		backBtn.setOnAction( e -> {
			primaryStage.setScene(sceneMap.get("options"));
		});
		backBtn.setGraphic(homeView);
		backBtn.setStyle(btnStyle.concat("-fx-font-size: 14; -fx-padding: 10; -fx-background-radius: 25px; -fx-cursor: hand"));

		BorderPane.setAlignment(backBtn, Pos.TOP_LEFT);
		pane.setTop(backBtn);

		Color backgroundColor = Color.web("#C7FBFF");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		Label title = new Label("List of all users:");
		title.setStyle(titleStyle);

		// stores the title and user list in here
		VBox users = new VBox(20, title, displayListUsers);
		users.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
		VBox.setMargin(users, new Insets(30));
		users.setAlignment(Pos.CENTER);
		displayListUsers.setMaxWidth(400);
		displayListUsers.setMaxHeight(250);

		pane.setCenter(users);
		return new Scene(pane, 800, 600);
	}

	public Scene createRulesScene(Stage primaryStage) {

		BorderPane pane = new BorderPane();

		VBox root = new VBox(20);
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(20));
		root.setStyle("-fx-background-color: #C7FBFF");

		Button backBtn = getBackBtn("startScene", primaryStage);

		// sets the title and styling
		Label title = new Label("Game Rules");
//		title.setFont(titleStyle);
		title.setStyle(titleStyle);
//		title.setStyle("-fx-text-fill: #000000");

		// sets all rules and makes sure the text wraps if it is too long for the window width
		Label rule1 = new Label("1. Objective: The goal of Battleship is to sink all of your opponent's ships before they sink all of yours.");
		rule1.setWrapText(true);

		Label rule2 = new Label("2. Setup: Each player places their ships on a grid without the other player seeing where they are placed.");
		rule2.setWrapText(true);

		Label rule3 = new Label("3. Gameplay: Players take turns guessing grid coordinates to attack enemy ships.");
		rule3.setWrapText(true);

		Label rule4 = new Label("4. Scoring: The game ends when all ships of one player are sunk. The player who sinks all enemy ships first wins.");
		rule4.setWrapText(true);

		BorderPane.setAlignment(backBtn, Pos.TOP_LEFT);
		pane.setTop(backBtn);

		Color backgroundColor = Color.web("#C7FBFF");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		root.getChildren().addAll(title, rule1, rule2, rule3, rule4);

		pane.setCenter(root);

		return new Scene(pane, 800, 600);
	}

	public Scene createIntroScene(Stage primaryStage) {
		StackPane root = new StackPane(); // overlay layout

		// sets up the background image
		String imgPath = "/battleship.jpg";
		Image backgroundImg = new Image(getClass().getResourceAsStream(imgPath));
		ImageView backgroundImageView = new ImageView(backgroundImg);
		backgroundImageView.setFitHeight(600);
		backgroundImageView.setFitWidth(800);
		backgroundImageView.setPreserveRatio(false);
		backgroundImageView.setSmooth(true);

		// adds the ImageView as the first layer
		root.getChildren().add(backgroundImageView);

		// Title label and styling
		Label header = new Label("BattleShip");
		header.setFont(javafx.scene.text.Font.font("Arial", 36));
		header.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

		// Start Button and styling
		Button startBtn = new Button("Start Game");
//		startBtn.setFont(javafx.scene.text.Font.font("Arial", 20));
		startBtn.setPrefSize(200, 50);
		startBtn.setStyle(btnStyle);
//		startBtn.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white; -fx-cursor: hand");
		startBtn.setOnAction(e -> primaryStage.setScene(sceneMap.get("clientLogin")));

		// Rules Button and styling
		Button rulesBtn = new Button("Rules");
//		rulesBtn.setFont(javafx.scene.text.Font.font("Arial", 20));
		rulesBtn.setPrefSize(200, 50);
		rulesBtn.setStyle(btnStyle);
//		rulesBtn.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white; -fx-cursor: hand");
		rulesBtn.setOnAction(e -> primaryStage.setScene(sceneMap.get("rules")));

		// stores the start and rules button side by side
		HBox buttonsBox = new HBox(10);
		buttonsBox.getChildren().addAll(startBtn, rulesBtn);
		buttonsBox.setAlignment(Pos.CENTER);

		// stores everything in a vertical box
		VBox layout = new VBox(20);
		layout.getChildren().addAll(header, buttonsBox);
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(30, 0, 0, 0));

		root.getChildren().add(layout);

		return new Scene(root, 800, 600);
	}

	private Scene createWaitingScene(Stage primaryStage){
		BorderPane pane = new BorderPane();

		Color backgroundColor = Color.web("#C7FBFF");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		Label waiting = new Label("There are no available users, please wait...");
		pane.setCenter(waiting);

		return new Scene(pane, 800, 600);
	}

	private void enableButtons() {
		Platform.runLater(() -> {
			// Enable buttons based on the enemy grid state
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					Button button = buttons2Enemy[x][y];
					// Only enable the button if it corresponds to a 'W' cell, indicating untargeted water
					button.setDisable(!(enemyGrid.get(x).get(y) == 'W'));
				}
			}
		});
	}

	private void disableButtons() {
		Platform.runLater(() -> {
			// Disable all buttons in the enemy grid
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					buttons2Enemy[x][y].setDisable(true);
				}
			}
		});
	}

}
