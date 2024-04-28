import java.awt.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

import javafx.animation.PauseTransition;
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
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;


public class GuiClient extends Application{
	TextField usernameField = new TextField();

	Button connectBtn = new Button("Connect");

	private TextField messageTextField;

	private String selectedUser = "";

	private Button previouslySelectedBtn = null;

	private boolean gameStarted = false;

	private boolean playingAI = false;

	HashMap<String, Scene> sceneMap;
	Client clientConnection;

	private String messageContent;
	ListView<String> listItems2;

	Stack<ShipInfo> stackOfShipsPlaced = new Stack<>();

	private boolean hasSelectedCell = false;
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

	private ShipInfo lastPlacedShip = null;

	GridPane gridPaneEnemy = new GridPane();
	Button horizontalBtn;
	Button verticalBtn;
	Button start = new Button("Start");

	int buttonsClickedCount = 0;
	private int placedShipsCounter = 0;
	String enemy;
	Boolean myTurn = false;

	int gameOverCheck = 0;
	int gameOverCheck2 = 0;

	boolean whoWon = false;

	PauseTransition pause = new PauseTransition(Duration.millis(360));

	Button NUKE;

	Button undoShipBtn;

	BattleshipAI ai;

	GridPane gridPaneTest = new GridPane();

	int aiShipSunk = 0;

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
					if (playingAI) {

						ai = new BattleshipAI();

					} else {
						if ("Paired".equals(msg.getMessageContent())) {
							enemy = msg.getPlayer2();
							myTurn = msg.getMyTurn();
							if (myTurn) {
//							enableButtons();
								NUKE.setDisable(false);
							} else {
//							disableButtons();
								NUKE.setDisable(true);
							}

							createUserVSUserScene(primaryStage, grid);
						}
						if ("Waiting".equals(msg.getMessageContent()) && msg.getPlayer1().equals(currUsername)) {
//							System.out.println(grid);
							primaryStage.setScene(sceneMap.get("waitingScene"));
						}

						if ("Win".equals(msg.getMessageContent())) {
							System.out.println("I am the winner");
							primaryStage.setScene(sceneMap.get("victoryScene"));
//							if(msg.getPlayer1().equals(currUsername)){
//								primaryStage.setScene(sceneMap.get("losingScene"));
//							} else {
//
//							}
						} else if ("Lose".equals(msg.getMessageContent())) {
							System.out.println("I am the loser");
							primaryStage.setScene(sceneMap.get("losingScene"));
						}
						else if ("Hit".equals(msg.getMessageContent())) {
							//update enemy's grid
							enemyGrid.get(msg.getX()).set(msg.getY(), 'H');
							myTurn = msg.getMyTurn();
							if (myTurn) {
								NUKE.setDisable(false);
							} else {
								shipEnemyInfos.addAll(msg.getShipInfo());
								NUKE.setDisable(true);
							}
							updateGridCell(msg.getX(), msg.getY(), msg.getMessageContent(), primaryStage);

						} else if ("Miss".equals(msg.getMessageContent())) {
							//update enemy's grid
							enemyGrid.get(msg.getX()).set(msg.getY(), 'M');
							myTurn = msg.getMyTurn();
							if (myTurn) {
//							enableButtons();
								NUKE.setDisable(false);
							} else {
//							disableButtons();
								shipEnemyInfos.addAll(msg.getShipInfo());
								NUKE.setDisable(true);
							}
							updateGridCell(msg.getX(), msg.getY(), msg.getMessageContent(), primaryStage);
						}
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

		NUKE = new Button("Hit");
		NUKE.setStyle(btnStyle);
		NUKE.setOnAction( e -> {
			if(playingAI){
				//TODO: play move on ai + add a waiting time
				myTurn = false;
				Platform.runLater(() -> {
					String result = "Miss";
					for(ShipInfo theEShip : shipEnemyInfos){
						for(Point position : theEShip.getPositions()){
							if(position.x == xMove && position.y == yMove){
								result = "Hit";
//								System.out.println("IT WAS A HIT");
//								theEShip.recordHit();
								break;
							}
						}
					}

					System.out.println("IT WAS A " + result);
					updateGridCell(xMove, yMove, result, primaryStage);
					NUKE.setDisable(true);
					//add waiting response
					pause.play();
					pause.setOnFinished(ev -> aiMakeMove(primaryStage));

				});
			} else {
				if (hasSelectedCell) {
					buttonsClickedCount = 0;
					NUKE.setDisable(true);
					clientConnection.send(new Message("Move", currUsername, enemy, xMove, yMove, false));
					hasSelectedCell = false;
				}
			}
		});

		// scene map for different scenes
		sceneMap = new HashMap<String, Scene>();
		sceneMap.put("startScene", createIntroScene(primaryStage));
		sceneMap.put("rules", createRulesScene(primaryStage));
//		sceneMap.put("client",  createClientGui(primaryStage));
		sceneMap.put("clientLogin", createLoginScene(primaryStage)); // adds login screen to scene map
		sceneMap.put("options", createOptionsScene(primaryStage)); // adds the options screen to scene map
		sceneMap.put("setUpShipScene", createSetUpShipScene(primaryStage));
//		sceneMap.put("userVSUser", createuserVSUserScene(primaryStage)); // add the main game screen to scene map
		sceneMap.put("waitingScene", createWaitingScene(primaryStage));
		sceneMap.put("victoryScene", createVictoryScene(primaryStage));
		sceneMap.put("losingScene", createLosingScene(primaryStage));


		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setScene(sceneMap.get("startScene")); // starts the scene in the login scene
		primaryStage.setTitle(currUsername);
		primaryStage.show();
	}

