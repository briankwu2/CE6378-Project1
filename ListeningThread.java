import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ListeningThread extends Thread
{
    private BufferedReader inSocket;
    private PrintWriter outFile;
    private AtomicBoolean activeFlag;
    private AtomicBoolean blueFlag;

    public ListeningThread (BufferedReader inSocket, PrintWriter outFile, AtomicBoolean activeFlag, AtomicBoolean blueFlag)
    {
        this.inSocket = inSocket;
        this.outFile = outFile;
        this.activeFlag = activeFlag;
        this.blueFlag = blueFlag;
    }

    @Override
    public void run()
    {
        // Will constant listen for messages in the socket and write to the outFile
        try  
        {
            String inputLine;
            while ((inputLine = inSocket.readLine()) != null)
            {
                if (inputLine.contains("MARKER:")) // Part of snapshot protocol, turn the process red (not blue)
                {
                    synchronized(blueFlag)
                    {
                        if (blueFlag.get()) // If the blue flag is true, then set it red and then record local state
                        {
                            blueFlag.set(false);
                            // FIXME: Record local state through vector clock
                        }
                        else
                        {
                            
                        }
                    }


                }
                synchronized(activeFlag)
                {
                    activeFlag.set(true); // Turn the node active when receiving a message
                }
                // Synchronized to ensure that no other threads overwrite messages
                synchronized(outFile)
                {
                    outFile.println(inputLine);
                }
                

            }
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
    }   
}




