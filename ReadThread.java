import java.io.*;

public class ReadThread implements Runnable
{
    BufferedReader in; //input stream from the server

    public ReadThread(BufferedReader in)
    {
        this.in = in; //save the stream ChatClient passed in
    }

    //runs in background so client can type and receive at the same time
    public void run()
    {
        try
        {
            //blocks until server sends something, prints it, loops back
            //returns null when connection closes so loop ends automatically
            String msg;
            while ((msg = in.readLine()) != null)
            {
                System.out.println(msg);
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}