	private void aiMakeMove(Stage primaryStage){

		Point aiHit = ai.makeAMove();
		System.out.println("TRYING TO HIT: " + aiHit.x + " " + aiHit.y);
		xMove = aiHit.x;
		yMove = aiHit.y;

		System.out.println("I AM AI");

		myTurn = true;

		String result = "Miss";
		for(ShipInfo myShip : shipInfos) {
			for (Point position : myShip.getPositions()) {
				if (position.x == aiHit.x && position.y == aiHit.y) {
					result = "Hit";
//					myShip.recordHit();
					break;
				}
			}
		}
		System.out.println("IT WAS A" + result);
		// see if we won or not

		updateGridCell(xMove, yMove, result, primaryStage);
		NUKE.setDisable(false);
		buttonsClickedCount--;

	}

	private void updateGridCell(int x, int y, String result, Stage primaryStage) {
//		Platform.runLater(() -> {
		System.out.println("RESULT on updateGridCell: " + result);

		// Determine if the update is for the user's grid or enemy's grid
		if (result.equals("Hit") || result.equals("Miss")) {
			char status = result.equals("Hit") ? 'H' : 'M';
			System.out.println("STATUS: " + status);

			// Update the user's grid if the opponent hits
			if (myTurn) {
				grid.get(x).set(y, status);
				Button btn = buttons2[x][y];
				btn.setText(result.equals("Hit") ? "H" : "M");
				btn.setDisable(true);
				btn.setStyle(result.equals("Hit") ? "-fx-background-color: red;" : "-fx-background-color: grey;");
			} else {
				enemyGrid.get(x).set(y, status);
				Button btn = buttons2Enemy[x][y];
				btn.setText(result.equals("Hit") ? "H" : "M");
				btn.setDisable(true);
				btn.setStyle(result.equals("Hit") ? "-fx-background-color: red;" : "-fx-background-color: grey;");
			}

			// Update the button on the user's grid UI to reflect the hit or miss

			ShipInfo ship;
			if (myTurn) {
				ship = findShipAt(x, y, shipInfos);
			} else {
				ship = findShipAt(x, y, shipEnemyInfos);
			}

			if (ship != null && status == 'H') {
				System.out.println("Hits Recorded Before Function Call: " + ship.hits);
				ship.recordHit();
				System.out.println("Hits Recorded After Function Call: " + ship.hits);
				if (ship.isSunk()) {
//						System.out.println("RECORDED AND SUNK");
					highlightSunkShip(ship, myTurn);
					if (myTurn) {
						System.out.println("GameOverCheck Before Increment: " + gameOverCheck);
						gameOverCheck++;
						System.out.println("GameOverCheck After Increment: " + gameOverCheck);
						if (gameOverCheck == 5) {
							clientConnection.send(new Message(enemy, "Win", currUsername));
						}
					} else {
						System.out.println("GameOverCheck2 Before Increment: " + gameOverCheck);
						gameOverCheck2++;
						System.out.println("GameOverCheck2 After Increment: " + gameOverCheck);
						if (gameOverCheck2 == 5) {
							clientConnection.send(new Message(currUsername, "Win", enemy));

//						gameOverCheck++;
//						if (gameOverCheck == 5) {
//							if(playingAI){
//								if(!myTurn){ // NOT SURE ABOUT THIS, NEED TO ACTUALLY USE MY BRAIN
//									primaryStage.setScene(sceneMap.get("victoryScene"));
//								} else {
//									primaryStage.setScene(sceneMap.get("losingScene"));
//								}
//
//							} else {
//								clientConnection.send(new Message(currUsername, "Ship Sunk", enemy));
//
//							}
						}
					}
				}
			}
//		});
		}
	}

