# Multiplayer Blackjack (Client / Server)

A simple, networked multiplayer Blackjack game written entirely in Java.  
This repository includes a server that manages a Blackjack table and a Swing GUI client that connects to the server. The project was developed for learning and coursework purposes, focusing on distributed systems, concurrency, and socket programming, using plain Java (no external libraries).

## Table of contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Message Protocol](#message-protocol)
- [Requirements](#requirements)
- [Running the Project](#running-the-project)
- [Configuration](#configuration)
- [Known Issues & Future Work](#known-issues--future-work)
- [Contributing](#contributing)
- [License](#license)

## Project Overview
This project implements a lightweight multiplayer Blackjack system:
- **Server:** A single central server accepts TCP clients and manages a single Blackjack table (up to 3 seated players and a waiting queue).
- **Client:** A Java Swing-based client connects to the server, displays the table, receives game updates in real-time, and allows the user to `HIT`, `STAND`, spectate, or log out.

**Key behaviors:**
- Players pay a 2-chip ante at the start of the round.
- The dealer follows common casino rules (draw until 17 or greater).
- Standard Blackjack payouts and tie rules are implemented (e.g., natural blackjack pays a premium).
- Automated timers handle the start of rounds and player turns. If a player fails to act within the time limit, an auto-stand is enforced to prevent deadlocks.

## Features
- **Server-driven game state:** The server deals cards, enforces rules, and securely calculates results.
- **Client GUI:** A visual representation of the table, showing players, dealer's hand, cards, real-time messages, and chip counts.
- **Queue System:** A waiting queue manages players when the table is full. Spectators can join the queue and are automatically promoted to the table when a seat opens up.
- **Custom Protocol:** A fast, simple text-based protocol over TCP handles commands and server broadcasts.

## Project Structure (Core Files)
- `src/server/ServerMain.java` — Main entry point for the server.
- `src/server/logic/BlackjackTable.java` — Core game logic, synchronization, and state management.
- `src/server/network/ClientHandler.java` — Per-client thread managing incoming socket commands.
- `src/client/ClientMain.java` — Main entry point for the Swing client.
- `src/client/gui/TableFrame.java` — Main Swing UI handling server events and rendering.
- `src/client/network/ConnectionThread.java` — Client-side networking background thread.
- `src/client/images/cards/` — Directory containing the card image assets used by the GUI.

## Message Protocol

**Client → Server**
- Format: `COMMAND:DATA` (UTF-8 string written via `DataOutputStream.writeUTF`)
- Commands:
    - `LOGIN:<username>` — Request to join the session.
    - `HIT:` — Request to draw a card.
    - `STAND:` — Request to stand.
    - `SPECTATE:` — Request to become a spectator (leaves the seat).
    - `LOGOUT:` — Safely disconnect and close the session.

**Server → Client**
- Format: `[HH:mm:ss];COMMAND;DATA1;DATA2;...` (Semicolon-separated)
- Examples:
    - `BEGIN;...` — A new round has started.
    - `ROUND_START;<name>;<chips>` — Player paid the ante; chip count updated.
    - `PLAYER_CARD;<playerName>;<cardName>;<totalPts>;<position>` — Card dealt to a player.
    - `DEALER_CARD;<cardName>;<face down|face up>;<total>` — Dealer's card logic.
    - `TURN;<playerName>` — Notifies clients whose turn it is.
    - `RESULT;<type>;<name>;<playerPts>;<dealerPts>;<playerChips>` — Round outcome for a player.
    - `WAIT;<name>;<position>` — Player added to the waiting queue.

*(See `ClientHandler.java` and `BlackjackTable.java` for the complete message parsing logic.)*

## Requirements
- **Java 8+** (Uses the `java.time` API). Java 11 or later is recommended.
- No external libraries or dependencies are required (relies on Java SE: Swing, `java.net`, `java.util.concurrent`).
- Recommended IDE: IntelliJ IDEA, Eclipse, or NetBeans.

## Running the Project

### Using an IDE (Recommended)
1. Clone the repository and open the project directory in your preferred IDE.
2. Ensure the `src` folder is marked as the Sources Root.
3. Run `server.ServerMain` first to start the server.
4. Run one or more instances of `client.ClientMain` to open the client GUIs and connect to the server.

### Command Line (Windows / Linux / macOS)
From the root directory of the project:

**1. Compile all Java sources:**
```bash
# Create an output directory
mkdir out

# Compile (Linux/macOS)
find src -name "*.java" > sources.txt
javac -d out @sources.txt

# Compile (Windows PowerShell)
$files = Get-ChildItem -Path .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $files

```

**2. Run the server:**
```bash
java -cp out server.ServerMain
```

**3. Run the client (in a new terminal):**
```bash
java -cp out client.ClientMain
```

## Configuration
- **Server Port:** Can be edited in `src/server/ServerMain.java` (defaults to port `4000`).
- **Assets:** Card images must be located inside the `src/client/images/cards/` directory for the client to render them correctly.

## Known Issues & Future Work
- **Single Table:** Currently, the server only handles a single global Blackjack table.
- **Ephemeral Data:** No persistent authentication or database. Player chips and states reset when the server restarts.
- **Possible Enhancements:**
    - Support for multiple concurrent tables/rooms.
    - Integration with a database (e.g., PostgreSQL/SQLite) for persistent player accounts and leaderboards.
    - In-game chat system between players.
    - Robust reconnect logic for dropped connections.

## Debugging Tips
- If clients cannot connect, verify that the server is running and listening on the correct port.
- Ensure your OS firewall allows Java applications to accept and establish incoming/outgoing connections.
- The server prints raw incoming messages to the console, and client events are logged in the GUI's text area, making it easy to trace protocol issues.

## Contributing
Contributions are welcome! Please fork the repository, make your changes, and submit a pull request. Make sure to test both the server and multiple client instances when adding new features or modifying the protocol.

## License
This project is provided for educational purposes.