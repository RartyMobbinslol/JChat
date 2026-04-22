import java.net.*; //Socket
import java.io.*; //streams and IOException
import java.util.*; //Scanner

public class ChatClient
{
    public static void main(String[] args)
    {
        try
        {
            //localhost means our own machine, 3000 has to match ChatServer
            Socket socket = new Socket("localhost", 3000);
            System.out.println("Successfully connected to server!");

            //same stream setup as ClientHandler
            //in reads from server, out sends to server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            //background thread handles incoming messages
            //so main thread can keep reading keyboard input at the same time
            ReadThread readThread = new ReadThread(in);
            Thread t = new Thread(readThread);
            t.start();

            //main thread sits here reading keyboard input and sending to server
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine())
            {
                String msg = scanner.nextLine();
                out.println(msg); //send to server
                if (msg.equals("/quit"))
                {
                    break;
                }
            }

            socket.close();
            System.out.println("Disconnected.");

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}