	private ShipInfo findShipAt(int x, int y, ArrayList<ShipInfo> shipList) {
		System.out.println("Looking for: " + x + ", " + y);
		for (ShipInfo ship : shipList) {  // Assuming shipInfos holds all ships
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
			Button btn = targetButtons[part.x][part.y];
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

		if(placedShipsCounter != 5){
			start.setDisable(true);
		}

//		BorderPane pane = new BorderPane();
		Button backBtn = getBackBtn("options", primaryStage);
//		VBox root = new VBox(10);
		Pane shipContainer = new Pane(); // container to hold all ships
		shipContainer.setPrefSize(200, 60);


		start.setStyle(btnStyle);
		start.setOnAction(e -> {
			gameStarted = true;
			System.out.println("Start clicked");
			System.out.println("Sending current username: " + currUsername);
			if(!playingAI) {
				clientConnection.send(new Message(currUsername, "Pair", grid, shipInfos));
			} else {
				ai = new BattleshipAI();
//				enemyGrid = ai.getGrid();
				shipEnemyInfos.addAll(ai.getShipInfo());
//				ai.setEnemyGrid(grid);
				createUserVSUserScene(primaryStage, grid);
			}
		});


		horizontalBtn = new Button("Place Horizontally");
		horizontalBtn.setStyle("-fx-background-color: #259EE8; -fx-text-fill: black; -fx-background-radius: 25px; -fx-padding: 14; -fx-cursor: hand; -fx-font-size: 18");
		verticalBtn = new Button("Place Vertically");
		verticalBtn.setStyle(btnStyle);

		int xOffset = 125; // initial horizontal offset for first ship
		int[] shipLengths = {5, 4, 3, 3, 2}; // all ship lengths
		for (int i = 0; i < shipLengths.length; i++) {
			int length = shipLengths[i];
			Button shipButton = new Button(String.valueOf(length));
			shipButton.setStyle("-fx-background-color: #7B3F00; -fx-text-fill: white; -fx-cursor: hand");
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


		undoShipBtn = new Button("Undo Ship");
		undoShipBtn.setStyle(btnStyle);
		undoShipBtn.setDisable(true);
		undoShipBtn.setOnAction(e -> {
			if (!stackOfShipsPlaced.isEmpty()) {
				ShipInfo shipToUndo = stackOfShipsPlaced.pop();
				removeShipFromGrid(shipToUndo);
				shipToUndo.isPlaced = false;
				shipToUndo.shipButton.setDisable(false);

				if (stackOfShipsPlaced.isEmpty()) {
					undoShipBtn.setDisable(true);
					lastPlacedShip = null;
				} else {
					lastPlacedShip = stackOfShipsPlaced.peek();
				}

				placedShipsCounter--;

				if(placedShipsCounter != 5){
					start.setDisable(true);
				}
			}
		});

		horizontalBtn.setAlignment(Pos.CENTER);
		verticalBtn.setAlignment(Pos.CENTER);
		start.setAlignment(Pos.CENTER_RIGHT);
		HBox placementButtons = new HBox(10, undoShipBtn, horizontalBtn, verticalBtn, start);
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

	private void removeShipFromGrid(ShipInfo ship) {
		System.out.println("Removing ship with positions: " + ship.getPositions());
		for (Point pos : ship.getPositions()) {
			int x = pos.y;
			int y = pos.x;
			System.out.println("Clearing position: " + x + ", " + y);
			buttons[x][y].setText("");  // Clear the text marking it as part of a ship
			occupied[x][y] = false;  // Mark the cell as unoccupied
			buttons[x][y].setStyle("-fx-background-color: #77BAFC;"); // Reset to default color
			grid.get(y).set(x, 'W'); // Set back to water
		}
		ship.getPositions().clear(); // Clear the positions in ship info
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

		stackOfShipsPlaced.push(ship);
		lastPlacedShip = ship;
		undoShipBtn.setDisable(false);
		ship.isPlaced = true;
	}

	private void initializeGrids(){
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
	}

	private void resetGrid() {

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				buttons[i][j].setText("");  // Clear any text
				buttons[i][j].setStyle("-fx-background-color: #77BAFC;");
				occupied[i][j] = false;  // Reset the occupancy state
			}
		}

		for (ShipInfo shipInfo : shipInfos) {
			shipInfo.shipButton.setDisable(false);
			shipInfo.shipButton.setStyle("-fx-background-color: #7B3F00; -fx-text-fill: white");
			shipInfo.isPlaced = false;
			shipInfo.hits = 0;
		}

		initializeGrids();
		buttons2 = new Button[size][size];
		buttons2Enemy = new Button[size][size];
		gameOverCheck = 0;
		gameOverCheck2 = 0;

		gameStarted = false;  // Reset game start flag
		undoShipBtn.setDisable(true);
		start.setDisable(true);
	}

	private boolean isPartOfSunkShip(int x, int y){
		for( ShipInfo shipInfo: shipEnemyInfos){
			if(shipInfo.getPositions().contains(new Point(x,y)) && shipInfo.isSunk()){
				return true;
			}
		}
		return false;
	}

	private void handleGridClick(int x, int y, Button clickedButton){
		if (previouslySelectedBtn != null && previouslySelectedBtn != clickedButton) {
			if(previouslySelectedBtn.getText() == "H") {
				if (!isPartOfSunkShip(xMove, yMove))
					previouslySelectedBtn.setStyle("-fx-background-color: #FF4B33;");
				else
					previouslySelectedBtn.setStyle("-fx-background-color: darkred;");
			}
			else
				previouslySelectedBtn.setStyle("-fx-background-color: #77BAFC;");
			if (!(previouslySelectedBtn.getText() == "H" || previouslySelectedBtn.getText() == "M")) {
				previouslySelectedBtn.setDisable(false);
			}
		}
		clickedButton.setStyle("-fx-background-color: #FFD700;");

		xMove = x;
		yMove = y;

		previouslySelectedBtn = clickedButton;
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

					handleGridClick(finalI, finalJ, button);
					button.setDisable(true);
					buttonsClickedCount++;
					hasSelectedCell = true;
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
					handleGridClick(finalI, finalJ, button);
					button.setDisable(true);
					buttonsClickedCount++;
					hasSelectedCell = true;
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
		ButtonLayer.setAlignment(Pos.CENTER);


		ButtonLayer.getChildren().addAll(NUKE);
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

		playAIBtn.setOnAction(e -> {
			playingAI = true;
			primaryStage.setScene(sceneMap.get("setUpShipScene"));
		});

		VBox root = new VBox(40, playUserBtn, playAIBtn);
		root.setStyle("-fx-background-color: #C7FBFF; -fx-font-family: 'serif'");
		root.setAlignment(Pos.CENTER);
		return new Scene(root, 800, 600);
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


	private Scene createVictoryScene(Stage primaryStage) {
		Text victoryText = new Text("Congratulations! You Win!");

		victoryText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		victoryText.setFill(Color.YELLOW);


		// Creating the Home Button
		Button homeButton = new Button("Home");
		homeButton.setOnAction(e -> {
			// Action to go Home
			resetGrid();
			playingAI = false;
			placedShipsCounter = 0;
			primaryStage.setScene(sceneMap.get("options"));
		});
		homeButton.setStyle(btnStyle);

		// Layout
		VBox layout = new VBox(20); // 20 is the spacing between elements
		layout.setAlignment(Pos.CENTER);
		layout.getChildren().addAll(victoryText, homeButton);

		// Set Background Image to fit the screen
		BackgroundImage myBI = new BackgroundImage(new Image("winImage.jpg"),
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true));

		layout.setBackground(new Background(myBI));

		// Creating the Scene
		Scene scene = new Scene(layout, 800, 600);
		return scene;
	}

	private Scene createLosingScene(Stage primaryStage) {
		// Creating the Text for Victory Message
		Text victoryText = new Text("LOSER! You LOST!");
		victoryText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		victoryText.setFill(Color.YELLOW);

		// Creating the Home Button
		Button homeButton = new Button("Home");
		homeButton.setStyle(btnStyle);
		homeButton.setOnAction(e -> {
			// Action to go Home
			resetGrid();
			placedShipsCounter = 0;
			primaryStage.setScene(sceneMap.get("options"));
		});

		// Layout
		VBox layout = new VBox(20); // 20 is the spacing between elements
		layout.setAlignment(Pos.CENTER);
		layout.getChildren().addAll(victoryText, homeButton);

		// Set Background Image to fit the screen
		BackgroundImage myBI = new BackgroundImage(new Image("loseImage.jpg"),
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true));
		layout.setBackground(new Background(myBI));

		// Creating the Scene
		Scene scene = new Scene(layout, 800, 600);
		return scene;
	}
}
