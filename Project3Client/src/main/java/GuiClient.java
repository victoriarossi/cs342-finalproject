import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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

	private List<ShipInfo> shipInfos = new ArrayList<>();
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
	Button horizontalBtn;
	Button verticalBtn;
	Button start;
	private int placedShipsCounter = 0;
	String enemy;
	Boolean myTurn = false;
	Button flipButton = new Button("Flip");
	Button hit;


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
//					// if the user leaves, shows the leave message in the chat log and updates the active users list
//					if (msg.getMessageContent().endsWith("has left the chat.")) {
//						//pop up an alert and end game (go back to home page)
//						listItems2.getItems().add(msg.getMessageContent());
////						updateUserList(msg);
//					}
//					else {
						if("Paired".equals(msg.getMessageContent())){
							enemy = msg.getPlayer2();
							myTurn = msg.getMyTurn();
							ArrayList<ArrayList<Character>> newGrid = new ArrayList<>();
							for (int i = 0; i < size; i++) {
								ArrayList<Character> row = new ArrayList<>();
								for (int j = 0; j < size; j++) {
									row.add(msg.getPlayer1grid().get(i).get(j)); // Replace defaultValue with the default value you want to initialize the grid with
								}
								newGrid.add(row);
							}
							grid.addAll(newGrid);
							// go to next scene
							System.out.println(grid);
							createuserVSUserScene(primaryStage, grid);
						}
						if ("Waiting".equals(msg.getMessageContent()) && msg.getPlayer1().equals(currUsername)){
//							System.out.println(grid);
							primaryStage.setScene(sceneMap.get("waitingScene"));
						} else {
							boolean isForCurrentUser = msg.getPlayer2().equals(clientConnection.getUsername());
							if (isForCurrentUser || msg.getPlayer1().equals(clientConnection.getUsername())) {
								String privateMsg = "Whisper from " + msg.getPlayer1() + ": " + msg.getMessageContent();
								listItems2.getItems().add(privateMsg);
							}
						}

						if("Hit".equals(msg.getMessageContent())){
							//update enemy's grid
							enemyGrid.get(msg.getX()).set(msg.getY(), 'H');
							myTurn = msg.getMyTurn();

						} else if("Miss".equals(msg.getMessageContent())){
							//update enemy's grid
							enemyGrid.get(msg.getX()).set(msg.getY(), 'M');
							myTurn = msg.getMyTurn();
						}
						// TODO: here we need to receive a message from the server and enable the button back.
						// That is when an enemy presses the hit button we need to deal with what happens
						// Tell the user if they were hit and re-enable the button.
						else if("PlayTurn".equals(msg.getMessageContent())) {
							if (myTurn) {
								hit.setDisable(false);
							} else {
								hit.setDisable(true);
							}
						}

						// updates the user list as long as it contains users
//						if (msg.getListOfUsers() != null) {
//							updateUserList(msg);
//						}

						// determines if message is private and meant for or from the current user
//						boolean isPrivate = msg.getMessageType() == Message.MessageType.PRIVATE;
//						boolean isForCurrentUser = msg.getPlayer2().equals(clientConnection.getUsername());

						// handles the private messages
//						if (isPrivate) {
//
//						}
						// handles non-private messages
//						else {
//							if (!"New User".equals(msg.getMessageContent())) {
//								listItems2.getItems().add(msg.getUserID() + " sent: " + msg.getMessageContent());
//							}
//						}
//					}
				}
			});
		});

//		GridPane gridPane = new GridPane();
//
//		for (int x = 0; x < size; x++) {
//			for (int y = 0; y < size; y++) {
//				Button button = new Button();
//				button.setPrefSize(30, 30);  // Set preferred size of each button
//				int finalI = x;
//				int finalJ = y;
//				button.setOnAction(e -> handleButtonAction(finalI, finalJ));
//				buttons[x][y] = button;
//				gridPane.add(button, x, y);
//			}
//		}

		clientConnection.start();
		// initialize lists view
		listItems2 = new ListView<String>();
		storeUsersInListView = FXCollections.observableArrayList();
		displayListUsers = new ListView<>();
		displayListItems = new ListView<>();

		b1 = new Button("Send"); // send button for messages
		b1.setOnAction(e->{
//			String messageContent = c1.getText();
			String currUsername = clientConnection.getUsername();
			Message message = new Message(currUsername, messageContent, "");
			clientConnection.send(message);
//			c1.clear();
		});

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


		// scene map for different scenes
		sceneMap = new HashMap<String, Scene>();
		sceneMap.put("startScene", createIntroScene(primaryStage));
		sceneMap.put("rules", createRulesScene(primaryStage));
