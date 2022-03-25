# p2p Chat

p2pChat app build with javaFx using UDP hole-punching



![chat-screen](https://user-images.githubusercontent.com/53341064/159577880-2e06a59c-7eed-4ffc-a8ce-e2849485051d.png)

client has two working modes:
1. direct connection to other client without registration
2. using main server for information exchange

# client to server

Main purpose of server is to act as a meeting point for two clients to exchange information - ip address and udp socket port number.

Server is also used to indicate which users from a friends lists are currently online and signaling to client that it is possible to start udp connection with those users.

Users ip is stored only while he is connected to main server and is deleted upon going offline.

Additional functionality includes saving friends lists in db (later will be moved to local storage).  


# client to client

Client to client communication is based on udp hole-punching method. Main downside to that method is that such connection is not guaranteed to work with every router.

Direct mode:

Connection to other client without registering account. Works by specifying other users public ip and agreeing upon using specific port number beforehand.
For now works only with single user, search for other users in this mode is disabled.

Server connection:

Enabled by registering new account (does not require additional data). 

Logging in enables other users search and adding them to yours contacts list making it possible to communicate with more than one user at a time compared to direct mode.

# configurations

server:

1. Postgresql must be installed
2. Change the login information for postgreSQL on application properties.
3. Download Maven dependencies

client:

1.Change server ip in Server class  

# TODO
1. multiple user support in direct mode
2. group chats
3. TURN functionality for server

