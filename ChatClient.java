import java.net.*; //gives us Socket
import java.io.*; //gives us BufferedReader, PrintWriter, and IOException
import java.util.*; //gives us Scanner

public class ChatClient
{
    public static void main(String[] args)
    {
        /*
        try/catch on mai. if any erors happen while connecting to the server, then
        e.printStackTrace() prints the error details
                    vvv
        */
        try
        {
            /*
            creates a socket connecting to the server
            "localhost" means that we're connecting to our own machine
            3000 is the port number, has to match what ChatServer is listening on
                        vvv
            */
            Socket socket = new Socket("localhost", 3000);
            System.out.println("connected to server!");

            /*
            set up streams
            in = for reading messages coming FROM the server
            out = for sending messages TO the server
            the "true" on PrintWriter sends data immediately
                        vvv
            */
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            /*
            ReadThread to listen for incoming messages in the background
            without this, we wouldnt be able to type and receive messages at the same time
            main thread handles typing, ReadThread handles incoming messages
            both run at the same time so neither one blocks the other
                        vvv
            */
            ReadThread readThread = new ReadThread(in);
            Thread t = new Thread(readThread);
            t.start();

            /*
            Scanner reads whatever is typed in the terminal
            this is the main thread, it sits here waiting for keyboard input
            and sends whatever is typed straight to the server
                        vvv
            */
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine())
            {
                String msg = scanner.nextLine(); //grab whatever the user typed
                out.println(msg); //send it to the server

                if (msg.equals("/quit"))
                {
                    break; //exit the loop, goes to disconnect message
                }
            }

            socket.close(); //frees up the connection when we're done
            System.out.println("Disconnected.");

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}