//		sceneMap.put("client",  createClientGui(primaryStage));
		sceneMap.put("clientLogin", createLoginScene(primaryStage)); // adds login screen to scene map
		sceneMap.put("options", createOptionsScene(primaryStage)); // adds the options screen to scene map
		sceneMap.put("setUpShipScene", createSetUpShipScene(primaryStage));
		sceneMap.put("users", createViewUsersScene(primaryStage)); // adds the view users screen to scene map
		sceneMap.put("selectUser", createSelectUserScene(primaryStage)); //add the select user screen to scene map
		sceneMap.put("viewMessages", createViewMessages(primaryStage));
//		sceneMap.put("userVSUser", createuserVSUserScene(primaryStage)); // add the main game screen to scene map
		sceneMap.put("waitingScene", createWaitingScene(primaryStage));

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

//		Scene scene = new Scene(gridPane);
//		primaryStage.setTitle("Battleship Game");
//		primaryStage.setScene(scene);
//		primaryStage.show();

		primaryStage.setScene(sceneMap.get("startScene")); // starts the scene in the login scene
		primaryStage.setTitle("Client");
		primaryStage.show();
	}

	private Button getBackBtn(String scene, Stage primaryStage){
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

	// creates main client UI
//	public Scene createClientGui(Stage primaryStage) {
//		BorderPane pane =  new BorderPane();
//
//		// sets and styles the title of the send message screen
//		Label title = new Label("Input your message:");
//		title.setStyle(subtitleStyle + "; -fx-padding: 10");
//
//		messageTextField = new TextField();
//		messageTextField.setMaxWidth(250);
//		messageTextField.setStyle("-fx-padding: 10; -fx-background-radius: 25px;");
//
//		// label to show the recipient of the message
//		Label sendTo = new Label("Send to: ");
//		sendTo.setStyle(subtitleStyle);
//
//		// creates the buttons to send to all or 1 user
//		Button allUsers = new Button("All users");
//		Button oneUser = new Button("One user");
//		allUsers.setStyle(btnStyle);
//		oneUser.setStyle(btnStyle);
//		HBox btns = new HBox(20, allUsers, oneUser);
//		btns.setAlignment(Pos.CENTER);
//
//		// handles on click event for the all users button
//		allUsers.setOnAction( e -> {
//			String messageContent = messageTextField.getText();
//			String currUsername = clientConnection.getUsername();
//			Message msg = new Message(currUsername, messageContent, Message.MessageType.BROADCAST);
//			clientConnection.send(msg);
//			messageTextField.clear();
//		});
//
//		// handles on click event for sending to 1 user button
//		oneUser.setOnAction(e -> {
//			messageContent = messageTextField.getText();
//			primaryStage.setScene(sceneMap.get("selectUser"));
//		});
//
//		// disables sending buttons if message field is empty
//		allUsers.disableProperty().bind(messageTextField.textProperty().isEmpty());
//		oneUser.disableProperty().bind(messageTextField.textProperty().isEmpty());
//
//		// Create back button
//		Image home = new Image("back_arrow.png");
//		ImageView homeView = new ImageView(home);
//		homeView.setFitHeight(15);
//		homeView.setFitWidth(15);
//		Button backBtn = new Button();
//
//		// brings you back to options screen on click
//		backBtn.setOnAction( e -> {
//			primaryStage.setScene(sceneMap.get("options"));
//		});
//		backBtn.setGraphic(homeView);
//		backBtn.setStyle(btnStyle.concat("-fx-font-size: 14; -fx-padding: 10; -fx-background-radius: 25px; -fx-cursor: hand"));
//
//		BorderPane.setAlignment(backBtn, Pos.TOP_LEFT);
//		pane.setTop(backBtn);
//		Color backgroundColor = Color.web("#C7FBFF");
//		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
//
//		// container for all main controls
//		clientBox = new VBox(20, title, messageTextField, sendTo, btns, listItems2);
//		clientBox.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
//		VBox.setMargin(clientBox, new Insets(30));
//		clientBox.setAlignment(Pos.CENTER);
//
//		pane.setCenter(clientBox); // sets the VBox as the central content of the BorderPane
//
//		return new Scene(pane,800, 600);
//	}


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

	private void enableDrag(Rectangle ship) {
		final offset drag = new offset(); // holds offset from mouse click to ship's origin
		final int cellSize = 30; // size of each cell in grid
		final int gridSize = 10; // 10x10 grid

		// event handler for mouse press actions on ship
		ship.setOnMousePressed(e -> {

			// calculates and stores the offset from where ship is clicked.
			drag.x = e.getSceneX() - (ship.getX() + ship.getTranslateX());
			drag.y = e.getSceneY() - (ship.getY() + ship.getTranslateY());
			ship.setCursor(Cursor.MOVE); // changes cursor to drag
		});

		// event handler for mouse drag
		ship.setOnMouseDragged(e -> {
			// updates ship's position based on cursor's current position minus offset
			ship.setTranslateX(e.getSceneX() - drag.x - ship.getX());
			ship.setTranslateY(e.getSceneY() - drag.y - ship.getY());
		});

		// event handler for mouse release
		ship.setOnMouseReleased(e -> {
			ship.setCursor(Cursor.HAND); // change cursor back to default

			// calculates the nearest grid position to place ship
			double potentialNewX = Math.round((ship.getTranslateX() + ship.getX()) / cellSize) * cellSize;
			double potentialNewY = Math.round((ship.getTranslateY() + ship.getY()) / cellSize) * cellSize;

			// set ship's position to the nearest grid point if over the grid
			if (potentialNewX >= 0 && potentialNewX + ship.getWidth() <= cellSize * gridSize &&
				potentialNewY >= 0 && potentialNewY + ship.getHeight() <= cellSize * gridSize) {
				ship.setTranslateX(potentialNewX - ship.getX());
				ship.setTranslateY(potentialNewY - ship.getY());
			}
			// reset position if not a valid drop
			else {
				ship.setTranslateX(0);
				ship.setTranslateY(0);
			}
		});
	}

	class offset { double x, y; } // helper class to store x and y offsets for dragging

//	public Scene createSetUpShipScene(Stage primaryStage) {
//		BorderPane pane =  new BorderPane();
//		Button backBtn = getBackBtn("options", primaryStage);
//		VBox root = new VBox(10);
//		Pane shipContainer = new Pane(); // container to hold all ships
//		shipContainer.setPrefSize(300, 150);
//
//		setupGrid(shipContainer); // grid setup in ship container
//
//		int xOffset = 10; // initial horizontal offset for first ship
//		int[] shipLengths = {5, 4, 3, 3, 2}; // all ship lengths
//		for (int length : shipLengths) {
//			Rectangle ship = new Rectangle(length * 30, 30); // creates ship with specific size
//			ship.setFill(Color.GRAY);
//			enableDrag(ship); // enables dragging functionality
//			ship.setX(xOffset); // sets initial x position of ship
//			ship.setY(525); // sets initial y position of ship
//			xOffset += (length * 30) + 10; // increments x offset for next ship
//			shipContainer.getChildren().add(ship); // adds ship to the container
//			ship.setViewOrder(-100.0); // ensures the ship is rendered on top
//		}
//
//		root.getChildren().addAll(new Label("Place your ships:"), shipContainer);
//		BorderPane.setAlignment(backBtn, Pos.TOP_LEFT);
//		pane.setTop(backBtn);
//
//		Color backgroundColor = Color.web("#C7FBFF");
//		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
//		pane.setCenter(root);

//		return new Scene(pane, 800, 600);
//	}
//
//	private void setupGrid(Pane boardPane) {
//		for (int i = 0; i < 10; i++) {
//			for (int j = 0; j < 10; j++) {
//				Rectangle cell = new Rectangle(30, 30); // create each cell in the grid
//				cell.setFill(Color.LIGHTBLUE);
//				cell.setStroke(Color.BLACK);
//				cell.setX(j * 30); // position cells horizontally
//				cell.setY(i * 30); // position cells vertically
//				boardPane.getChildren().add(cell); // adds cell to the pane
//			}
//		}
//	}

	public Scene createSetUpShipScene(Stage primaryStage) {
//		List<Rectangle> ships = new ArrayList<>();
//		List<Boolean> isHorizontal = new ArrayList<>();

		
		GridPane gridPane = new GridPane();
		gridPane.setAlignment(Pos.TOP_CENTER);
		gridPane.setHgap(0);
		gridPane.setVgap(0);


		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				Button button = new Button();
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
		VBox root = new VBox(10);
		Pane shipContainer = new Pane(); // container to hold all ships
		shipContainer.setPrefSize(200, 60);

		start = new Button("Start");
		start.setStyle(btnStyle);
		start.setOnAction(e -> {
			gameStarted = true;
			System.out.println("Start clicked");
			clientConnection.send(new Message(currUsername, "Pair", grid));
		});

		if(placedShipsCounter != 5){
			start.setDisable(true);
		}

		horizontalBtn = new Button("Place Horizontally");
		verticalBtn = new Button("Place Vertically");

		int xOffset = 10; // initial horizontal offset for first ship
		int[] shipLengths = {5, 4, 3, 3, 2}; // all ship lengths
//		String[] theNames = {""};
//		String[] shipLengths = {"5", "4", "3", "3", "2"};
		for (int i = 0; i < shipLengths.length; i++) {
//			Rectangle ship = new Rectangle(length * 30, 30); // creates ship with specific size
//			ship.setFill(Color.GRAY);
//			enableDrag(ship); // enables dragging functionality
//			ship.setY(100); // sets initial y position of ship
//			ships.add(ship);
//			isHorizontal.add(true);
//			ship.setViewOrder(-100.0); // ensures the ship is rendered on top
			int length = shipLengths[i];
			Button shipButton = new Button(String.valueOf(length));
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

		HBox.setMargin(horizontalBtn, new Insets(0, 0, 0, 10)); // Adds 10 pixels of margin to the left of horizontalBtn
		HBox.setMargin(verticalBtn, new Insets(0, 0, 0, 10)); // Adds 10 pixels of margin to the left of verticalBtn
		HBox layout1 = new HBox(20, horizontalBtn, verticalBtn);

		Label placeShips = new Label("Place your ships:");
		placeShips.setStyle(subtitleStyle);

		VBox.setMargin(start, new Insets(0, 0, 0, 10));
		VBox.setMargin(placeShips, new Insets(0, 0, 0, 10));

		root.getChildren().addAll(placeShips, shipContainer, layout1, start);
		BorderPane newPane = new BorderPane();
		newPane.setCenter(gridPane);
		newPane.setBottom(root);
		newPane.setTop(backBtn);

		Scene scene = new Scene(newPane, 800, 600);
		primaryStage.setTitle("Battleship Game");
		primaryStage.setScene(scene);
		primaryStage.show();
//		return new Scene(pane, 800, 600);
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

				buttons[startX + i][startY].setText(shipLength);  // Mark the button as part of a ship
				occupied[startX + i][startY] = true;  // Mark cells as occupied
				buttons[startX + i][startY].setStyle("-fx-background-color: navy; -fx-text-fill: white");
				grid.get(startY).set(startX + i, 'B');
			}
		}
		else {
			// Place ship vertically
			for (int i = 0; i < ship.length; i++) {
				buttons[startX][startY + i].setText(shipLength);  // Mark the grid cell as occupied
				occupied[startX][startY + i] = true;  // Mark the cell as occupied
				buttons[startX][startY + i].setStyle("-fx-background-color: navy; -fx-text-fill: white");
				grid.get(startY + i).set(startX, 'B');
			}
		}
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


	private void createuserVSUserScene(Stage primaryStage, ArrayList<ArrayList<Character>> grid) {

		HBox root = new HBox(20);

//		root.setStyle("-fx-background-color: #C7FBFF; -fx-font-family: 'serif'");

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
				int finalI = x;
				int finalJ = y;
				button.setOnAction(e -> {
					handleButtonAction(finalI, finalJ);
				});
				button.setDisable(true);
				buttons2[x][y] = button;
				gridPane.add(button, y,  x);
			}
		}

		GridPane gridPaneEnemy = new GridPane();
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
					handleGridClick(finalI, finalJ);
					clientConnection.send(new Message("Move", currUsername, enemy, finalI, finalJ, true));
				});
				buttons2Enemy[x][y] = button;
				gridPaneEnemy.add(button, y,  x);
			}
		}

		// TODO: This is the hit button. When the user presses it we send messageContent "PlayTurn"
		// This message will also contain the x and y coord for the server to check if the user hit another ship or not
		// IE the clientThread.grid's ship.
		hit = new Button("Hit");
		if(myTurn){
			hit.setDisable(false);
		}
		//TODO: make sure that the user cannot send empty hit
		hit.setStyle(btnStyle);
		hit.setOnAction( e -> {
			hit.setDisable(true);
//			clientConnection.send(new Message("PlayTurn", enemy));
			clientConnection.send(new Message("Move", currUsername, enemy, xMove, yMove, true));
		});

		BorderPane pane = new BorderPane();
		Color backgroundColor = Color.web("#C7FBFF");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		root.getChildren().addAll(gridPane, gridPaneEnemy);
		root.setAlignment(Pos.CENTER);
		pane.setCenter(root);
		pane.setBottom(hit);
