import java.net.*; //ServerSocket and Socket
import java.io.*; //BufferedReader, PrintWriter, and IOException
import java.util.*; //List, ArrayList, and Collections

public class ChatServer
{
    static int port = 3000; //stores the port number the server listens on

    /*
    ArrayList creates a normal resizable list
    Collections.synchronizedList wraps it so multiple threads can add and 
    remove from it at the same time without corruption
                vvv
    */
    static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args)
    {
        /*
        try/catch on main
        if anything goes wrong with the server socket,
        e.printStackTrace() prints the error details
                    vvv
        */
        try
        {
            //create the server and tell it to listen on port 3000
            //from this moment on the server is open and waiting for clients!!
            ServerSocket server = new ServerSocket(port);
            System.out.println("Started server on port: " + port);

            //keep accepting new clients forever
            //without this, loop the server would only accept one client and then shut down
            while (true)
            {
                /*
                blocks here and freezes until someone connects
                the moment a client connects, accept() sends back
                a 'Socket' object showing that specific connection
                                vvv
                */
                Socket socket = server.accept();
                System.out.println("new connection: " + socket.getInetAddress());

                //create a new handler for this client and add them to the list
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);

                /*
                new Thread(handler) creates a new thread and tells it to run ClientHandler
                this works because ClientHandler has Runnable, meaning it has a run() method
                t.start() launches the thread
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

    //sends a message to everyone connected except the sender
    //skip the sender so they dont see their own message echoed back
    public static void broadcast(String msg, ClientHandler sender)
    {
        /*
        synchronized block is like a lock
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
                if (client != sender)
                {
                    client.send(msg);
                }
            }
        }
    }
}