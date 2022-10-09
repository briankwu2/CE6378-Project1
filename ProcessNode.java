import java.util.*; // For lists
import java.net.*;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessNode {
    // Implement Connection to every node in config file (for now test just three manually)
    // Each node will have a client connection to a node, and a server thread that runs
    // Process Node will first
    /*
     * 1. Connect to all respective nodes (client threads will connect to neighbors)
     *      0 -> 1,2,3,4
     *      1 -> 2,3,4
     *      2 -> 3, 4
     *      3 -> 4
     * 2. If it's active it sends somewhere from minMessages to maxMessages then turns passive (random pick), then turns passive
     * 3. If it's passive and has sent <= maxMessages, then turn active when it receives a message
     * 
     * Implementation Steps
     * Make the nodes be able to connect to eachother and send basic messages 
     * Make it so that they send 
     * 
     * Variables:
     * Each node will have some minPerActive and maxPerActive constants
     * Each node will have a passive/active flag (True, False)
     * minSendDelay int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
     * 
     */
    private int minPerActive;
    private int maxPerActive;
    private int MAX_NUMBER;
    private int snapshotDelay;
    private int messagesSent;
    private int minSendDelay;
    private AtomicBoolean activeFlag;
    private int nodeNum;
    private String hostName;
    private int listenPort;
    
    // For CL Protocol
    private AtomicBoolean blueFlag;
    private List<String> applicationMessages;
    private ArrayList<Integer> vectorClock;

    // I/O Structures
    private List<Socket> socketList = Collections.synchronizedList(new ArrayList<Socket>()); // Creates a thread-safe Socket List
    private List<PrintWriter> outList = Collections.synchronizedList(new ArrayList<PrintWriter>()); // Creates a thread-safe output channel list
    private PrintWriter outFile;

    /* Public Constructor that assigns the node number, hostname, and listening port.
     * It makes node 0 ACTIVE 
     * It then creates a server thread that will listen to any client connections
     */
    
    public ProcessNode(int nodeNum, String hostName, int listenPort, String configFile)
    {
        
        this.nodeNum = nodeNum;
        this.hostName = hostName;
        this.listenPort = listenPort;
        configNode(configFile);
        AtomicBoolean activeFlag = new AtomicBoolean();
        AtomicBoolean blueFlag = new AtomicBoolean();
        this.blueFlag = blueFlag;
        this.activeFlag = activeFlag;

        this.applicationMessages = new ArrayList<String>();
        this.vectorClock = new ArrayList<Integer>();

        // Assign first active flag
        if (nodeNum == 0) {activeFlag.set(true);}
        else {activeFlag.set(false);}
        if(nodeNum == 0) {blueFlag.set(false);}
        else {blueFlag.set(false);}

        // Try-catch block that opens up a output file containing all the application messages sent 
        try
        {
            PrintWriter outFile = new PrintWriter (new FileWriter("Node" + this.nodeNum + "AppMessages.txt"));
            this.outFile = outFile;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        createServerClass(); // Creates a server thread that listens for connecting nodes and returns a socket to the connecting node

        // Create hook to cleanup everything if terminated or Ctrl-C
        Runtime.getRuntime().addShutdownHook(new Thread() 
        {
            public void run() {
                System.out.println("Exiting All Threads... Cleaning up...");
                cleanUpFunction();
                System.out.println("Done! Bye!");
                }
        });
    }

    public void configNode(String configFile)
    {
        try(
        BufferedReader inFile = new BufferedReader(new FileReader(configFile));
        )
        {
            String config = inFile.readLine();
            while (config.charAt(0) == '#')
            {
                config = inFile.readLine();
            }
            
            String configParameters[] = config.split(" ");
            this.minPerActive = Integer.parseInt(configParameters[1]);
            this.maxPerActive = Integer.parseInt(configParameters[2]);
            this.minSendDelay = Integer.parseInt(configParameters[3]);
            this.snapshotDelay = Integer.parseInt(configParameters[4]);
            this.MAX_NUMBER = Integer.parseInt(configParameters[5]);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    // Creates a server thread that listens and establishes connections
    public void createServerClass()
    {
        try
        {
            Thread listeningServer = new ServerClass(this.listenPort,this.socketList, this.outList, this.outFile,this.activeFlag,this.blueFlag);
            System.out.println("Server is now listening on port " + listenPort);
            listeningServer.start(); // Starts listening for new client connections
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    // Requests to connect with another node's server thread and establish a connection
    public Socket requestConnection(String remoteHost, int remotePort)
    {
        System.out.println("Attempting to connect to " + remoteHost + " on port number " + remotePort + "...");
        
        while(true)
        {
            try
            {
                Socket connectSocket = new Socket(remoteHost, remotePort); // Attempts to create a connection with host/port
                if (connectSocket != null)
                {
                    synchronized(socketList)
                    {
                        socketList.add(connectSocket); // Adds the socket to the socketlist
                    }
                    synchronized(outList)
                    {
                        PrintWriter out = new PrintWriter(connectSocket.getOutputStream(),true);
                        outList.add(out);
                    }
                    synchronized(outFile)
                    {
                        try
                        {
                            BufferedReader in = new BufferedReader(
                                new InputStreamReader(connectSocket.getInputStream()));
                            Thread listeningThread = new ListeningThread(in, outFile,activeFlag, blueFlag);
                            listeningThread.start(); // Starts a new listening thread that will append to the output file
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                    System.out.println("Connected to " + remoteHost + " on " + remotePort);
                    return connectSocket;
                }
            }
            catch (Exception e)
            {   
                System.out.println("Unable to connect to the port... retrying...");
            }
        }
    }

    // Thread that will always listen to the socket connections from other nodes and write it to the output file
    public void createReceivingThread(BufferedReader inSocket)
    {
        // Create a thread that listens to the specified socket
        // Synchronized Writes to a file
        try
        {
            Thread t = new ListeningThread(inSocket, this.outFile, activeFlag,blueFlag);
            t.start(); // Starts the new listening thread that writes to the output file
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Send a random amount of messages between minPerActive and maxPerActive to a random node within the connection list.
    public void sendMessages(String message, int toNodeNum)
    {
        try
        {
            outList.get(toNodeNum).println("From Node " + hostName + "(" + this.nodeNum + ")" + ": " + message); 
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    // Will send a random amount of messages to some node
    public void turnActive()
    {

        if (messagesSent >= MAX_NUMBER)
        {
            synchronized(activeFlag)
            {
                activeFlag.set(false);
            }
            return;
        }

        int randomNode = ThreadLocalRandom.current().nextInt(0,socketList.size()); // Get Random node
        int amountMessages = calcAmountMessages(); // Calculate random amount of messages
        for (int i = 0; i < amountMessages; i++)
        {
            int messageContent = ThreadLocalRandom.current().nextInt(0,100);
            sendMessages(Integer.toString(messageContent), randomNode); // Sends random message
            try
            {
                Thread.sleep(minSendDelay);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        
        messagesSent += amountMessages; // Add number of sent messages
      
        synchronized(activeFlag)
        {
            activeFlag.set(false); 
        }

    }
    public int calcAmountMessages()
    {
        //Calculates a uniformly distributed random amount of messages between minPerActive and maxPerActivek
        int randomNum = ThreadLocalRandom.current().nextInt(minPerActive, maxPerActive + 1); // Random amount of messages
        return randomNum;
    }

    
    public void cleanUpFunction()
    {
        // Cleans up all the sockets and used resources (assuming they are not in a try-with-resources block)
        try
        {
            outFile.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void runSnapshot()
    {

        /* 
         * 
         * 
         */

    }

    public void runNode()
    {
        while(true)
        {
            synchronized(activeFlag)
            {
                if (activeFlag.get())
                {
                    turnActive();
                }
                else if (!activeFlag.get())
                {
                    if(messagesSent >= MAX_NUMBER)
                    {
                        break;
                    }
                }
            }

        }
        System.out.println("Done sending messages, CTRL + C to exit.");
    }   


    public static void main(String[] args) throws IOException {
        
        

    }

}
