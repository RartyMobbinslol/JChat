import java.net.*; 
import java.io.*;
import java.util.*;
public class ChatServer
{
    static int port = 3000;
    //rooms
    static final String[] ROOM_NAMES = {"chat1", "chat2", "private"};
    //clients
    static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    //map of room name for the list of clients in the room
    static Map<String, List<ClientHandler>> rooms = new HashMap<>();
    //set up the rooms when server starts
    static
    {
        for (String room : ROOM_NAMES)
        {
            rooms.put(room, Collections.synchronizedList(new ArrayList<>()));
        }
    }
    public static void main(String[] args)
    {
        try
        {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Server running on port: " + port);
            while (true) // loop keeps accepting clients forever, hands off thread and blocks til connection made
            {
                Socket socket = server.accept();
                System.out.println("new connection: " + socket.getInetAddress());
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                Thread t = new Thread(handler);
                t.start();
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    //sends msg to everyone in a specific room except the sender
    public static void broadcast(String msg, ClientHandler sender, String room)
    {
        List<ClientHandler> roomClients = rooms.get(room);
        if (roomClients == null) return;
        synchronized (roomClients)
        {
            for (ClientHandler client : roomClients)
            {
                if (client != sender)
                {
                    client.send(msg);
                }
            }
        }
    }

    //sends msg to a specific client
    public static void sendDM(String msg, ClientHandler sender, String targetName)
    {
        for (ClientHandler client : clients)
        {
            if (client.username != null && client.username.equals(targetName))
            {
                client.send("[DM from " + sender.username + "]: " + msg);
                sender.send("[DM to " + targetName + "]: " + msg);
                logDM(sender.username, targetName, msg);
                return;
            }
        }
        sender.send("User " + targetName + " not found.");
    }

    //log a room mmsg to that rooms txt file
    public static void logMessage(String room, String msg)
    {
        try
        {
            FileWriter fw = new FileWriter(room + ".txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(msg);
            bw.newLine();
            bw.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //log a dm to a file named after both users, make sure its alphabetical
    public static void logDM(String user1, String user2, String msg)
    {
        //sort names alphabetically and make sure the dm file stays the same
        String[] names = {user1, user2};
        Arrays.sort(names);
        String filename = "dm_" + names[0] + "_" + names[1] + ".txt";

        try
        {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("[" + user1 + " -> " + user2 + "]: " + msg);
            bw.newLine();
            bw.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

public static List<String> getHistory(String room) // returns chat history
{
    List<String> history = new ArrayList<>();
    File file = new File(room + ".txt");
    if (!file.exists()) return history;
    try
    {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null)
        {
            history.add(line);
        }
        br.close();
    } catch (IOException e)
    {
        e.printStackTrace();
    }
    return history;
}
}