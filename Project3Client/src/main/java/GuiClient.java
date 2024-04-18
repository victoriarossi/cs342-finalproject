import java.io.Serializable;
import java.util.HashMap;
import java.util.function.Consumer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
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

	TextField c1;
	Button b1;
	HashMap<String, Scene> sceneMap;
	VBox clientBox;
	Client clientConnection;

	private String messageContent;
	ListView<String> listItems2;
	ListView<String> displayListUsers;
	ListView<String> displayListItems;
	ObservableList<String> storeUsersInListView;

	String currUsername;


	// styling strings for different UI
	String btnStyle = "-fx-background-color: #DDC6A3; -fx-text-fill: black; -fx-background-radius: 25px; -fx-padding: 14; -fx-cursor: hand; -fx-font-size: 18";
	String titleStyle = "-fx-font-size: 24; -fx-font-weight: bold";
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
					updateUserList(msg);
					primaryStage.setScene(sceneMap.get("options"));
				}

				// handles case where the username is already taken
				else if ("Taken Username".equals(msg.getMessageContent())) {
					showAlert("Be original, Professor McCarty dislikes copycats!", primaryStage);
				}
				else {
					// if the user leaves, shows the leave message in the chat log and updates the active users list
					if (msg.getMessageContent().endsWith("has left the chat.")) {
						listItems2.getItems().add(msg.getMessageContent());
						updateUserList(msg);
					}
					else {

						// updates the user list as long as it contains users
						if (msg.getListOfUsers() != null) {
							updateUserList(msg);
						}

						// determines if message is private and meant for or from the current user
						boolean isPrivate = msg.getMessageType() == Message.MessageType.PRIVATE;
						boolean isForCurrentUser = isPrivate && msg.getUserIDReceiver().equals(clientConnection.getUsername());

						// handles the private messages
						if (isPrivate) {
							if (isForCurrentUser || msg.getUserID().equals(clientConnection.getUsername())) {
								String privateMsg = "Whisper from " + msg.getUserID() + ": " + msg.getMessageContent();
								listItems2.getItems().add(privateMsg);
							}
						}
						// handles non-private messages
						else {
							if (!"New User".equals(msg.getMessageContent())) {
								listItems2.getItems().add(msg.getUserID() + " sent: " + msg.getMessageContent());
							}
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

		c1 = new TextField(); // input field for messages
		b1 = new Button("Send"); // send button for messages
		b1.setOnAction(e->{
			String messageContent = c1.getText();
			String currUsername = clientConnection.getUsername();
			Message message = new Message(currUsername, messageContent, Message.MessageType.BROADCAST);
			clientConnection.send(message);
			c1.clear();
		});

		// scene map for different scenes
		sceneMap = new HashMap<String, Scene>();
		sceneMap.put("startScene", createIntroScene(primaryStage));
		sceneMap.put("rules", createRulesScene(primaryStage));
		sceneMap.put("client",  createClientGui(primaryStage));
		sceneMap.put("clientLogin", createLoginScene(primaryStage)); // adds login screen to scene map
		sceneMap.put("options", createOptionsScene(primaryStage)); // adds the options screen to scene map
		sceneMap.put("users", createViewUsersScene(primaryStage)); // adds the view users screen to scene map
		sceneMap.put("selectUser", createSelectUserScene(primaryStage)); //add the select user screen to scene map
		sceneMap.put("viewMessages", createViewMessages(primaryStage));

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

	private Button getBackBtn(String scene, Stage primaryStage){
		// Create back button
		Image home = new Image("back_arrow.png");
		ImageView homeView = new ImageView(home);
		homeView.setFitHeight(15);
		homeView.setFitWidth(15);
		Button backBtn = new Button();

		// brings you back to home screen on click
		backBtn.setOnAction( e -> {
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
		root.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
		root.setAlignment(Pos.CENTER);

		BorderPane.setAlignment(backBtn, Pos.TOP_LEFT);
		pane.setTop(backBtn);
		Color backgroundColor = Color.web("#F4DAB3");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		pane.setCenter(root); // sets the VBox as the central content of the BorderPane

		// returns login scene
		return new Scene(pane,800, 600);
	}

	// creates main client UI
	public Scene createClientGui(Stage primaryStage) {
		BorderPane pane =  new BorderPane();

		// sets and styles the title of the send message screen
		Label title = new Label("Input your message:");
		title.setStyle(subtitleStyle + "; -fx-padding: 10");

		messageTextField = new TextField();
		messageTextField.setMaxWidth(250);
		messageTextField.setStyle("-fx-padding: 10; -fx-background-radius: 25px;");

		// label to show the recipient of the message
		Label sendTo = new Label("Send to: ");
		sendTo.setStyle(subtitleStyle);

		// creates the buttons to send to all or 1 user
		Button allUsers = new Button("All users");
		Button oneUser = new Button("One user");
		allUsers.setStyle(btnStyle);
		oneUser.setStyle(btnStyle);
		HBox btns = new HBox(20, allUsers, oneUser);
		btns.setAlignment(Pos.CENTER);

		// handles on click event for the all users button
		allUsers.setOnAction( e -> {
			String messageContent = messageTextField.getText();
			String currUsername = clientConnection.getUsername();
			Message msg = new Message(currUsername, messageContent, Message.MessageType.BROADCAST);
			clientConnection.send(msg);
			messageTextField.clear();
		});

		// handles on click event for sending to 1 user button
		oneUser.setOnAction(e -> {
			messageContent = messageTextField.getText();
			primaryStage.setScene(sceneMap.get("selectUser"));
		});

		// disables sending buttons if message field is empty
		allUsers.disableProperty().bind(messageTextField.textProperty().isEmpty());
		oneUser.disableProperty().bind(messageTextField.textProperty().isEmpty());

		// Create back button
		Image home = new Image("back_arrow.png");
		ImageView homeView = new ImageView(home);
		homeView.setFitHeight(15);
		homeView.setFitWidth(15);
		Button backBtn = new Button();

		// brings you back to options screen on click
		backBtn.setOnAction( e -> {
			primaryStage.setScene(sceneMap.get("options"));
		});
		backBtn.setGraphic(homeView);
		backBtn.setStyle(btnStyle.concat("-fx-font-size: 14; -fx-padding: 10; -fx-background-radius: 25px; -fx-cursor: hand"));

		BorderPane.setAlignment(backBtn, Pos.TOP_LEFT);
		pane.setTop(backBtn);
		Color backgroundColor = Color.web("#F4DAB3");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		// container for all main controls
		clientBox = new VBox(20, title, messageTextField, sendTo, btns, listItems2);
		clientBox.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
		VBox.setMargin(clientBox, new Insets(30));
		clientBox.setAlignment(Pos.CENTER);

		pane.setCenter(clientBox); // sets the VBox as the central content of the BorderPane

		return new Scene(pane,800, 600);
	}


	// shows popup for invalid usernames
	private void showAlert(String message, Stage primaryStage) {

		VBox root = new VBox(20);
		root.setAlignment(Pos.CENTER);
		root.setStyle("-fx-background-color: #F4DAB3");

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
		Scene errorScene = new Scene(root, 500, 400);
		primaryStage.setScene(errorScene);
		primaryStage.show();
	}

	// creates options scene
	public Scene createOptionsScene(Stage primaryStage){



		VBox root = new VBox(40);
		root.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
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

		Color backgroundColor = Color.web("#F4DAB3");
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
		root.setStyle("-fx-background-color: #00BFFF");

		Button backBtn = getBackBtn("startScene", primaryStage);

		// sets the title and styling
		Label title = new Label("Game Rules");
		title.setFont(javafx.scene.text.Font.font("Arial", 24));
		title.setStyle("-fx-text-fill: #FFFFFF");

		// sets all rules and makes sure the text wraps if it is too long for the window width
		Label rule1 = new Label("1. Objective: The goal of Battleship is to sink all of your opponent's ships before they sink all of yours.");
		rule1.setWrapText(true);

		Label rule2 = new Label("2. Setup: Each player places their ships on a grid without the other player seeing where they are placed.");
		rule2.setWrapText(true);

		Label rule3 = new Label("3. Gameplay: Players take turns guessing grid coordinates to attack enemy ships.");
		rule3.setWrapText(true);

		Label rule4 = new Label("4. Scoring: The game ends when all ships of one player are sunk. The player who sinks all enemy ships first wins.");
		rule4.setWrapText(true);

		// creates return button and its styling/event handler
//		Button returnBtn = new Button("Return to Main Menu");
//		returnBtn.setFont(javafx.scene.text.Font.font("Arial", 16));
//		returnBtn.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white; -fx-cursor: hand");
//		returnBtn.setOnAction(e -> primaryStage.setScene(sceneMap.get("startScene")));

		BorderPane.setAlignment(backBtn, Pos.TOP_LEFT);
		pane.setTop(backBtn);

		Color backgroundColor = Color.web("#F4DAB3");
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
		startBtn.setFont(javafx.scene.text.Font.font("Arial", 20));
		startBtn.setPrefSize(200, 50);
		startBtn.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white; -fx-cursor: hand");
		startBtn.setOnAction(e -> primaryStage.setScene(sceneMap.get("clientLogin")));

		// Rules Button and styling
		Button rulesBtn = new Button("Rules");
		rulesBtn.setFont(javafx.scene.text.Font.font("Arial", 20));
		rulesBtn.setPrefSize(200, 50);
		rulesBtn.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white; -fx-cursor: hand");
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

		Color backgroundColor = Color.web("#F4DAB3");
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
		send.setOnAction(e -> {
			String currMsgContent = messageTextField.getText();
			String usernameCurrent = clientConnection.getUsername();
			Message msg = new Message(usernameCurrent, currMsgContent, selectedUser);
			clientConnection.send(msg);
			listItems2.getItems().add("Sent to " + selectedUser + ": " + currMsgContent);
			messageTextField.clear();
			primaryStage.setScene(sceneMap.get("client"));
		});

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

		Color backgroundColor = Color.web("#F4DAB3");
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

	// helper function to update the user list
	private void updateUserList(Message msg) {
		storeUsersInListView.clear(); // clears current list of users in order to update
		storeUsersInListView.addAll(msg.getListOfUsers()); // adds all users received from server message
		displayListUsers.setItems(storeUsersInListView); // sets updated lists
		displayListItems.getItems().clear(); // clears items in display list that is used for selecting user to send private messages
		for(String user: msg.getListOfUsers()) { // goes through each user received in message's user list and adds user to list if allowed
			if(!user.equals(currUsername))
				displayListItems.getItems().add(user);
		}
	}
}
