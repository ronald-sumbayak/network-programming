package chatapp;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

import echo.multithread.MultiThreadEchoServer;

public class ChatServer extends MultiThreadEchoServer {
    
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
        /*
        This is a long list of reserved messages
        available in this server.
        I did my best to make it as clear as possible.
        Take a deep breath before you begin,
        try to understand thoroughly before any edit,
        and don't do anything stupid.
        
        Regards, me.
        */
        reservedMessages = new ArrayList<> ();
        reservedMessages.add (new ReservedMessage ("/", "commands") {
            @Override
            void onHandle (Client client, String command, String arg) {
                client.sendSystemMessage ("Command List:\n" +
                                          "    /commands\t\t\t\t- show this message.\n" +
                                          "    !broadcast [msg]\t\t\t- broadcast message to all friends\n" +
                                          "    @[username] [msg]\t\t- send pm to specified username.\n" +
                                          "    #[groupname] [msg]\t\t- send message to group.\n" +
                                          "    /users\t\t\t\t\t- show list of user connected to this chat app.\n" +
                                          "    /add [username]\t\t\t- add user as friend\n" +
                                          "    /remove [username]\t\t- remove user as friend\n" +
                                          "    /friends\t\t\t\t- display friend list\n" +
                                          "    /create [groupname]\t\t- create a group.\n" +
                                          "    /join [groupname]\t\t- join a group.\n" +
                                          "    /leave [groupname]\t\t- leave a group.\n" +
                                          "    /members [groupname]\t- display the members of a group.\n" +
                                          "    /time [username]\t\t\t- get user local time\n" +
                                          "    /quit\t\t\t\t\t- quit this chat app.\n");
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
                    client.sendAsUserMessage (username, "User does not exists.");
                else
                    clients.get (username).sendMessage (client, message);
            }
        });
        reservedMessages.add (new ReservedMessage ("#", "") {
            @Override
            protected void onHandle (Client client, String groupName, String message) {
                if (!groups.containsKey (groupName))
                    client.sendAsGroupMessage (groupName, "Group does not exists.");
                else {
                    Group group = groups.get (groupName);
                    
                    if (!group.hasMember (client))
                        client.sendAsGroupMessage (groupName, "You are not member of this group.");
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
                client.sendSystemMessage ("User List:\n" + msg);
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "add") {
            @Override
            void onHandle (Client client, String command, String username) {
                if (!clients.containsKey (username))
                    client.sendAsUserMessage (username, "User does not exists.");
                else
                    client.addFriend (clients.get (username));
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "remove") {
            @Override
            void onHandle (Client client, String command, String username) {
                if (!clients.containsKey (username))
                    client.sendAsUserMessage (username, "User does not exists.");
                else
                    client.unFriend (clients.get (username));
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "friends") {
            @Override
            protected void onHandle (Client client, String command, String arg) {
                String msg = client.friendList
                    .stream ()
                    .map (Client::indentedUsername)
                    .collect (Collectors.joining ("\n"));
                client.sendSystemMessage ("Friend List:\n" + msg);
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "create") {
            @Override
            protected void onHandle (Client client, String command, String groupName) {
                if (!groupName.matches ("[a-zA-Z0-9]+"))
                    client.sendAsGroupMessage (groupName, "Group name can only contains alphanumeric.");
                else {
                    if (groups.containsKey (groupName))
                        client.sendAsGroupMessage (groupName, "Group already exists.");
                    else {
                        groups.put (groupName, new Group (groupName));
                        client.sendAsGroupMessage (groupName, "Group created.");
                    }
                }
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "join") {
            @Override
            protected void onHandle (Client client, String command, String groupName) {
                if (!groups.containsKey (groupName))
                    client.sendAsGroupMessage (groupName, "Group does not exists.");
                else
                    groups.get (groupName).add (client);
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "leave") {
            @Override
            protected void onHandle (Client client, String command, String groupName) {
                if (!groups.containsKey (groupName))
                    client.sendAsGroupMessage (groupName, "Group does not exists.");
                else
                    groups.get (groupName).remove (client);
            }
        });
        reservedMessages.add (new ReservedMessage ("/", "members") {
            @Override
            protected void onHandle (Client client, String command, String groupName) {
                if (!groups.containsKey (groupName))
                    client.sendAsGroupMessage (groupName, "Group does not exists.");
                else {
                    Group group = groups.get (groupName);
                    output.println (group.memberList.size ());
                    if (!group.hasMember (client))
                        client.sendAsGroupMessage (groupName, "You are not member of this group.");
                    else {
                        String msg = group.memberList
                            .stream ()
                            .map (Client::indentedUsername)
                            .collect (Collectors.joining ("\n"));
                        output.println (msg);
                        client.sendSystemMessage (String.format ("Member List (%s):\n%s", groupName, msg));
                    }
                }
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
                sendSystemMessage ("Enter your username!");
                while ((username = is.readLine ()) != null)
                    if (!username.matches ("[a-zA-Z0-9]+"))
                        sendSystemMessage ("Username can only contains alphanumeric!");
                    else if (clients.containsKey (username))
                        sendSystemMessage ("Username already taken!");
                    else
                        break;
                
                // username valid.
                // Notify the user and system, also add it to socket list.
                sendSystemMessage (String.format ("Welcome, %s. Type /commands for help.", username));
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
            return String.format ("\t%s", username);
        }
        
        void sendSystemMessage (String msg) {
            os.println ("@system " + msg);
        }
        
        void sendAsUserMessage (String username, String msg) {
            os.println (String.format ("@%s %s", username, msg));
        }
        
        void sendAsGroupMessage (String groupName, String msg) {
            os.println (String.format ("#%s %s", groupName, msg));
        }
        
        void sendMessage (Client from, String msg) {
            sendAsUserMessage (from.username, String.format ("%s: %s", from.username, msg));
        }
        
        void addFriend (Client user) {
            if (friends.containsKey (user.username))
                sendAsUserMessage (user.username, "You already friend with " + user.username);
            else {
                friends.put (user.username, user);
                friendList.add (user);
                sendAsUserMessage (user.username, user.username + " added as a friend.");
                user.sendAsUserMessage (username, username + " added you as friend.");
            }
        }
        
        void unFriend (Client friend) {
            if (!friends.containsKey (friend.username))
                sendAsUserMessage (friend.username, String.format ("You didn't friend with %s.", friend.username));
            else {
                friends.remove (friend.username);
                friendList.remove (friend);
                sendAsUserMessage (friend.username, friend.username + " removed from friend list.");
                friend.sendAsUserMessage (username, username + " removed you from friend list.");
            }
        }
        
        void joinGroup (Group group) {
            clientGroups.add (group);
            sendAsGroupMessage (group.name, "You joined the group.");
        }
        
        void leaveGroup (Group group) {
            clientGroups.remove (group);
            sendAsGroupMessage (group.name, "You left the group.");
        }
        
        void exit () {
            try {
                for (Group group : clientGroups)
                    group.remove (this);
            }
            catch (ConcurrentModificationException ignored) {}
    
            for (Client client : clientList)
                if (client.friends.containsKey (username))
                    client.unFriend (this);
    
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
    
        /**
         * send message to group as normal message with sender
         * indicator included.
         *
         * @param sender member who send the message.
         * @param msg    message.
         */
        void sendMessage (Client sender, String msg) {
            for (Client client : memberList)
                if (client != sender)
                    client.sendAsGroupMessage (name, String.format ("%s: %s", sender.username, msg));
        }
    
        /**
         * send message to group without sender indicator.
         *
         * @param sender member who triggered this event. This member
         *               will be excluded.
         * @param msg    message.
         */
        void sendSystemMessage (Client sender, String msg) {
            for (Client client : memberList)
                if (client != sender)
                    client.sendAsGroupMessage (name, msg);
        }
        
        boolean hasMember (Client client) {
            return members.containsValue (client);
        }
        
        void add (Client client) {
            if (hasMember (client)) {
                client.sendAsGroupMessage (name, "You already joined the group.");
                return;
            }
            
            // put socket to server socket list and
            // add this group to socket group list
            members.put (client.username, client);
            memberList.add (client);
            client.joinGroup (this);
            
            // notify system and all member of the group
            // that someone joined this group.
            sendSystemMessage (client, client.username + " joined.");
        }
        
        void remove (Client client) {
            if (!hasMember (client)) {
                client.sendAsGroupMessage (name, "You are not member of this group.");
                return;
            }
            
            // remove socket from server socket list and
            // remove this group from socket group list
            members.remove (client.username);
            memberList.remove (client);
            client.leaveGroup (this);
            
            // notify system and all member of the group
            // that someone left this group.
            sendSystemMessage (client, client.username + " left.");
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
