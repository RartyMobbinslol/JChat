import java.io.*; //BufferedReader and IOException

public class ReadThread implements Runnable
{
    /*
    BufferedReader declared up here so constructor and the run method can access it
                vvv
    */
    BufferedReader in;

    //constructor receives input stream from ChatClient and stores it
    public ReadThread(BufferedReader in)
    {
        this.in = in; //this.in means THIS specific objects in
    }

    //Java calls this automatically when t.start() is called in ChatClient
    //this whole method runs on a separate background thread
    //so it can constantly listen for messages without blocking the main thread
    public void run()
    {
        /*
        try/catch wraps everything because reading from a network
                    vvv
        */
        try
        {
            /*
            same loop pattern used in ClientHandler
            readLine() freezes until the server sends something
            the moment a message arrives it gets stored in msg and printed
            readLine() only returns null when the connection is closed
            loop ends when the server shuts down or client disconnects
                        vvv
            */
            String msg;
            while ((msg = in.readLine()) != null)
            {
                System.out.println(msg); //print whatever the server sent
            }

        } catch (IOException e)
        {
            //print error details
            e.printStackTrace();
        }
    }
}