import java.net.*; //gives us ServerSocket and Socket
import java.io.*; //gives us BufferedReader, PrintWriter, and IOException
import java.util.*; //gives us List, ArrayList, and Collections

public class ChatServer
{
    static int port = 3000; //stores the port number the server listens on

    /*
    ArrayList<>() creates a normal resizable list
    Collections.synchronizedList() wraps it so multiple threads can
    add and remove from it at the same time without corrupting the data
    static means it belongs to the class itself, not any specific object
    so every ClientHandler thread can access the same one list
                vvv
    */
    static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args)
    {
        /*
        try/catch instead of "throws IOException" on main
        if anything goes wrong with the server socket,
        e.printStackTrace() prints the full error details
                    vvv
        */
        try
        {
            //create the server and tell it to listen on port 3000
            //from this moment on the server is open and waiting for clients
            ServerSocket server = new ServerSocket(port);
            System.out.println("Started server on port: " + port);

            //keep accepting new clients forever
            //without this loop the server would accept one client then shut down
            while (true)
            {
                /*
                blocks here and freezes until someone connects
                the moment a client connects, accept() hands us back
                a Socket object representing that specific connection
                                vvv
                */
                Socket socket = server.accept();
                System.out.println("New connection: " + socket.getInetAddress());

                //create a new handler for this client and add them to the list
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);

                /*
                new Thread(handler) creates a new thread and tells it to run our ClientHandler
                this works because ClientHandler implements Runnable, meaning it has a run() method
                t.start() actually launches the thread
                without threading, the server would freeze handling one person
                and nobody else could connect
                                vvv
                */
                Thread t = new Thread(handler);
                t.start();
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //sends a message to everyone currently connected except the sender
    //we skip the sender so they dont see their own message echoed back
    public static void broadcast(String msg, ClientHandler sender)
    {
        /*
        synchronized block acts as a lock
        when one thread enters here, no other thread can enter at the same time
        this prevents a ConcurrentModificationException which happens when
        one thread is looping through the list while another is adding or removing from it
                    vvv
        */
        synchronized (clients)
        {
            //for each client in the list, send them the message if theyre not the sender
            for (ClientHandler client : clients)
            {
                if (client != sender) //!= means "is not the same object as"
                {
                    client.send(msg);
                }
            }
        }
    }
}