import java.net.*; //ServerSocket, Socket
import java.io.*; //streams and IOException
import java.util.*; //List, ArrayList, Collections

public class ChatServer
{
    static int port = 3000;

    /*
    synchronized list so multiple threads can add and remove without corrupting the data
    */
    static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args)
    {
        try
        {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Server is running on port: " + port);

            while (true) //keep accepting clients forever
            {
                Socket socket = server.accept(); //blocks until someone connects
                System.out.println("New connection: " + socket.getInetAddress());

                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);

                //hand off to its own thread so main loop can keep accepting
                Thread t = new Thread(handler);
                t.start();
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void broadcast(String msg, ClientHandler sender)
    {
        //locklist while looping so no other thread can modify it
        synchronized (clients)
        {
            for (ClientHandler client : clients)
            {
                if (client != sender) //skip sender
                {
                    client.send(msg);
                }
            }
        }
    }
}