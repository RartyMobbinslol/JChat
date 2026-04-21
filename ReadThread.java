import java.io.*;

public class ReadThread implements Runnable {
    BufferedReader in;

    public ReadThread(BufferedReader in) {
        this.in = in;
    }

    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println(msg);
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server.");
        }
    }
}