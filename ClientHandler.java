import java.net.*;
import java.io.*;
import java.util.*;
public class ClientHandler implements Runnable
{
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    String username;
    String currentRoom;

    public ClientHandler(Socket socket)
    {
        this.socket = socket;
        try
        {
            //wrap socket streams to read and write text, true = auto flush
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void run()
    {
        try
        {
            //get username, no duplicates
            out.println("Enter username: ");
            username = in.readLine();
            for (ClientHandler client : ChatServer.clients)
            {
                if (client != this && client.username != null && client.username.equals(username))
                {
                    out.println("Username already taken. Disconnecting.");
                    cleanup();
                    return;
                }
            }

            //get room, default to chat1 if invalid
            out.println("Choose a room .. chat1, chat2, private: ");
            String chosenRoom = in.readLine();
            if (!ChatServer.rooms.containsKey(chosenRoom))
            {
                chosenRoom = "chat1";
                out.println("Room not found, placing you in chat1.");
            }
            currentRoom = chosenRoom;
            ChatServer.rooms.get(currentRoom).add(this);

            //show history then announce joined user
            List<String> history = ChatServer.getHistory(currentRoom);
            if (!history.isEmpty())
            {
                out.println("- previous messages -");
                for (String line : history)
                {
                    out.println(line);
                }
                out.println("- end of history -");
            }
            System.out.println(username + " joined " + currentRoom);
            ChatServer.broadcast(username + " has joined!", this, currentRoom);
            out.println("hello " + username + "! You are in " + currentRoom + ". /quit to leave or /dm + username to DM someone.");

            //main chat loop
            String msg;
            while ((msg = in.readLine()) != null)
            {
                if (msg.equals("/quit"))
                {
                    out.println("Goodbye!");
                    break;
                }

                //handle /dm username message
                if (msg.startsWith("/dm"))
                {
                    String[] parts = msg.split(" ", 3);
                    if (parts.length < 3)
                    {
                        out.println("Usage: /dm username message");
                    }
                    else
                    {
                        ChatServer.sendDM(parts[2], this, parts[1]);
                    }
                    continue;
                }

                System.out.println("[" + currentRoom + "][" + username + "]: " + msg);
                ChatServer.broadcast(username + ": " + msg, this, currentRoom);
                ChatServer.logMessage(currentRoom, username + ": " + msg);

                // !!! START PING !!!
                if (msg.contains("@"))
                {
                    //grab word after @, search clients, send ping if found
                    int atIndex = msg.indexOf("@");
                    String[] words = msg.substring(atIndex + 1).split(" ");
                    String targetName = words[0];
                    ClientHandler target = null;
                    for (ClientHandler client : ChatServer.clients)
                    {
                        if (client.username != null && client.username.equals(targetName))
                        {
                            target = client;
                            break;
                        }
                    }
                    if (target != null && target != this)
                    {
                        target.send("[PING from " + username + "]: " + msg);
                    }
                }
                // !!! END PING !!!
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            //always runs no matter how they disconnect
            cleanup();
        }
    }

    //other classes call this to send to this specific client
    public void send(String msg)
    {
        out.println(msg);
    }

    private void cleanup()
    {
        try
        {
            //remove from global list and room, broadcast leave, close socket
            ChatServer.clients.remove(this);
            if (currentRoom != null)
            {
                ChatServer.rooms.get(currentRoom).remove(this);
                ChatServer.broadcast(username + " has left the chat.", this, currentRoom);
            }
            if (username != null) System.out.println(username + " disconnected.");
            socket.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}