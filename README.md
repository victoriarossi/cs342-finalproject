# Networked Battleship (Java + JavaFX) — CS 342 Final Project

**Course:** UIC CS 342  
**Project:** Final Project (Group) — Battleship

A team project that implements **Battleship** as a **networked client/server game** using Java sockets and a JavaFX UI.

At a high level:
- The **server** accepts client connections, keeps track of connected players, and routes messages between clients.
- The **client** provides a JavaFX interface for joining a game and playing.
- An optional **AI opponent** is included (random ship placement + non-repeating move selection).


---

## Tech stack

- **Java** (client + server)
- **JavaFX** (GUI)
- **TCP sockets** on localhost (`127.0.0.1:5555`)
- **Object streams** (`ObjectInputStream` / `ObjectOutputStream`)
- **Serializable** objects for sending structured messages and game state

---

## Gameplay overview (Battleship)

Battleship is played on a grid where players:
1. Place ships (horizontal/vertical)
2. Take turns guessing coordinates
3. Receive **hit/miss** feedback
4. Win by sinking all opponent ships

---

## Architecture

### Server side
- Listens on port **5555** and spawns a per-client handler thread
- Uses a `Message` object as the main “envelope” for communication
- Tracks connected users and can broadcast updates (e.g., user list) and route direct messages

Entry points / files:
- `GuiServer.java` — JavaFX server window + startup
- `Server.java` — networking + client thread management

### Client side
- Connects to `127.0.0.1:5555`
- Uses object streams to send/receive `Message` objects
- JavaFX GUI handles screens + user interaction

Entry points / files:
- `GuiClient.java` — JavaFX client app
- `Client.java` — socket connection + send/receive loop

---

## AI opponent

`BattleshipAI.java` provides a baseline AI that:
- randomly places ships on a 10×10 grid without overlap
- generates random **non-repeating** moves

This is meant as a functional opponent, not a “smart” Battleship strategy.

---

## Key classes / data objects

- `Message.java` — Serializable message object used for communication
- `ShipInfo.java` — Serializable ship representation (ship length + placed positions)
- `UserInfo.java` — per-player game state container (username, grid, ship list)
- `BattleshipAI.java` — AI placement + move generation

---

## How to run

This assignment is typically structured as **two Maven projects** (one client, one server).

1) **Start the server**
- Run `GuiServer` first (server must be running before clients connect).

2) **Start one or more clients**
- Run `GuiClient` in a separate process.
- Run **two clients** to test player-vs-player.
- Run a **single client** to test player-vs-AI (if enabled in your UI flow).

---

## Wireframes / diagrams

CS 342 required UI wireframes and state diagrams for both client and server.  
If you included hand-drawn wireframes, they’re still worth keeping in the repo (even as photos/scans) because they show the intended screen flow and state transitions.

---

## Team

Group project for CS 342.  
Victoria Rossi
Shaan Kohli
Hristian Tountchev  

---

## Author

Hristian Tountchev  
UIC — CS 342
