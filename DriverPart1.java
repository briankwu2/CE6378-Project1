import java.io.*;

public class DriverPart1 {
    public static void main(String[] args) throws Exception{
        // The format is java testing <listenPort> <nodeNum> 
        int listenPort = Integer.parseInt(args[0]);
        int nodeNum = Integer.parseInt(args[1]);
        ProcessNode node = new ProcessNode(nodeNum, "localhost",listenPort, "config.txt");
        // Test receiving thread and send messages method
        // Create two nodes
        String localHost = "localhost";

        if (nodeNum == 0)
        {
            node.requestConnection(localHost, listenPort + 1);
            node.requestConnection(localHost, listenPort + 2);
            node.requestConnection(localHost, listenPort + 3);
            node.runNode();
        }
        else if (nodeNum == 1)
        {
            node.requestConnection(localHost, listenPort + 1);
            node.requestConnection(localHost, listenPort + 2);
            node.runNode();
        }
        else if (nodeNum == 2)
        {
            node.requestConnection(localHost, listenPort + 1);
            node.runNode();
        }
        else if (nodeNum == 3)
        {
            node.runNode();
        }

        while(true)
        {
        }

    }
    
}