//		return new Scene(root, 800, 600);
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

	public Scene createSelectUserScene(Stage primaryStage){
		BorderPane pane = new BorderPane();

		// Create back button
		Image home = new Image("back_arrow.png");
		ImageView homeView = new ImageView(home);
		homeView.setFitHeight(15);
		homeView.setFitWidth(15);
		Button backBtn = new Button();

		// brings you back to the clientGUI screen when you click the back button
		backBtn.setOnAction( e -> {
			primaryStage.setScene(sceneMap.get("client"));
		});
		backBtn.setGraphic(homeView);
		backBtn.setStyle(btnStyle.concat("-fx-font-size: 14; -fx-padding: 10; -fx-background-radius: 25px; -fx-cursor: hand"));

		BorderPane.setAlignment(backBtn, Pos.TOP_LEFT);
		pane.setTop(backBtn);

		Color backgroundColor = Color.web("#C7FBFF");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		Label title = new Label("Select the user you want to send to:");
		title.setStyle(titleStyle);

		Button send = new Button("Send");

		// event handler for selecting a user from the list
		displayListItems.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				selectedUser = displayListItems.getSelectionModel().getSelectedItem();
				send.setDisable(false); // enable send button after a user is selected
			}
		});
		
		// disables the button to send if no user is selected
		if(selectedUser == ""){
			send.setDisable(true);
		}
		send.setStyle(btnStyle);

		// event handles for the send button
