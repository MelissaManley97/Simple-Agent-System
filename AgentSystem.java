
import java.io.IOException;
import java.util.ArrayList;
import java.lang.Class;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AgentSystem class that starts one or more agents based on a given list of
 * classnames, maintains a list of local agents and their classes, finds remote
 * agents, and can send messages to another agent.
 */
public class AgentSystem {

    private static ArrayList<Agent> localAgents = new ArrayList<>(); // Local agents.

    private static final int PORT = 9989; // Port to send messages on.

    /**
     * Main method that instantiates the local agents based on the command line
     * arguments and starts the given agents.
     *
     * @param args Command line arguments
     * @throws UnknownHostException
     */
    public static void main(String[] args) throws UnknownHostException {
        instantiateLocalAgents(args);
        startAgents();
    }


    /**
     * Adds command line arguments to the localAgents list.
     *
     * @param args String array of command line arguments.
     */
    private static void instantiateLocalAgents(String[] args) {
        Agent temp;
        for (int i = 0; i < args.length; i++) {
            try {
                String className = args[i];
                Class tempClass = Class.forName(className);
                Object thisAgent = tempClass.newInstance();
                try {
                    // Check if this is of type Agent before casting it to an Agent class.
                    if(thisAgent instanceof Agent) {
                        temp = (Agent) tempClass.newInstance();
                        localAgents.add(temp);
                    }
                } catch (InstantiationException ex) {
                    Logger.getLogger(AgentSystem.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(AgentSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AgentSystem.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(AgentSystem.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(AgentSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Add Agents to the localAgents list.
     *
     * @param args Agent array of agents to add.
     */
    public static void addLocalAgents(Agent[] args) {
        for (int i = 0; i < args.length; i++) {
            localAgents.add(args[i]);
        }
    }

    /**
     * Remove Agents to the localAgents list.
     *
     * @param args Agent array of agents to remove.
     */
    private static void removeLocalAgents(Agent[] args) {
        for (int i = 0; i < args.length; i++) {
            localAgents.remove(args[i]);
        }
    }

    /**
     * Starts all local agents.
     */
    private static void startAgents() {
        for (Agent ag : localAgents) {
            ag.start();
        }
    }

    /**
     * Retrieves the port number to send messages on.
     *
     * @return Integer representing the port number.
     */
    public static int getPort() {
        return PORT;
    }

    /**
     * Finds this machine's IV4 broadcast IP address (for Ubuntu only).
     *
     * @return
     */
    private static InetAddress getBroadcastLAN() {
        try {
            // Find this machine's IV4 IP address (for Ubuntu).
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            int counterOuter = 1;
            int countInner = 0;
            InetAddress myipAddr = InetAddress.getLocalHost();
            for (NetworkInterface netint : Collections.list(nets)) {
                // For Ubuntu, the regular (not loopback) IP is the first
                // NetworkInterface listed.
                if (counterOuter == 1) {
                    Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                    InetAddress temp = myipAddr;
                    for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                        if(inetAddress instanceof Inet4Address) {
                            myipAddr = inetAddress;
                            break;
                        }
                    }
                }
                counterOuter++;
            }

            // Convert this machine's IP address to the broadcast IP address.
            String add = myipAddr.getHostAddress();
            String[] splitAdd = add.split("\\."); // Split by period.
            String broadcastInt = "255";
            splitAdd[splitAdd.length - 1] = broadcastInt;
            StringBuilder bldr = new StringBuilder();
            for (String s : splitAdd) {
                bldr.append(s);
                bldr.append(".");
            }
            bldr.deleteCharAt(bldr.length() - 1);

            // Broadcast LAN.
            InetAddress LAN = InetAddress.getByName(bldr.toString());
	    return LAN;
        } catch (UnknownHostException ex) {
            Logger.getLogger(AgentSystem.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(AgentSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null; // Error occurred.
    }

    /**
     * Finds a remote agent by broadcasting a message and waiting for a
     * response.
     *
     * @param outMessage The broadcast message to send.
     * @return
     */
    public static DatagramPacket findAgent(String outMessage) {
        try {
            // Get the local address network's broadcast IP.
            InetAddress LAN = getBroadcastLAN();

            // Try to send a broadcast message that all currently running
            // agents should receive.
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] buf = new byte[1000];
            DatagramPacket dgPacket = new DatagramPacket(buf, buf.length);
            buf = outMessage.getBytes();
            DatagramPacket out = new DatagramPacket(buf, buf.length, LAN, PORT);
            sendMsg(socket, outMessage, LAN, PORT);

            // Wait for a response to the broadcast message.
            socket.receive(dgPacket);
            socket.close();
            return dgPacket; // Return the DatagramPacket of the agent found.
        } catch (IOException ex) {
            Logger.getLogger(AgentSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Sends a message given the socket, target address, and target port.
     *
     * @param socket The socket to send the message through.
     * @param message The message to send.
     * @param address The IP address to send the message to.
     * @param targetPort The port to send the message through.
     */
    public static void sendMsg(DatagramSocket socket, String message, InetAddress address, int targetPort) {
        try {
            byte[] buf = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, address, targetPort);
            socket.send(sendPacket);
        } catch (SocketException ex) {
            Logger.getLogger(AgentSystem.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AgentSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
