import java.net.*; //Socket
import java.io.*; // BufferedReader, PrintWriter, IOException
import java.util.*; //ArrayList

public class ClientHandler implements Runnable
{
    //declared up here so every method in the class can see them
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    String username;

    //constructor runs the moment we do "new ClientHandler(socket)" in ChatServer
    public ClientHandler(Socket socket)
    {
        this.socket = socket; //REMEMBER: this.socket means THIS specific objects socket
        try
        {
            /*
            three layers of wrapping:
            socket.getInputStream() gets raw bytes coming in from the client
            InputStreamReader bytes into readable characters
            BufferedReader gives
             readLine() so we can read one complete line at a time
                        vvv
            */
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            /*
            socket.getOutputStream gets raw bytes going out to the client
            PrintWriter gives println()
            auto flush sends data immediately, without it messages could get stuck in buffer
                        vvv
            */
            out = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //java calls this when t.start() is called in ChatServer
    //everything the client does for their session happens here
    public void run()
    {
        try
        {
            //send prompt to client and wait, then stores username when 'enter' typed
            out.println("Enter a username: ");
            username = in.readLine();

            /*
            loops through clients to check for duplicate names
            skip comparing ourselves to ourselves
            make sure the other client has a username
                        vvv
            */
            for (ClientHandler client : ChatServer.clients)
            {
                if (client != this && client.username != null && client.username.equals(username))
                {
                    out.println("Username already taken. Disconnecting.");
                    cleanup(); //close their connection, remove them from the list
                    return;    //exit run(),end thread
                }
            }

            //let the server chat know they joined
            System.out.println(username + " joined the chat.");
            ChatServer.broadcast(username + " has joined!", this);
            out.println("Welcome " + username + "! Type /quit to leave.");

            /*
            main chat loop
            in.readLine() returns null only when connection is closed
            so this loop automatically ends if the client force quits
                        vvv
            */
            String msg;
            while ((msg = in.readLine()) != null)
            {
                if (msg.equals("/quit"))
                {
                    out.println("Goodbye!");
                    break; //exit the loop, falls through to finally block
                }

                System.out.println("[" + username + "]: " + msg);

                //send to everyone else, we pass "this" so broadcast skips sending it back to us
                ChatServer.broadcast(username + ": " + msg, this);

                // START PING DETECTION 
                if (msg.contains("@")) //check if theres an @ anywhere in the message
                {
                    /*
                    indexOf() returns the position of @ in the string
                                vvv
                    */
                    int atIndex = msg.indexOf("@");

                    /*
                    substring() cuts the string starting at the position we give it
                    atIndex + 1 starts right AFTER the @ to skip it
                    ex: "hey @omar" becomes "omar"
                                vvv
                    */
                    String afterAt = msg.substring(atIndex + 1);

                    /*
                    split(" ") chops the string into an array wherever theres a space
                    ex: "omar how are you" becomes ["omar", "how", "are", "you"]
                    words[0] grabs the first word which is the username we want
                                vvv
                    */
                    String[] words = afterAt.split (" " );
                    String targetName = words[0];

                    ClientHandler target = null; //null means we havent found them yet

                    //search through every connected client to find the one with that username
                    for (ClientHandler client : ChatServer.clients)
                    {
                        if (client.username != null && client.username.equals(targetName))
                        {
                            target = client; //found them
                            break;           //stop searching
                        }
                    }

                    /*
                    two safety checks before sending:
                    target != null = make sure we actually found someone
                    target != this = make sure you cant ping yourself
                                vvv
                    */
                    if (target != null && target != this)
                    {
                        //send directly to that one client, nobody else gets this copy
                        target.send("[PING from " + username + "]: " + msg);
                    }
                }
                // END PING DETECTION
            }

        } catch (IOException e)
        {
            e.printStackTrace(); //something went wrong with the connection
        } finally
        {
            //finally always runs no matter what, guarantees cleanup always happens
            cleanup();
        }
    }

    //other classes call this to send a message to this specific client
    public void send(String msg)
    {
        out.println(msg);
    }

    //handles when a client disconnects
    private void cleanup()
    {
        try
        {
            ChatServer.clients.remove(this); //take them off the list so they stop getting broadcasts
            if (username != null) //only broadcast if they made it past the username stage
            {
                ChatServer.broadcast(username + " has left the chat.", this);
                System.out.println(username + " disconnected.");
            }
            socket.close(); //free connection
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}