//		send.setOnAction(e -> {
//			String currMsgContent = messageTextField.getText();
//			String usernameCurrent = clientConnection.getUsername();
////			Message msg = new Message(usernameCurrent, currMsgContent, selectedUser);
//			clientConnection.send(msg);
//			listItems2.getItems().add("Sent to " + selectedUser + ": " + currMsgContent);
//			messageTextField.clear();
//			primaryStage.setScene(sceneMap.get("client"));
//		});

		// layout for user selection
		VBox users = new VBox(20, title, displayListItems, send);
		users.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
		VBox.setMargin(users, new Insets(30));
		users.setAlignment(Pos.CENTER);
		displayListItems.setMaxWidth(400);
		displayListItems.setMaxHeight(200);
		pane.setCenter(users);
		return new Scene(pane,800, 600);
	}

	public Scene createViewMessages(Stage primaryStage) {
		BorderPane pane = new BorderPane();
//
		// Create back button
		Image home = new Image("back_arrow.png");
		ImageView homeView = new ImageView(home);
		homeView.setFitHeight(15);
		homeView.setFitWidth(15);
		Button backBtn = new Button();

		// brings you back to options scene when you click back button
		backBtn.setOnAction( e -> {
			primaryStage.setScene(sceneMap.get("options"));
		});
		backBtn.setGraphic(homeView);
		backBtn.setStyle(btnStyle.concat("-fx-font-size: 14; -fx-padding: 10; -fx-background-radius: 25px; -fx-cursor: hand"));

		BorderPane.setAlignment(backBtn, Pos.TOP_LEFT);
		pane.setTop(backBtn);

		Color backgroundColor = Color.web("#C7FBFF");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		Label title = new Label("Messages");
		title.setStyle(titleStyle);

		VBox vbox = new VBox(10, title, listItems2); // adds message chat to vbox

		VBox.setMargin(vbox, new Insets(70));
		vbox.setAlignment(Pos.CENTER);
		BorderPane.setMargin(pane, new Insets(70));
		listItems2.setMaxWidth(400);
		listItems2.setMaxHeight(250);
		pane.setCenter(vbox);

		return new Scene(pane, 800, 600);
	}

	private Scene createWaitingScene(Stage primaryStage){
		BorderPane pane = new BorderPane();

		Color backgroundColor = Color.web("#C7FBFF");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		Label waiting = new Label("There are no available users, please wait...");
		pane.setCenter(waiting);

		return new Scene(pane, 800, 600);
	}

	// helper function to update the user list
