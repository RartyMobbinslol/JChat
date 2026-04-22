import java.net.*; //Socket
import java.io.*; //BufferedReader, PrintWriter, IOException
import java.util.*; //ArrayList

public class ClientHandler implements Runnable
{
    //each client gets their own copy of these
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    String username;

    public ClientHandler(Socket socket)
    {
        this.socket = socket;
        try
        {
            //wrap the socket streams so we can read and write full lines of text
            //goes BufferedReader wrapping InputStreamReader wrapping raw socket bytes
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //true means auto flush, sends data immediately instead of buffering it
            out = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //t.start() in ChatServer calls this automatically in a new thread
    public void run()
    {
        try
        {
            //ask for a name and wait for them to type one
            out.println("Enter a username: ");
            username = in.readLine();

            //check for duplicate names
            for (ClientHandler client : ChatServer.clients)
            {
                //skip ourselves, skip anyone without a name yet, compare actual text with equals
                if (client != this && client.username != null && client.username.equals(username))
                {
                    out.println("Username already taken. Disconnecting.");
                    cleanup();
                    return; //kill this thread
                }
            }

            System.out.println(username + " joined the chat.");
            ChatServer.broadcast(username + " has joined!", this);
            out.println("Welcome " + username + "! Type /quit to leave.");

            //main chat loop, readLine blocks until they send something
            //returns null if connection closes so loop handles force quits automatically
            String msg;
            while ((msg = in.readLine()) != null)
            {
                if (msg.equals("/quit"))
                {
                    out.println("Goodbye!");
                    break; //cleanup runs in finally
                }

                System.out.println("[" + username + "]: " + msg);
                ChatServer.broadcast(username + ": " + msg, this);

                // --- PING DETECTION ---
                if (msg.contains("@"))
                {
                    //find the @ and grab everything after it
                    int atIndex = msg.indexOf("@");
                    String afterAt = msg.substring(atIndex + 1); //skip the @ itself

                    //first word after @ is the target username
                    //so "hey @omar how are you" gives us omar as words[0]
                    String[] words = afterAt.split(" ");
                    String targetName = words[0];

                    //search the client list for that username
                    ClientHandler target = null;
                    for (ClientHandler client : ChatServer.clients)
                    {
                        if (client.username != null && client.username.equals(targetName))
                        {
                            target = client;
                            break; //found them, stop searching
                        }
                    }

                    //make sure we found someone and its not ourselves
                    if (target != null && target != this)
                    {
                        //send directly to them, nobody else gets this copy
                        target.send("[PING from " + username + "]: " + msg);
                    }
                }
                // --- END PING DETECTION ---
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

    //private so only this class can call it
    private void cleanup()
    {
        try
        {
            ChatServer.clients.remove(this); //stop them getting broadcasts
            if (username != null)
            {
                ChatServer.broadcast(username + " has left the chat.", this);
                System.out.println(username + " disconnected.");
            }
            socket.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}