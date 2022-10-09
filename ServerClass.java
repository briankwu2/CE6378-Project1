import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerClass extends Thread 
{
    private ServerSocket serverSocket;
    private List<Socket> socketList;
    private List<PrintWriter> outList;
    private PrintWriter outFile;
    private int port;
    private AtomicBoolean activeFlag;
    private AtomicBoolean blueFlag;

    public ServerClass(int port, List<Socket> socketList, List<PrintWriter> outList,
    PrintWriter outFile, AtomicBoolean activeFlag, AtomicBoolean blueFlag) throws IOException
    {
        serverSocket = new ServerSocket(port); // Creates a new server socket
        this.socketList = socketList;
        this.port = port;
        this.outList = outList;
        this.outFile = outFile;
        this.activeFlag = activeFlag;
        this.blueFlag = blueFlag;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Socket server = serverSocket.accept();
                synchronized(this.socketList){
                    socketList.add(server);
                }
                synchronized(this.outList)
                {
                    PrintWriter out = new PrintWriter(server.getOutputStream(),true);
                    outList.add(out);
                }
                synchronized(this.outFile)
                {
                    try 
                    {
                        BufferedReader in = new BufferedReader(
                            new InputStreamReader(server.getInputStream()));
                        Thread listeningThread = new ListeningThread(in, outFile, activeFlag,blueFlag);
                        listeningThread.start(); // Starts a new listening thread that will append to the output file
                        
                        System.out.println("New listening thread created. Listening to " + server.getRemoteSocketAddress());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

               }

                System.out.println("Socket connected to " + server.getRemoteSocketAddress());
                // testFunction();
            }
            catch (IOException e)
            {
                System.err.println("Could not listen on port " + port);
            }
            
        }
    }

    // public void testFunction()
    // {
    //     synchronized(socketList)
    //     {
    //         System.out.println("We are inside ServerClass Thread");
    //         System.out.print("Current Socket List is: ");
    //         for (int i = 0; i < socketList.size();i++)
    //         {
    //             System.out.println(socketList.get(i).getRemoteSocketAddress());
    //         }
    //         System.out.println("There is " + outList.size() + " of outputs");
    //         System.out.println("There is " + inList.size() + " of inputs");

    //     }
        
    // }

    public static void main(String[] args) throws IOException {
        // Tests ServerClass Functionality
        System.out.println("Server Class has now started...");
        List<Socket> sockList = new ArrayList<Socket>();
        int port = Integer.parseInt(args[0]);
        List<PrintWriter> outList = new ArrayList<PrintWriter>();
        PrintWriter outFile = new PrintWriter (new FileWriter("testingServerClass.txt"));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            System.out.println("Exiting All Threads... Cleaning up...");
            outFile.close();
            System.out.println("Done! Bye!");
            }
            });
        AtomicBoolean activeFlag = new AtomicBoolean();
        activeFlag.set(true);
        AtomicBoolean blueFlag = new AtomicBoolean();

        Thread t = new ServerClass(port, sockList, outList, outFile, activeFlag, blueFlag);
        t.start(); 
        
    }


}