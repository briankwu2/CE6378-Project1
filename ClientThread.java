import java.net.*;
import java.io.*;

/*
 * This ClientThread should be connected to a neighboring node, and send messages to its connected node.
 */
public class ClientThread extends Thread
{
    private Socket client;

    private ClientThread (String host, int port) throws IOException, UnknownHostException
    {
        this.client = new Socket(host,port);
        System.out.println("Connecting to " + client.getRemoteSocketAddress());
    }

    @Override
    public void run()
    {
        // Uses a try-with-resources block so that these sockets close after program termination
        try (
            PrintWriter out = new PrintWriter(client.getOutputStream(),true);
            BufferedReader stdIn = new BufferedReader(
                new InputStreamReader(System.in));
            BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
        )
        {
            String userInput;
            while((userInput = stdIn.readLine()) != null)
            {
                out.println(userInput);
                System.out.println("Echo: " + userInput); 
                if (userInput.equals("Goodbye!"))
                {
                    System.out.println("Client is closing connection to " + client.getRemoteSocketAddress());
                    break;
                }
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        Thread client = new ClientThread("localhost", port);
        client.start();

    }

}
    
