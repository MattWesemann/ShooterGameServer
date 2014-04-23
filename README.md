This is the backend server for a top-down arena shooter created as a project for CSCI 443.

Notable features of server:
- Socket I/O done asyncly. The sockets from every connected player have their own read thread and 
  write for their socket. The send thread works by owning a concurrent queue that takes messages passed
  in from the main server loop. This makes sure that all sends still happen in the right order. The 
  read thread reads messages from the clients which are then added to another concurrent queue that
  the game server owns. 
- Message based. This allows for a scalable framework by making as much asynchronous as possible while
  still making sure that everything required to be synchronous is serialized. The game server simply processes
  messages while also updating game objects at a fixed rate (like bullets moving).
- Uses simple dead reckoning. Both the client and the server simulate parts of the game. If a client fires,
  it knows to show and move the bullets without asking the server where the bullet is. It can also do collision
  detection to remove bullets. This reduces lag because you do not have to wait for a server response. The server
  then should correct the client periodically with the correct information. The server has the final say in what heppens.
- Gracefully handles socket disconnects. When someone disconnects the other players are made aware so they can be
  removed from the map. Some timers will send information at a given future time (the 3 second delay for spawning
  for example). If the player spawning disconnects, the server detects and does not send spawn information for a
  non-existent player.

  
Missing features:
- Better validation of client messages.
- Improved dead reckoning by correcting clients.