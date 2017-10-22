package chatapp;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

import echo.multithread.MultiThreadEchoServer;

class ChatServer extends MultiThreadEchoServer {
    
    private Map<String, Client> clients = new HashMap<> ();
    private List<Client> clientList = new ArrayList<> ();
    private Map<String, Group> groups = new HashMap<> ();
    
    private List<ReservedMessage> reservedMessages;
    
    private ChatServer () {
        this (80);
    }
    
    private ChatServer (int port) {
        super (port);
        initReservedMessage ();
    }
    
    private void initReservedMessage () {
        reservedMessages = new ArrayList<> ();
        reservedMessages.add (new ReservedMessage ("/", "commands") {
            @Override
            void onHandle (Client client, String command, String arg) {
                client.sendMessage ("Command List:\n" +
                                    "    /commands            - show this message.\n" +
                                    "    !broadcast [msg]     - broadcast message to all friends" +
                                    "    @[username] [msg]    - send pm to specified username.\n" +
                                    "    #[groupname] [msg]   - send message to group.\n" +
                                    "    /users               - show list of user connected to this chat app.\n" +
                                    "    /add [username]      - add user as friend" +
                                    "    /remove [username]   - remove user as friend" +
                                    "    /friends             - display friend list" +
                                    "    /create [groupname]  - create a group.\n" +
                                    "    /join [groupname]    - join a group.\n" +
                                    "    /leave [groupname]   - leave a group.\n" +
                                    "    /members [groupname] - display the members of a group.\n" +
                                    "    /time [username]     - get user local time" +
                                    "    /quit                - quit this chat app.");
            }
        });
        reservedMessages.add (new ReservedMessage ("!", "broadcast") {
            @Override
            void onHandle (Client client, String command, String message) {
                for (Client friend : client.friendList)
                    friend.sendMessage (client, message);
            }
        });
        reservedMessages.add (new ReservedMessage ("@", "") {
            @Override
            protected void onHandle (Client client, String username, String message) {
                if (!clients.containsKey (username))
                    client.sendMessage ("User does not exists.");
                else
                    clients.get (username).sendMessage (client, message);
            }
        });
        reservedMessages.add (new ReservedMessage ("#", "") {
            @Override
            protected void onHandle (Client client, String groupName, String message) {
                if (!groups.containsKey (groupName))
                    client.sendMessage ("Group does not exists");
                else {
                    Group group = groups.get (groupName);
                    
                    if (!group.hasMember (client))
                        client.sendMessage (String.format ("You are not member of this group (%s).", groupName));
                    else
                        group.sendMessage (client, message);
                }
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "users") {
            @Override
            protected void onHandle (Client client, String command, String arg) {
                String msg = clientList
                    .stream ()
                    .map (Client::indentedUsername)
                    .collect (Collectors.joining ("\n"));
                client.sendMessage (String.format ("User List:\n%s", msg));
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "add") {
            @Override
            void onHandle (Client client, String command, String username) {
                if (!clients.containsKey (username))
                    client.sendMessage ("User does not exists");
                else
                    client.addFriend (clients.get (username));
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "remove") {
            @Override
            void onHandle (Client client, String command, String username) {
                if (!clients.containsKey (username))
                    client.sendMessage ("User does not exists");
                else
                    client.unfriend (clients.get (username));
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "friends") {
            @Override
            protected void onHandle (Client client, String command, String arg) {
                String msg = client.friendList
                    .stream ()
                    .map (Client::indentedUsername)
                    .collect (Collectors.joining ("\n"));
                client.sendMessage (String.format ("Friend List:\n%s", msg));
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "create") {
            @Override
            protected void onHandle (Client client, String command, String groupName) {
                if (!groupName.matches ("[a-zA-Z0-9]+"))
                    client.sendMessage ("Group name can only contains alphanumeric.");
                else {
                    if (groups.containsKey (groupName))
                        client.sendMessage ("Group already exists.");
                    else {
                        groups.put (groupName, new Group (groupName));
                        client.sendMessage (String.format ("Group (%s) created.", groupName));
                        output.println (String.format ("Group (%s) created by %s.", groupName, client.username));
                    }
                }
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "join") {
            @Override
            protected void onHandle (Client client, String command, String groupName) {
                if (!groups.containsKey (groupName))
                    client.sendMessage ("Group does not exists.");
                else
                    groups.get (groupName).add (client);
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "leave") {
            @Override
            protected void onHandle (Client client, String command, String groupName) {
                if (!groups.containsKey (groupName))
                    client.sendMessage ("Group does not exists.");
                else
                    groups.get (groupName).remove (client);
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "members") {
            @Override
            protected void onHandle (Client client, String command, String groupName) {
                if (!groups.containsKey (groupName))
                    client.sendMessage ("Group does not exists.");
                else {
                    Group group = groups.get (groupName);
                    if (!group.hasMember (client))
                        client.sendMessage (String.format ("You are not member of this group (%s).", groupName));
                    else {
                        String msg = group.memberList
                            .stream ()
                            .map (Client::indentedUsername)
                            .collect (Collectors.joining ("\n"));
                        client.sendMessage (String.format ("Member List (%s):\n%s", groupName, msg));
                    }
                }
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "time") {
            @Override
            void onHandle (Client client, String command, String username) {
                if (!clients.containsKey (username))
                    client.sendMessage ("User does not exists.");
                else
                    clients.get (username).sendMessage (String.format ("/time %s", client.username));
            }
        });
    }
    
    @Override
    protected void handleClient (Socket client) {
        new Client (client);
    }
    
    public static void main (String[] argv) {
        ChatServer server = new ChatServer ();
        server.start ();
    }
    
    private class Client extends Thread {
        
        String username;
        
        private List<Group> clientGroups = new ArrayList<> ();
        private List<Client> friendList = new ArrayList<> ();
        private Map<String, Client> friends = new HashMap<> ();
        private BufferedReader is;
        private PrintWriter os;
        private Socket socket;
        
        Client (Socket socket) {
            try {
                this.socket = socket;
                is = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
                os = new PrintWriter (socket.getOutputStream (), true);
                start ();
            }
            catch (IOException e) {
                e.printStackTrace (System.out);
                close (socket);
            }
        }
        
        @Override
        public void run () {
            try {
                // prompt user for a valid username
                while ((username = is.readLine ()) != null)
                    if (!username.matches ("[a-zA-Z0-9]+"))
                        os.println ("Username can only contains alphanumeric!");
                    else if (clients.containsKey (username))
                        os.println ("Username already taken!");
                    else
                        break;
                
                // username valid.
                // Notify the user and system, also add it to socket list.
                String msg = String.format ("%s joined.", username);
                os.println (msg);
                os.println (String.format ("Welcome, %s. Type /commands for help.", username));
                output.println (msg);
                clients.put (username, this);
                clientList.add (this);
                
                // request listener loop
                String request;
                while ((request = is.readLine ()) != null) {
                    if (request.equals ("/quit"))
                        break;
                    
                    // find matching command available.
                    // Drop request (ignore) if none.
                    for (ReservedMessage reservedMessage : reservedMessages) {
                        if (request.startsWith (reservedMessage.delimiter + reservedMessage.command)) {
                            reservedMessage.handle (this, request);
                            break;
                        }
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace (System.out);
            }
            
            exit ();
        }
        
        String indentedUsername () {
            return String.format ("    %s", username);
        }
        
        void sendMessage (Client from, String msg) {
            os.println (String.format ("%s: %s", from.username, msg));
        }
        
        void sendMessage (String msg) {
            os.println (msg);
        }
        
        void addFriend (Client user) {
            friends.put (user.username, user);
            friendList.add (user);
            sendMessage (String.format ("%s added.", user.username));
        }
        
        void unfriend (Client friend) {
            if (!friends.containsKey (friend.username))
                sendMessage (String.format ("You didn't friend with %s", friend.username));
            else {
                friends.remove (friend.username);
                friendList.remove (friend);
                sendMessage (String.format ("%s removed from friend list", friend.username));
            }
        }
        
        void joinGroup (Group group) {
            clientGroups.add (group);
            os.println (String.format ("You joined the group (%s).", group.name));
        }
        
        void leaveGroup (Group group) {
            clientGroups.remove (group);
            os.println (String.format ("You left the group (%s).", group.name));
        }
        
        void exit () {
            try {
                for (Group group : clientGroups)
                group.remove (this);
            }
            catch (ConcurrentModificationException ignored) {}
    
            for (Client client : clientList) {
                if (client.friends.containsKey (username))
                    client.unfriend (this);
            }
    
            clients.remove (username);
            clientList.remove (this);
            close (socket);
        }
    }
    
    private class Group {
        
        private Map<String, Client> members = new HashMap<> ();
        private String name;
        
        List<Client> memberList = new ArrayList<> ();
        
        Group (String name) {
            this.name = name;
        }
        
        String buildMessage (Client sender, String msg) {
            return String.format ("%s%s",
                                  sender.username,
                                  buildSystemMessage (msg));
        }
        
        String buildSystemMessage (String msg) {
            return String.format ("@%s: %s", name, msg);
        }
        
        void sendMessage (Client sender, String msg) {
            String message = buildMessage (sender, msg);
            for (Client client : memberList)
                if (client != sender)
                    client.sendMessage (message);
        }
        
        void sendSystemMessage (Client sender, String msg) {
            String message = buildSystemMessage (msg);
            for (Client client : memberList)
                if (client != sender)
                    client.sendMessage (message);
        }
        
        boolean hasMember (Client client) {
            return members.containsValue (client);
        }
        
        void add (Client client) {
            if (hasMember (client)) {
                client.sendMessage (String.format ("You already joined the group (%s).", name));
                return;
            }
            
            // put socket to server socket list and
            // add this group to socket group list
            members.put (client.username, client);
            memberList.add (client);
            client.joinGroup (this);
            
            // notify system and all member of the group
            // that someone joined this group.
            String msg;
            msg = String.format ("%s joined.", client.username);
            sendSystemMessage (client, msg);
            output.println (buildSystemMessage (msg));
        }
        
        void remove (Client client) {
            if (!hasMember (client)) {
                client.sendMessage (String.format ("You are not member of this group (%s).", name));
                return;
            }
            
            // remove socket from server socket list and
            // remove this group from socket group list
            members.remove (client.username);
            memberList.remove (client);
            client.leaveGroup (this);
            
            // notify system and all member of the group
            // that someone left this group.
            String msg;
            msg = String.format ("%s left.", client.username);
            sendSystemMessage (client, msg);
            output.println (buildSystemMessage (msg));
        }
    }
    
    private abstract class ReservedMessage {
    
        String delimiter;
        String command;
    
        ReservedMessage (String delimiter, String command) {
            this.delimiter = delimiter;
            this.command = command;
        }
        
        final void handle (Client client, String request) {
            String args[] = request.split (String.format ("[%s ]", delimiter), 3);
            onHandle (client, args[1], args.length == 3 ? args[2] : null);
        }
        
        abstract void onHandle (Client client, String command, String arg);
    }
}