//	private void updateUserList(Message msg) {
//		storeUsersInListView.clear(); // clears current list of users in order to update
//		storeUsersInListView.addAll(msg.getListOfUsers()); // adds all users received from server message
//		displayListUsers.setItems(storeUsersInListView); // sets updated lists
//		displayListItems.getItems().clear(); // clears items in display list that is used for selecting user to send private messages
//		for(String user: msg.getListOfUsers()) { // goes through each user received in message's user list and adds user to list if allowed
//			if(!user.equals(currUsername))
//				displayListItems.getItems().add(user);
//		}
//	}




	//		flipButton.setOnAction(e -> {
//
//			// for each ship, toggles it horizontally/vertically
//			for (int i = 0; i < ships.size(); i++) {
//
//				Rectangle ship = ships.get(i);
//				boolean horizontal = isHorizontal.get(i);
//
//				if (horizontal) {
//					ship.setWidth(shipLengths[i] * 30);
//					ship.setHeight(30);
//				} else {
//					ship.setWidth(30);
//					ship.setHeight(shipLengths[i] * 30);
//				}
//				isHorizontal.set(i, !horizontal);
//
//			}
//		});

	class ShipInfo {
		Button shipButton;
		int length;
		boolean isPlaced = false;

		public ShipInfo(Button shipButton, int length) {
			this.shipButton = shipButton;
			this.length = length;
		}
	}

//	public class BattleshipAI {
//		private int gridSize = 10; // assuming a 10x10 grid
//		private boolean[][] grid = new boolean[gridSize][gridSize]; // track placed ships
//		public GuiClient theGui = new GuiClient();
//
//		// Randomly place ships on the grid
//		public void placeShips() {
//			int[] shipSizes = {5, 4, 3, 3, 2}; // sizes of the ships
//			Random random = new Random();
//
//			for (int size : shipSizes) {
//				boolean placed = false;
//				while (!placed) {
//					int x = random.nextInt(gridSize);
//					int y = random.nextInt(gridSize);
//					boolean horizontal = random.nextBoolean();
//					//int startX, int startY, ShipInfo ship
//					if (canPlaceShip(x, y, ShipInfo ship)) {
//						for (int i = 0; i < size; i++) {
//							if (horizontal) {
//								grid[x][y + i] = true; // Place horizontally
//							} else {
//								grid[x + i][y] = true; // Place vertically
//							}
//						}
//						placed = true;
//					}
//				}
//			}
//
//		}
//	}
}
